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

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public class Revive implements CommandExecutor {
	private final Plugin pl;

	public Revive(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return true;

		// Checks if the player has the right to do the command
		if(!player.hasPermission("seriousrp.medics.revive")) {
			player.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
			return true;
		}

		// Checks if the correct number of argument was given
		if(args.length != 1){
			player.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/revive <player>" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.DRevive"));
			return true;
		}

		// Gets and checks if a target was given
		Player target = Bukkit.getPlayer(args[0]);
		if(target == null) {
			assert target != null;
			player.sendMessage(getShortPrefixString() + getConfigString("Medics.MedRevive.mistaken").replace("%target%", target.getDisplayName()));
			return true;
		}

		// Checks if there is a need
		if(target.getHealth() >= 4.0D) {
			player.sendMessage(getConfigString("Medics.MedRevive.NoNeed"));
			return true;
		}

		target.setHealth(8.0D);
		player.sendMessage(getConfigString("Medics.MedRevive.MedicRevive").replace("%target%", target.getDisplayName()));
		target.sendMessage(getConfigString("Medics.MedRevive.TargetRevived").replace("%medic%", player.getDisplayName()));

		target.removePotionEffect(PotionEffectType.RESISTANCE);
		target.removePotionEffect(PotionEffectType.SLOWNESS);
		target.removePotionEffect(PotionEffectType.BLINDNESS);
		target.removePotionEffect(PotionEffectType.HUNGER);

		target.setFoodLevel(20);

		return true;
	}
}
