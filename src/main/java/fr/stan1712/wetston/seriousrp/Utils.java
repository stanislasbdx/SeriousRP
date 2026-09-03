package fr.stan1712.wetston.seriousrp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Utils {
	private Utils() {
		throw new IllegalStateException("Utility class");
	}

	public static class ConfigFactory {
		private static FileConfiguration plConfig;

		private ConfigFactory() {
			throw new IllegalStateException("Utility class");
		}

		public static void overrideConfig(FileConfiguration config) {
			plConfig = config;
		}

		public static void resetConfig() {
			plConfig = null;
		}

		private static FileConfiguration config() {
			if (plConfig == null) {
				plConfig = JavaPlugin.getPlugin(Main.class).getConfig();
			}
			return plConfig;
		}

		public static String getConfigString(String path) {
			return Objects.requireNonNull(config().getString(path)).replace("&", "§");
		}
		public static Boolean getConfigBoolean(String path) {
			return config().getBoolean(path);
		}

		public static String getPrefixString() {
			return getConfigString("Prefix");
		}
		public static String getShortPrefixString() {
			return getConfigString("ShortPrefix");
		}
	}
}
