package fr.stan1712.srp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public class configupdate implements Listener {
	
	private Main pl;
	
	private void VersionUpdate() {		  
		FileConfiguration config = pl.getConfig();
		pl.getConfig();
		  
		config.set("Version", "Version 3.9.8-4.2b"); //Changement de version du plugin
	}
	
	private void NewAddings() {		  
		FileConfiguration config = pl.getConfig();
		pl.getConfig();

		// NE PAS OUBLIER DE LE CHANGER DANS LE CONFIG.YML !!
		  
		config.createSection("Modules");
		config.set("Modules.NoModule", "Le module n'est pas actif.");
		config.set("Modules.Error", "Une erreur a ete trouvee ! Regardez la console ainsi que votre fichier 'config.yml'");
		config.set("Modules.CustomRecipes", true);
		config.set("Modules.RPDeath", true);
		config.set("Modules.Medics", false);
		config.set("Modules.TownSystem", true);
		config.set("Modules.RPMobile", true);
		config.set("Modules.Chairs", true);

		config.set("HelpMsg.DRevive", "Reanime et soigne un joueur");
		config.set("HelpMsg.DMedinfo", "Permet de savoir l'etat de sante d'un joueur");
		//config.set("HelpMsg.DHRRevive", "Vous reanime et vous soigne");
	}
	
	public configupdate(Main pl)
	  {
		  this.pl = pl;
		  FileConfiguration config = pl.getConfig();
		  pl.getConfig();
		  
		  VersionUpdate();
		  NewAddings();
		  
		  config.options().header("SeriousRP | Owner : stan1712 \nTraductors : ErHak_ / legaming04 -> https://github.com/stan1712/SeriousRP/wiki/Translations \nOur Discord : https://discord.gg/DkQSQa7");
		  
		  if(this.pl.getConfig().getBoolean("ConfigFix") == true) {
			  config.options().copyDefaults(true); //False, laisse les infos des utilisateurs | True, remplace leurs infos par les miennes
			  config.options().copyHeader(true);
			  
			  config.set("ConfigFix", false);
			  
			  System.out.println("[SeriousRP] Config reloaded !");
		  }
	  }
}
