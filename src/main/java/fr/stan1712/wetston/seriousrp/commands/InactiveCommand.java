package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public class InactiveCommand implements CommandExecutor {
	private final Plugin pl;
	private final String inactiveModule;

	public InactiveCommand(Main pl, String module) {
		this.pl = pl;
		this.inactiveModule = module;

		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return true;

		if(this.pl.getConfig().getBoolean("Core.Modules.InactiveDebug")) {
			player.sendMessage(getShortPrefixString() + getConfigString("Core.Modules.InactiveMessage").replace("%module%", inactiveModule));
		}

		return true;
	}
}
