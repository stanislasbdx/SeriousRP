package fr.stan1712.srp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
	
	public telephone(srp srp) {
		// TODO Auto-generated constructor stub
	}

	@EventHandler
	public void onClickInventaire(InventoryClickEvent e){
		if(e.getInventory().getTitle().equalsIgnoreCase("Telephone")){		
			if(e.getCurrentItem().getType() == Material.BARRIER){
				Player p = (Player) e.getWhoClicked();
				p.closeInventory();
				e.setCancelled(true);
			}
			if(e.getCurrentItem().getType() == Material.EMERALD_BLOCK){
				Player p = (Player) e.getWhoClicked();
				p.closeInventory();
				p.performCommand("tellraw @p ['',{'text':'+----- ----- ----- -----+','color':'aqua'},{'text':'\n'},{'text':'❱❱ Entrez le numéro de téléphone du destinataire : ','color':'gold'},{'text':'\n'},{'text':'+----- ----- ----- -----+','color':'aqua'}]");
				e.setCancelled(true);
			}
			if(e.getCurrentItem().getType() == Material.REDSTONE_BLOCK){
				Player p = (Player) e.getWhoClicked();
				p.closeInventory();
				p.performCommand("tellraw @p ['',{'text':'+----- ----- ----- -----+','color':'aqua'},{'text':'\n'},{'text':'❱❱ Appel raccroché !','color':'red'},{'text':'\n'},{'text':'+----- ----- ----- -----+','color':'aqua'}]");
				e.setCancelled(true);
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
