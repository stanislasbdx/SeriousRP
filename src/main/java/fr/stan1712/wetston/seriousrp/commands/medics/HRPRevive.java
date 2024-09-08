package fr.stan1712.wetston.seriousrp.commands.medics;

import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public class HRPRevive implements CommandExecutor {
	private final Plugin pl;

	public HRPRevive(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return true;

		if(player.hasPermission("seriousrp.medics.hrprevive")) {
			if(player.getHealth() < 4.0D) {
				player.setHealth(8.0D);
				player.sendMessage(this.pl.getConfig().getString("Medics.MedRevive.SelfRevive").replace("&", "§"));

				player.removePotionEffect(PotionEffectType.RESISTANCE);
				player.removePotionEffect(PotionEffectType.SLOWNESS);
				player.removePotionEffect(PotionEffectType.BLINDNESS);
				player.removePotionEffect(PotionEffectType.HUNGER);

				player.setFoodLevel(20);
			}
			else {
				player.sendMessage(this.pl.getConfig().getString("Medics.MedRevive.NoNeed").replace("&", "§"));
			}
		}
		else {
			player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
		}

		return true;
	}
}
