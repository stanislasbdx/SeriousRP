package fr.stan1712.seriousrp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class Recipes implements Listener {
    private final Main pl;
  
	public Recipes(Main pl) {
        this.pl = pl;
        pl.getConfig();
        
        Bukkit.resetRecipes();
        
        NamespacedKey saddle_key = new NamespacedKey(this.pl, "srp_saddle");
		ShapedRecipe saddle = new ShapedRecipe(saddle_key, new ItemStack(Material.SADDLE, 1));
		saddle.shape(" C ","CFC","I I");
		saddle.setIngredient('C', Material.LEATHER);
		saddle.setIngredient('I', Material.STRING);
		saddle.setIngredient('F', Material.IRON_INGOT);
		Bukkit.addRecipe(saddle);
		
		NamespacedKey nametag_key = new NamespacedKey(this.pl, "srp_nametag");
		ShapedRecipe nametag = new ShapedRecipe(nametag_key, new ItemStack(Material.NAME_TAG));
		nametag.shape("  I"," B ","P  ");
		nametag.setIngredient('P', Material.PAPER);
		nametag.setIngredient('I', Material.STRING);
		nametag.setIngredient('B', Material.BOOK_AND_QUILL);
		this.pl.getServer().addRecipe(nametag);
		
		NamespacedKey horseiron_key = new NamespacedKey(this.pl, "srp_horseiron");
		ShapedRecipe horseiron = new ShapedRecipe(horseiron_key, new ItemStack(Material.IRON_BARDING));
		horseiron.shape("   ","IBI","II ");
		horseiron.setIngredient('I', Material.IRON_INGOT);
		horseiron.setIngredient('B', Material.IRON_BLOCK);
		this.pl.getServer().addRecipe(horseiron);
		
		NamespacedKey horsegold_key = new NamespacedKey(this.pl, "srp_horsegold");
		ShapedRecipe horsegold = new ShapedRecipe(horsegold_key, new ItemStack(Material.GOLD_BARDING));
		horsegold.shape("   ","IBI","II ");
		horsegold.setIngredient('I', Material.GOLD_INGOT);
		horsegold.setIngredient('B', Material.GOLD_BLOCK);
		this.pl.getServer().addRecipe(horsegold);
		
		NamespacedKey horsediamond_key = new NamespacedKey(this.pl, "srp_horsediamond");
		ShapedRecipe horsediamond = new ShapedRecipe(horsediamond_key, new ItemStack(Material.DIAMOND_BARDING));
		horsediamond.shape("   ","IBI","II ");
		horsediamond.setIngredient('I', Material.DIAMOND);
		horsediamond.setIngredient('B', Material.DIAMOND_BLOCK);
		this.pl.getServer().addRecipe(horsediamond);

		NamespacedKey steak_key = new NamespacedKey(this.pl, "srp_steak");
		ShapedRecipe steak = new ShapedRecipe(steak_key, new ItemStack(Material.RAW_BEEF));
		steak.shape("RRR","RBR","RRR");
		steak.setIngredient('B', Material.BOWL);
		steak.setIngredient('R', Material.ROTTEN_FLESH);
		this.pl.getServer().addRecipe(steak);
		
		NamespacedKey gunpowder_key = new NamespacedKey(this.pl, "srp_gunpowder");
		ShapedRecipe gunpowder = new ShapedRecipe(gunpowder_key, new ItemStack(Material.SULPHUR));
		gunpowder.shape("CRS","   ","   ");
		gunpowder.setIngredient('C', Material.COAL);
		gunpowder.setIngredient('R', Material.REDSTONE);
		gunpowder.setIngredient('S', Material.SUGAR);
		this.pl.getServer().addRecipe(gunpowder);
		
		NamespacedKey leather_key = new NamespacedKey(this.pl, "srp_leather");
		ShapedRecipe leather = new ShapedRecipe(leather_key, new ItemStack(Material.LEATHER));
		leather.shape("   "," RR"," RR");
		leather.setIngredient('R', Material.ROTTEN_FLESH);
		this.pl.getServer().addRecipe(leather);
		
		NamespacedKey packedice_key = new NamespacedKey(this.pl, "srp_packedice");
		ShapedRecipe packedice = new ShapedRecipe(packedice_key, new ItemStack(Material.PACKED_ICE));
		packedice.shape("   "," II"," II");
		packedice.setIngredient('I', Material.ICE);
		this.pl.getServer().addRecipe(packedice);
	}

}
