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


	public void onEnable()
	  {
	    instance = this;
	    
	    System.out.println("[SeriousRP] #------------#");
	    System.out.println("[SeriousRP] SeriousRP v3.7");
	    System.out.println("[SeriousRP] Activation plugin");
	    System.out.println("[SeriousRP] Par stan1712");
	    System.out.println("[SeriousRP] #------------#");
	    
	    PluginManager pm = getServer().getPluginManager();
	    pm.registerEvents(new actions(), this);
	    pm.registerEvents(new commands(this), this);
	    pm.registerEvents(new deaths(), this);
	    pm.registerEvents(new falldamage(), this);
	    //pm.registerEvents(new telephone(this), this);
	    
	    //getCommand("broadcast").setExecutor(new broadcast(this));
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
	    
	  }
	  
	  public void onDisable()
	  {
	    System.out.println("[SeriousRP] #------------#");
	    System.out.println("[SeriousRP] SeriousRP v3.7");
	    System.out.println("[SeriousRP] Desactivation plugin");
	    System.out.println("[SeriousRP] Par stan1712");
	    System.out.println("[SeriousRP] #------------#");
	  }
	}
