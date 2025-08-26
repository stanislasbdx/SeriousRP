package fr.stan1712.wetston.seriousrp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class Utils {
	private Utils() {
		throw new IllegalStateException("Utility class");
	}

	private static final Plugin plugin = Main.getPlugin(Main.class);
	private static final FileConfiguration plConfig = plugin.getConfig();

	public static class ConfigFactory {
		private ConfigFactory() {
			throw new IllegalStateException("Utility class");
		}

		public static String getConfigString(String path) {
			return Objects.requireNonNull(plConfig.getString(path)).replace("&", "§");
		}
		public static Boolean getConfigBoolean(String path) {
			return plConfig.getBoolean(path);
		}

		public static String getPrefixString() {
			return getConfigString("Prefix");
		}
		public static String getShortPrefixString() {
			return getConfigString("ShortPrefix");
		}
	}
}
