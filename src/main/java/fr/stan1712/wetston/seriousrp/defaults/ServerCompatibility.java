package fr.stan1712.wetston.seriousrp.defaults;

import java.util.regex.Pattern;

public final class ServerCompatibility {
	private static final Pattern VERSION_PATTERN = Pattern.compile("\\d[.]\\d+", Pattern.MULTILINE);

	private ServerCompatibility() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean hasMinecraftVersionToken(String serverVersion) {
		return serverVersion != null && VERSION_PATTERN.matcher(serverVersion).find();
	}

	public static boolean isSupportedServerType(String serverType) {
		return serverType != null
			&& (serverType.contains("Spigot") || serverType.contains("Paper") || serverType.contains("Purpur"));
	}

	public static boolean isSupportedMinecraftRelease(String serverVersion) {
		return serverVersion != null && serverVersion.contains("1.21");
	}

	public static boolean isCompatible(String serverType, String serverVersion) {
		return hasMinecraftVersionToken(serverVersion)
			&& isSupportedServerType(serverType)
			&& isSupportedMinecraftRelease(serverVersion);
	}
}
