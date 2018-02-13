package com.wanderingcorgi.chatbubbles;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Random;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class Events implements Listener{
	private final Main plugin;
	
	ArrayList<ChatBubble> Bubbles = new ArrayList<ChatBubble>(); 
	
	public Events(Main plugin){
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		
		BukkitScheduler scheduler = Bukkit.getScheduler(); 
		scheduler.runTaskTimer(plugin,  new HandleBubbleFollow(), 0l,  1l); 
		scheduler.runTaskTimer(plugin, new HandleBubbleCreate(), 0l, 1l); 
		scheduler.runTaskTimer(plugin, new HandleBubbleCleanup(), 20l * 5l, 5l); 
		scheduler.runTaskTimer(plugin,  new HandleVillagerMessages(), 0l,  20l * 5l); 
	}
	
	public void KillAllBubbles(){
		for(int i = 0; i < Bubbles.size(); ++i){
			Bubbles.get(i).Kill();
		}
		
		Bubbles.clear();
	}
	
	public class HandleBubbleFollow implements Runnable{
		@Override
        public void run(){
			for(int i = 0; i < Bubbles.size(); ++i){
				ChatBubble bubble = Bubbles.get(i); 
				if(bubble.CreatedAt == 0l)
					continue; 
				
				if(bubble.Owner == null) {
					bubble.MarkedForCleanup = true; 
					continue; 
				}
				
				bubble.Stand.teleport(bubble.Owner.getLocation()); 
				bubble.Stand.setVelocity(bubble.Owner.getVelocity());
			}
		}
	}
	
	public class HandleBubbleCreate implements Runnable{
		@Override
        public void run(){
			for(int i = 0; i < Bubbles.size(); ++i){
				ChatBubble bubble = Bubbles.get(i); 
				if(bubble.CreatedAt != 0l)
					continue; 
				
				for(int j = 0; j < Bubbles.size(); ++j){
					ChatBubble otherBubble = Bubbles.get(j);
					if(otherBubble != bubble && otherBubble.Owner == bubble.Owner)
						otherBubble.MarkedForCleanup = true; 
				}
				
				bubble.Create();
			}
		}
	}

	public class HandleBubbleCleanup implements Runnable{
		ArrayList<ChatBubble> Graveyard = new ArrayList<ChatBubble>(); 
		
		@Override
        public void run(){
			long KillAfter = 1000l * 5l;
			
			Graveyard.clear();
			
			long currentTime = System.currentTimeMillis();
			
			for(int i = 0; i < Bubbles.size(); ++i){
				ChatBubble bubble = Bubbles.get(i);  
				
				if(bubble.CreatedAt == 0l)
					continue; 
				
				long timeDiff = currentTime - bubble.CreatedAt; 
				if(bubble.MarkedForCleanup || timeDiff > KillAfter){
					bubble.Kill();
					Graveyard.add(bubble);  
				}
			}
			
			for(int i = 0; i < Graveyard.size(); ++i){
				ChatBubble bubble = Graveyard.get(i); 
				Bubbles.remove(bubble); 
			} 
		}
	}
	
	public class HandleVillagerMessages implements Runnable {
		
		String[] PlayerNames; 
		List<String> Messages; 
		List<World> Worlds = null; 
		Random Random; 
		
		public class VillagerMessage {
			public List<String> Messages;
		}
		
		int CountOfSubstring(String original, String substring){
			int lastIndex = 0;
			int count = 0;

			while (lastIndex != -1) {

			    lastIndex = original.indexOf(substring, lastIndex);

			    if (lastIndex != -1) {
			        count++;
			        lastIndex += original.length();
			    }
			}
			
			return count; 
		}
		
		public HandleVillagerMessages(){
			Random = new Random(System.currentTimeMillis());
			Worlds = Bukkit.getWorlds(); 
			Messages = new ArrayList<String>();
			
			URL url;
			try {
				url = new URL("https://archive.4craft.us/data/comments");
				Scanner s = new Scanner(url.openStream());
				
				StringBuilder builder = new StringBuilder(); 
				while(s.hasNextLine()){
					String data = s.nextLine();
					builder.append(data); 
				}
				
				String alldata = builder.toString().trim(); 
				
				JsonReader reader = new JsonReader(new StringReader(alldata));
				reader.setLenient(true);
				
				Gson gson = new Gson(); 
				Type type = new TypeToken<List<String>>() {}.getType();
				List<String> messages = gson.fromJson(reader, type);
				
				int maxPlayerFoundCount = 0; 
				for(int i = 0; i < messages.size(); ++i){
					String message = messages.get(i); 
					
					if(message == null || message.isEmpty() || Messages.contains(message)) 
						continue; 
					
					// figure out the max number of random names we need to generate for messages 
					// players be cray 
					int countOfPlayer = CountOfSubstring(message.toLowerCase(), "$player") + 1; 
					countOfPlayer += CountOfSubstring(message.toLowerCase(), "$Player"); 
					if(countOfPlayer > maxPlayerFoundCount)
						maxPlayerFoundCount = countOfPlayer; 
					
					Messages.add(message); 
				}
				
				PlayerNames = new String[maxPlayerFoundCount]; 
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run(){
			
			for(int i = 0; i < Worlds.size(); ++i){
				World world = Worlds.get(i); 
			
				
				List<Player> players = world.getPlayers(); 
				List<Entity> entities = world.getEntities();
				
				if(players.size() == 0 || entities.size() == 0)
					continue; 
				
				for(int j = 0; j < PlayerNames.length; ++j){
					PlayerNames[j] = players.get(Random.nextInt(players.size())).getDisplayName();
				}
				
				for(int j = 0; j < entities.size(); ++j){
					try {
						Entity entity = entities.get(j); 
						
						// only let villagers speak 
						if(entity.getType() != EntityType.VILLAGER)
							continue; 
						
						// 1% chance every second 
						if(Random.nextInt(100) > 1) 
							continue;
					
						// choose some random players to talk about 
						Player randomPlayer1 = players.get(Random.nextInt(players.size()));
						Player randomPlayer2 = players.get(Random.nextInt(players.size())); 
						
						// choose a message
						int index = Random.nextInt(Messages.size()); 
						String message = Messages.get(index); 
						
						// format it 
						message = message.replace("$player", "%s"); 
						message = message.replace("$Player", "%s"); 
						message = String.format(message, PlayerNames); 
					
						// display it and add to que 
						ChatBubble bubble = new ChatBubble(message, entity); 
						Bubbles.add(bubble); 
					} catch (MissingFormatArgumentException e) {
						continue; 
					}
				}
			}
		}
	}
	
	// disabled for 4craft 
	/*@EventHandler
	public void AsyncPlayerChatEvent(AsyncPlayerChatEvent event){
		String message = event.getMessage(); 
		Player player = event.getPlayer(); 
		ChatBubble bubble = new ChatBubble(message, player); 
		Bubbles.add(bubble); 
	}*/
}
