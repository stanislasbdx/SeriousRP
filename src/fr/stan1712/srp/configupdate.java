package fr.stan1712.srp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public class configupdate implements Listener {
	
	private srp pl;
	
	public configupdate(srp pl)
	  {
		  this.pl = pl;
		  pl.getConfig();
		  
		  FileConfiguration config = pl.getConfig();
		  pl.getConfig();
		  
		  config.set("Version", "Version 3.9.8-3.0g");
		  config.options().header("SeriousRP | Owner : stan1712 \nTraductors : ErHak_ / legaming04 -> https://github.com/stan1712/SeriousRP/wiki/Translations \nOur Discord : https://discord.gg/DkQSQa7");
		  
		  if(this.pl.getConfig().getBoolean("ConfigFix") == true) {
			  config.options().copyDefaults(true); //False, laisse les infos des utilisateurs | True, remplace leurs infos par les miennes
			  config.options().copyHeader(true);
			  
			  config.set("ConfigFix", false);
			  
			  System.out.println("[SeriousRP] Config reloaded");
		  }
	  }
}
