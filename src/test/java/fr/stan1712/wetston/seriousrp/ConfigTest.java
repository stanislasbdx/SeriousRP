package fr.stan1712.wetston.seriousrp;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConfigTest {

	@TempDir Path tempDir;

	@Mock Plugin plugin;
	@Mock PluginDescriptionFile description;

	@Test
	void existingUpgradeLogDoesNotRecurseAndBumpsVersion() throws Exception {
		YamlConfiguration config = new YamlConfiguration();
		config.set("Version", "0");
		config.set("ConfigFix", false);
		stubPlugin(config);

		File upgrades = tempDir.resolve("upgrades").toFile();
		assertTrue(upgrades.mkdirs());
		assertTrue(new File(upgrades, "0_to_5.2.0.yml").createNewFile());

		assertDoesNotThrow(() -> new Config(plugin));
		assertEquals("5.2.0", config.getString("Version"));
		assertFalse(config.getBoolean("ConfigFix"));
	}

	@Test
	void matchingVersionSkipsUpgradeLog() {
		YamlConfiguration config = new YamlConfiguration();
		config.set("Version", "5.2.0");
		config.set("ConfigFix", false);
		stubPlugin(config);

		assertDoesNotThrow(() -> new Config(plugin));
		assertFalse(tempDir.resolve("upgrades").toFile().exists());
		assertEquals("5.2.0", config.getString("Version"));
	}

	private void stubPlugin(YamlConfiguration config) {
		when(plugin.getConfig()).thenReturn(config);
		when(plugin.getDescription()).thenReturn(description);
		when(description.getVersion()).thenReturn("5.2.0");
		when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
	}
}
