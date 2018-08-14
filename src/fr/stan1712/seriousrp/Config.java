package fr.stan1712.seriousrp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import net.md_5.bungee.api.ChatColor;

public class Config implements Listener {
	private Main pl;

	private void VersionUpdate() {
		FileConfiguration config = this.pl.getConfig();
		this.pl.getConfig();
		    
		config.set("Version", "Version 3.9.9");
	}
	
	public Config(Main pl) {
	    this.pl = pl;
	    FileConfiguration config = pl.getConfig();
	    pl.getConfig();
	    
	    VersionUpdate();
	    
	    config.options().header("SeriousRP | Owner : stan1712 \nTraductors : ErHak_ / legaming04 -> https://github.com/stan1712/SeriousRP/wiki/Translations \nOur Discord : https://discord.gg/DkQSQa7");
	    if (this.pl.getConfig().getBoolean("ConfigFix"))
	    {
	      config.options().copyDefaults(true);
	      config.options().copyHeader(true);
	      
	      config.set("ConfigFix", Boolean.valueOf(false));
	      
	      pl.getServer().getConsoleSender().sendMessage("[SeriousRP] " + ChatColor.GREEN + "config.yml fixed !");
	    }
	    
	    pl.getServer().getConsoleSender().sendMessage("[SeriousRP] " + ChatColor.GREEN + "Config file up and running !");
	}
}
