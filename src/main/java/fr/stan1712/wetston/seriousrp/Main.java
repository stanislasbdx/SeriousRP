package fr.stan1712.wetston.seriousrp;

import fr.stan1712.wetston.seriousrp.commands.Cheques;
import fr.stan1712.wetston.seriousrp.events.Cheque;
import fr.stan1712.wetston.seriousrp.events.Death;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin {
	private static final Logger _log = LoggerFactory.getLogger("SeriousRP - Core");
	public final PluginManager pluginManager = getServer().getPluginManager();
	public static Economy economy = null;

	public static final int SPIGOT_PLUGIN_ID = 31443;

	public boolean versionCheck() {
		final String logStep = "versionCheck";
		final String serverVersion = getServer().getVersion();
		final String serverType = getServer().getName();

		_log.info("[{}] Checking server version : {} {}", logStep, serverType, serverVersion);

		final Pattern versionPattern = Pattern.compile("\\d[.]\\d+", Pattern.MULTILINE);
		final Matcher versionMatcher = versionPattern.matcher(serverVersion);
		if(!versionMatcher.find() || (!serverType.contains("Spigot") && !serverType.contains("Paper"))) {
			_log.error("[{}] * Server type {} unknown, disabling plugin.", logStep, serverVersion);

			pluginManager.disablePlugin(this);
			return false;
		}
		final String version = versionMatcher.group();

		switch (version) {
			case "1.21", "1.20", "1.19", "1.18", "1.17", "1.16", "1.15", "1.14", "1.13" -> {
				_log.info("[{}] Version check !", logStep);
				_log.info("[{}] If you got issues, report them on Github", logStep);
			}
			case "1.12" -> {
				_log.info("[{}] {} may have issues while running !", logStep, version);
				_log.info("[{}] If you got any, report them on Github", logStep);
			}
			default -> {
				_log.error("[{}] * Version {} is not supported by {}, disabling plugin.", logStep, serverVersion, getName());

				pluginManager.disablePlugin(this);
				return false;
			}
		}

		return true;
	}

	public void dependenciesCheck() {
		final String logStep = "dependenciesCheck";
		_log.info("[{}] Dependencies checking...", logStep);

		File telecomFolder = new File("plugins/Telecom");
		if (!telecomFolder.exists()) {
			_log.warn("[{}] {Telecom} Telecom dependency was not found ! Disabling RPMobile module.", logStep);
			getConfig().set("Core.Modules.RPMobile", Boolean.FALSE);
		}
		else {
			_log.info("[{}] {Telecom} Vault hooked !", logStep);
		}

		if (!setupEconomy() ) {
			_log.warn("[{}] {Vault} Vault dependency was not found ! Disabling Economy modules.", logStep);
			getConfig().set("Core.Modules.Economy", Boolean.FALSE);
		}
		else {
			_log.info("[{}] {Vault} Vault hooked !", logStep);
		}
	}
	private void updateCheck() {
		final String logStep = "updateCheck";
		new UpdateChecker(this, SPIGOT_PLUGIN_ID).getVersion(version -> {
			if(!this.getDescription().getVersion().equalsIgnoreCase(version)) {
				_log.info("[{}] An update is available on Spigot ! ({})", logStep, version);
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
		final String logStep = "loadMetrics";
		final int bStatsId = 7297;

		new Metrics(this, bStatsId);

		_log.info("{} bStats metrics loaded", logStep);
	}

	private void loadConfig() {
		pluginManager.registerEvents(new Config(), this);
		saveConfig();
	}
	private void loadRecipes() {
		final String logStep = "[loadRecipes] ";
		pluginManager.registerEvents(new Recipes(this), this);
		_log.info("{} All recipes have been added !", logStep);
	}
	private void loadModules() {
		final String logStep = "[loadModules] ";

		if(getConfig().getBoolean("Core.Modules.CustomRecipes")) {
			_log.info("{} CustomRecipes > ON", logStep);
			loadRecipes();
		}
		else _log.info("{} CustomRecipes > OFF", logStep);

		pluginManager.registerEvents(new Death(this), this);
		if(getConfig().getBoolean("Core.Modules.RPDeath")) _log.info("{} RPDeath > ON", logStep);
		else _log.info("{} RPDeath > OFF", logStep);

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

		/*if(getConfig().getBoolean("Core.Modules.TownSystem")) _log.info("{} TownSystem > ON", logStep);
		else _log.info("{} TownSystem > OFF", logStep);*/

		/*if(getConfig().getBoolean("Core.Modules.RPMobiles") == true) {
			console.sendMessage("[SeriousRP] " + ChatColor.GREEN + "RPMobiles > ON");
		}
		else {
			console.sendMessage("[SeriousRP] " + ChatColor.RED + "RPMobiles > OFF");
		}*/
		if(getConfig().getBoolean("Core.Modules.Chairs")) _log.info("{} Chairs > ON", logStep);
		else _log.info("{} Chairs > OFF", logStep);

		if(getConfig().getBoolean("Core.Modules.Economy")) {
			_log.info("{} Economy > ON", logStep);
			pluginManager.registerEvents(new Cheque(this), this);
		}
		else _log.info("{} Economy > OFF", logStep);
	}
	private void loadCommands() {
		final String logStep = "[loadCommands] ";

		Objects.requireNonNull(getCommand("cheque")).setExecutor(new Cheques(this));
		_log.info("{} /cheque commands loaded", logStep);

		_log.info("{} All commands have been added !", logStep);
	}

	private void logNewStep(String step) {
		_log.info("{@ {}} ---", step);
	}

	@Override
	public void onEnable() {
		logNewStep("updateCheck");
		updateCheck();

		logNewStep("versionCheck");
		final boolean versionCorrect = versionCheck();

		if(versionCorrect) {
			logNewStep("dependenciesCheck");
			dependenciesCheck();

			logNewStep("loadMetrics");
			loadMetrics();

			logNewStep("loadConfig");
			loadConfig();

			logNewStep("loadModules");
			loadModules();

			logNewStep("loadCommands");
			loadCommands();
		}
	}
}
