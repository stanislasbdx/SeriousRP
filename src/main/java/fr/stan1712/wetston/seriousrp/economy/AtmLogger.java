package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class AtmLogger {
	private final Path file;

	public AtmLogger(Plugin plugin) {
		this(plugin.getDataFolder().toPath().resolve("logs").resolve("atm.log"));
	}

	AtmLogger(Path file) {
		this.file = file;
	}

	public void log(String action, String details) {
		String line = Instant.now() + " " + action + " " + (details == null ? "" : details) + System.lineSeparator();
		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(file, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
		catch (IOException ignored) {
			// best-effort logging
		}
	}
}
