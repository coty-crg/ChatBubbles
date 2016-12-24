package com.wanderingcorgi.chatbubbles;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class Events implements Listener{
	private final Main plugin;
	
	ArrayList<ChatBubble> Bubbles = new ArrayList<ChatBubble>(); 
	
	public Events(Main plugin){
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		
		BukkitScheduler scheduler = Bukkit.getScheduler(); 
		scheduler.runTaskTimer(plugin, new HandleBubbleCreate(), 0l, 1l); 
		scheduler.runTaskTimer(plugin, new HandleBubbleCleanup(), 20l * 5l, 5l); 
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
	
	@EventHandler
	public void AsyncPlayerChatEvent(AsyncPlayerChatEvent event){
		String message = event.getMessage(); 
		Player player = event.getPlayer(); 
		ChatBubble bubble = new ChatBubble(message, player); 
		Bubbles.add(bubble); 
	}
}
