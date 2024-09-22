package fr.stan1712.wetston.seriousrp.commands.medics;

import fr.stan1712.wetston.seriousrp.Main;
import fr.stan1712.wetston.seriousrp.defaults.StrStructure;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;

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
				player.sendMessage(ChatColor.GOLD + "» " + getConfigString("Medics.MedInfo.Usage"));
				return true;
			}

			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				player.sendMessage(ChatColor.GOLD + "» " + getConfigString("Medics.TargetError").replace("%target%", args[0]));
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

			player.sendMessage(StrStructure.START_TITLE_BOX + args[0] + StrStructure.END_TITLE_BOX);
			player.sendMessage("§9§l" + this.pl.getConfig().getString("Medics.MedInfo.Vitals").replace("&", "§"));
			if (targetHealth <= 9.0D) {
				player.sendMessage("§c" + this.pl.getConfig().getString("Medics.MedInfo.Health").replace("&", "§") + " §4" + Math.round(targetHealth / 2.0D) + "§c " + this.pl.getConfig().getString("Medics.MedInfo.Hearts").replace("&", "§"));
			}
			else {
				player.sendMessage("§a" + this.pl.getConfig().getString("Medics.MedInfo.Health").replace("&", "§") + " §2" + Math.round(targetHealth / 2.0D) + " §a" + this.pl.getConfig().getString("Medics.MedInfo.Hearts").replace("&", "§"));
			}
			if (targetFoodLevel <= 9.0D) {
				player.sendMessage("§c" + this.pl.getConfig().getString("Medics.MedInfo.Food").replace("&", "§") + " §4" + Math.round(targetFoodLevel / 2.0D) + "§c / 10");
			}
			else {
				player.sendMessage("§a" + this.pl.getConfig().getString("Medics.MedInfo.Food").replace("&", "§") + " §2" + Math.round(targetFoodLevel / 2.0D) + "§a / 10");
			}

			if(!effects.isEmpty()) {
				player.sendMessage(StrStructure.BOTTOM_BOX);
				player.sendMessage("§9§l" + this.pl.getConfig().getString("Medics.MedInfo.Effects").replace("&", "§"));
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

					player.sendMessage(effectCategoryLitteral + result.toString().trim());
				});
			}

			player.sendMessage(StrStructure.BOTTOM_BOX);
			player.sendMessage("§9§l" + this.pl.getConfig().getString("Medics.MedInfo.Location").replace("&", "§"));
			player.sendMessage(String.format("§2%s §a%s §2%s§a, §2%s§a, §2%s§a %s §2%s§a (%s)",
				target.getDisplayName(),
				this.pl.getConfig().getString("Medics.MedInfo.Coordinates.IsAt").replace("&", "§"),
				targetBlockX,
				targetBlockY,
				targetBlockZ,
				this.pl.getConfig().getString("Medics.MedInfo.Coordinates.InWorld").replace("&", "§"),
				targetWorld,
				this.pl.getConfig().getString("Medics.MedInfo.Coordinates.Distance").replace("%blocks%", String.format("§2%s§a", blocksDistance)).replace("&", "§")
			));
			player.sendMessage(StrStructure.BOTTOM_BOX);
		}
		else {
			player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
		}

		return true;
	}
}
