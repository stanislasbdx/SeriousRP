package fr.stan1712.srp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class telephone implements Listener {
	
  private FileConfiguration config;
  private srp pl;
  
  public telephone(srp pl)
  {
    this.pl = pl;
    this.config = pl.getConfig();
  }

	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		
		Player player = e.getPlayer();
		Action action = e.getAction();
		ItemStack it = e.getItem();
		
		if(it.getType() == Material.TRIPWIRE_HOOK){
          player.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
          player.sendMessage(ChatColor.GOLD + "❱❱ Vous venez d'ouvrir votre téléphone.");
          player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");
          
          if(e.getItem().getItemMeta().equals("Téléphone")){	
  			if(e.getItem().getType() == Material.BARRIER){
  				player.closeInventory();
  				e.setCancelled(true);
  			}
  			if(e.getItem().getType() == Material.EMERALD_BLOCK){
  				player.closeInventory();
  				player.performCommand("tellraw @p ['',{'text':'+----- ----- ----- -----+','color':'aqua'},{'text':'\n'},{'text':'❱❱ Entrez le numéro de téléphone du destinataire : ','color':'gold'},{'text':'\n'},{'text':'+----- ----- ----- -----+','color':'aqua'}]");
  				e.setCancelled(true);
  			}
  			if(e.getItem().getType() == Material.REDSTONE_BLOCK){
  				player.closeInventory();
  				player.performCommand("tellraw @p ['',{'text':'+----- ----- ----- -----+','color':'aqua'},{'text':'\n'},{'text':'❱❱ Appel raccroché !','color':'red'},{'text':'\n'},{'text':'+----- ----- ----- -----+','color':'aqua'}]");
  				e.setCancelled(true);
  			}
  		}
		}
	}
	
	@EventHandler
	public void onClickBoussole(PlayerInteractEvent e){
		Player p = e.getPlayer();
		if(e.getItem().getType() == Material.TRIPWIRE_HOOK){
			if(e.getAction() == Action.RIGHT_CLICK_AIR){
				Inventory inv = Bukkit.createInventory(null, 4*9, "Telephone");
				
				ItemStack coffre = new ItemStack(Material.BARRIER, 1);
				ItemMeta coffreM = coffre.getItemMeta();
				coffreM.setDisplayName(ChatColor.RED+"❱❱ Retour");
				coffre.setItemMeta(coffreM);
				
				ItemStack appel = new ItemStack(Material.EMERALD_BLOCK, 1);
				ItemMeta appelM = appel.getItemMeta();
				appelM.setDisplayName(ChatColor.GREEN+"❱❱ Appeler");
				appel.setItemMeta(appelM);
				
				ItemStack raccroche = new ItemStack(Material.REDSTONE_BLOCK, 1);
				ItemMeta raccrocheM = raccroche.getItemMeta();
				raccrocheM.setDisplayName(ChatColor.DARK_RED+"❱❱ Raccrocher");
				raccroche.setItemMeta(raccrocheM);
				
				inv.setItem(11, raccroche);
				inv.setItem(10, appel);
				inv.setItem(35, coffre);
				p.openInventory(inv);
			}
		}
	}
}
