package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public class RandomTeleportation implements CommandExecutor {
	private final Plugin pl;

	private final int maxBlockRange;
	private Random randomNum = new Random();

	public RandomTeleportation(Main pl) {
		this.pl = pl;

		this.maxBlockRange = pl.getConfig().getInt("MicroModules.RandomBlocks");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return false;

		if(player.hasPermission("serious.randomtp")) {
			if (player.getWorld().getEnvironment() == World.Environment.NORMAL) {
				player.sendMessage(getConfigString("ShortPrefix") + getConfigString("MicroModules.RandomTeleport"));

				player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 50, 100));
				player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 50, 100));
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 100));


				Location rtpLocation = player.getLocation();

				double distanceBetweenRandLocAndLoc = 0;
				while (distanceBetweenRandLocAndLoc < (double) maxBlockRange / 3) {
					RandomLocation randomLoc = getRandomLocation(player);

					rtpLocation = new Location(player.getWorld(), randomLoc.randLocX(), randomLoc.randLocY(), randomLoc.randLocZ());

					distanceBetweenRandLocAndLoc = rtpLocation.distance(player.getLocation());
				}

				player.teleport(rtpLocation);
			}
			else {
				player.sendMessage(getShortPrefixString() + getConfigString("MicroModules.RTPDisabledWorld"));
			}
		}
		else {
			player.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
		}

		return true;
	}

	private RandomLocation getRandomLocation(Player player) {
		int randLocX = player.getLocation().getBlockX() + randomNum.nextInt(maxBlockRange);
		int randLocZ = player.getLocation().getBlockZ() + randomNum.nextInt(maxBlockRange);

		if (randomNum.nextInt(maxBlockRange) % 2 == 0) {
			randLocX = -randLocX;
			randLocZ = -randLocZ;
		}

		int randLocY = player.getWorld().getHighestBlockYAt(randLocX, randLocZ) + 1;

		return new RandomLocation(randLocX, randLocZ, randLocY);
	}

	private record RandomLocation(int randLocX, int randLocZ, int randLocY) {
	}
}
