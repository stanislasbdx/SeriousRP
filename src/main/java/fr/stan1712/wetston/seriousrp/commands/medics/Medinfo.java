package fr.stan1712.wetston.seriousrp.commands.medics;

import fr.stan1712.wetston.seriousrp.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

public class Medinfo implements CommandExecutor {
	private final Plugin pl;

	public Medinfo(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return true;

		if(this.pl.getConfig().getBoolean("Core.Modules.Medics")) {
			if(player.hasPermission("seriousrp.medinfo")) {
				Player target = Bukkit.getPlayer(args[0]);

				if (target != null) {
					double targetHealth = target.getHealth();
					double targetFoodLevel = target.getFoodLevel();

					Collection<PotionEffect> effects = target.getActivePotionEffects();

					Object[] objects = effects.toArray();

					int x = target.getLocation().getBlockX();
					int y = target.getLocation().getBlockY();
					int z = target.getLocation().getBlockZ();
					String monde = target.getWorld().getName();

					player.sendMessage(ChatColor.AQUA + "+----- ♖ " + args[0] + " ♖ -----+");
					player.sendMessage("§9§l" + this.pl.getConfig().getString("Medics.MedInfo.Vitals").replace("&", "§"));
					if (targetHealth > 10.0D) {
						player.sendMessage("§a" + this.pl.getConfig().getString("Medics.MedInfo.Health").replace("&", "§") + " " + targetHealth / 2.0D + " " + this.pl.getConfig().getString("Medics.MedInfo.Hearts").replace("&", "§"));
					}
					else if (targetHealth <= 9.0D) {
						player.sendMessage("§c" + this.pl.getConfig().getString("Medics.MedInfo.Health").replace("&", "§") + " " + targetHealth / 2.0D + " " + this.pl.getConfig().getString("Medics.MedInfo.Hearts").replace("&", "§"));
					}
					if (targetFoodLevel > 10.0D) {
						player.sendMessage("§a" + this.pl.getConfig().getString("Medics.MedInfo.Food").replace("&", "§") + " " + targetFoodLevel / 2.0D + " / 10");
					}
					else if (targetFoodLevel <= 9.0D) {
						player.sendMessage("§c" + this.pl.getConfig().getString("Medics.MedInfo.Food").replace("&", "§") + " " + targetFoodLevel / 2.0D + " / 10");
					}

					player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
					player.sendMessage("§9§l" + this.pl.getConfig().getString("Medics.MedInfo.Effects").replace("&", "§"));
					for (Object object : objects) {
						player.sendMessage("§a- " + String.valueOf(object).split(":")[0]);
					}
					player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
					player.sendMessage("§9§l" + this.pl.getConfig().getString("Medics.MedInfo.Location").replace("&", "§"));
					player.sendMessage("§a" + args[0] + " " + this.pl.getConfig().getString("Medics.MedInfo.Coordinates").replace("&", "§") + " " + x + ", " + y + ", " + z + " @ " + monde);
					player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
				}
				else {
					player.sendMessage(ChatColor.DARK_RED + args[0] + " " + this.pl.getConfig().getString("Medic.Error").replace("&", "§"));
				}

			}
			else {
				player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
			}
		}
		else {
			if(this.pl.getConfig().getBoolean("Core.Modules.InactiveDebug")) {
				player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.Modules.InactiveMessage").replace("&", "§").replace("%module%", "Medics"));
			}
		}

		return true;
	}
}
