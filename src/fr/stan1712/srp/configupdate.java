package fr.stan1712.srp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public class configupdate implements Listener {
	
	private Main pl;
	
	private void versionupdate() {		  
		FileConfiguration config = pl.getConfig();
		pl.getConfig();
		  
		config.set("Version", "Version 3.9.8-3.8b"); //Changement de version du plugin
	}
	
	private void newaddings() {		  
		FileConfiguration config = pl.getConfig();
		pl.getConfig();
		  
		config.createSection("Modules");
		config.set("Modules.NoModule", "Le module n'est pas actif.");
		config.set("Modules.CustomRecipes", true);
		config.set("Modules.RPDeath", false);
		config.set("Modules.Medics", true);
		config.set("Modules.TownSystem", true);
		config.set("Modules.RPMobile", true);
		config.set("Modules.Chairs", true);

		config.createSection("heal");
		config.set("heal.checkRate", 20);
		config.set("heal.amount", 1);
		config.set("heal.enabled", true);

		config.createSection("RandomWords");
		config.set("RandomWords.to", "à");

		config.set("HelpMsg.DRevive", "Réanime et soigne un joueur");
		config.set("HelpMsg.DMedinfo", "Permet de savoir l'etat de santé d'un joueur");
		config.set("HelpMsg.DHRRevive", "Vous réanime et vous soigne");
	}
	
	public configupdate(Main pl)
	  {
		  this.pl = pl;
		  FileConfiguration config = pl.getConfig();
		  pl.getConfig();
		  
		  versionupdate();
		  newaddings();
		  
		  config.options().header("SeriousRP | Owner : stan1712 \nTraductors : ErHak_ / legaming04 -> https://github.com/stan1712/SeriousRP/wiki/Translations \nOur Discord : https://discord.gg/DkQSQa7");
		  
		  if(this.pl.getConfig().getBoolean("ConfigFix") == true) {
			  config.options().copyDefaults(true); //False, laisse les infos des utilisateurs | True, remplace leurs infos par les miennes
			  config.options().copyHeader(true);
			  
			  config.set("ConfigFix", false);
			  
			  System.out.println("[SeriousRP] Config reloaded !");
		  }
	  }
}
