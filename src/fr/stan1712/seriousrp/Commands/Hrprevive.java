package fr.stan1712.seriousrp.Commands;

import fr.stan1712.seriousrp.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public class Hrprevive implements CommandExecutor {
	private final Plugin pl;

	public Hrprevive(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return true;

		Player player = (Player) sender;

		if(this.pl.getConfig().getBoolean("Core.Modules.Medics")) {
			if(player.hasPermission("seriousrp.medhrprevive")) {
				if(player.getHealth() < 10.0D) {
					player.sendMessage(this.pl.getConfig().getString("Medics.MedRevive.Self").replace("&", "§"));

					player.setHealth(8.0D);

					player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
					player.removePotionEffect(PotionEffectType.SLOW);
					player.removePotionEffect(PotionEffectType.BLINDNESS);
					player.removePotionEffect(PotionEffectType.HUNGER);
					player.removePotionEffect(PotionEffectType.JUMP);

					player.setFoodLevel(20);
				}
				else {
					player.sendMessage(this.pl.getConfig().getString("Medics.MedRevive.NoNeed").replace("&", "§"));
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
