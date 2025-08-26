package fr.stan1712.wetston.seriousrp.events;

import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public class Death implements Listener {
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
					if(player.getGameMode().equals(GameMode.SURVIVAL) || player.getGameMode().equals(GameMode.ADVENTURE)) {
						Location playerLocation = player.getLocation();

						player.spigot().respawn();
						player.teleport(playerLocation);

						player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE , 1000000, 1000));
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 1000000, 5));
						player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 1000));
						player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 1000000, 1000));

						player.sendMessage(getShortPrefixString() + getConfigString("Medics.Comate"));

						player.setHealth(2.0D);
						player.setFoodLevel(1);
					}
				}
				else if(plugin.getConfig().getBoolean("Core.Modules.RPDeath")) {
					ItemStack beef = new ItemStack(Material.BEEF, 4);
					ItemStack bones = new ItemStack(Material.BONE, 5);
					player.getWorld().dropItem(player.getLocation(), beef);
					player.getWorld().dropItem(player.getLocation(), bones);
				}
			}

		}.runTaskLater(plugin, 10);
	}

}