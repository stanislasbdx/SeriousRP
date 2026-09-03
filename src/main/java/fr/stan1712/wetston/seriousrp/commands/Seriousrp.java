package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.Config;
import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.*;

public class Seriousrp implements CommandExecutor {
	private final Plugin pl;

	public Seriousrp(Main pl) {
		this.pl = pl;
	}

	private final String statusOnStr = ChatColor.GREEN + "ON";
	private final String statusOffStr = ChatColor.RED + "OFF";

	public void sendMessageStatusModule(String moduleName, CommandSender sender) {
		String status = getConfigBoolean("Core.Modules." + moduleName) ? statusOnStr : statusOffStr;
		sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + moduleName + ChatColor.GRAY + " > " + status);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("seriousrp.info")) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
			return true;
		}

		if (args.length == 0) {
			handleUsage(sender);
			return true;
		}

		String subCommand = args[0];
		if (subCommand.equalsIgnoreCase("version")) {
			handleVersion(sender);
		} else if (subCommand.equalsIgnoreCase("help")) {
			handleHelp(sender);
		} else if (subCommand.equalsIgnoreCase("status") || subCommand.equalsIgnoreCase("modules")) {
			handleModules(sender);
		} else if (subCommand.equalsIgnoreCase("reload")) {
			handleReload(sender);
		}

		return true;
	}

	private void handleVersion(CommandSender sender) {
		sender.sendMessage(getPrefixString());
		sender.sendMessage(ChatColor.GRAY + "» Version " + ChatColor.AQUA + getConfigString("Version"));
	}

	private void handleHelp(CommandSender sender) {
		sender.sendMessage(getPrefixString());
		sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/srtp" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.Dsrtp"));
		sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/lift" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.DLift"));
		if (this.pl.getConfig().getBoolean("Core.Modules.Medics")) {
			sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/medinfo <player>" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.DMedinfo"));
			sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/revive <player>" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.DRevive"));
			sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/hrprevive = " + getConfigString("Core.HelpMsg.DHRPRevive"));
		}
		if (this.pl.getConfig().getBoolean("Core.Modules.Economy")) {
			sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/cheque <amount>" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.DCheque"));
		}
		sender.sendMessage("");
		sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/seriousrp help" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.DVersion"));
		sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/seriousrp version" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.DHelp"));
		sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/seriousrp modules" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.DStatus"));
		sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + "/seriousrp reload" + ChatColor.GRAY + " : " + getConfigString("Core.HelpMsg.DReload"));
	}

	private void handleModules(CommandSender sender) {
		sender.sendMessage(getPrefixString());
		sendMessageStatusModule("CustomRecipes", sender);
		sendMessageStatusModule("RPDeath", sender);
		sendMessageStatusModule("Medics", sender);
		sendMessageStatusModule("Chairs", sender);
		sendMessageStatusModule("Economy", sender);
	}

	private void handleReload(CommandSender sender) {
		if (!sender.hasPermission("seriousrp.admin.reload")) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
			return;
		}

		new Config();
		this.pl.saveConfig();
		sender.sendMessage(getShortPrefixString() + getConfigString("Core.Reload"));
	}

	private void handleUsage(CommandSender sender) {
		sender.sendMessage(getPrefixString());
		sender.sendMessage(ChatColor.GRAY + "» " + getConfigString("Core.HelpMsg.Help"));
		sender.sendMessage(ChatColor.GRAY + "» " + getConfigString("Core.HelpMsg.VersionHelp"));
		sender.sendMessage(ChatColor.GRAY + "» " + getConfigString("Core.HelpMsg.StatusHelp"));
		sender.sendMessage(ChatColor.GRAY + "» " + getConfigString("Core.HelpMsg.ReloadHelp"));
	}
}
