package fr.stan1712.srp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public class configupdate implements Listener {
	
	  private FileConfiguration config;
	  private FileConfiguration messages;
	  private srp pl;
	  
	  public configupdate(srp pl)
	  {
		  this.pl = pl;
		  FileConfiguration config = pl.getConfig();
		  this.config = pl.getConfig();
		  
		  config.set("Version", "Version 3.9.8-2.5b");
		  config.options().copyDefaults(true);
	  }
	  public void messageupdate(srp pl) {
		  this.pl = pl;
		  FileConfiguration messages = pl.getConfig();
		  this.messages = pl.getConfig();
		  
    	  messages.createSection("HelpMsg");
		  messages.addDefault("HelpMsg.Dsrtp", "Pouvoir se téléporter aléatoirement");
		  messages.addDefault("HelpMsg.DTown", "Permet de se téléporter dans la ville");
		  messages.addDefault("HelpMsg.DTownSet", "Permet de définir le point de spawn de la ville");
		  messages.addDefault("HelpMsg.DTownWhere", "Permet de savoir ou se trouve la ville");
		  messages.addDefault("HelpMsg.DPhone", "Affiche le menu du telephone");
		  messages.addDefault("HelpMsg.DRevive", "Permet de réanimer un joueur");
		  messages.addDefault("HelpMsg.DMedInfo", "Permet de connaître les statistiques d'un joueur");
		  messages.addDefault("HelpMsg.DHelp", "Affiche l'aide");
		  messages.addDefault("HelpMsg.DVersion", "Connaître la version du plugin");
		  
		  messages.createSection("Medic");
		  messages.addDefault("Medic.Revive", "&aVous avez bien reanimé le joueur");
		  messages.addDefault("Medic.Error", "&cn''est pas en ligne ou vous avez mal orthographie son pseudo !");
		  messages.addDefault("Medic.NoNeed", "&cLe joueur n''a pas besoin de se faire réanimer");
		  
		  messages.createSection("MedInfo");
		  messages.addDefault("MedInfo.Health", "Vie :");
		  messages.addDefault("MedInfo.Hearts", "coeurs");
		  messages.addDefault("MedInfo.Food", "Nourriture :");
		  messages.addDefault("MedInfo.Coordinates", "est en :");
		  messages.addDefault("MedInfo.Effects", "Effets");
		 
		  messages.createSection("Phone");
		  messages.addDefault("Phone.LorePhone1", "&9Voici votre téléphone");
		  messages.addDefault("Phone.LorePhone2", "&9Clic droit pour l''ouvrir");
		  messages.addDefault("Phone.LorePhone3", "&6Propriétaire :");
		  messages.addDefault("Phone.Answer", "&aDécrocher");
		  messages.addDefault("Phone.HangUp", "&cRaccrocher");
		  messages.addDefault("Phone.Shutdown", "&cEteindre");
		  messages.addDefault("Phone.You", "&aOui, c''est vous, vous êtes beau non ?");
		  messages.addDefault("Phone.Music", "&cBientôt...");
		  messages.addDefault("Phone.GoToTown", "&aDirection : Ville");
		  messages.addDefault("Phone.YourNumber", "&cBientôt...");
		  
		  messages.createSection("Teleports");
		  messages.addDefault("Teleports.GoToTown", "Vous avez été téléporté dans la ville");
		  messages.addDefault("Teleports.SetTown", "Vous avez défini le spawn de la ville");
		  messages.addDefault("Teleports.TownWhere", "Le spawn de la ville se trouve en :");
		  
		  messages.options().copyDefaults(true);
	  }
}
