package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RandomTeleportation implements CommandExecutor {
	private final Plugin pl;

	private final int maxBlockRange;

	public RandomTeleportation(Main pl) {
		this.pl = pl;

		this.maxBlockRange = pl.getConfig().getInt("MicroModules.RandomBlocks");
	}

	private static final Logger _log = LoggerFactory.getLogger("SeriousRP - SRP");

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return false;

		if(player.hasPermission("serious.randomtp")) {
			player.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
			player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("MicroModules.RandomTeleport").replace("&", "§"));
			player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");

			player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 50, 100));
			player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 50, 100));
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 100));

			Location rtpLocation = player.getLocation();

			double distanceBetweenRandLocAndLoc = 0;
			while (distanceBetweenRandLocAndLoc < (double) maxBlockRange / 3) {
				RandomLocation randomLoc = getRandomLocation(player);

				rtpLocation = new Location(player.getWorld(), randomLoc.randLocX(), randomLoc.randLocY(), randomLoc.randLocZ());
				_log.info("distance: " + rtpLocation.distance(player.getLocation()) + " min distance : " + maxBlockRange / 3);

				distanceBetweenRandLocAndLoc = rtpLocation.distance(player.getLocation());
			}

			player.teleport(rtpLocation);
		}
		else {
			player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
		}

		return true;
	}

	private RandomLocation getRandomLocation(Player player) {
		int randLocX = player.getLocation().getBlockX() + new Random().nextInt(maxBlockRange);
		int randLocZ = player.getLocation().getBlockZ() + new Random().nextInt(maxBlockRange);

		if (new Random().nextInt(maxBlockRange) % 2 == 0) {
			randLocX = -randLocX;
			randLocZ = -randLocZ;
		}

		int randLocY = player.getWorld().getHighestBlockYAt(randLocX, randLocZ) + 1;

		return new RandomLocation(randLocX, randLocZ, randLocY);
	}

	private record RandomLocation(int randLocX, int randLocZ, int randLocY) {
	}
}
