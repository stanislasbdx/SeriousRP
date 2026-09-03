package fr.stan1712.wetston.seriousrp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static fr.stan1712.wetston.seriousrp.Main.SPIGOT_PLUGIN_ID;

public class Config implements Listener {
	private final Plugin plugin = Main.getPlugin(Main.class);
	private static final Logger _log = LoggerFactory.getLogger("SeriousRP - Config");
	private static final DateTimeFormatter UPGRADE_LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

	String version = this.plugin.getDescription().getVersion();
	String fileVersion = this.plugin.getConfig().getString("Version");

	private void checkConfigVersion() {
		plugin.getConfig();
		final String logStep = "[@ checkConfigVersion] ";

		if(!version.equals(fileVersion)) {
			final String upgradeFilesDir = plugin.getDataFolder() + "/upgrades/";

			final boolean upgradeDirCreated = new File(upgradeFilesDir).mkdirs();
			if(upgradeDirCreated) _log.debug("{}{} folder created", logStep, new File(upgradeFilesDir).getPath());

			File upgradeFile = new File(upgradeFilesDir, fileVersion + "_to_" + version + ".yml");

			try {
				if(!upgradeFile.exists()) {
					final boolean upgradeFileCreated = upgradeFile.createNewFile();
					if(upgradeFileCreated) _log.debug("{}.yml file created", upgradeFile.getPath());

					FileConfiguration configReport = YamlConfiguration.loadConfiguration(upgradeFile);

					final ArrayList<String> headerUpgradeStrings = new ArrayList<>();
					headerUpgradeStrings.add(String.format("SeriousRP Upgrade Log [%s]", UPGRADE_LOG_DATE_FORMAT.format(LocalDateTime.now())));
					headerUpgradeStrings.add(String.format("Upgrade from version %s to %s", fileVersion, version));
					headerUpgradeStrings.add("All those informations can be used and sent to a developer in you have issues with upgrading your plugin");
					configReport.options().setHeader(headerUpgradeStrings);

					configReport.set("report.serverVersion", plugin.getServer().getVersion());

					new UpdateChecker(this.plugin, SPIGOT_PLUGIN_ID).getVersion(remoteVersion -> {
						if(!plugin.getDescription().getVersion().equalsIgnoreCase(remoteVersion)) configReport.set("report.versionDiffers", remoteVersion);
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

					_log.debug("{}Log created (upgrades/{}_to_{}.yml) !", logStep, fileVersion, version);

					plugin.getConfig().set("Version", version);
					_log.info("{}config.yml upgraded ({} -> {}) !", logStep, fileVersion, version);
				}
				else {
					_log.warn("{}Log {}_to_{}.yml already exists !", logStep, fileVersion, version);
				}
			} catch (IOException e) {
				_log.error("{}Unable to create the upgrade log !", logStep);
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
