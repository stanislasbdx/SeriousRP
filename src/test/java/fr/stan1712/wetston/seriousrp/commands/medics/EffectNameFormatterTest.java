package fr.stan1712.wetston.seriousrp.commands.medics;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EffectNameFormatterTest {

	@Test
	void formatNamespacedKeyTitleCasesUnderscores() {
		assertEquals("Slow Falling", EffectNameFormatter.formatNamespacedKey("minecraft:slow_falling"));
		assertEquals("Speed", EffectNameFormatter.formatNamespacedKey("minecraft:speed"));
	}

	@Test
	void formatNamespacedKeyReturnsEmptyForInvalidKeys() {
		assertEquals("", EffectNameFormatter.formatNamespacedKey(null));
		assertEquals("", EffectNameFormatter.formatNamespacedKey("speed"));
		assertEquals("", EffectNameFormatter.formatNamespacedKey("minecraft:"));
		assertEquals("", EffectNameFormatter.formatNamespacedKey("minecraft::x"));
		assertEquals("Slow", EffectNameFormatter.formatNamespacedKey("minecraft:_slow_"));
	}

	@Test
	void constructorIsHidden() throws Exception {
		Constructor<EffectNameFormatter> constructor = EffectNameFormatter.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertEquals(IllegalStateException.class, thrown.getCause().getClass());
	}
}
