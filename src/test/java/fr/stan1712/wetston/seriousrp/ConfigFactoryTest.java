package fr.stan1712.wetston.seriousrp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		Main main = org.mockito.Mockito.mock(Main.class);
		org.mockito.Mockito.when(main.getConfig()).thenReturn(live);

		try (org.mockito.MockedStatic<org.bukkit.plugin.java.JavaPlugin> javaPlugin =
			     org.mockito.Mockito.mockStatic(org.bukkit.plugin.java.JavaPlugin.class)) {
			javaPlugin.when(() -> org.bukkit.plugin.java.JavaPlugin.getPlugin(Main.class)).thenReturn(main);
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
