package fr.stan1712.wetston.seriousrp.events;

import fr.stan1712.wetston.seriousrp.Main;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class Death implements Listener {
	//private Plugin plugin = Main.getPlugin(Main.class);

	Plugin plugin;

	public Death(Main plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onDeathPlayer(PlayerDeathEvent e) {
		new BukkitRunnable() {

			@Override
			public void run() {
				Player player = e.getEntity();

				if(plugin.getConfig().getBoolean("Core.Modules.Medics")) {
					double x = player.getLocation().getBlockX();
					double y = player.getLocation().getBlockY();
					double z = player.getLocation().getBlockZ();

					World playerWorld = Bukkit.getWorld(player.getWorld().getName());
					playerWorld.getDifficulty();

					player.spigot().respawn();
					player.teleport(new Location(playerWorld, x, y, z));

					player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 1000));
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 5));
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 1000));
					player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 1000000, 1000));
					player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1000000, 999));

					player.sendMessage(Objects.requireNonNull(plugin.getConfig().getString("Medics.Comate")).replace("&", "§"));

					player.setHealth(2.0D);
					player.setFoodLevel(1);
				}

				if(plugin.getConfig().getBoolean("Core.Modules.RPDeath")) {
					ItemStack beef = new ItemStack(Material.BEEF, 4);
					ItemStack bones = new ItemStack(Material.BONE, 5);
					player.getWorld().dropItem(player.getLocation(), beef);
					player.getWorld().dropItem(player.getLocation(), bones);
				}
			}

		}.runTaskLater(plugin, 10);
	}

}