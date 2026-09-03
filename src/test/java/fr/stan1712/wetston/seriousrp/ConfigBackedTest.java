package fr.stan1712.wetston.seriousrp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class ConfigBackedTest {

	@BeforeEach
	void loadPluginConfig() {
		InputStream stream = Objects.requireNonNull(
			getClass().getClassLoader().getResourceAsStream("config.yml"),
			"config.yml missing from test classpath"
		);
		YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
		config.set("Medics.MedRevive.mistaken", "Unknown player %target%");
		Utils.ConfigFactory.overrideConfig(config);
	}

	@AfterEach
	void resetPluginConfig() {
		Utils.ConfigFactory.resetConfig();
	}
}
