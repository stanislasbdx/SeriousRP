package fr.stan1712.seriousrp.Commands;

import fr.stan1712.seriousrp.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
		if (!(sender instanceof Player)) return true;

		Player player = (Player) sender;

		if(this.pl.getConfig().getBoolean("Core.Modules.Medics")) {
			if(player.hasPermission("seriousrp.medinfo")) {
				Player target = Bukkit.getPlayer(args[1]);
				if (target != null) {
					double vie = target.getHealth();
					double hunger = target.getFoodLevel();

					Collection<PotionEffect> effects = target.getActivePotionEffects();

					int x = target.getLocation().getBlockX();
					int y = target.getLocation().getBlockY();
					int z = target.getLocation().getBlockZ();
					String monde = target.getWorld().getName();

					player.sendMessage(ChatColor.AQUA + "+----- ♖ " + args[1] + " ♖ -----+");
					if (vie > 10.0D) {
						player.sendMessage("§a" + this.pl.getConfig().getString("Medics.MedInfo.Health").replace("&", "§") + " " + vie / 2.0D + " " + this.pl.getConfig().getString("Medics.MedInfo.Hearts").replace("&", "§"));
					}
					else if (vie <= 9.0D) {
						player.sendMessage("§c" + this.pl.getConfig().getString("Medics.MedInfo.Health").replace("&", "§") + " " + vie / 2.0D + " " + this.pl.getConfig().getString("Medics.MedInfo.Hearts").replace("&", "§"));
					}
					if (hunger > 10.0D) {
						player.sendMessage("§a" + this.pl.getConfig().getString("Medics.MedInfo.Food").replace("&", "§") + " " + hunger / 2.0D + " / 10");
					}
					else if (hunger <= 9.0D) {
						player.sendMessage("§c" + this.pl.getConfig().getString("Medics.MedInfo.Food").replace("&", "§") + " " + hunger / 2.0D + " / 10");
					}

					player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
					player.sendMessage("§a" + this.pl.getConfig().getString("Medics.MedInfo.Effects").replace("&", "§") + " " + effects);
					player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
					player.sendMessage("§a" + args[1] + " " + this.pl.getConfig().getString("Medics.MedInfo.Coordinates").replace("&", "§") + " " + x + ", " + y + ", " + z + " > " + monde);
					player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
				}
				else {
					player.sendMessage(ChatColor.DARK_RED + args[1] + " " + this.pl.getConfig().getString("Medic.Error").replace("&", "§"));
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
