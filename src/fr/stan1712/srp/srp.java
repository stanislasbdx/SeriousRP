package fr.stan1712.srp;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import fr.stan1712.srp.commands;

public class srp extends JavaPlugin implements Listener{

public static srp instance;
public static srp getInstance(){
		return instance;
	}

public void onEnable(){
		instance = this;
		
		System.out.println("[SeriousRP] #------------#");
		System.out.println("[SeriousRP] SeriousRP "+getConfig().getString("Version").replace("&", "§"));
		System.out.println("[SeriousRP] Boot sequence launched");
		if(getConfig().getBoolean("RPDeath") == true) {
			System.out.println("[SeriousRP] RealDeath module activated");
		}
		if(getConfig().getBoolean("Medics") == true) {
			System.out.println("[SeriousRP] Medics module activated");
		}
		if(getConfig().getBoolean("TownSystem") == true) {
			System.out.println("[SeriousRP] Town module activated");
		}
		System.out.println("[SeriousRP] Classes loaded !");
		System.out.println("[SeriousRP] #------------#");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new actions(), this);
		pm.registerEvents(new commands(this), this);
		pm.registerEvents(new deaths(this), this);
		pm.registerEvents(new falldamage(), this);
		pm.registerEvents(new spawnitems(), this);
		pm.registerEvents(new phone(this), this);
		
		getConfig().options().copyDefaults(false);
	    saveDefaultConfig();
		
		ShapedRecipe saddle =  new ShapedRecipe(new ItemStack(Material.SADDLE, 1));
		saddle.shape(new String[] {" C ","CFC","I I"});
		saddle.setIngredient('C', Material.LEATHER);
		saddle.setIngredient('I', Material.STRING);
		saddle.setIngredient('F', Material.IRON_INGOT);
		getServer().addRecipe(saddle);
		
		ShapedRecipe nametag = new ShapedRecipe(new ItemStack(Material.NAME_TAG));
		nametag.shape(new String[] {"  I"," B ","P  "});
		nametag.setIngredient('P', Material.PAPER);
		nametag.setIngredient('I', Material.STRING);
		nametag.setIngredient('B', Material.BOOK_AND_QUILL);
		getServer().addRecipe(nametag);
		
		ShapedRecipe horseiron = new ShapedRecipe(new ItemStack(Material.IRON_BARDING));
		horseiron.shape(new String[] {"   ","IBI","II "});
		horseiron.setIngredient('I', Material.IRON_INGOT);
		horseiron.setIngredient('B', Material.IRON_BLOCK);
		getServer().addRecipe(horseiron);
		
		ShapedRecipe horsegold = new ShapedRecipe(new ItemStack(Material.GOLD_BARDING));
		horsegold.shape(new String[] {"   ","IBI","II "});
		horsegold.setIngredient('I', Material.GOLD_INGOT);
		horsegold.setIngredient('B', Material.GOLD_BLOCK);
		getServer().addRecipe(horsegold);
		
		ShapedRecipe horsediamond = new ShapedRecipe(new ItemStack(Material.DIAMOND_BARDING));
		horsediamond.shape(new String[] {"   ","IBI","II "});
		horsediamond.setIngredient('I', Material.DIAMOND);
		horsediamond.setIngredient('B', Material.DIAMOND_BLOCK);
		getServer().addRecipe(horsediamond);
		
		ShapedRecipe steak = new ShapedRecipe(new ItemStack(Material.RAW_BEEF));
		steak.shape(new String[] {"RRR","RBR","RRR"});
		steak.setIngredient('B', Material.BOWL);
		steak.setIngredient('R', Material.ROTTEN_FLESH);
		getServer().addRecipe(steak);
	}
  
public void onDisable(){
		System.out.println("[SeriousRP] #------------#");
		System.out.println("[SeriousRP] SeriousRP "+getConfig().getString("Version").replace("&", "§"));
		System.out.println("[SeriousRP] Shutdown sequence launched");
		System.out.println("[SeriousRP] Classes disabled");
		System.out.println("[SeriousRP] #------------#");
	}
}
