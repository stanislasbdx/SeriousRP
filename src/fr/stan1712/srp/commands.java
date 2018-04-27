package fr.stan1712.srp;

import java.util.Collection;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class commands implements Listener
{
  private FileConfiguration config;
  private srp pl;
  
  public commands(srp pl)
  {
    this.pl = pl;
    this.config = pl.getConfig();
  }
  
  @EventHandler
  public void onCommandes(PlayerCommandPreprocessEvent e)
  {	
    Player p = e.getPlayer();
    String pseudo = p.getDisplayName();
    String msg = e.getMessage();
    String[] args = msg.split(" ");

    if (args[0].equalsIgnoreCase("/seriousrp"))
    {
      if (p.hasPermission("seriousrp.info"))
      {
        if (args.length == 1)
        {
          p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
          p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("Help").replace("&", "§"));
          p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("VersionHelp").replace("&", "§"));
          p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("DiscordHelp").replace("&", "§"));
          p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
        }
        if (args.length == 2)
        {
          if (args[1].equalsIgnoreCase("version"))
          {
            p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
            p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("Version").replace("&", "§"));
            p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
          }
          if (args[1].equalsIgnoreCase("discord"))
          {
            p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
            p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("Discord").replace("&", "§"));
            p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
          }
          if (args[1].equalsIgnoreCase("help"))
          {
            p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
            p.sendMessage(ChatColor.GOLD + "❱❱ /srtp = " + this.pl.getConfig().getString("HelpMsg.Dsrtp").replace("&", "§"));
            p.sendMessage(ChatColor.GOLD + "❱❱ /srtown = " + this.pl.getConfig().getString("HelpMsg.DTown").replace("&", "§"));
            p.sendMessage(ChatColor.GOLD + "❱❱ /srtown set = " + this.pl.getConfig().getString("HelpMsg.DTownSet").replace("&", "§"));
            p.sendMessage(ChatColor.GOLD + "❱❱ /srtown where = " + this.pl.getConfig().getString("HelpMsg.DTownWhere").replace("&", "§"));
            p.sendMessage(ChatColor.GOLD + "❱❱ /mobile = " + this.pl.getConfig().getString("HelpMsg.DPhone").replace("&", "§"));
            p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
            p.sendMessage(ChatColor.GOLD + "❱❱ /seriousrp help = " + this.pl.getConfig().getString("HelpMsg.DVersion").replace("&", "§"));
            p.sendMessage(ChatColor.GOLD + "❱❱ /seriousrp version = " + this.pl.getConfig().getString("HelpMsg.DHelp").replace("&", "§"));
            p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
          }
        }
      }
      else
      {
        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
        p.sendMessage(ChatColor.RED + "❱❱ Vous n'avez pas la permission !");
        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
      }
      e.setCancelled(true);
    }
    if (args[0].equalsIgnoreCase("/srtp")) {
      if (p.hasPermission("seriousrp.random"))
      {
        Random r = new Random();
        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
        p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("RandomTeleport").replace("&", "§"));
        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");

	    p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 100));
	    p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 50, 100));
	    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 100));
        
        int x = p.getLocation().getBlockX() + r.nextInt(10000);
        int z = p.getLocation().getBlockZ() + r.nextInt(10000);

        int rtpcheck = p.getWorld().getHighestBlockYAt(x, z);
        
        int rtpdone = rtpcheck + 1;
        
        Location rtp = new Location(p.getWorld(), x, rtpdone, z);

        p.teleport(rtp);
        
        e.setCancelled(true);
      }
      else
      {
        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
        p.sendMessage(ChatColor.RED + "❱❱ Vous n'avez pas la permission !");
        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
      }
    }
    if(this.pl.getConfig().getBoolean("TownSystem") == true ) {
	    if (args[0].equalsIgnoreCase("/srtown"))
	    {
	      if ((p.hasPermission("seriousrp.tptown")) && 
	        (args.length == 1))
	      {
	        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
	        p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("Teleports.GoToTown").replace("&", "§"));
	        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
	        
	        double x = this.pl.getConfig().getDouble("Locations.Town.x");
	        double y = this.pl.getConfig().getDouble("Locations.Town.y");
	        double z = this.pl.getConfig().getDouble("Locations.Town.z");
	        String monde = this.pl.getConfig().getString("Locations.Town.WorldName");
	        World world = Bukkit.getWorld(monde);
	        
	        p.teleport(new Location(world, x, y, z));
	      }
	      if (p.hasPermission("seriousrp.townset")){
			    if ((args.length == 2) && (args[1].equalsIgnoreCase("set"))){
			      p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
			      p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("Teleports.SetTown").replace("&", "§"));
			      p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
			      
			      this.config.set("Locations.Town.x", Integer.valueOf(p.getLocation().getBlockX()));
			      this.config.set("Locations.Town.y", Integer.valueOf(p.getLocation().getBlockY()));
			      this.config.set("Locations.Town.z", Integer.valueOf(p.getLocation().getBlockZ()));
			      this.config.set("Locations.Town.WorldName", p.getWorld().getName());
			      this.pl.saveConfig();
			    }
	      }
	      if (p.hasPermission("seriousrp.townwhere")){
			    if ((args.length == 2) && (args[1].equalsIgnoreCase("where"))){     
			      double x = this.pl.getConfig().getDouble("Locations.Town.x");
			      double y = this.pl.getConfig().getDouble("Locations.Town.y");
			      double z = this.pl.getConfig().getDouble("Locations.Town.z");
			      
			      p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
			      p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("Teleports.TownWhere").replace("&", "§") + " " + x + ", " + y + ", " + z);
			      p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
			    }
	      }
	      else
	      {
	        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
	        p.sendMessage(ChatColor.RED + "❱❱ Vous n'avez pas la permission !");
	        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
	      }
      }
     }
    else {
			p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("NoModule").replace("&", "§"));
	}
    if(this.pl.getConfig().getBoolean("Medics") == true ) {
	    if (args[0].equalsIgnoreCase("/revive"))
	    {
	      if (p.hasPermission("seriousrp.medicrevive"))
	      {
	    	  Player cible = Bukkit.getPlayer(args[1]);
	    	  
	    	  if(cible != null){	
	    		  if(cible.getHealth() < 10) {
	    			  	cible.setHealth(20);
	    			  	p.sendMessage(this.pl.getConfig().getString("Medic.Revive").replace("&", "§") + " " + args[1]);
						
						cible.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
						cible.removePotionEffect(PotionEffectType.SLOW);
						cible.removePotionEffect(PotionEffectType.BLINDNESS);
						cible.removePotionEffect(PotionEffectType.HUNGER);
						
						cible.setFoodLevel(10);
	    		  }
	    		  else {
	    			  p.sendMessage(this.pl.getConfig().getString("Medic.NoNeed").replace("&", "§"));
	    		  }
	    	  }
        	  else{
        	  	  p.sendMessage(ChatColor.DARK_RED + args[1] + " " + this.pl.getConfig().getString("Medic.Error").replace("&", "§"));
        	  }
	      }
	      else
	      {
	        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
	        p.sendMessage(ChatColor.RED + "❱❱ Vous n'avez pas la permission !");
	        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
	      }
      }
	    if (args[0].equalsIgnoreCase("/hrprevive"))
	    {
	      if (p.hasPermission("seriousrp.medichrp"))
	      {    	  
			  if(p.getHealth() < 10) {
				  	p.setHealth(20);
				  	p.sendMessage(this.pl.getConfig().getString("Medic.Revive").replace("&", "§") + " " + pseudo);
					
					p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
					p.removePotionEffect(PotionEffectType.SLOW);
					p.removePotionEffect(PotionEffectType.BLINDNESS);
					p.removePotionEffect(PotionEffectType.HUNGER);
					
					p.setFoodLevel(10);
			  }
    		  else {
    			  p.sendMessage(this.pl.getConfig().getString("Medic.NoNeed").replace("&", "§"));
    		  }
	      }
	      else
	      {
	        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
	        p.sendMessage(ChatColor.RED + "❱❱ Vous n'avez pas la permission !");
	        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
	      }
	    }
	    if (args[0].equalsIgnoreCase("/medinfo"))
	    {
	      if (p.hasPermission("seriousrp.medinfo"))
	      {
	    	  Player cible = Bukkit.getPlayer(args[1]);
	    	  
	    	  if(cible != null){	    		
	    		double vie = cible.getHealth();
	    		double faim = cible.getFoodLevel();
	    		
	    		Collection<PotionEffect> effects = cible.getActivePotionEffects();
	    		
	    		int x = Integer.valueOf(cible.getLocation().getBlockX());
	    		int y = Integer.valueOf(cible.getLocation().getBlockY());
	    		int z = Integer.valueOf(cible.getLocation().getBlockZ());
	    		String monde = cible.getWorld().getName();
	    		
	    		p.sendMessage(ChatColor.AQUA + "+----- ♖ MedInfo " + args[1] + " ♖ -----+");
	    		if(vie > 10){
	    			p.sendMessage("§a" + this.pl.getConfig().getString("MedInfo.Health").replace("&", "§") + " " + vie/2 + " " + this.pl.getConfig().getString("MedInfo.Hearts").replace("&", "§"));
	    		}
	    		else if(vie <= 9){
	    			p.sendMessage("§c" + this.pl.getConfig().getString("MedInfo.Health").replace("&", "§") + " " + vie/2 + " " + this.pl.getConfig().getString("MedInfo.Hearts").replace("&", "§"));
	    		}
	    		p.sendMessage("§a" + this.pl.getConfig().getString("MedInfo.Food").replace("&", "§") + " " + faim/2 + " / 10");
	    		p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
	    		p.sendMessage("§a" + this.pl.getConfig().getString("MedInfo.Effects").replace("&", "§") + effects);
	    		p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
	    		p.sendMessage("§a" + args[1] + " " + this.pl.getConfig().getString("MedInfo.Coordinates").replace("&", "§") + " " + x + ", " + y + ", " + z + " > " + monde);
	    		p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
	    	  }
	    	  else{
	    	  	  p.sendMessage(ChatColor.DARK_RED + args[1] + " " + this.pl.getConfig().getString("Medic.Error").replace("&", "§"));
	    	  }
	      }
	      else
	      {
	        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
	        p.sendMessage(ChatColor.RED + "❱❱ Vous n'avez pas la permission !");
	        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
	      }
	  }
    }
    else {
			p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("NoModule").replace("&", "§"));
	}
  	}
  }
