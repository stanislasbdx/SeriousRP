package fr.stan1712.wetston.seriousrp;

import fr.stan1712.wetston.seriousrp.events.Death;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin {
	final private static Logger _log = LoggerFactory.getLogger(Main.class);
	final public PluginManager pluginManager = getServer().getPluginManager();
	public static Economy economy = null;
	public static int spigotPluginId = 31443;

	public void versionCheck() {
		final String logStep = getName() + " @ versionCheck";
		final String serverVersion = getServer().getVersion();
		_log.info("[" + logStep + "] Checking server version : " + serverVersion);

		final Pattern versionPattern = Pattern.compile("[0-9][.][0-9]+", Pattern.MULTILINE);
		final Matcher versionMatcher = versionPattern.matcher(serverVersion);
		if(!versionMatcher.find() || (!serverVersion.contains("Spigot") && !serverVersion.contains("Paper"))) {
			_log.error("[" + logStep + "] * Version " + serverVersion + "unknown, disabling plugin.");
			pluginManager.disablePlugin(this);
			return;
		}
		final String version = versionMatcher.group();

		switch (version) {
			case "1.19", "1.18", "1.17", "1.16", "1.15", "1.14", "1.13" -> {
				_log.info("[" + logStep + "] Version check !");
				_log.info("[" + logStep + "] If you got issues, report them on Github");
			}
			case "1.12" -> {
				_log.info("[" + logStep + "] " + version + " may have issues while running !");
				_log.info("[" + logStep + "] If you got any, report them on Github");
			}
			case "1.11" -> {
				_log.info("[" + logStep + "] " + version + " is not supported by SeriousRP.");
				_log.info("[" + logStep + "] If you got any, report them on Github");
			}
			default -> {
				_log.error("[" + logStep + "] * Version is not supported by " + getName() + ", disabling plugin.");
				pluginManager.disablePlugin(this);
			}
		}
	}

	public void dependenciesCheck() {
		final String logStep = getName() + " @ dependenciesCheck";
		_log.info("[" + logStep + "] Dependencies checking...");

		File Tdirect = new File("plugins/Telecom");
		if (!Tdirect.exists()) {
			_log.warn("[" + logStep + "] {Telecom} Telecom dependency was not found ! Disabling RPMobile module.");
			getConfig().set("Core.Modules.RPMobile", Boolean.FALSE);
		}
		else {
			_log.info("[" + logStep + "] {Telecom} Vault hooked !");
		}

		if (!setupEconomy() ) {
			_log.warn("[" + logStep + "] {Vault} Vault dependency was not found ! Disabling Economy modules.");
			getConfig().set("Core.Modules.Economy", Boolean.FALSE);
		}
		else {
			_log.info("[" + logStep + "] {Vault} Vault hooked !");
		}
	}
	private void updateCheck() {
		final String logStep = getName() + " @ updateCheck";
		new UpdateChecker(this, spigotPluginId).getVersion(version -> {
			if(!this.getDescription().getVersion().equalsIgnoreCase(version)) {
				_log.info("[" + logStep + "] An update is available on Spigot ! (" + version + ")");
			}
		});
	}

	private boolean setupEconomy() {
		if (pluginManager.getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		return true;
	}

	private void loadMetrics() {
		final String logStep = getName() + " @ loadMetrics";
		int _bStatsId = 7297;
		new Metrics(this, _bStatsId);
		_log.info("[" + logStep + "] bStats metrics loaded");
	}

	private void loadConfig() {
		pluginManager.registerEvents(new Config(), this);
		saveConfig();
	}
	private void loadRecipes() {
		final String logStep = "[" + getName() + " @ loadRecipes] ";
		pluginManager.registerEvents(new Recipes(this), this);
		_log.info(logStep + "All recipes have been added !");
	}
	private void loadModules() {
		final String logStep = "[" + getName() + " @ loadModules] ";

		if(getConfig().getBoolean("Core.Modules.CustomRecipes")) {
			_log.info(logStep + "CustomRecipes > ON");
			loadRecipes();
		}
		else _log.info(logStep + "CustomRecipes > OFF");

		pluginManager.registerEvents(new Death(this), this);
		if(getConfig().getBoolean("Core.Modules.RPDeath")) _log.info(logStep + "RPDeath > ON");
		else _log.info(logStep + "RPDeath > OFF");

		/*if(getConfig().getBoolean("Core.Modules.Medics")) {
			getCommand("revive").setExecutor(new Revive(this));
			getCommand("hrprevive").setExecutor(new Hrprevive(this));
			getCommand("medinfo").setExecutor(new Medinfo(this));

			_log.info(logStep + "Medics > ON");

			_log.warn(logStep + "If you use the 'Medics' module, please set the difficulty to (at least) EASY");
		}
		else {
			_log.info(logStep + "Medics > OFF");
		}*/

		if(getConfig().getBoolean("Core.Modules.TownSystem")) _log.info(logStep + "TownSystem > ON");
		else _log.info(logStep + "TownSystem > OFF");

		/*if(getConfig().getBoolean("Core.Modules.RPMobiles") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "RPMobiles > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "RPMobiles > OFF");
		}*/
		if(getConfig().getBoolean("Core.Modules.Chairs")) _log.info(logStep + "Chairs > ON");
		else _log.info(logStep + "Chairs > OFF");

		if(getConfig().getBoolean("Core.Modules.Economy")) _log.info(logStep + "Economy > ON");
		else _log.info(logStep + "Economy > OFF");
	}

	private void logNewStep(String step) {
		_log.info("[" + getName() + " @-> " + step + "] ---");
	}

	@Override
	public void onEnable() {
		//(new Thread(this::loadMetrics, getName() + " - Startup")).start();

		logNewStep("updateCheck");
		updateCheck();

		logNewStep("versionCheck");
		versionCheck();

		logNewStep("dependenciesCheck");
		dependenciesCheck();

		logNewStep("loadMetrics");
		loadMetrics();

		logNewStep("loadConfig");
		loadConfig();

		logNewStep("loadModules");
		loadModules();
	}
}
