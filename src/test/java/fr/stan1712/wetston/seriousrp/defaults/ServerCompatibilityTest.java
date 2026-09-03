package fr.stan1712.wetston.seriousrp.defaults;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerCompatibilityTest {

	@Test
	void acceptsPaperPurpurAndSpigotOn121() {
		assertTrue(ServerCompatibility.isCompatible("Paper", "git-Paper-1.21.9"));
		assertTrue(ServerCompatibility.isCompatible("Purpur", "Purpur 1.21.11"));
		assertTrue(ServerCompatibility.isCompatible("Spigot", "git-Spigot-1.21"));
	}

	@Test
	void rejectsUnknownTypeOrUnsupportedRelease() {
		assertFalse(ServerCompatibility.isCompatible("Vanilla", "1.21.1"));
		assertFalse(ServerCompatibility.isCompatible("Paper", "1.20.4"));
		assertFalse(ServerCompatibility.isCompatible("Paper", "no-version"));
		assertFalse(ServerCompatibility.isCompatible(null, "1.21"));
		assertFalse(ServerCompatibility.hasMinecraftVersionToken(null));
		assertFalse(ServerCompatibility.isSupportedServerType(null));
		assertFalse(ServerCompatibility.isSupportedMinecraftRelease(null));
	}

	@Test
	void constructorIsHidden() throws Exception {
		Constructor<ServerCompatibility> constructor = ServerCompatibility.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertEquals(IllegalStateException.class, thrown.getCause().getClass());
	}
}
