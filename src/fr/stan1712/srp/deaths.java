package fr.stan1712.srp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class deaths implements Listener {
	
	  private Main pl;
	  
	  public deaths(Main pl)
	  {
	    this.pl = pl;
	  }
	  
	public deaths() {}

	@EventHandler
	public void onDeathPlayer(PlayerDeathEvent e){
		Player p = e.getEntity();
		if(this.pl.getConfig().getBoolean("Medics") == true && this.pl.getConfig().getBoolean("RPDeath") == true) {
			p.sendMessage(this.pl.getConfig().getString("Module.Error").replace("&", "§"));
		}
		else{
			if(this.pl.getConfig().getBoolean("RPDeath") == true && this.pl.getConfig().getBoolean("Medics") == false) {
				if (p instanceof Player){
					ItemStack beef = new ItemStack(Material.RAW_BEEF,4);
					ItemStack bones = new ItemStack(Material.BONE,5);
					p.getWorld().dropItem(p.getLocation(), beef);
					p.getWorld().dropItem(p.getLocation(), bones);
					
				}
			}
			if(this.pl.getConfig().getBoolean("Medics") == true && this.pl.getConfig().getBoolean("RPDeath") == false) {
				double x = Integer.valueOf(p.getLocation().getBlockX());
				double y = Integer.valueOf(p.getLocation().getBlockY());
				double z = Integer.valueOf(p.getLocation().getBlockZ());
				String monde = p.getWorld().getName();
				World world = Bukkit.getWorld(monde);
				
				p.spigot().respawn();
				p.teleport(new Location(world, x, y, z));
				
				p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 1000));
				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 5));
				p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 1000));
				p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 1000000, 1000));
				
				p.setHealth(0.5);
				p.setFoodLevel(1);
			}
		}
	}
}
