package fr.stan1712.srp;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener{

public static Main instance;
public static Main getInstance(){
		return instance;
	}

FileConfiguration config = getConfig();
String version = Bukkit.getServer().getVersion();

public boolean isLatest(){
	if(Bukkit.getServer().getVersion().contains("1.12")){
		System.out.println("[SeriousRP] Server version : 1.12");
		System.out.println("[SeriousRP] Version check !");
	}
	else if(version.contains("1.11")){
		System.out.println("[SeriousRP] Server version : " + version);
		System.out.println("[SeriousRP] Version check !");
	}
	else{
		System.out.println("[SeriousRP] ** WARNING **");
		System.out.println("[SeriousRP] SeriousRP is not supported on version inferior to 1.11.X");
		System.out.println("[SeriousRP] be careful, no support will be given !");
		System.out.println("[SeriousRP] ** ** * ** **");
	}
	return false;
}

public void depencies(){
	File Tdirect = new File("plugins/Telecom");
	File Vdirect = new File("plugins/Vault");

	if(!Tdirect.exists()){
		System.out.println("[SeriousRP] {Telecom} ** WARNING **");
		System.out.println("[SeriousRP] {Telecom} Telecom was not found !");
		System.out.println("[SeriousRP] {Telecom} Deactivation of the module RPMobile.");
		System.out.println("[SeriousRP] {Telecom} If you want to use SRP mobile, you will need Telecom");
		System.out.println("[SeriousRP] {Telecom} ** ** **");

		getConfig().set("RPMobile", false);
	}
	else{
		System.out.println("[SeriousRP] {Telecom} Telecom found !");
	}

	if (!Vdirect.exists()) {
		System.out.println("[SeriousRP] {Vault} Vault was not found");
	}
	else {
		System.out.println("[SeriousRP] {Vault} Vault found !");
	}
}

public void vaultini(){
	System.out.println("[SeriousRP] Soon...");
}

public void onEnable(){
		instance = this;
		
		System.out.println("[SeriousRP] #------ ------#");
		System.out.println("[SeriousRP] SeriousRP " + getConfig().getString("Version"));
		System.out.println("[SeriousRP] Boot sequence launched");

		System.out.println("[SeriousRP] ");
		System.out.println("[SeriousRP] #- Module manager, loading modules -#");
		if(getConfig().getBoolean("RPDeath") == true) {

			System.out.println("[SeriousRP] Roleplay Deaths module activated");
		}
		if(getConfig().getBoolean("Medics") == true) {
			System.out.println("[SeriousRP] Medics System module activated");
		}
		if(getConfig().getBoolean("TownSystem") == true) {
			System.out.println("[SeriousRP] Town System module activated");
		}
		if(getConfig().getBoolean("CustomRecipes") == true) {
			System.out.println("[SeriousRP] Custom Recipes module activated");
		}
		if(getConfig().getBoolean("RPMobile") == true) {
			System.out.println("[SeriousRP] Roleplay Mobile module activated");
		}
		if (getConfig().getBoolean("Chairs") == true) {
			System.out.println("[SeriousRP] Chairs module activated");
		}
		

		File direct = new File("plugins/SeriousRP");
		if (!direct.exists()) {
			direct.mkdir();
			System.out.println("[SeriousRP] 'SeriousRP' directory created !");
		}
		File config = new File("plugins/SeriousRP", "config.yml");
		if (!config.exists()) {
			try {
				config.createNewFile();
				getConfig().set("ConfigFix", false);

				System.out.println("[SeriousRP] 'config.yml' created !");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("[SeriousRP] ");
		System.out.println("[SeriousRP] #- Loading commands -#");
		this.getCommand("/seriousrp").setExecutor(new Cseriousrp(this));
		this.getCommand("/srtp").setExecutor(new Csrtp(this));
		
		System.out.println("[SeriousRP] ");
		System.out.println("[SeriousRP] #- Class manager, loading classes -#");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new actions(), this);
		//pm.registerEvents(new commands(this), this);
		pm.registerEvents(new deaths(this), this);
		pm.registerEvents(new falldamage(), this);
		pm.registerEvents(new spawnitems(), this);
		pm.registerEvents(new phone(this), this);
		pm.registerEvents(new configupdate(this), this);

		System.out.println("[SeriousRP] All classes have been loaded");

		System.out.println("[SeriousRP] ");
		System.out.println("[SeriousRP] #- Depencies manager -#");
		depencies();

		System.out.println("[SeriousRP] ");
		System.out.println("[SeriousRP] #- Version check -#");
		isLatest();

		System.out.println("[SeriousRP] ");
		System.out.println("[SeriousRP] #- Vault API check -#");
		vaultini();

		System.out.println("[SeriousRP] ");
		System.out.println("[SeriousRP] #- Config writer, writing and creating configs files -#");
	    saveConfig();
		System.out.println("[SeriousRP] Config file up and runnning !");

		System.out.println("[SeriousRP] #------ ------#");
		
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

public void onDisable(){
		PluginManager pm = getServer().getPluginManager();

		System.out.println("[SeriousRP] #------ ------#");
		System.out.println("[SeriousRP] SeriousRP " + getConfig().getString("Version"));
		System.out.println("[SeriousRP] Shutdown sequence launched");

		System.out.println("[SeriousRP] ");
		System.out.println("[SeriousRP] #- Module manager, disabling modules -#");
		if(getConfig().getBoolean("RPDeath") == true) {
			System.out.println("[SeriousRP] Roleplay Deaths module deactivated");
		}
		if(getConfig().getBoolean("Medics") == true) {
			System.out.println("[SeriousRP] Medics System module deactivated");
		}
		if(getConfig().getBoolean("TownSystem") == true) {
			System.out.println("[SeriousRP] Town System deactivated");
		}
		if(getConfig().getBoolean("CustomRecipes") == true) {
			System.out.println("[SeriousRP] Custom recipes module deactivated");
		}
		if(getConfig().getBoolean("RPMobile") == true) {
			System.out.println("[SeriousRP] Roleplay Mobile module deactivated");
		}
		if (getConfig().getBoolean("Chairs") == true) {
			System.out.println("[SeriousRP] Chairs module deactivated");
		}

		System.out.println("[SeriousRP] All classes have been disabled");
		System.out.println("[SeriousRP] #------ ------#");

		pm.disablePlugins();
	}
}
