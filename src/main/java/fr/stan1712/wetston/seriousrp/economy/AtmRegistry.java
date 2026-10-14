package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class AtmRegistry {
	public record Entry(String world, int x, int y, int z, UUID owner) {
		public Optional<Location> location() {
			World loaded = Bukkit.getWorld(world);
			if (loaded == null) {
				return Optional.empty();
			}
			return Optional.of(new Location(loaded, x, y, z));
		}

		public String key() {
			return world + ";" + x + ";" + y + ";" + z;
		}
	}

	private final File file;
	private final FileConfiguration data;

	public AtmRegistry(Plugin plugin) {
		this(new File(plugin.getDataFolder(), "atms.yml"));
	}

	AtmRegistry(File file) {
		this.file = file;
		this.data = YamlConfiguration.loadConfiguration(file);
	}

	public void register(Location location, UUID owner) {
		if (location == null || location.getWorld() == null || owner == null) {
			return;
		}
		Entry entry = new Entry(
			location.getWorld().getName(),
			location.getBlockX(),
			location.getBlockY(),
			location.getBlockZ(),
			owner
		);
		data.set(entry.key() + ".owner", owner.toString());
		data.set(entry.key() + ".world", entry.world());
		data.set(entry.key() + ".x", entry.x());
		data.set(entry.key() + ".y", entry.y());
		data.set(entry.key() + ".z", entry.z());
		save();
	}

	public void unregister(Location location) {
		if (location == null || location.getWorld() == null) {
			return;
		}
		String key = location.getWorld().getName()
			+ ";" + location.getBlockX()
			+ ";" + location.getBlockY()
			+ ";" + location.getBlockZ();
		data.set(key, null);
		save();
	}

	public List<Entry> all() {
		List<Entry> entries = new ArrayList<>();
		for (String key : data.getKeys(false)) {
			parse(key).ifPresent(entries::add);
		}
		return List.copyOf(entries);
	}

	public List<Entry> ownedBy(UUID owner) {
		return all().stream().filter(entry -> entry.owner().equals(owner)).toList();
	}

	private Optional<Entry> parse(String key) {
		String ownerRaw = data.getString(key + ".owner");
		String world = data.getString(key + ".world");
		if (ownerRaw == null || world == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(new Entry(
				world,
				data.getInt(key + ".x"),
				data.getInt(key + ".y"),
				data.getInt(key + ".z"),
				UUID.fromString(ownerRaw)
			));
		}
		catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	private void save() {
		try {
			File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			data.save(file);
		}
		catch (IOException ignored) {
			// best-effort persistence
		}
	}
}
