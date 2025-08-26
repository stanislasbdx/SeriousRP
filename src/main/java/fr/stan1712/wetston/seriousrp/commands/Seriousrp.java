package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.Config;
import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.*;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public class Seriousrp implements CommandExecutor {
	private final Plugin pl;

	public Seriousrp(Main pl) {
		this.pl = pl;
	}

	String statusOnStr = ChatColor.GREEN + "ON";
	String statusOffStr = ChatColor.RED + "OFF";

	public void sendMessageStatusModule(String moduleName, CommandSender sender) {
		String status = getConfigBoolean("Core.Modules.CustomRecipes") ? statusOnStr : statusOffStr;

		sender.sendMessage(ChatColor.GRAY + "» " + ChatColor.AQUA + moduleName + ChatColor.GRAY + " > " + status);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("seriousrp.info")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("version")) {
					sender.sendMessage(getPrefixString());
					sender.sendMessage(ChatColor.GRAY + "» Version " + ChatColor.AQUA + getConfigString("Version"));
				}
				else if (args[0].equalsIgnoreCase("help")) {
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
				else if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("modules")) {
					sender.sendMessage(getPrefixString());
					sendMessageStatusModule("CustomRecipes", sender);
					sendMessageStatusModule("RPDeath", sender);
					sendMessageStatusModule("Medics", sender);
					sendMessageStatusModule("Chairs", sender);
					sendMessageStatusModule("Economy", sender);
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("seriousrp.admin.reload")) {
						new Config();
						this.pl.saveConfig();

						sender.sendMessage(getShortPrefixString() + getConfigString("Core.Reload"));
					} else {
						sender.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
					}
				}
			} else {
				sender.sendMessage(getPrefixString());
				sender.sendMessage(ChatColor.GRAY + "» " + getConfigString("Core.HelpMsg.Help"));
				sender.sendMessage(ChatColor.GRAY + "» " + getConfigString("Core.HelpMsg.VersionHelp"));
				sender.sendMessage(ChatColor.GRAY + "» " + getConfigString("Core.HelpMsg.StatusHelp"));
				sender.sendMessage(ChatColor.GRAY + "» " + getConfigString("Core.HelpMsg.ReloadHelp"));
			}
		} else {
			sender.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
		}

		return true;
	}
}
