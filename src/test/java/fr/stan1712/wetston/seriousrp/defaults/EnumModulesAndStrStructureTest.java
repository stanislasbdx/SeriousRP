package fr.stan1712.wetston.seriousrp.defaults;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumModulesAndStrStructureTest {

	@Test
	void moduleConstantsMatchConfigKeysUsage() {
		assertEquals("CUSTOM_RECIPES", EnumModules.CUSTOM_RECIPES);
		assertEquals("ROLEPLAY_DEATH", EnumModules.RP_DEATH);
		assertEquals("MEDICS", EnumModules.MEDICS);
		assertEquals("CHAIRS", EnumModules.CHAIRS);
		assertEquals("ECONOMY", EnumModules.ECONOMY);
	}

	@Test
	void titleBoxConstantsAreNonEmpty() {
		assertFalse(StrStructure.BOTTOM_BOX.isBlank());
		assertTrue(StrStructure.START_TITLE_BOX.contains("+-----"));
		assertTrue(StrStructure.END_TITLE_BOX.contains("-----+"));
	}

	@Test
	void constructorsAreHidden() throws Exception {
		assertHidden(EnumModules.class);
		assertHidden(StrStructure.class);
	}

	private static void assertHidden(Class<?> type) throws Exception {
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertEquals(IllegalStateException.class, thrown.getCause().getClass());
	}
}
