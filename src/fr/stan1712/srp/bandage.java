package fr.stan1712.srp;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class bandage implements Listener {

	public bandage(srp srp) {
		// TODO Auto-generated constructor stub
	}
	
	public void onInteract(PlayerInteractEvent event){
		Player p = event.getPlayer();
		Action action = event.getAction();
		ItemStack it = event.getItem();
		
		ItemStack bandage = new ItemStack(Material.PAPER, 1);
	    ItemMeta meta = bandage.getItemMeta();
	    meta.setDisplayName("§cBandage");
	    ArrayList<String> lore = new ArrayList<String>();
	    lore.add(ChatColor.RED + "Clique droit pour se soigner !");
	    meta.setLore(lore);
	    bandage.setItemMeta(meta);
	    bandage.setItemMeta(meta);
	    
		if(it == null) return;
		
		if(it.getType() == Material.PAPER && it.hasItemMeta() && it.getItemMeta().hasDisplayName() && it.getItemMeta().getDisplayName().equalsIgnoreCase("§cBandage")){
			if(action == Action.RIGHT_CLICK_AIR){
				p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 5, 100));
				p.sendMessage("Vous êtes soignés !");
			}
		}
	}
}
