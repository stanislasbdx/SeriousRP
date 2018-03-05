package fr.stan1712.srp;

import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

public class phone implements Listener {
	
	@SuppressWarnings("unused")
	private FileConfiguration config;
	  private srp pl;
	  
	  public phone(srp pl)
	  {
	    this.pl = pl;
	    this.config = pl.getConfig();
	  }
	
	@EventHandler
	public void onCommandes(PlayerCommandPreprocessEvent e){
		Player p = e.getPlayer();
		String msg = e.getMessage();
		String[] args = msg.split(" ");
		
		ItemStack phone = new ItemStack(Material.END_CRYSTAL, 1);
		ItemMeta customM = phone.getItemMeta();
		customM.setDisplayName("§bTéléphone");
		customM.setLore(Arrays.asList(this.pl.getConfig().getString("Phone.LorePhone1").replace("&", "§"),this.pl.getConfig().getString("Phone.LorePhone2").replace("&", "§"),this.pl.getConfig().getString("Phone.LorePhone3").replace("&", "§")+" "+p.getDisplayName()));
		customM.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
		phone.setItemMeta(customM);
		
		if (args[0].equalsIgnoreCase("/mobile")){
			if(p.getInventory().contains(phone) == true){
				Inventory inv = Bukkit.createInventory(null, 36, "§bTéléphone de "+p.getDisplayName());
				
				ItemStack headskull = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
				SkullMeta headskullM = (SkullMeta) headskull.getItemMeta();
				headskullM.setOwner(p.getName());
				headskullM.setDisplayName(ChatColor.AQUA+p.getDisplayName());
				headskull.setItemMeta(headskullM);
				
				inv.setItem(0, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(1, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(2, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(3, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(5, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(6, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(7, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(8, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(9, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(17, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(18, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(26, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(27, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(35, getItem(Material.STAINED_GLASS_PANE, " "));
				
				inv.setItem(4, headskull);
				
				//inv.setItem(4, getItem(Material.SKULL_ITEM, ChatColor.AQUA+p.getDisplayName()));
				inv.setItem(10, getItem(Material.EMERALD_BLOCK, "" + this.pl.getConfig().getString("Phone.Answer").replace("&", "§")));
				inv.setItem(11, getItem(Material.REDSTONE_BLOCK, "" + this.pl.getConfig().getString("Phone.HangUp").replace("&", "§")));
				inv.setItem(28, getItem(Material.BARRIER, "" + this.pl.getConfig().getString("Phone.Shutdown").replace("&", "§")));
				inv.setItem(34, getItem(Material.REDSTONE_LAMP_OFF, "" + this.pl.getConfig().getString("Phone.YourNumber").replace("&", "§")));
				if(this.pl.getConfig().getBoolean("TownSystem") == true ) {
					inv.setItem(16, getItem(Material.NETHER_STAR, "" + this.pl.getConfig().getString("Phone.GoToTown").replace("&", "§")));
				}
				inv.setItem(31, getItem(Material.JUKEBOX, "" + this.pl.getConfig().getString("Phone.Music").replace("&", "§")));
				
				p.openInventory(inv);
			}
			else{
				p.getInventory().addItem(phone);
				p.updateInventory();
			};
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event){
		Player p = event.getPlayer();
		Action action = event.getAction();
		ItemStack it = event.getItem();
		
		if(it == null) return;
		
		if(it.getType() == Material.END_CRYSTAL && it.hasItemMeta() && it.getItemMeta().hasDisplayName() && it.getItemMeta().getDisplayName().equalsIgnoreCase("§bTéléphone")){
			if(action == Action.RIGHT_CLICK_AIR){
				
				Inventory inv = Bukkit.createInventory(null, 36, "§bTéléphone de "+p.getDisplayName());
				
				ItemStack headskull = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
				SkullMeta headskullM = (SkullMeta) headskull.getItemMeta();
				headskullM.setOwner(p.getName());
				headskullM.setDisplayName(ChatColor.AQUA+p.getDisplayName());
				headskull.setItemMeta(headskullM);
				
				inv.setItem(0, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(1, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(2, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(3, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(5, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(6, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(7, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(8, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(9, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(17, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(18, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(26, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(27, getItem(Material.STAINED_GLASS_PANE, " "));
				inv.setItem(35, getItem(Material.STAINED_GLASS_PANE, " "));
				
				inv.setItem(4, headskull);
				
				//inv.setItem(4, getItem(Material.SKULL_ITEM, ChatColor.AQUA+p.getDisplayName()));
				inv.setItem(10, getItem(Material.EMERALD_BLOCK, "" + this.pl.getConfig().getString("Phone.Answer").replace("&", "§")));
				inv.setItem(11, getItem(Material.REDSTONE_BLOCK, "" + this.pl.getConfig().getString("Phone.HangUp").replace("&", "§")));
				inv.setItem(28, getItem(Material.BARRIER, "" + this.pl.getConfig().getString("Phone.Shutdown").replace("&", "§")));
				inv.setItem(34, getItem(Material.REDSTONE_LAMP_OFF, "" + this.pl.getConfig().getString("Phone.YourNumber").replace("&", "§")));
				if(this.pl.getConfig().getBoolean("TownSystem") == true ) {
					inv.setItem(16, getItem(Material.NETHER_STAR, "" + this.pl.getConfig().getString("Phone.GoToTown").replace("&", "§")));
				}
				inv.setItem(31, getItem(Material.JUKEBOX, "" + this.pl.getConfig().getString("Phone.Music").replace("&", "§")));
				
				p.openInventory(inv);
				
			}
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event){
		Inventory inv = event.getInventory();
		Player p = (Player) event.getWhoClicked();
		ItemStack current = event.getCurrentItem();
		
		if(current == null) return;
		
		if(inv.getName().equalsIgnoreCase("§bTéléphone de "+p.getDisplayName())){
			
			event.setCancelled(true);
			
			//if(current.getType() == Material.EMERALD_BLOCK){
			//	p.closeInventory();
			//	p.sendMessage("§bVous avez §adécroché");
			//}
			
			//if(current.getType() == Material.REDSTONE_BLOCK){
			//	p.closeInventory();
			//	p.sendMessage("§bVous avez §craccroché");
			//}
			
			if(current.getType() == Material.BARRIER){
				p.closeInventory();
			}
			
			if(current.getType() == Material.SKULL_ITEM) {
				p.sendMessage(this.pl.getConfig().getString("Phone.You").replace("&", "§"));
				p.closeInventory();
			}
			
			if(current.getType() == Material.NETHER_STAR){
				p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 22, 100));
				p.closeInventory();
				
				double x = this.pl.getConfig().getDouble("Locations.Town.x");
		        double y = this.pl.getConfig().getDouble("Locations.Town.y");
		        double z = this.pl.getConfig().getDouble("Locations.Town.z");
		        String monde = this.pl.getConfig().getString("Locations.Town.WorldName");
		        World world = Bukkit.getWorld(monde);
		        
		        p.teleport(new Location(world, x, y, z));
		        
		        p.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
		        p.sendMessage(ChatColor.GOLD + "❱❱ " + this.pl.getConfig().getString("Teleports.GoToTown").replace("&", "§"));
		        p.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
			}
		}
	}
	
	public ItemStack getItem(Material material, String customName){
		ItemStack it = new ItemStack(material, 1);
		ItemMeta itM = it.getItemMeta();
		itM.setDisplayName(customName);
		it.setItemMeta(itM);
		return it;
	}
	
	//public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
	//  if (cmd.getName().equalsIgnoreCase("chat")) {
	//  	if (sender.hasPermission("seriousrp.chat"))
	//      {
	//      	Player target = Bukkit.getPlayer(args[0]);
	//      	
	//      	if(target != null){
	//      		String message = "";
	//      		
	//      		for(int i = 1; i != args.length; i++)
	//      			message += args[i] + " ";
	//      			
	//      		target.sendMessage(ChatColor.GOLD+"["+ChatColor.AQUA+sender.getName()+ChatColor.GOLD+" -> "+ChatColor.AQUA+target.getName()+ChatColor.GOLD+"] : "+ChatColor.AQUA+ message);
	//      		sender.sendMessage(ChatColor.GOLD+"["+ChatColor.AQUA+target.getName()+ChatColor.GOLD+" <- "+ChatColor.AQUA+sender.getName()+ChatColor.GOLD+"] : "+ChatColor.AQUA+ message);
	//      		
	//      	}
	//      	else{
	//      		sender.sendMessage("§7Le joueur "+ChatColor.AQUA+target+"&7n'est pas en ligne");
	//      	}
	//      }
	//      else
	//      {
	//        sender.sendMessage(ChatColor.AQUA + "+----- ♖ " + getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
	//        sender.sendMessage(ChatColor.RED + "❱❱ Vous n'avez pas la permission !");
	//        sender.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
	//      }
	//  }
	//	return true;
	//}
}
