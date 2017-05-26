package fr.stan1712.srp;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class deaths implements Listener {
	
	public deaths() {}

	@EventHandler
	public void onDeathPlayer(PlayerDeathEvent e){
		Player p = e.getEntity();
		if (p instanceof Player){
			ItemStack beef = new ItemStack(Material.RAW_BEEF,4);
			ItemStack bones = new ItemStack(Material.BONE,5);
			p.getWorld().dropItem(p.getLocation(), beef);
			p.getWorld().dropItem(p.getLocation(), bones);
			
		}
	}
}
