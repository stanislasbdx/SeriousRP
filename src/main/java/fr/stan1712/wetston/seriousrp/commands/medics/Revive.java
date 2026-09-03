package fr.stan1712.wetston.seriousrp.commands.medics;

import fr.stan1712.wetston.seriousrp.BukkitStatusEffects;
import fr.stan1712.wetston.seriousrp.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public class Revive implements CommandExecutor {
	private final Consumer<Player> comaClearer;

	public Revive(Main pl) {
		this(pl, BukkitStatusEffects::clearComa);
	}

	Revive(Main pl, Consumer<Player> comaClearer) {
		pl.getConfig();
		this.comaClearer = comaClearer;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return false;

		if(player.hasPermission("seriousrp.medics.revive")) {
			if(args.length == 1){
				Player target = Bukkit.getPlayer(args[0]);

				if(target != null) {
					if(target.getHealth() < 4.0D) {
						target.setHealth(8.0D);
						player.sendMessage(getConfigString("Medics.MedRevive.MedicRevive").replace("%target%", target.getDisplayName()));
						target.sendMessage(getConfigString("Medics.MedRevive.TargetRevived").replace("%medic%", player.getDisplayName()));
						comaClearer.accept(target);
						target.setFoodLevel(20);
					}
					else {
						player.sendMessage(getConfigString("Medics.MedRevive.NoNeed"));
					}
				}
				else {
					player.sendMessage(getShortPrefixString() + getConfigString("Medics.MedRevive.mistaken").replace("%target%", args[0]));
				}
			}
			else {
				player.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/revive <player>" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.DRevive"));
				return false;
			}
		}
		else {
			player.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
		}

		return true;
	}
}
