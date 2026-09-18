package fr.stan1712.wetston.seriousrp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ConfigFactoryTest extends ConfigBackedTest {

	@Test
	void getConfigStringColorizesAmpersandCodes() {
		assertEquals("§7[§bsRP§7] » ", Utils.ConfigFactory.getShortPrefixString());
		assertTrue(Utils.ConfigFactory.getPrefixString().startsWith("§f«"));
	}

	@Test
	void getConfigBooleanReadsModuleFlags() {
		assertEquals(Boolean.TRUE, Utils.ConfigFactory.getConfigBoolean("Core.Modules.Economy"));
		assertEquals(Boolean.FALSE, Utils.ConfigFactory.getConfigBoolean("Core.Modules.Medics"));
	}

	@Test
	void shippedCashWalletMappingKeepsSettingsAndMessages() {
		assertEquals("§cOnly cash can go in a wallet", Utils.ConfigFactory.getConfigString("Economy.Cash.Wallet.NotCash"));
		assertEquals("§cThis wallet is already open", Utils.ConfigFactory.getConfigString("Economy.Cash.Wallet.AlreadyOpen"));

		InputStream stream = Objects.requireNonNull(
			getClass().getClassLoader().getResourceAsStream("config.yml")
		);
		YamlConfiguration loaded = YamlConfiguration.loadConfiguration(
			new InputStreamReader(stream, StandardCharsets.UTF_8)
		);
		assertEquals(18, loaded.getInt("Economy.Cash.Wallet.DefaultSlots"));
		assertEquals("LEATHER", loaded.getString("Economy.Cash.Wallet.Material"));
		assertTrue(loaded.contains("Economy.Cash.Wallet.Recipe.Shape"));
	}

	@Test
	void overrideConfigReplacesValuesForTests() {
		YamlConfiguration override = new YamlConfiguration();
		override.set("Prefix", "&aTEST");
		Utils.ConfigFactory.overrideConfig(override);

		assertEquals("§aTEST", Utils.ConfigFactory.getPrefixString());
	}

	@Test
	void configFallsBackToJavaPluginWhenNotOverridden() {
		Utils.ConfigFactory.resetConfig();
		YamlConfiguration live = new YamlConfiguration();
		live.set("Prefix", "&bLIVE");
		Main main = mock(Main.class);
		when(main.getConfig()).thenReturn(live);

		try (MockedStatic<JavaPlugin> javaPlugin = mockStatic(JavaPlugin.class)) {
			javaPlugin.when(() -> JavaPlugin.getPlugin(Main.class)).thenReturn(main);
			assertEquals("§bLIVE", Utils.ConfigFactory.getPrefixString());
		}
	}

	@Test
	void utilityConstructorsAreHidden() throws Exception {
		assertHiddenUtility(Utils.class);
		assertHiddenUtility(Utils.ConfigFactory.class);
	}

	private static void assertHiddenUtility(Class<?> type) throws Exception {
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertEquals(IllegalStateException.class, thrown.getCause().getClass());
	}
}
