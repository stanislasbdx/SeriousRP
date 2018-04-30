package fr.stan1712.srp;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import fr.stan1712.srp.commands;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;


public class srp extends JavaPlugin implements Listener{

public static srp instance;
public static srp getInstance(){
		return instance;
	}

FileConfiguration config = getConfig();

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
		if(getConfig().getBoolean("CustomRecipes") == true) {
			System.out.println("[SeriousRP] Custom recipes module activated");
		}
		if(getConfig().getBoolean("RPMobile") == true) {
			System.out.println("[SeriousRP] Roleplay Mobile module activated");
		}
		System.out.println("[SeriousRP] All classes are loaded !");
		System.out.println("[SeriousRP] Config file up and runnning !");
		System.out.println("[SeriousRP] #------------#");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new actions(), this);
		pm.registerEvents(new commands(this), this);
		pm.registerEvents(new deaths(this), this);
		pm.registerEvents(new falldamage(), this);
		pm.registerEvents(new spawnitems(), this);
		pm.registerEvents(new phone(this), this);
		pm.registerEvents(new configupdate(this), this);
		
		  File msg = new File("plugins/SeriousRP", "messages.yml");
	      if (!msg.exists()) {
	          try {
	        	  msg.createNewFile();
	          } catch (IOException e) {
	              e.printStackTrace();
	          }
	      }
		  File config = new File("plugins/SeriousRP", "config.yml");
	      if (!config.exists()) {
	          try {
	        	  config.createNewFile();
	          } catch (IOException e) {
	              e.printStackTrace();
	          }
	      }
	      
	    saveConfig();
		
	    if(getConfig().getBoolean("CustomRecipes") == true) {
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
			
			ShapedRecipe gunpowder = new ShapedRecipe(new ItemStack(Material.SULPHUR));
			gunpowder.shape(new String[] {"CRS","   ","   "});
			gunpowder.setIngredient('C', Material.COAL);
			gunpowder.setIngredient('R', Material.REDSTONE);
			gunpowder.setIngredient('S', Material.SUGAR);
			getServer().addRecipe(gunpowder);
	    }
	}

public class ExamplePlugin extends JavaPlugin {
    
    private final Logger log = Logger.getLogger("Minecraft");
    private Economy econ = null;
    private Permission perms = null;
    private Chat chat = null;

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(!(sender instanceof Player)) {
            log.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if(command.getLabel().equals("test-economy")) {
            // Lets give the player 1.05 currency (note that SOME economic plugins require rounding!)
            sender.sendMessage(String.format("You have %s", econ.format(econ.getBalance(player.getName()))));
            EconomyResponse r = econ.depositPlayer(player, 1.05);
            if(r.transactionSuccess()) {
                sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
            } else {
                sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
            return true;
        } else if(command.getLabel().equals("test-permission")) {
            // Lets test if user has the node "example.plugin.awesome" to determine if they are awesome or just suck
            if(perms.has(player, "example.plugin.awesome")) {
                sender.sendMessage("You are awesome!");
            } else {
                sender.sendMessage("You suck!");
            }
            return true;
        } else {
            return false;
        }
    }
    
    public Economy getEconomy() {
        return econ;
    }
    
    public Permission getPermissions() {
        return perms;
    }
    
    public Chat getChat() {
        return chat;
    }
}
  
public void onDisable(){
		System.out.println("[SeriousRP] #------------#");
		System.out.println("[SeriousRP] SeriousRP "+getConfig().getString("Version").replace("&", "§"));
		System.out.println("[SeriousRP] Shutdown sequence launched");
		if(getConfig().getBoolean("RPDeath") == true) {
			System.out.println("[SeriousRP] RealDeath module deactivated");
		}
		if(getConfig().getBoolean("Medics") == true) {
			System.out.println("[SeriousRP] Medics module deactivated");
		}
		if(getConfig().getBoolean("TownSystem") == true) {
			System.out.println("[SeriousRP] Town module deactivated");
		}
		if(getConfig().getBoolean("CustomRecipes") == true) {
			System.out.println("[SeriousRP] Custom recipes module deactivated");
		}
		if(getConfig().getBoolean("RPMobile") == true) {
			System.out.println("[SeriousRP] Roleplay Mobile module deactivated");
		}
		System.out.println("[SeriousRP] Classes disabled");
		System.out.println("[SeriousRP] #------------#");
	}
}
