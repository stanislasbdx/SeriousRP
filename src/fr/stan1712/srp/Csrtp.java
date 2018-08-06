package fr.stan1712.srp;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Csrtp implements CommandExecutor {
	
    private FileConfiguration config;
    private Main pl;
  
    public Csrtp(Main pl)
    {
        this.pl = pl;
        this.config = pl.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
        	String name = sender.getName();
        	Player p = ((Player) sender).getPlayer();
	        if (args[0].equalsIgnoreCase("/srtp")){
	            if (p.hasPermission("seriousrp.random")){
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
	            }
	            else{
	                p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
	                p.sendMessage(ChatColor.RED + "❱❱ Vous n'avez pas la permission !");
	                p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
	            }
	        }
        }
		return false;
	}

}
