package fr.stan1712.seriousrp;

import java.util.Collection;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_12_R1.CommandExecute;

public class Commands extends CommandExecute implements Listener, CommandExecutor {	
	private Plugin plugin = Main.getPlugin(Main.class);
	
	public String seriousrp = "seriousrp";
	public String srtp = "srtp";
	
	// TownSystem
	public String srtown = "srtown";
	
	// Medics
	public String revive = "revive";
	public String hrprevive = "hrprevive";
	public String medinfo = "medinfo";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		Player player = (Player) sender;
		
		if(sender instanceof Player) {
			// /seriousrp <version/commands/info>
			if(cmd.getName().equalsIgnoreCase(seriousrp)) {
				if(sender.hasPermission("seriousrp.info")) {
					if(args.length == 1) {
						if(args[0].equalsIgnoreCase("version")) {
				            sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + plugin.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
				            sender.sendMessage(ChatColor.GOLD + "» " + plugin.getConfig().getString("Version").replace("&", "§"));
				            sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
							return true;
						}
						else if(args[0].equalsIgnoreCase("help")) {
							sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + plugin.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
				            sender.sendMessage(ChatColor.GOLD + "» /srtp = " + plugin.getConfig().getString("Core.HelpMsg.Dsrtp").replace("&", "§"));
				            sender.sendMessage(ChatColor.GOLD + "» /srtown = " + plugin.getConfig().getString("Core.HelpMsg.DTown").replace("&", "§"));
				            sender.sendMessage(ChatColor.GOLD + "» /srtown set = " + plugin.getConfig().getString("Core.HelpMsg.DTownSet").replace("&", "§"));
				            sender.sendMessage(ChatColor.GOLD + "» /srtown where = " + plugin.getConfig().getString("Core.HelpMsg.DTownWhere").replace("&", "§"));
				            sender.sendMessage(ChatColor.GOLD + "» /mobile = " + plugin.getConfig().getString("Core.HelpMsg.DPhone").replace("&", "§"));
				            sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
				            sender.sendMessage(ChatColor.GOLD + "» /seriousrp help = " + plugin.getConfig().getString("Core.HelpMsg.DVersion").replace("&", "§"));
				            sender.sendMessage(ChatColor.GOLD + "» /seriousrp version = " + plugin.getConfig().getString("Core.HelpMsg.DHelp").replace("&", "§"));
				            sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
							return true;
						}
						else if(args[0].equalsIgnoreCase("discord")) {
							sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + plugin.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
				            sender.sendMessage(ChatColor.GOLD + "» " + plugin.getConfig().getString("Core.Discord").replace("&", "§") + " https://discord.gg/DkQSQa7");
				            sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
				            return true;
						}
						else if(args[0].equalsIgnoreCase("status")) {
							sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + plugin.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
							if(plugin.getConfig().getBoolean("Core.Modules.CustomRecipes") == true) {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "CustomRecipes > ON");
							}
							else {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "CustomRecipes > OFF");
							}
							if(plugin.getConfig().getBoolean("Core.Modules.RPDeath") == true) {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "RPDeath > ON");
							}
							else {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "RPDeath > OFF");
							}
							if(plugin.getConfig().getBoolean("Core.Modules.Medics") == true) {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "Medics > ON");
							}
							else {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "Medics > OFF");
							}
							if(plugin.getConfig().getBoolean("Core.Modules.TownSystem") == true) {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "TownSystem > ON");
							}
							else {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "TownSystem > OFF");
							}
							if(plugin.getConfig().getBoolean("Core.Modules.RPMobiles") == true) {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "RPMobiles > ON");
							}
							else {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "RPMobiles > OFF");
							}
							if(plugin.getConfig().getBoolean("Core.Modules.Chairs") == true) {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.GREEN + "Chairs > ON");
							}
							else {
								sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.RED + "Chairs > OFF");
							}
				            sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
				            return true;
						}
						else {
							return false;
						}
					}
					else {
						sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + plugin.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
						sender.sendMessage(ChatColor.GOLD + "» " + plugin.getConfig().getString("Core.HelpMsg.Help").replace("&", "§"));
						sender.sendMessage(ChatColor.GOLD + "» " + plugin.getConfig().getString("Core.HelpMsg.VersionHelp").replace("&", "§"));
						sender.sendMessage(ChatColor.GOLD + "» " + plugin.getConfig().getString("Core.HelpMsg.DiscordHelp").replace("&", "§"));
						sender.sendMessage(ChatColor.GOLD + "» " + plugin.getConfig().getString("Core.HelpMsg.StatusHelp").replace("&", "§"));
						sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
						return true;
					}
				}
				else {
					sender.sendMessage("[" + plugin.getConfig().getString("Prefix").replace("&", "§") + "]" + plugin.getConfig().getString("Core.NoPerms").replace("&", "§"));
					return true;
				}
			}
			
			// /srtp
			if(cmd.getName().equalsIgnoreCase(srtp)) {
				if(sender.hasPermission("serious.randomtp")) {
					Random r = new Random();
					
					sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + plugin.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
					sender.sendMessage(ChatColor.GOLD + "» " + plugin.getConfig().getString("MicroModules.RandomTeleport").replace("&", "§"));
					sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
					
					player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 100));
					player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 50, 100));
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 100));
					
					int x = ((Player) sender).getLocation().getBlockX() + r.nextInt(10000);
					int z = ((Player) sender).getLocation().getBlockX() + r.nextInt(10000);
					
					int rtpcheck = ((Player) sender).getWorld().getHighestBlockYAt(x, z);
					int rtpdone = rtpcheck + 1;
					
					Location rtp = new Location(((Player) sender).getWorld(), x, rtpdone, z);
					((Player) sender).teleport(rtp);
					
					return true;
				}
				else {
					sender.sendMessage("[" + plugin.getConfig().getString("Prefix").replace("&", "§") + "]" + plugin.getConfig().getString("Core.NoPerms").replace("&", "§"));
					return true;
				}
			}
			
			// /srtown <set/where>
			if(cmd.getName().equalsIgnoreCase(srtown)) {
				double x = plugin.getConfig().getDouble("TownSystem.Locations.Town.x");
				double y = plugin.getConfig().getDouble("TownSystem.Locations.Town.y");
				double z = plugin.getConfig().getDouble("TownSystem.Locations.Town.z");
				String monde = plugin.getConfig().getString("TownSystem.Locations.Town.WorldName");
				World world = Bukkit.getWorld(monde);
				
				if(sender.hasPermission("seriousrp.town")) {
					if(args.length == 1) {
						if(args[0].equalsIgnoreCase("set")) {
							if(sender.hasPermission("seriousrp.townset")) {
								plugin.getConfig().set("TownSystem.Locations.Town.x", Integer.valueOf(((Player) sender).getLocation().getBlockX()));
								plugin.getConfig().set("TownSystem.Locations.Town.y", Integer.valueOf(((Player) sender).getLocation().getBlockY()));
								plugin.getConfig().set("TownSystem.Locations.Town.z", Integer.valueOf(((Player) sender).getLocation().getBlockZ()));
								plugin.getConfig().set("TownSystem.Locations.Town.WorldName", ((Player) sender).getWorld().getName());
								plugin.saveConfig();
								
								sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + plugin.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
								sender.sendMessage(ChatColor.GOLD + "» " + plugin.getConfig().getString("TownSystem.Teleports.SetTown").replace("&", "§"));
								sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
								return true;
							}
							else {
								sender.sendMessage("[" + plugin.getConfig().getString("Prefix").replace("&", "§") + "]" + plugin.getConfig().getString("Core.NoPerms").replace("&", "§"));
								return true;
							}
						}
						else if(args[0].equalsIgnoreCase("where")) {
							sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + plugin.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
							sender.sendMessage(ChatColor.GOLD + "» " + plugin.getConfig().getString("TownSystem.Teleports.TownWhere").replace("&", "§") + " x" + x + ", y" + y + ", z" + z);
							sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
				            return true;
						}
						else {
							return false;
						}
					}
					else {
						((Player) sender).teleport(new Location(world, x, y, z));
						
						sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + plugin.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
						sender.sendMessage(ChatColor.GOLD + "» " + plugin.getConfig().getString("TownSystem.Teleports.GoToTown").replace("&", "§"));
						sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
						return true;
					}
				}
				else {
					sender.sendMessage("[" + plugin.getConfig().getString("Prefix").replace("&", "§") + "]" + plugin.getConfig().getString("Core.NoPerms").replace("&", "§"));
					return true;
				}
			}
			
			// /revive
			if(cmd.getName().equalsIgnoreCase(revive)) {
				if(sender.hasPermission("seriousrp.medrevive")) {
					Player cible = Bukkit.getPlayer(args[0]);
					if(cible != null) {
						if(cible.getHealth() < 10.0D) {
							cible.setHealth(20.0D);
							sender.sendMessage(plugin.getConfig().getString("Medics.MedRevive.Revive").replace("&", "§") + " " + args[0]);
							
							cible.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
							cible.removePotionEffect(PotionEffectType.SLOW);
							cible.removePotionEffect(PotionEffectType.BLINDNESS);
							cible.removePotionEffect(PotionEffectType.HUNGER);
							cible.setFoodLevel(10);
							return true;
						}
						else {
							sender.sendMessage(plugin.getConfig().getString("Medics.MedRevive.NoNeed").replace("&", "§"));
							return true;
						}
					}
					else {
						sender.sendMessage(ChatColor.DARK_RED + args[0] + " " + plugin.getConfig().getString("Medics.MedRevive.Error").replace("&", "§"));
						return true;
					}
				}
				else {
					sender.sendMessage("[" + plugin.getConfig().getString("Prefix").replace("&", "§") + "]" + plugin.getConfig().getString("Core.NoPerms").replace("&", "§"));
					return true;
				}
			}
			// /hrprevive
			if(cmd.getName().equalsIgnoreCase("hrprevive")) {
				if(sender.hasPermission("seriousrp.medhrprevive")) {
					if(player.getHealth() < 10.0D) {
						sender.sendMessage(plugin.getConfig().getString("Medics.MedRevive.Revive").replace("&", "§") + " " + args[0]);
						
						player.setFoodLevel(10);
						player.setHealth(20.0D);
						
						player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
						player.removePotionEffect(PotionEffectType.SLOW);
						player.removePotionEffect(PotionEffectType.BLINDNESS);
						player.removePotionEffect(PotionEffectType.HUNGER);
						player.removePotionEffect(PotionEffectType.SLOW);
						return true;
					}
					else {
						sender.sendMessage(plugin.getConfig().getString("Medics.MedRevive.NoNeed").replace("&", "§"));
						return true;
					}
				}
				else {
					sender.sendMessage("[" + plugin.getConfig().getString("Prefix").replace("&", "§") + "]" + plugin.getConfig().getString("Core.NoPerms").replace("&", "§"));
					return true;
				}
			}
			// /medinfo
			if(cmd.getName().equalsIgnoreCase(medinfo)) {
				if(sender.hasPermission("seriousrp.medinfo")) {
					Player cible = Bukkit.getPlayer(args[0]);
					if (cible != null) {
						double vie = cible.getHealth();
						double faim = cible.getFoodLevel();
						    
						Collection<PotionEffect> effects = cible.getActivePotionEffects();
						    
						int x = Integer.valueOf(cible.getLocation().getBlockX()).intValue();
						int y = Integer.valueOf(cible.getLocation().getBlockY()).intValue();
						int z = Integer.valueOf(cible.getLocation().getBlockZ()).intValue();
						String monde = cible.getWorld().getName();
						    
						sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + args[0] + " ♖ -----+");
						if (vie > 10.0D) {
							sender.sendMessage("§a" + plugin.getConfig().getString("Medics.MedInfo.Health").replace("&", "§") + " " + vie / 2.0D + " " + plugin.getConfig().getString("Medics.MedInfo.Hearts").replace("&", "§"));
						}
						else if (vie <= 9.0D) {
							sender.sendMessage("§c" + plugin.getConfig().getString("Medics.MedInfo.Health").replace("&", "§") + " " + vie / 2.0D + " " + plugin.getConfig().getString("Medics.MedInfo.Hearts").replace("&", "§"));
						}
						sender.sendMessage("§a" + plugin.getConfig().getString("Medics.MedInfo.Food").replace("&", "§") + " " + faim / 2.0D + " / 10");
						sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
						sender.sendMessage("§a" + plugin.getConfig().getString("Medics.MedInfo.Effects").replace("&", "§") + " " + effects);
						sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
						sender.sendMessage("§a" + args[0] + " " + plugin.getConfig().getString("Medics.MedInfo.Coordinates").replace("&", "§") + " " + x + ", " + y + ", " + z + " > " + monde);
						sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
					}
					else {
						sender.sendMessage(ChatColor.DARK_RED + args[0] + " " + plugin.getConfig().getString("Medic.Error").replace("&", "§"));
					}
					
				}
				else {
					sender.sendMessage("[" + plugin.getConfig().getString("Prefix").replace("&", "§") + "]" + plugin.getConfig().getString("Core.NoPerms").replace("&", "§"));
					return true;
				}
			}
			
			
		}
		else {
			sender.sendMessage(ChatColor.RED + "Only real players can use this command !");
			return true;
		}
		
		return false;
	}
}
