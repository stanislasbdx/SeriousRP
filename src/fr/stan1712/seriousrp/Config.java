package fr.stan1712.seriousrp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import net.md_5.bungee.api.ChatColor;

public class Config implements Listener {
	private Plugin plugin = Main.getPlugin(Main.class);
	
	String version = this.plugin.getDescription().getVersion();
	String fileVersion = this.plugin.getConfig().getString("Version");
	
	Boolean updatable;

	private void VersionUpdate() {
		plugin.getConfig();
		    
		if(!version.equals(fileVersion)) {
			new File(plugin.getDataFolder() + "/upgrades/").mkdirs();
			File upgradeFile = new File(plugin.getDataFolder() + "/upgrades/", fileVersion + "_to_" + version + ".yml");
			
			try {
				if(!upgradeFile.exists()) {
					upgradeFile.createNewFile();
					
					FileConfiguration configReport = YamlConfiguration.loadConfiguration(upgradeFile);
					
			        configReport.options().header("SeriousRP Upgrade Log [" + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(Calendar.getInstance().getTime()) + "]\n"
			        		+ "Upgrade from version " + fileVersion + " to " + version + "\n"
			        		+ "All those informations can be used and sent to a developer in you have issues with upgrading your plugin");
			        
			        
			        configReport.set("report.serverVersion", plugin.getServer().getVersion());
			        //configReport.set("report.error", false);
			        
			        new UpdateChecker(this.plugin, 31443).getVersion(version -> {
			            if(!plugin.getDescription().getVersion().equalsIgnoreCase(version)) {
			            	configReport.set("report.updateAvailable", true);
			            	configReport.set("report.updateAvailable.version", version);
			            	configReport.set("report.updateAvailable.link", "https://www.spigotmc.org/resources/seriousrp.31443/");
			            }
			            else {
			            	configReport.set("report.updateAvailable", false);
			            }
			        });
			        
			        configReport.set("options.configFix", plugin.getConfig().getBoolean("ConfigFix"));
			        if(plugin.getConfig().getInt("Version") != 0) {
			        	configReport.set("options.firstRun", false);
			        }
			        else {
			        	configReport.set("options.firstRun", true);
			        }
			        configReport.set("options.depencies.vault", new File("plugins/Vault").exists());
			        configReport.set("options.depencies.telecom", new File("plugins/Telecom").exists());
			        
			        configReport.set("options.modules.CustomRecipes", plugin.getConfig().getBoolean("Core.Modules.CustomRecipes"));
			        configReport.set("options.modules.RPDeath", plugin.getConfig().getBoolean("Core.Modules.RPDeath"));
			        configReport.set("options.modules.Medics", plugin.getConfig().getBoolean("Core.Modules.Medics"));
			        configReport.set("options.modules.TownSystem", plugin.getConfig().getBoolean("Core.Modules.TownSystem"));
			        configReport.set("options.modules.Chairs", plugin.getConfig().getBoolean("Core.Modules.Chairs"));
			        configReport.set("options.modules.Economy", plugin.getConfig().getBoolean("Core.Modules.Economy"));
			        
			        configReport.set("lastConfig", plugin.getConfig().getRoot());
			        
			        configReport.save(upgradeFile);
					
					plugin.getServer().getConsoleSender().sendMessage("[SeriousRP] " + ChatColor.GREEN + "Log created (upgrades/" + fileVersion + "_to_" + version + ".yml) !");
					
					plugin.getConfig().set("Version", version);
					plugin.getServer().getConsoleSender().sendMessage("[SeriousRP] " + ChatColor.GREEN + "config.yml upgraded (" + fileVersion + " -> " + version + ") !");
				}
				else {
					plugin.getServer().getConsoleSender().sendMessage("[SeriousRP] " + ChatColor.RED + "Log " + ChatColor.DARK_RED + fileVersion + "_to_" + version + ".yml " + ChatColor.RED + "already exists !");
				}
			} catch (IOException e) {
				plugin.getServer().getConsoleSender().sendMessage("[SeriousRP] " + ChatColor.RED + "Unable to create the upgrade log !");
			}
			
			plugin.getConfig().set("ConfigFix", Boolean.valueOf(true));
			new Config();
            plugin.saveConfig();
		}
	}
	
	public Config() {
	    FileConfiguration config = plugin.getConfig();
		plugin.getConfig();
	    
	    VersionUpdate();
	    
	    config.options().header("SeriousRP | Owner : stan1712\n"
	    		+ "Traductors : ErHak_ / legaming04 -> https://github.com/stan1712/SeriousRP/wiki/Translations\n"
	    		+ "Our Discord : https://discord.gg/DkQSQa7\n"
	    		+ "Material list : https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html (for the Chairs module)");
	    
	    if(config.getBoolean("ConfigFix")) {
	      config.options().copyDefaults(true);
	      config.options().copyHeader(true);
	      
	      config.set("ConfigFix", Boolean.valueOf(false));
	      
	      plugin.getServer().getConsoleSender().sendMessage("[SeriousRP] " + ChatColor.GREEN + "config.yml updated !");
	    }
	    
		plugin.getServer().getConsoleSender().sendMessage("[SeriousRP] " + ChatColor.GREEN + "Config file up and running !");
	}
}
