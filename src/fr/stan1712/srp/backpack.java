package fr.stan1712.srp;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class backpack implements Listener {
	public srp plugin;
	public backpack(srp m){
		this.plugin = m;
	}

	@SuppressWarnings("unused")
	private FileConfiguration config;
	private srp pl;
	
	public void backpack1(srp pl){
		this.pl = pl;
		this.config = pl.getConfig();
	}
	
	public backpack() {
	}

	@EventHandler
	public void interact(PlayerInteractEvent event){
		Player player= event.getPlayer();
		UUID id = player.getUniqueId();
		
		ItemStack is = event.getItem();
		if(is!= null){
			if(event.getAction()== Action.RIGHT_CLICK_AIR){
				if(is.getType()== Material.APPLE){
					if(plugin.inventories.containsKey(id)){
						player.openInventory(plugin.inventories.get(id));
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler
	public void join(PlayerJoinEvent event){
		Player player = event.getPlayer();
		UUID id = player.getUniqueId();
		
		if(!plugin.inventories.containsKey(id)){
			if(this.pl.getConfig().get(id.toString())!= null){
				Object i = this.pl.getConfig().getList(id.toString() + ".items");
				ItemStack[] contents = null;
			if(i instanceof ItemStack[]){
				contents = (ItemStack[]) i;
			}else if(i instanceof List){
				contents = (ItemStack[]) ((List<ItemStack>) i).toArray(new ItemStack[0]);
			}
			Inventory inv = Bukkit.createInventory(player, 27, this.pl.getConfig().getString("BackPack Name").replace("&", "§"));
			inv.setContents(contents);
			plugin.inventories.put(id, inv);
			
			}else{
				plugin.inventories.put(id, Bukkit.createInventory(player, 27, this.pl.getConfig().getString("BackPack Name").replace("&", "§")));
			}
		}
		
	}
	
	@EventHandler
	public void leave(PlayerQuitEvent event){
		Player player = event.getPlayer();
		UUID id = player.getUniqueId();
		
		ItemStack[] contents = player.getInventory().getContents();
		this.pl.getConfig().set(id.toString() + ".items", contents);
		this.pl.saveConfig();
	}
}
