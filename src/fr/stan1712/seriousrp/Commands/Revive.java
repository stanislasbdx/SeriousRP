package fr.stan1712.seriousrp.Commands;

import fr.stan1712.seriousrp.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public class Revive implements CommandExecutor {
	private final Plugin pl;

	public Revive(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return true;

		Player player = (Player) sender;

		if(this.pl.getConfig().getBoolean("Core.Modules.Medics")) {
			if(player.hasPermission("seriousrp.medrevive")) {
				if(args.length == 1){
					Player player2 = Bukkit.getPlayer(args[0]);
					if(player2 != null) {
						if(player2.getHealth() < 10.0D) {
							player2.setHealth(8.0D);
							player.sendMessage(this.pl.getConfig().getString("Medics.MedRevive.Revive").replace("&", "§") + " " + args[0]);

							player2.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
							player2.removePotionEffect(PotionEffectType.SLOW);
							player2.removePotionEffect(PotionEffectType.BLINDNESS);
							player2.removePotionEffect(PotionEffectType.HUNGER);
							player2.removePotionEffect(PotionEffectType.JUMP);

							player2.setFoodLevel(20);
						}
						else {
							player.sendMessage(this.pl.getConfig().getString("Medics.MedRevive.NoNeed").replace("&", "§"));
						}
					}
					else {
						player.sendMessage(ChatColor.DARK_RED + args[0] + " " + this.pl.getConfig().getString("Medics.MedRevive.Error").replace("&", "§"));
					}
				}
				else {
					player.sendMessage(ChatColor.GOLD + "» /revive <player> = " + this.pl.getConfig().getString("Core.HelpMsg.DRevive").replace("&", "§"));
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
