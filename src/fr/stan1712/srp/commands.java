package fr.stan1712.srp;

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
          if (args[1].equalsIgnoreCase("help"))
          {
            p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
            p.sendMessage(ChatColor.GOLD + "❱❱ /srtp = " + this.pl.getConfig().getString("HelpMsg.Dsrtp").replace("&", "§"));
            p.sendMessage(ChatColor.GOLD + "❱❱ /srtown = " + this.pl.getConfig().getString("HelpMsg.DTown").replace("&", "§"));
            p.sendMessage(ChatColor.GOLD + "❱❱ /srtown set = " + this.pl.getConfig().getString("HelpMsg.DTownSet").replace("&", "§"));
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
        int y = p.getWorld().getMaxHeight() - 150;
        int z = p.getLocation().getBlockZ() + r.nextInt(10000);
        
        Location rtp = new Location(p.getWorld(), x, y, z);
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
    //if (args[0].equalsIgnoreCase("/chat")) {
    //	if (p.hasPermission("seriousrp.chat"))
    //    {
    //    	Player target = Bukkit.getPlayer(args[0]);
    //    	
    //    	if(target != null){
    //    		String message = "";
    //    		
    //    		for(int i = 1; i != args.length; i++)
    //    			message += args[i] + " ";
    //    			
    //    		target.sendMessage(ChatColor.GOLD+"["+ChatColor.AQUA+p.getName()+ChatColor.GOLD+" -> "+ChatColor.AQUA+target.getName()+ChatColor.GOLD+"] : "+ChatColor.AQUA+ message);
    //    		p.sendMessage(ChatColor.GOLD+"["+ChatColor.AQUA+target.getName()+ChatColor.GOLD+" <- "+ChatColor.AQUA+target.getName()+ChatColor.GOLD+"] : "+ChatColor.AQUA+ message);
    //    		
    //    	}
    //    	else{
    //    		p.sendMessage("§7Le joueur "+ChatColor.AQUA+target+"§7 n'est pas en ligne !");
    //    	}
    //    }
    //    else
    //    {
    //      p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
    //      p.sendMessage(ChatColor.RED + "❱❱ Vous n'avez pas la permission !");
    //      p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
    //    }
    }
  }
