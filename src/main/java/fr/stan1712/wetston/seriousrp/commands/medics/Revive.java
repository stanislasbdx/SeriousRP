package fr.stan1712.wetston.seriousrp.commands.medics;

import fr.stan1712.wetston.seriousrp.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
		if (!(sender instanceof Player player)) return true;

		if(player.hasPermission("seriousrp.medics.revive")) {
			if(args.length == 1){
				Player target = Bukkit.getPlayer(args[0]);

				if(target != null) {
					if(target.getHealth() < 2.0D) {
						target.setHealth(8.0D);
						player.sendMessage(this.pl.getConfig().getString("Medics.MedRevive.MedicRevive").replace("&", "§").replace("%target%", target.getDisplayName()));
						target.sendMessage(this.pl.getConfig().getString("Medics.MedRevive.TargetRevived").replace("&", "§").replace("%medic%", player.getDisplayName()));

						target.removePotionEffect(PotionEffectType.RESISTANCE);
						target.removePotionEffect(PotionEffectType.SLOWNESS);
						target.removePotionEffect(PotionEffectType.BLINDNESS);
						target.removePotionEffect(PotionEffectType.HUNGER);

						target.setFoodLevel(20);
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

		return true;
	}
}
