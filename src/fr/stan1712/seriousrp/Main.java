package fr.stan1712.seriousrp;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener {	
	public void versionCheck() {
		String version = getServer().getVersion();
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "Server version : " + version);
		if(version.contains("Spigot") || version.contains("Paper")) {
			if(version.contains("1.13") || version.contains("1.14") || version.contains("1.15")) {
				console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "Version check !");
				console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "If you got issues, you can report them on Github");
			}
			else if(version.contains("1.12")){
				console.sendMessage("[SeriousRP] " + ChatColor.GOLD + "*** WARNING ***");
				console.sendMessage("[SeriousRP] " + ChatColor.GOLD + "* 1.12 may have errors while running *");
				console.sendMessage("[SeriousRP] " + ChatColor.GOLD + "* If you got errors, report it on Github *");
				console.sendMessage("[SeriousRP] " + ChatColor.GOLD + "*** *** ***");
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
		//File Tdirect = new File("plugins/Telecom");
		File Vdirect = new File("plugins/Vault");
		/*if (!Tdirect.exists()) {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** *** *** *** *** *** WARNING *** *** *** *** *** *** *** ***");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* {Telecom} Telecom was not found !	                       *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* {Telecom} Deactivation of the module RPMobile.	               *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* {Telecom} If you want to use SRP mobile, you will need Telecom. *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***");
			  
			getConfig().set("Core.Modules.RPMobile", Boolean.valueOf(false));
		}
		else {
			System.out.println("[SeriousRP] {Telecom} Telecom hooked !");
		}*/
		if (!Vdirect.exists()) {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** *** *** *** *** *** WARNING *** *** *** *** *** *** *** ***");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* {Vault} Vault was not found !	       			       *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* {Vault} Deactivation of the module Economy.		       *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "* {Vault} If you want to use SRP mobile, you will need Vault.     *");
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***");
			  
			getConfig().set("Core.Modules.Economy", Boolean.valueOf(false));
		}
		else {			
			System.out.println("[SeriousRP] {Vault} Vault hooked !");
		}
	}
	
	private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	public static Economy economy = null;
	
	public PluginManager pm = getServer().getPluginManager();
	public ConsoleCommandSender console = getServer().getConsoleSender();
	
	public void onEnable() {
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "# ------ SeriousRP ------ #");
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "SeriousRP v" + getConfig().getString("Version"));
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "Boot sequence launched !");
		int pluginId = 7297;
		@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this, pluginId);
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "Metrics enabled !");
		
		/*
		 * Version checker
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Version check -#");
		versionCheck();
		new UpdateChecker(this, 31443).getVersion(version -> {
            if(!this.getDescription().getVersion().equalsIgnoreCase(version)) {            	
            	console.sendMessage("[SeriousRP] " + ChatColor.BLUE + "An update is available on Spigot ! (" + version + ")");
            }
        });
		
		
		/*
		 * Class loader (project)
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Class manager, loading classes -#");
		pm.registerEvents(new Events(this), this);
		pm.registerEvents(new Deaths(), this);
		console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "All classes have been loaded");

		/*
		 * Depencies like Telecom and Vault
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Depencies Manager -#");
		depencies();
		
		//vault loader
		if(getConfig().getBoolean("Core.Modules.Economy") == true) {
			if(setupEconomy()) {
				console.sendMessage("[SeriousRP] {Vault} Economy loaded !");
			}
			else {
				console.sendMessage("[SeriousRP] " + ChatColor.RED + "Error while activating economy, Economy system is now inactive.");
				getConfig().set("Core.Modules.Economy", Boolean.valueOf(false));
			}
		}
		
		/*
		 * Commands loader
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Commands manager, loading commands -#");
		pm.registerEvents(new Commands(this), this);
		console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "All commands have been loaded");
		
		/*
		 * Custom recipes
		 */
		if(getConfig().getBoolean("Core.Modules.CustomRecipes") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
			console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Custom recipes [Module] -#");
			pm.registerEvents(new Recipes(this), this);
			
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "All recipes have been added !");
		}
		
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
		/*if(getConfig().getBoolean("Core.Modules.RPMobiles") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "RPMobiles > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "RPMobiles > OFF");
		}*/
		if(getConfig().getBoolean("Core.Modules.Chairs") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "Chairs > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "Chairs > OFF");
		}
		if(getConfig().getBoolean("Core.Modules.Economy") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "Economy > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "Economy > OFF");
		}	      	
		
		/*
		 * Config creator/modificator
		 */
		console.sendMessage("[SeriousRP] " + ChatColor.BLUE + " ");
		console.sendMessage("[SeriousRP] " + ChatColor.DARK_BLUE + "#- Config manager -#");
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
