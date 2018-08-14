package fr.stan1712.seriousrp;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
	public void versionCheck() {
		String version = getServer().getVersion();
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "Server version : " + version);
		if(version.contains("Spigot")) {
			if(version.contains("1.12")) {
				console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "Version check !");
				console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "If you got issues, you can report them on Github");
			}
			else if(version.contains("1.11")){
				console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "Version check !");
				console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "If you got issues, you can report them on Github");
			}
			else if(version.contains("1.10")){
				console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "Version check !");
				console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "If you got issues, you can report them on Github");
			}
			else if(version.contains("1.9")){
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** WARNING ***");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* 1.9 is not supported by SeriousRP *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* If you got error, don't report it on Github *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** ***");
			}
			else if(version.contains("1.8")){
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** WARNING ***");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* 1.8 is not supported by SeriousRP *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* If you got error, don't report it on Github *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** ***");
			}
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "***  ***  UNKNOWN VERSION  ***  ***");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* The plugin will be disabled now *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** *** *** *** *** *** *** ***");
			pm.disablePlugin(this);
		}
	}
	
	PluginManager pm = getServer().getPluginManager();
	ConsoleCommandSender console = getServer().getConsoleSender();
	private Commands commands = new Commands();
	
	public void onEnable() {
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "# ------ SeriousRP ------ #");
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "SeriousRP Version " + getConfig().getString("Version"));
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "Boot sequence launched !");
		
		/* 
		 * Modules in config.yml
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Module manager, loading modules -#");
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "Soon...");
		
		/*
		 * Class loader (project)
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Class manager, loading classes -#");
		pm.registerEvents(new Commands(), this);
		console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "All classes have been loaded");

		/*
		 * Depencies like Telecom and Vault
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Depencies Manager -#");
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "Soon...");
		
		/*
		 * Commands loader
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Commands manager, loading commands -#");
		getCommand(commands.cmd1).setExecutor(commands);
		console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "All commands have been loaded");
		
		/*
		 * Config creator/modificator
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Config writer, writing and creating configs files -#");
		pm.registerEvents(new Config(this), this);
		saveConfig();

		/*
		 * Version checker
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Version check -#");
		versionCheck();

		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "# ------ SeriousRP ------ #");
		
	}
	
	public void onDisable() {
		console.sendMessage(ChatColor.BLUE + "SeriousRP version 3.9.9 arret du plugin !");
	}
}
