package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class AtmLogger {
	private static final int TAIL_BYTES = 64 * 1024;
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

	public List<String> recentCassetteLines(int limit, String contains) {
		if (limit <= 0 || !Files.isRegularFile(file)) {
			return List.of();
		}
		String text = readTail();
		if (text.isEmpty()) {
			return List.of();
		}
		String[] lines = text.split("\\R");
		List<String> matched = new ArrayList<>();
		for (int index = lines.length - 1; index >= 0 && matched.size() < limit; index--) {
			String line = lines[index].strip();
			if (line.isEmpty() || !isCassetteLine(line)) {
				continue;
			}
			if (contains != null && !line.contains(contains)) {
				continue;
			}
			matched.add(line);
		}
		return List.copyOf(matched);
	}

	private static boolean isCassetteLine(String line) {
		return line.contains("cassette=") || line.contains("cassettes=");
	}

	private String readTail() {
		try {
			long size = Files.size(file);
			if (size <= TAIL_BYTES) {
				return Files.readString(file, StandardCharsets.UTF_8);
			}
			byte[] buffer = new byte[TAIL_BYTES];
			try (SeekableByteChannel channel = Files.newByteChannel(file)) {
				channel.position(size - TAIL_BYTES);
				int read = channel.read(ByteBuffer.wrap(buffer));
				if (read <= 0) {
					return "";
				}
				String text = new String(buffer, 0, read, StandardCharsets.UTF_8);
				int newline = text.indexOf('\n');
				return newline >= 0 ? text.substring(newline + 1) : text;
			}
		}
		catch (IOException ignored) {
			return "";
		}
	}
}
