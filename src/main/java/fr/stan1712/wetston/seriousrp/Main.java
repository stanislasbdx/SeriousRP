package fr.stan1712.wetston.seriousrp;

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
	private final int _pluginId = 7297;
	final public PluginManager pluginManager = getServer().getPluginManager();
	public static Economy economy = null;

	public void versionCheck() {
		final String serverVersion = getServer().getVersion();
		_log.info("[" + getName() + "] Checking server version : " + serverVersion);

		final Pattern versionPattern = Pattern.compile("[0-9][.][0-9]+", Pattern.MULTILINE);
		final Matcher versionMatcher = versionPattern.matcher(serverVersion);
		if(!versionMatcher.find() || (!serverVersion.contains("Spigot") && !serverVersion.contains("Paper"))) {
			_log.error("[" + getName() + "] * Version " + serverVersion + "unknown, disabling plugin.");
			pluginManager.disablePlugin(this);
			return;
		}
		final String version = versionMatcher.group();

		switch (version) {
			case "1.19", "1.18", "1.17", "1.16", "1.15", "1.14", "1.13" -> {
				_log.info("[" + getName() + "] Version check !");
				_log.info("[" + getName() + "] If you got issues, report them on Github");
			}
			case "1.12" -> {
				_log.info("[" + getName() + "] " + version + " may have issues while running !");
				_log.info("[" + getName() + "] If you got any, report them on Github");
			}
			case "1.11" -> {
				_log.info("[" + getName() + "] " + version + " is not supported by SeriousRP.");
				_log.info("[" + getName() + "] If you got any, report them on Github");
			}
			default -> {
				_log.error("[" + getName() + "] * Version is not supported by " + getName() + ", disabling plugin.");
				pluginManager.disablePlugin(this);
			}
		}
	}

	public void dependencyCheck() {
		_log.info("[" + getName() + "] Dependencies checking...");

		File Tdirect = new File("plugins/Telecom");
		if (!Tdirect.exists()) {
			_log.warn("[" + getName() + "] {Telecom} Telecom dependency was not found ! Disabling RPMobile module.");
			getConfig().set("Core.Modules.RPMobile", Boolean.FALSE);
		}
		else {
			_log.info("[" + getName() + "] {Telecom} Vault hooked !");
		}

		if (!setupEconomy() ) {
			_log.warn("[" + getName() + "] {Vault} Vault dependency was not found ! Disabling Economy modules.");
			getConfig().set("Core.Modules.Economy", Boolean.FALSE);
		}
		else {
			_log.info("[" + getName() + "] {Vault} Vault hooked !");
		}
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
		return economy != null;
	}

	@Override
	public void onEnable() {
		dependencyCheck();
		versionCheck();
	}
}
