package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;
import java.util.Random;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public class RandomTeleportation implements CommandExecutor {
	private final int maxBlockRange;
	private final Random randomNum = new Random();

	public RandomTeleportation(Main pl) {
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

				getRandomLocation(player).ifPresent(randomLoc ->
					player.teleport(new Location(player.getWorld(), randomLoc.randLocX(), randomLoc.randLocY(), randomLoc.randLocZ()))
				);
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

	private Optional<RandomLocation> getRandomLocation(Player player) {
		int originX = player.getLocation().getBlockX();
		int originZ = player.getLocation().getBlockZ();

		return RtpLocationSampler.sample(originX, originZ, maxBlockRange, randomNum)
			.map(horizontal -> {
				int randLocY = player.getWorld().getHighestBlockYAt(horizontal.x(), horizontal.z()) + 1;
				return new RandomLocation(horizontal.x(), horizontal.z(), randLocY);
			});
	}

	private record RandomLocation(int randLocX, int randLocZ, int randLocY) {
	}
}
