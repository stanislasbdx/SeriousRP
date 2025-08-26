package fr.stan1712.wetston.seriousrp.commands.medics;

import fr.stan1712.wetston.seriousrp.Main;
import fr.stan1712.wetston.seriousrp.defaults.StrStructure;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectTypeCategory;

import java.util.Collection;
import java.util.Objects;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.*;

public class Medinfo implements CommandExecutor {
	private final Plugin pl;

	public Medinfo(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return true;

		if(player.hasPermission("seriousrp.medics.info")) {
			if (args.length == 0) {
				player.sendMessage(getShortPrefixString() + getConfigString("Medics.MedInfo.Usage"));
				return true;
			}

			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				player.sendMessage(getShortPrefixString() + getConfigString("Medics.TargetError").replace("%target%", args[0]));
				return true;
			}

			double targetHealth = target.getHealth();
			double targetFoodLevel = target.getFoodLevel();

			Collection<PotionEffect> effects = target.getActivePotionEffects();

			int targetBlockX = target.getLocation().getBlockX();
			int targetBlockY = target.getLocation().getBlockY();
			int targetBlockZ = target.getLocation().getBlockZ();
			String targetWorld = target.getWorld().getName();

			Location playerLoc = player.getLocation();
			int blocksDistance = (int) Math.round(playerLoc.distance(target.getLocation()));

			player.sendMessage(ChatColor.WHITE + "« " + ChatColor.GRAY + "[" + ChatColor.AQUA + target.getDisplayName() + ChatColor.GRAY + "]" + ChatColor.WHITE + " »");
			player.sendMessage(ChatColor.GRAY + "» " + ChatColor.WHITE + getConfigString("Medics.MedInfo.Vitals"));

			ChatColor targetHealthColor = targetHealth <= 9.0D ? ChatColor.RED : ChatColor.GREEN;
			ChatColor targetFoodColor = targetFoodLevel <= 9.0D ? ChatColor.RED : ChatColor.GREEN;

			player.sendMessage(ChatColor.GRAY + "» " + getConfigString("Medics.MedInfo.Health") + " " + targetHealthColor + Math.round(targetHealth / 2.0D) + " " + ChatColor.GRAY + getConfigString("Medics.MedInfo.Hearts"));
			player.sendMessage(ChatColor.GRAY + "» " + getConfigString("Medics.MedInfo.Food") + " " + targetFoodColor + Math.round(targetFoodLevel / 2.0D) + " " + ChatColor.GRAY + "/ 10");

			if (!effects.isEmpty()) {
				player.sendMessage("");
				player.sendMessage(ChatColor.GRAY + "» " + ChatColor.WHITE + getConfigString("Medics.MedInfo.Effects"));
				effects.forEach(effect -> {
					String effectCategoryLitteral = Objects.equals(effect.getType().getCategory(), PotionEffectTypeCategory.HARMFUL) ? "§4- §c" : "§2+ §a";
					String effectKey = String.valueOf(effect.getType().getKey());

					String[] parts = effectKey.split(":")[1].split("_");
					StringBuilder result = new StringBuilder();
					for (String part : parts) {
						result
							.append(Character.toUpperCase(part.charAt(0)))
							.append(part.substring(1))
							.append(" ");
					}

					player.sendMessage(ChatColor.GRAY + "» " + effectCategoryLitteral + result.toString().trim());
				});
			}

			if (target.getWorld() == player.getWorld()) {
				player.sendMessage("");
				player.sendMessage(ChatColor.GRAY + "» " + ChatColor.WHITE + getConfigString("Medics.MedInfo.Location"));
				player.sendMessage(ChatColor.GRAY + "» " +
					String.format("§b%s §7%s §b%s§7, §b%s§7, §b%s§7 %s §b%s§7 (%s)",
						target.getDisplayName(),
						getConfigString("Medics.MedInfo.Coordinates.IsAt"),
						targetBlockX,
						targetBlockY,
						targetBlockZ,
						getConfigString("Medics.MedInfo.Coordinates.InWorld"),
						targetWorld,
						getConfigString("Medics.MedInfo.Coordinates.Distance").replace("%blocks%", String.format("§b%s§7", blocksDistance))
					)
				);
			}
		}
		else {
			player.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
		}

		return true;
	}
}
