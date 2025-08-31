package fr.stan1712.wetston.seriousrp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class Recipes implements Listener {

	public Recipes(Main pl) {

		NamespacedKey saddleKey = new NamespacedKey(pl, "srp_saddle");
		ShapedRecipe saddle = new ShapedRecipe(saddleKey, new ItemStack(Material.SADDLE, 1));
		saddle.shape(" C ","CFC","I I");
		saddle.setIngredient('C', Material.LEATHER);
		saddle.setIngredient('I', Material.STRING);
		saddle.setIngredient('F', Material.IRON_INGOT);
		Bukkit.addRecipe(saddle);

		NamespacedKey nametagKey = new NamespacedKey(pl, "srp_nametag");
		ShapedRecipe nametag = new ShapedRecipe(nametagKey, new ItemStack(Material.NAME_TAG));
		nametag.shape("  I"," B ","P  ");
		nametag.setIngredient('P', Material.PAPER);
		nametag.setIngredient('I', Material.STRING);
		nametag.setIngredient('B', Material.WRITABLE_BOOK);
		pl.getServer().addRecipe(nametag);

		NamespacedKey horseironKey = new NamespacedKey(pl, "srp_iron_horse_armor");
		ShapedRecipe horseiron = new ShapedRecipe(horseironKey, new ItemStack(Material.IRON_HORSE_ARMOR));
		horseiron.shape("   ","IBI","II ");
		horseiron.setIngredient('I', Material.IRON_INGOT);
		horseiron.setIngredient('B', Material.IRON_BLOCK);
		pl.getServer().addRecipe(horseiron);

		NamespacedKey horsegoldKey = new NamespacedKey(pl, "srp_gold_horse_armor");
		ShapedRecipe horsegold = new ShapedRecipe(horsegoldKey, new ItemStack(Material.GOLDEN_HORSE_ARMOR));
		horsegold.shape("   ","IBI","II ");
		horsegold.setIngredient('I', Material.GOLD_INGOT);
		horsegold.setIngredient('B', Material.GOLD_BLOCK);
		pl.getServer().addRecipe(horsegold);

		NamespacedKey horsediamondKey = new NamespacedKey(pl, "srp_diamond_horse_armor");
		ShapedRecipe horsediamond = new ShapedRecipe(horsediamondKey, new ItemStack(Material.DIAMOND_HORSE_ARMOR));
		horsediamond.shape("   ","IBI","II ");
		horsediamond.setIngredient('I', Material.DIAMOND);
		horsediamond.setIngredient('B', Material.DIAMOND_BLOCK);
		pl.getServer().addRecipe(horsediamond);

		NamespacedKey steakKey = new NamespacedKey(pl, "srp_steak");
		ShapedRecipe steak = new ShapedRecipe(steakKey, new ItemStack(Material.COOKED_BEEF));
		steak.shape("RRR","RBR","RRR");
		steak.setIngredient('B', Material.BOWL);
		steak.setIngredient('R', Material.ROTTEN_FLESH);
		pl.getServer().addRecipe(steak);

		NamespacedKey gunpowderKey = new NamespacedKey(pl, "srp_gunpowder");
		ShapedRecipe gunpowder = new ShapedRecipe(gunpowderKey, new ItemStack(Material.GUNPOWDER));
		gunpowder.shape("CRS","   ","   ");
		gunpowder.setIngredient('C', Material.COAL);
		gunpowder.setIngredient('R', Material.REDSTONE);
		gunpowder.setIngredient('S', Material.SUGAR);
		pl.getServer().addRecipe(gunpowder);

		NamespacedKey leatherKey = new NamespacedKey(pl, "srp_leather");
		ShapedRecipe leather = new ShapedRecipe(leatherKey, new ItemStack(Material.LEATHER));
		leather.shape("   "," RR"," RR");
		leather.setIngredient('R', Material.ROTTEN_FLESH);
		pl.getServer().addRecipe(leather);

		NamespacedKey packediceKey = new NamespacedKey(pl, "srp_packedice");
		ShapedRecipe packedice = new ShapedRecipe(packediceKey, new ItemStack(Material.PACKED_ICE));
		packedice.shape("   "," II"," II");
		packedice.setIngredient('I', Material.ICE);
		pl.getServer().addRecipe(packedice);
	}

}
