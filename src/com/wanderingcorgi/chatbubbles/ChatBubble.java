package com.wanderingcorgi.chatbubbles;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ChatBubble {

	public boolean MarkedForCleanup = false; 
	public long CreatedAt = 0l; 
	public ArmorStand Stand;
	
	public String Message; 
	public Entity Owner;
	
	public ChatBubble(String message, Entity owner){
		Message = message; 
		Owner = owner; 
	}
	
	public void Create(){
		CreatedAt = System.currentTimeMillis(); 
		Location location = Owner.getLocation();
		World world = location.getWorld(); 
		Stand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
		Stand.setGravity(false); 
		Stand.setCanPickupItems(false);
		Stand.setCustomNameVisible(true); 
		Stand.setVisible(false); 
		Stand.setCustomName(Message);		
		Stand.setInvulnerable(true);
		Stand.setAI(false);
		Stand.setCollidable(false);
		Stand.setCanPickupItems(false);
	}
	
	public void Kill(){
		Stand.remove();
	}
}
