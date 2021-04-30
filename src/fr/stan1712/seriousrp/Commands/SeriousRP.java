package fr.stan1712.seriousrp.Commands;

import fr.stan1712.seriousrp.Config;
import fr.stan1712.seriousrp.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SeriousRP implements CommandExecutor {

	private final Plugin pl;

	public SeriousRP(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return true;

		Player player = (Player) sender;

		player.sendMessage(String.valueOf(args.length));

		if (player.hasPermission("seriousrp.info")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("version")) {
					player.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
					player.sendMessage(ChatColor.GOLD + "» Version " + this.pl.getConfig().getString("Version").replace("&", "§"));
					player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
				} else if (args[0].equalsIgnoreCase("help")) {
					player.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
					player.sendMessage(ChatColor.GOLD + "» /srtp = " + this.pl.getConfig().getString("Core.HelpMsg.Dsrtp").replace("&", "§"));
					player.sendMessage(ChatColor.GOLD + "» /lift = " + this.pl.getConfig().getString("Core.HelpMsg.DLift").replace("&", "§"));
					if (this.pl.getConfig().getBoolean("Core.Modules.TownSystem")) {
						player.sendMessage(ChatColor.GOLD + "» /srtown = " + this.pl.getConfig().getString("Core.HelpMsg.DTown").replace("&", "§"));
						player.sendMessage(ChatColor.GOLD + "» /srtown set = " + this.pl.getConfig().getString("Core.HelpMsg.DTownSet").replace("&", "§"));
						player.sendMessage(ChatColor.GOLD + "» /srtown where = " + this.pl.getConfig().getString("Core.HelpMsg.DTownWhere").replace("&", "§"));
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.Medics")) {
						player.sendMessage(ChatColor.GOLD + "» /medinfo <player> = " + this.pl.getConfig().getString("Core.HelpMsg.DMedinfo").replace("&", "§"));
						player.sendMessage(ChatColor.GOLD + "» /revive <player> = " + this.pl.getConfig().getString("Core.HelpMsg.DRevive").replace("&", "§"));
						player.sendMessage(ChatColor.GOLD + "» /hrprevive = " + this.pl.getConfig().getString("Core.HelpMsg.DHRPRevive").replace("&", "§"));
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.Economy")) {
						player.sendMessage(ChatColor.GOLD + "» /cheque <amount> = " + this.pl.getConfig().getString("Core.HelpMsg.DCheque").replace("&", "§"));
					}
					player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
					player.sendMessage(ChatColor.GOLD + "» /seriousrp help = " + this.pl.getConfig().getString("Core.HelpMsg.DVersion").replace("&", "§"));
					player.sendMessage(ChatColor.GOLD + "» /seriousrp version = " + this.pl.getConfig().getString("Core.HelpMsg.DHelp").replace("&", "§"));
					player.sendMessage(ChatColor.GOLD + "» /seriousrp status = " + this.pl.getConfig().getString("Core.HelpMsg.DStatus").replace("&", "§"));
					player.sendMessage(ChatColor.GOLD + "» /seriousrp reload = " + this.pl.getConfig().getString("Core.HelpMsg.DReload").replace("&", "§"));
					player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
				} else if (args[0].equalsIgnoreCase("status")) {
					player.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
					if (this.pl.getConfig().getBoolean("Core.Modules.CustomRecipes")) {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "CustomRecipes > ON");
					} else {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "CustomRecipes > OFF");
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.RPDeath")) {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "RPDeath > ON");
					} else {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "RPDeath > OFF");
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.Medics")) {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "Medics > ON");
					} else {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "Medics > OFF");
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.TownSystem")) {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "TownSystem > ON");
					} else {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "TownSystem > OFF");
					}
						/*if(this.pl.getConfig().getBoolean("Core.Modules.RPMobiles") == true) {
							player.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "RPMobiles > ON");
						}
						else {
							player.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "RPMobiles > OFF");
						}*/
					if (this.pl.getConfig().getBoolean("Core.Modules.Chairs")) {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "Chairs > ON");
					} else {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "Chairs > OFF");
					}
					if (this.pl.getConfig().getBoolean("Core.Modules.Economy")) {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "Economy > ON");
					} else {
						player.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "Economy > OFF");
					}
					player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (player.hasPermission("seriousrp.admin.reload")) {
						new Config();
						this.pl.saveConfig();

						player.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
						player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.Reload").replace("&", "§"));
						player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
					} else {
						player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
					}
				}
			} else {
				player.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
				player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.HelpMsg.Help").replace("&", "§"));
				player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.HelpMsg.VersionHelp").replace("&", "§"));
				player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.HelpMsg.StatusHelp").replace("&", "§"));
				player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.HelpMsg.ReloadHelp").replace("&", "§"));
				player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
			}
		} else {
			player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
		}

		return true;
	}
}
