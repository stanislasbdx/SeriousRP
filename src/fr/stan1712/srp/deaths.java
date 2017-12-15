package fr.stan1712.srp;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class deaths implements Listener {
	
	  private srp pl;
	  
	  public deaths(srp pl)
	  {
	    this.pl = pl;
	  }
	  
	public deaths() {}

	@EventHandler
	public void onDeathPlayer(PlayerDeathEvent e){
		Player p = e.getEntity();
		if(this.pl.getConfig().getBoolean("DeathItems")) {
			if (p instanceof Player){
				ItemStack beef = new ItemStack(Material.RAW_BEEF,4);
				ItemStack bones = new ItemStack(Material.BONE,5);
				p.getWorld().dropItem(p.getLocation(), beef);
				p.getWorld().dropItem(p.getLocation(), bones);
				
			}
		}
		else {
			p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("NoModule").replace("&", "§"));
		}
	}
}
