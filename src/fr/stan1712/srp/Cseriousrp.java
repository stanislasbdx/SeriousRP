package fr.stan1712.srp;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Cseriousrp implements CommandExecutor {
	
    private FileConfiguration config;
    private Main pl;
  
    public Cseriousrp(Main pl)
    {
        this.pl = pl;
        this.config = pl.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
        	String name = sender.getName();
        	Player p = ((Player) sender).getPlayer();
        	if (p.hasPermission("seriousrp.info")){
                if (args.length == 1){
                    p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
                    p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("Help").replace("&", "§"));
                    p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("VersionHelp").replace("&", "§"));
                    p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("DiscordHelp").replace("&", "§"));
                    p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
                }
                if (args.length == 2){
                    if (args[1].equalsIgnoreCase("version")){
                        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
                        p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("Version").replace("&", "§"));
                        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
                    }
                    if (args[1].equalsIgnoreCase("discord")){
                        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
                        p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("Discord").replace("&", "§"));
                        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
                    }
                    if (args[1].equalsIgnoreCase("help")){
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
                    if(args[1].equalsIgnoreCase("modules")) {
                        if(this.pl.getConfig().getBoolean("RPDeath") == true) {
                        	System.out.println("[SeriousRP] Roleplay Deaths module activated");
                        }
                        if(this.pl.getConfig().getBoolean("Medics") == true) {
                            System.out.println("[SeriousRP] Medics System module activated");
                        }
                        if(this.pl.getConfig().getBoolean("TownSystem") == true) {
                            System.out.println("[SeriousRP] Town System module activated");
                        }
                        if(this.pl.getConfig().getBoolean("CustomRecipes") == true) {
                            System.out.println("[SeriousRP] Custom Recipes module activated");
                        }
                        if(this.pl.getConfig().getBoolean("RPMobile") == true) {
                            System.out.println("[SeriousRP] Roleplay Mobile module activated");
                        }
                        if (this.pl.getConfig().getBoolean("Chairs") == true) {
                            System.out.println("[SeriousRP] Chairs module activated");
                        }
                    }
                }
            }
            else{
                p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
                p.sendMessage(ChatColor.RED + "❱❱ Vous n'avez pas la permission !");
                p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
            }
        }
        return false;
    }

}
