package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.Config;
import fr.stan1712.wetston.seriousrp.Main;
import fr.stan1712.wetston.seriousrp.defaults.StrStructure;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class Seriousrp implements CommandExecutor {
	private final Plugin pl;

	public Seriousrp(Main pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("seriousrp.info")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("version")) {
					sender.sendMessage(StrStructure.START_TITLE_BOX + this.pl.getConfig().getString("Prefix").replace("&", "§") + StrStructure.END_TITLE_BOX);
					sender.sendMessage(ChatColor.GOLD + "» Version " + this.pl.getConfig().getString("Version").replace("&", "§"));
					sender.sendMessage(StrStructure.BOTTOM_BOX);
				} else if (args[0].equalsIgnoreCase("help")) {
					sender.sendMessage(StrStructure.START_TITLE_BOX + this.pl.getConfig().getString("Prefix").replace("&", "§") + StrStructure.END_TITLE_BOX);
					sender.sendMessage(ChatColor.GOLD + "» /srtp = " + this.pl.getConfig().getString("Core.HelpMsg.Dsrtp").replace("&", "§"));
					sender.sendMessage(ChatColor.GOLD + "» /lift = " + this.pl.getConfig().getString("Core.HelpMsg.DLift").replace("&", "§"));
					if (this.pl.getConfig().getBoolean("Core.Modules.Medics")) {
						sender.sendMessage(ChatColor.GOLD + "» /medinfo <player> = " + this.pl.getConfig().getString("Core.HelpMsg.DMedinfo").replace("&", "§"));
						sender.sendMessage(ChatColor.GOLD + "» /revive <player> = " + this.pl.getConfig().getString("Core.HelpMsg.DRevive").replace("&", "§"));
						sender.sendMessage(ChatColor.GOLD + "» /hrprevive = " + this.pl.getConfig().getString("Core.HelpMsg.DHRPRevive").replace("&", "§"));
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.Economy")) {
						sender.sendMessage(ChatColor.GOLD + "» /cheque <amount> = " + this.pl.getConfig().getString("Core.HelpMsg.DCheque").replace("&", "§"));
					}
					sender.sendMessage(StrStructure.BOTTOM_BOX);
					sender.sendMessage(ChatColor.GOLD + "» /seriousrp help = " + this.pl.getConfig().getString("Core.HelpMsg.DVersion").replace("&", "§"));
					sender.sendMessage(ChatColor.GOLD + "» /seriousrp version = " + this.pl.getConfig().getString("Core.HelpMsg.DHelp").replace("&", "§"));
					sender.sendMessage(ChatColor.GOLD + "» /seriousrp modules = " + this.pl.getConfig().getString("Core.HelpMsg.DStatus").replace("&", "§"));
					sender.sendMessage(ChatColor.GOLD + "» /seriousrp reload = " + this.pl.getConfig().getString("Core.HelpMsg.DReload").replace("&", "§"));
					sender.sendMessage(StrStructure.BOTTOM_BOX);
				} else if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("modules")) {
					sender.sendMessage(StrStructure.START_TITLE_BOX + this.pl.getConfig().getString("Prefix").replace("&", "§") + StrStructure.END_TITLE_BOX);
					if (this.pl.getConfig().getBoolean("Core.Modules.CustomRecipes")) {
						sender.sendMessage(ChatColor.GOLD + "» CustomRecipes > " + ChatColor.GREEN + "ON");
					} else {
						sender.sendMessage(ChatColor.GOLD + "» CustomRecipes > " + ChatColor.RED + "OFF");
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.RPDeath")) {
						sender.sendMessage(ChatColor.GOLD + "» RPDeath > " + ChatColor.GREEN + "ON");
					} else {
						sender.sendMessage(ChatColor.GOLD + "» RPDeath > " + ChatColor.RED + "OFF");
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.Medics")) {
						sender.sendMessage(ChatColor.GOLD + "» Medics > " + ChatColor.GREEN + "ON");
					} else {
						sender.sendMessage(ChatColor.GOLD + "» Medics > " + ChatColor.RED + "OFF");
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.Chairs")) {
						sender.sendMessage(ChatColor.GOLD + "» Chairs > " + ChatColor.GREEN + "ON");
					} else {
						sender.sendMessage(ChatColor.GOLD + "» Chairs > " + ChatColor.RED + "OFF");
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.Economy")) {
						sender.sendMessage(ChatColor.GOLD + "» Economy > " + ChatColor.GREEN + "ON");
					} else {
						sender.sendMessage(ChatColor.GOLD + "» Economy > " + ChatColor.RED + "OFF");
					}
					sender.sendMessage(StrStructure.BOTTOM_BOX);
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("seriousrp.admin.reload")) {
						new Config();
						this.pl.saveConfig();

						sender.sendMessage(StrStructure.START_TITLE_BOX + this.pl.getConfig().getString("Prefix").replace("&", "§") + StrStructure.END_TITLE_BOX);
						sender.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.Reload").replace("&", "§"));
						sender.sendMessage(StrStructure.BOTTOM_BOX);
					} else {
						sender.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
					}
				}
			} else {
				sender.sendMessage(StrStructure.START_TITLE_BOX + this.pl.getConfig().getString("Prefix").replace("&", "§") + StrStructure.END_TITLE_BOX);
				sender.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.HelpMsg.Help").replace("&", "§"));
				sender.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.HelpMsg.VersionHelp").replace("&", "§"));
				sender.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.HelpMsg.StatusHelp").replace("&", "§"));
				sender.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.HelpMsg.ReloadHelp").replace("&", "§"));
				sender.sendMessage(StrStructure.BOTTOM_BOX);
			}
		} else {
			sender.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
		}

		return true;
	}
}
