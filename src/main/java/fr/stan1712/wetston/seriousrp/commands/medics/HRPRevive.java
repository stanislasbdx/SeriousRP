package fr.stan1712.wetston.seriousrp.commands.medics;

import fr.stan1712.wetston.seriousrp.BukkitStatusEffects;
import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public class HRPRevive implements CommandExecutor {
	private final Consumer<Player> comaClearer;

	public HRPRevive(Main pl) {
		this(pl, BukkitStatusEffects::clearComa);
	}

	HRPRevive(Main pl, Consumer<Player> comaClearer) {
		pl.getConfig();
		this.comaClearer = comaClearer;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return false;

		if(player.hasPermission("seriousrp.medics.hrprevive")) {
			if(player.getHealth() < 4.0D) {
				player.setHealth(8.0D);
				player.sendMessage(getShortPrefixString() + getConfigString("Medics.MedRevive.SelfRevive"));
				comaClearer.accept(player);
				player.setFoodLevel(20);
			}
			else {
				player.sendMessage(getShortPrefixString() + getConfigString("Medics.MedRevive.NoNeed"));
			}
		}
		else {
			player.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
		}

		return true;
	}
}
