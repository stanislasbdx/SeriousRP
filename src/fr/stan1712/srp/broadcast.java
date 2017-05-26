package fr.stan1712.srp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class broadcast implements CommandExecutor {

	public broadcast(srp srp) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {

			if(args.length == 0){
				sender.sendMessage("Veuillez inscrire §9/annonce + §7votre message");
			}
			
			if(args.length >= 1){
				
				StringBuilder broadcast = new StringBuilder("");
				
				for(String mot : args){
					if(!broadcast.equals("")) broadcast.append(" ");
					broadcast.append(mot);
				}
				
				Bukkit.broadcastMessage("§7[§9Annonce§7]" + broadcast);
				
			}
	
		
		return false;
	}

}