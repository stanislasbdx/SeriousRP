package fr.stan1712.wetston.seriousrp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static fr.stan1712.wetston.seriousrp.Main.SPIGOT_PLUGIN_ID;

public class Config implements Listener {
	private final Plugin plugin = Main.getPlugin(Main.class);
	final private static Logger _log = LoggerFactory.getLogger("SeriousRP - Config");

	String version = this.plugin.getDescription().getVersion();
	String fileVersion = this.plugin.getConfig().getString("Version");

	private void checkConfigVersion() {
		plugin.getConfig();
		final String logStep = "[@ checkConfigVersion] ";

		if(!version.equals(fileVersion)) {
			final String upgradeFilesDir = plugin.getDataFolder() + "/upgrades/";

			final boolean upgradeDirCreated = new File(upgradeFilesDir).mkdirs();
			if(upgradeDirCreated) _log.debug(logStep + new File(upgradeFilesDir).getPath() + " folder created");

			File upgradeFile = new File(upgradeFilesDir, fileVersion + "_to_" + version + ".yml");

			try {
				if(!upgradeFile.exists()) {
					final boolean upgradeFileCreated = upgradeFile.createNewFile();
					if(upgradeFileCreated) _log.debug(upgradeFile.getPath() + ".yml file created");

					FileConfiguration configReport = YamlConfiguration.loadConfiguration(upgradeFile);

					final ArrayList<String> headerUpgradeStrings = new ArrayList<>();
					headerUpgradeStrings.add("SeriousRP Upgrade Log [" + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(Calendar.getInstance().getTime()) + "]");
					headerUpgradeStrings.add("Upgrade from version " + fileVersion + " to " + version);
					headerUpgradeStrings.add("All those informations can be used and sent to a developer in you have issues with upgrading your plugin");
					configReport.options().setHeader(headerUpgradeStrings);

					configReport.set("report.serverVersion", plugin.getServer().getVersion());

					new UpdateChecker(this.plugin, SPIGOT_PLUGIN_ID).getVersion(version -> {
						if(!plugin.getDescription().getVersion().equalsIgnoreCase(version)) configReport.set("report.versionDiffers", version);
					});

					configReport.set("options.configFix", plugin.getConfig().getBoolean("ConfigFix"));

					configReport.set("plugin.firstRun", plugin.getConfig().getInt("Version") == 0);

					configReport.set("plugin.dependencies.vault", new File("plugins/Vault").exists());

					configReport.set("plugin.modules.CustomRecipes", plugin.getConfig().getBoolean("Core.Modules.CustomRecipes"));
					configReport.set("plugin.modules.RPDeath", plugin.getConfig().getBoolean("Core.Modules.RPDeath"));
					configReport.set("plugin.modules.Medics", plugin.getConfig().getBoolean("Core.Modules.Medics"));
					configReport.set("plugin.modules.Chairs", plugin.getConfig().getBoolean("Core.Modules.Chairs"));
					configReport.set("plugin.modules.Economy", plugin.getConfig().getBoolean("Core.Modules.Economy"));

					configReport.set("lastConfig", plugin.getConfig().getRoot());

					configReport.save(upgradeFile);

					_log.debug(logStep + "Log created (upgrades/" + fileVersion + "_to_" + version + ".yml) !");

					plugin.getConfig().set("Version", version);
					_log.info(logStep + "config.yml upgraded (" + fileVersion + " -> " + version + ") !");
				}
				else {
					_log.warn(logStep + "Log " + fileVersion + "_to_" + version + ".yml already exists !");
				}
			} catch (IOException e) {
				_log.error(logStep + "Unable to create the upgrade log !");
			}

			plugin.getConfig().set("ConfigFix", Boolean.TRUE);
			new Config();
			plugin.saveConfig();
		}
	}

	public Config() {
		FileConfiguration config = plugin.getConfig();
		plugin.getConfig();

		checkConfigVersion();

		final ArrayList<String> headerStrings = new ArrayList<>();
		headerStrings.add("SeriousRP | Owner : stan1712");
		headerStrings.add("Traductors : ErHak_ / legaming04 -> https://github.com/stan1712/SeriousRP/wiki/Translations");
		headerStrings.add("Our Discord : https://discord.gg/DkQSQa7");
		headerStrings.add("Material list : https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html (for the Chairs module)");
		config.options().setHeader(headerStrings);

		if(config.getBoolean("ConfigFix")) {
			config.options().copyDefaults(true);
			config.options().parseComments(true);

			config.set("ConfigFix", Boolean.FALSE);

			_log.debug("Config file 'config.yml' updated !");
		}

		_log.info("Config file reloaded !");
	}
}