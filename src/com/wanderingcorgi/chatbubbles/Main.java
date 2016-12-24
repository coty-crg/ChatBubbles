package com.wanderingcorgi.chatbubbles;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

	public static String dataFolder; 
	public boolean AmSaving; 
	
	Events PluginEvents;
	
	public void onEnable(){
		Bukkit.getConsoleSender().sendMessage("[Starting chat bubble plugin]");
		dataFolder = getDataFolder().toString(); 

		PluginEvents = new Events(this);
		Bukkit.getConsoleSender().sendMessage("[Successfully started chat bubble plugin]");
	}
	
	public void onDisable(){
		Bukkit.getConsoleSender().sendMessage("[Stopping chat bubble plugin]");
		PluginEvents.KillAllBubbles(); 
		Bukkit.getConsoleSender().sendMessage("[Successfully stopped chat bubble plugin]");
	}
}
