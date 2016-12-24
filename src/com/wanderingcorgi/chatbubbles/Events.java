package com.wanderingcorgi.chatbubbles;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

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
				
				Vector ownerPosition = bubble.Owner.getLocation().toVector(); 
				Vector standPosition = bubble.Stand.getLocation().toVector(); 
				Vector positionDiff = ownerPosition.clone().subtract(standPosition); //.multiply(2d); 
				Vector newPosition = standPosition.clone().add(positionDiff); 
				
				Location newLocation = new Location(bubble.Owner.getWorld(), 
						newPosition.getX(), 
						newPosition.getY(), 
						newPosition.getZ()); 
				
				bubble.Stand.teleport(newLocation); 
				
				Vector ownerVelocity = bubble.Owner.getVelocity().clone();
				Vector newVelocity = ownerVelocity.multiply(2d); 
				
				bubble.Stand.setVelocity(newVelocity);
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
		
		List<String> Messages; 
		List<World> Worlds = null; 
		Random Random; 
		
		public class VillagerMessage {
			public List<String> Messages;
		}
		
		public HandleVillagerMessages(){
			Random = new Random(System.currentTimeMillis());
			Worlds = Bukkit.getWorlds(); 
			Messages = new ArrayList<String>();
			
			URL url;
			try {
				url = new URL("http://archive.4craft.us/data/comments");
				Scanner s = new Scanner(url.openStream());
				
				StringBuilder builder = new StringBuilder(); 
				while(s.hasNextLine()){
					String data = s.nextLine();
					builder.append(data); 
				}
				
				String alldata = builder.toString().trim(); 
				// Bukkit.getConsoleSender().sendMessage(alldata);
				
				JsonReader reader = new JsonReader(new StringReader(alldata));
				reader.setLenient(true);
				
				Gson gson = new Gson(); 
				Type type = new TypeToken<List<String>>() {}.getType();
				List<String> messages = gson.fromJson(reader, type);
				
				for(int i = 0; i < messages.size(); ++i){
					String message = messages.get(i); 
					
					if(message == null || message.isEmpty() || Messages.contains(message)) 
						continue; 
					
					Messages.add(message); 
				}
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
				
				for(int j = 0; j < entities.size(); ++j){
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
					message = String.format(message, 
							randomPlayer1.getDisplayName(), 
							randomPlayer2.getDisplayName()); 
					
					// display it and add to que 
					ChatBubble bubble = new ChatBubble(message, entity); 
					Bubbles.add(bubble); 
				}
			}
		}
	}
	
	@EventHandler
	public void AsyncPlayerChatEvent(AsyncPlayerChatEvent event){
		String message = event.getMessage(); 
		Player player = event.getPlayer(); 
		ChatBubble bubble = new ChatBubble(message, player); 
		Bubbles.add(bubble); 
	}
}
