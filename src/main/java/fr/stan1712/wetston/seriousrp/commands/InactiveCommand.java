package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class InactiveCommand implements CommandExecutor {
	private final Plugin pl;
	private final String inactiveModule;

	public InactiveCommand(Main pl, String module) {
		this.pl = pl;
		this.inactiveModule = module.toString();

		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return true;

		if(this.pl.getConfig().getBoolean("Core.Modules.InactiveDebug")) {
			player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.Modules.InactiveMessage").replace("&", "§").replace("%module%", inactiveModule));
		}

		return true;
	}
}
