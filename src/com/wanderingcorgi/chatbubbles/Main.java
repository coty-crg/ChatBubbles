package com.wanderingcorgi.chatbubbles;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

	public static String dataFolder; 
	public boolean AmSaving; 
	
	public void onEnable(){
		Bukkit.getConsoleSender().sendMessage("[Starting chat bubble plugin]");
		dataFolder = getDataFolder().toString(); 

		Events pluginEvents = new Events(this);
	}
	
	public void onDisable(){
		Bukkit.getConsoleSender().sendMessage("[Stopping chat bubble plugin]");
	}
}
