package fr.stan1712.seriousrp;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {	
	public void versionCheck() {
		String version = getServer().getVersion();
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "Server version : " + version);
		if(version.contains("Spigot")) {
			if(version.contains("1.12")) {
				console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "Version check !");
				console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "If you got issues, you can report them on Github");
			}
			else if(version.contains("1.11")){
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** WARNING ***");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* 1.11 is not supported by SeriousRP *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* If you got errors, report it on Github *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** ***");
			}
			else if(version.contains("1.10")){
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** WARNING ***");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* 1.10 is not supported by SeriousRP *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* If you got errors, don't report it on Github *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** ***");
			}
			else if(version.contains("1.9")){
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** WARNING ***");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* 1.9 is not supported by SeriousRP *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* If you got errors, don't report it on Github *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** ***");
			}
			else if(version.contains("1.8")){
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** WARNING ***");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* 1.8 is not supported by SeriousRP *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "* If you got errors, don't report it on Github *");
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** ***");
			}
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "***  ***  UNKNOWN VERSION  ***  ***");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* The plugin will be disabled now *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** *** *** *** *** *** *** ***");
			pm.disablePlugins();
		}
	}
	
	public void depencies() {
		File Tdirect = new File("plugins/Telecom");
		File Vdirect = new File("plugins/Vault");
		if (!Tdirect.exists()) {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** WARNING ***");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* {Telecom} Telecom was not found ! *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* {Telecom} Deactivation of the module RPMobile. *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* {Telecom} If you want to use SRP mobile, you will need Telecom *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** *** *** *** *** *** *** ***");
			  
			getConfig().set("Core.Modules.RPMobile", Boolean.valueOf(false));
		}
		else {
			System.out.println("[SeriousRP] {Telecom} Telecom found !");
		}
		if (!Vdirect.exists()) {
			System.out.println("[SeriousRP] {Vault} Vault was not found");
		}
		else {
			System.out.println("[SeriousRP] {Vault} Vault found !");
		}
	}
	
	PluginManager pm = getServer().getPluginManager();
	ConsoleCommandSender console = getServer().getConsoleSender();
	
	public void onEnable() {
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "# ------ SeriousRP ------ #");
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "SeriousRP " + getConfig().getString("Version"));
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "Boot sequence launched !");
		
		/*
		 * Version checker
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Version check -#");
		versionCheck();
		
		/* 
		 * Modules in config.yml
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Module manager, loading modules -#");
		if(getConfig().getBoolean("Core.Modules.CustomRecipes") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "CustomRecipes > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "CustomRecipes > OFF");
		}
		if(getConfig().getBoolean("Core.Modules.RPDeath") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "RPDeath > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "RPDeath > OFF");
		}
		if(getConfig().getBoolean("Core.Modules.Medics") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "Medics > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "Medics > OFF");
		}
		if(getConfig().getBoolean("Core.Modules.TownSystem") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "TownSystem > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "TownSystem > OFF");
		}
		if(getConfig().getBoolean("Core.Modules.RPMobiles") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "RPMobiles > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "RPMobiles > OFF");
		}
		if(getConfig().getBoolean("Core.Modules.Chairs") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "Chairs > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "Chairs > OFF");
		}
		
		/*
		 * Class loader (project)
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Class manager, loading classes -#");
		pm.registerEvents(new Commands(), this);
		pm.registerEvents(new Events(), this);
		pm.registerEvents(new Deaths(), this);
		console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "All classes have been loaded");

		/*
		 * Depencies like Telecom and Vault
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Depencies Manager -#");
		depencies();
		
		/*
		 * Commands loader
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Commands manager, loading commands -#");
		Commands commands = new Commands();
		getCommand(commands.seriousrp).setExecutor(commands);
		getCommand(commands.srtp).setExecutor(commands);
		
		if(getConfig().getBoolean("Core.Modules.TownSystem") == true) {
			getCommand(commands.srtown).setExecutor(commands);
		}
		if(getConfig().getBoolean("Core.Modules.Medics") == true) {
			getCommand(commands.revive).setExecutor(commands);
			getCommand(commands.hrprevive).setExecutor(commands);
			getCommand(commands.medinfo).setExecutor(commands);
		}
		console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "All commands have been loaded");
		
		/*
		 * Custom recipes
		 */
		if(getConfig().getBoolean("Core.Modules.CustomRecipes") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
			console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Custom recipes [Module] -#");
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "All recipes have been added !");
		}
		
		/*
		 * Config creator/modificator
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Config writer, writing and creating configs files -#");
		pm.registerEvents(new Config(), this);
		saveConfig();

		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "# ------ SeriousRP ------ #");
	}
	
	public void onDisable() {
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "# ------ SeriousRP ------ #");
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "SeriousRP " + getConfig().getString("Version"));
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "Shutdown sequence launched !");
		
		/* 
		 * Disable statement
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Goodbye ! -#");
		
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "# ------ SeriousRP ------ #");
	}
}
