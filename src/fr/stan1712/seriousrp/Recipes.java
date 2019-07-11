package fr.stan1712.seriousrp;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class Recipes implements Listener {
    private Main pl;
  
    public Recipes(Main pl) {
        this.pl = pl;
        pl.getConfig();
        
		ShapedRecipe saddle =  new ShapedRecipe(new ItemStack(Material.SADDLE, 1));
		saddle.shape(new String[] {" C ","CFC","I I"});
		saddle.setIngredient('C', Material.LEATHER);
		saddle.setIngredient('I', Material.STRING);
		saddle.setIngredient('F', Material.IRON_INGOT);
		this.pl.getServer().addRecipe(saddle);
		
		ShapedRecipe nametag = new ShapedRecipe(new ItemStack(Material.NAME_TAG));
		nametag.shape(new String[] {"  I"," B ","P  "});
		nametag.setIngredient('P', Material.PAPER);
		nametag.setIngredient('I', Material.STRING);
		nametag.setIngredient('B', Material.BOOK_AND_QUILL);
		this.pl.getServer().addRecipe(nametag);
		
		ShapedRecipe horseiron = new ShapedRecipe(new ItemStack(Material.IRON_BARDING));
		horseiron.shape(new String[] {"   ","IBI","II "});
		horseiron.setIngredient('I', Material.IRON_INGOT);
		horseiron.setIngredient('B', Material.IRON_BLOCK);
		this.pl.getServer().addRecipe(horseiron);
		
		ShapedRecipe horsegold = new ShapedRecipe(new ItemStack(Material.GOLD_BARDING));
		horsegold.shape(new String[] {"   ","IBI","II "});
		horsegold.setIngredient('I', Material.GOLD_INGOT);
		horsegold.setIngredient('B', Material.GOLD_BLOCK);
		this.pl.getServer().addRecipe(horsegold);
		
		ShapedRecipe horsediamond = new ShapedRecipe(new ItemStack(Material.DIAMOND_BARDING));
		horsediamond.shape(new String[] {"   ","IBI","II "});
		horsediamond.setIngredient('I', Material.DIAMOND);
		horsediamond.setIngredient('B', Material.DIAMOND_BLOCK);
		this.pl.getServer().addRecipe(horsediamond);
		
		ShapedRecipe steak = new ShapedRecipe(new ItemStack(Material.RAW_BEEF));
		steak.shape(new String[] {"RRR","RBR","RRR"});
		steak.setIngredient('B', Material.BOWL);
		steak.setIngredient('R', Material.ROTTEN_FLESH);
		this.pl.getServer().addRecipe(steak);
		
		ShapedRecipe gunpowder = new ShapedRecipe(new ItemStack(Material.SULPHUR));
		gunpowder.shape(new String[] {"CRS","   ","   "});
		gunpowder.setIngredient('C', Material.COAL);
		gunpowder.setIngredient('R', Material.REDSTONE);
		gunpowder.setIngredient('S', Material.SUGAR);
		this.pl.getServer().addRecipe(gunpowder);
	}

}
