package fr.stan1712.seriousrp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Deaths implements Listener {
	private Plugin plugin = Main.getPlugin(Main.class);

	@EventHandler
	public void onDeathPlayer(PlayerDeathEvent e) {
		Player p = e.getEntity();
		
		if(plugin.getConfig().getBoolean("Core.Modules.Medics") == true && (p instanceof Player)) {
			double x = Integer.valueOf(p.getLocation().getBlockX()).intValue();
			double y = Integer.valueOf(p.getLocation().getBlockY()).intValue();
			double z = Integer.valueOf(p.getLocation().getBlockZ()).intValue();
			String monde = p.getWorld().getName();
			World world = Bukkit.getWorld(monde);
			  
			p.spigot().respawn();
			p.teleport(new Location(world, x, y, z));
			  
			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 1000));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 5));
			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 1000));
			p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 1000000, 1000));
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1000000, 999));
			  
			p.sendMessage(plugin.getConfig().getString("Medics.Comate").replace("&", "§"));
			p.setHealth(0.5D);
			p.setFoodLevel(1);
		}
		
		if(plugin.getConfig().getBoolean("Core.Modules.RPDeath") == true && (p instanceof Player)) {
			ItemStack beef = new ItemStack(Material.RAW_BEEF, 4);
			ItemStack bones = new ItemStack(Material.BONE, 5);
			p.getWorld().dropItem(p.getLocation(), beef);
			p.getWorld().dropItem(p.getLocation(), bones);
		}
	}
	
}