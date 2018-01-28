package fr.stan1712.srp;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class menu implements Listener {
	  private Inventory inv;
	  
	  public menu(srp srp) {
		// TODO Auto-generated constructor stub
	}

	public void Menu(Plugin p)
	  {
	    this.inv = Bukkit.getServer().createInventory(null, 9, "§b§l§nCrafting");
	    
	    ItemStack bandage = new ItemStack(Material.PAPER, 1);
	    ItemMeta meta = bandage.getItemMeta();
	    meta.setDisplayName(ChatColor.RED + "Gauze");
	    ArrayList<String> lore = new ArrayList();
	    lore.add(ChatColor.RED + "Needs 1 cloth");
	    meta.setLore(lore);
	    bandage.setItemMeta(meta);
	    
	    ItemStack medkit = new ItemStack(Material.BOOK, 1);
	    ItemMeta meta1 = medkit.getItemMeta();
	    meta1.setDisplayName(ChatColor.RED + "MedKit");
	    ArrayList<String> lore1 = new ArrayList();
	    lore1.add(ChatColor.RED + "Needs 1 cloth + 1 Gauze");
	    meta1.setLore(lore1);
	    medkit.setItemMeta(meta1);
	    
	    this.inv.setItem(2, bandage);
	    this.inv.setItem(6, medkit);
	    
	    Bukkit.getServer().getPluginManager().registerEvents(this, p);
	  }
	  
	  public void show(Player p)
	  {
	    p.openInventory(this.inv);
	  }
	  
	  @EventHandler
	  public void onInventoryClick1(InventoryClickEvent e)
	  {
	    if (!e.getInventory().getName().equalsIgnoreCase(this.inv.getName())) {
	      return;
	    }
	    if (e.getCurrentItem().getItemMeta() == null) {
	      return;
	    }
	    if (e.getCurrentItem().getItemMeta().getDisplayName().contains(ChatColor.RED + "Gauze"))
	    {
	      ItemStack cloth = new ItemStack(Material.BOOK, 3);
	      ItemMeta meta2 = cloth.getItemMeta();
	      meta2.setDisplayName(ChatColor.RED + "cloth");
	      cloth.setItemMeta(meta2);
	      if (e.getWhoClicked().getInventory().getItemInMainHand().equals(meta2))
	      {
	        ItemStack bandage = new ItemStack(Material.PAPER, 1);
	        ItemMeta meta = bandage.getItemMeta();
	        meta.setDisplayName(ChatColor.RED + "Gauze");
	        ArrayList<String> lore = new ArrayList();
	        lore.add(ChatColor.RED + "Right click to heal!");
	        meta.setLore(lore);
	        bandage.setItemMeta(meta);
	        e.getWhoClicked().getInventory().removeItem(new ItemStack[] { cloth });
	        e.getWhoClicked().getInventory().addItem(new ItemStack[] { bandage });
	        e.getWhoClicked().closeInventory();
	      }
	      else
	      {
	        e.getWhoClicked().closeInventory();
	        e.getWhoClicked().sendMessage(ChatColor.RED + "You need 3 cloth in order to craft gauze");
	      }
	    }
	    if (!e.getInventory().getName().equalsIgnoreCase(this.inv.getName())) {
	      return;
	    }
	    if (e.getCurrentItem().getItemMeta() == null) {
	      return;
	    }
	    if (e.getCurrentItem().getItemMeta().getDisplayName().contains(ChatColor.RED + "MedKit"))
	    {
	      ItemStack cloth2 = new ItemStack(Material.BOOK, 1);
	      ItemMeta meta3 = cloth2.getItemMeta();
	      meta3.setDisplayName(ChatColor.RED + "cloth");
	      cloth2.setItemMeta(meta3);
	      ItemStack bandage2 = new ItemStack(Material.PAPER, 1);
	      ItemMeta meta4 = bandage2.getItemMeta();
	      meta4.setDisplayName(ChatColor.RED + "Gauze");
	      ArrayList<String> lore = new ArrayList();
	      lore.add(ChatColor.RED + "Right click to heal!");
	      meta4.setLore(lore);
	      bandage2.setItemMeta(meta4);
	      ItemStack medkit = new ItemStack(Material.BOOK, 1);
	      ItemMeta meta1 = medkit.getItemMeta();
	      meta1.setDisplayName(ChatColor.RED + "MedKit");
	      ArrayList<String> lore1 = new ArrayList();
	      lore1.add(ChatColor.RED + "Right click to heal!");
	      meta1.setLore(lore1);
	      medkit.setItemMeta(meta1);
	      if (e.getWhoClicked().getInventory().getItemInMainHand().equals(meta3))
	      {
	        if (e.getWhoClicked().getInventory().getItemInMainHand().equals(meta4))
	        {
	          e.getWhoClicked().getInventory().removeItem(new ItemStack[] { cloth2 });
	          e.getWhoClicked().getInventory().removeItem(new ItemStack[] { bandage2 });
	          e.getWhoClicked().getInventory().addItem(new ItemStack[] { medkit });
	          e.getWhoClicked().closeInventory();
	        }
	      }
	      else
	      {
	        e.getWhoClicked().closeInventory();
	        e.getWhoClicked().sendMessage(ChatColor.RED + "You need 3 Gauze and 5 cloth to craft! in order to craft a bandage!");
	      }
	    }
	  }
}
