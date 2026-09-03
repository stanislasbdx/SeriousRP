package fr.stan1712.wetston.seriousrp.commands.medics;

import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectTypeCategory;

import java.util.Collection;
import java.util.Objects;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public class Medinfo implements CommandExecutor {
	public Medinfo(Main pl) {
		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return false;

		if (!player.hasPermission("seriousrp.medics.info")) {
			player.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
			return true;
		}

		if (args.length == 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Medics.MedInfo.Usage"));
			return false;
		}

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			player.sendMessage(getShortPrefixString() + getConfigString("Medics.TargetError").replace("%target%", args[0]));
			return true;
		}

		player.sendMessage(ChatColor.WHITE + "« " + ChatColor.GRAY + "[" + ChatColor.AQUA + target.getDisplayName() + ChatColor.GRAY + "]" + ChatColor.WHITE + " »");
		sendVitals(player, target);
		sendEffects(player, target.getActivePotionEffects());
		sendLocation(player, target);

		return true;
	}

	private void sendVitals(Player player, Player target) {
		double targetHealth = target.getHealth();
		double targetFoodLevel = target.getFoodLevel();

		player.sendMessage(ChatColor.GRAY + "» " + ChatColor.WHITE + getConfigString("Medics.MedInfo.Vitals"));

		ChatColor targetHealthColor = targetHealth <= 9.0D ? ChatColor.RED : ChatColor.GREEN;
		ChatColor targetFoodColor = targetFoodLevel <= 9.0D ? ChatColor.RED : ChatColor.GREEN;

		player.sendMessage(ChatColor.GRAY + "» " + getConfigString("Medics.MedInfo.Health") + " " + targetHealthColor + Math.round(targetHealth / 2.0D) + " " + ChatColor.GRAY + getConfigString("Medics.MedInfo.Hearts"));
		player.sendMessage(ChatColor.GRAY + "» " + getConfigString("Medics.MedInfo.Food") + " " + targetFoodColor + Math.round(targetFoodLevel / 2.0D) + " " + ChatColor.GRAY + "/ 10");
	}

	private void sendEffects(Player player, Collection<PotionEffect> effects) {
		if (effects.isEmpty()) {
			return;
		}

		player.sendMessage("");
		player.sendMessage(ChatColor.GRAY + "» " + ChatColor.WHITE + getConfigString("Medics.MedInfo.Effects"));
		effects.forEach(effect -> {
			String effectCategoryLitteral = Objects.equals(effect.getType().getCategory(), PotionEffectTypeCategory.HARMFUL) ? "§4- §c" : "§2+ §a";
			player.sendMessage(ChatColor.GRAY + "» " + effectCategoryLitteral + EffectNameFormatter.formatNamespacedKey(String.valueOf(effect.getType().getKeyOrThrow())));
		});
	}

	private void sendLocation(Player player, Player target) {
		if (target.getWorld() != player.getWorld()) {
			return;
		}

		int blocksDistance = (int) Math.round(player.getLocation().distance(target.getLocation()));

		player.sendMessage("");
		player.sendMessage(ChatColor.GRAY + "» " + ChatColor.WHITE + getConfigString("Medics.MedInfo.Location"));
		player.sendMessage(ChatColor.GRAY + "» " +
			String.format("§b%s §7%s §b%s§7, §b%s§7, §b%s§7 %s §b%s§7 (%s)",
				target.getDisplayName(),
				getConfigString("Medics.MedInfo.Coordinates.IsAt"),
				target.getLocation().getBlockX(),
				target.getLocation().getBlockY(),
				target.getLocation().getBlockZ(),
				getConfigString("Medics.MedInfo.Coordinates.InWorld"),
				target.getWorld().getName(),
				getConfigString("Medics.MedInfo.Coordinates.Distance").replace("%blocks%", String.format("§b%s§7", blocksDistance))
			)
		);
	}
}
