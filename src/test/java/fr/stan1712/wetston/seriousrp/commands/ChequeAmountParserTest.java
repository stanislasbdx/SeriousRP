package fr.stan1712.wetston.seriousrp.commands;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChequeAmountParserTest {

	@Test
	void isIntAcceptsIntegersOnly() {
		assertTrue(ChequeAmountParser.isInt("12"));
		assertFalse(ChequeAmountParser.isInt("12.5"));
		assertFalse(ChequeAmountParser.isInt("abc"));
	}

	@Test
	void isFloatAcceptsDecimalNumbers() {
		assertTrue(ChequeAmountParser.isFloat("12.5"));
		assertTrue(ChequeAmountParser.isFloat("12"));
		assertFalse(ChequeAmountParser.isFloat("abc"));
	}

	@Test
	void isPositiveAmountRejectsZeroNegativeAndNull() {
		assertTrue(ChequeAmountParser.isPositiveAmount("1"));
		assertTrue(ChequeAmountParser.isPositiveAmount("1.5"));
		assertFalse(ChequeAmountParser.isPositiveAmount("0"));
		assertFalse(ChequeAmountParser.isPositiveAmount("-3"));
		assertFalse(ChequeAmountParser.isPositiveAmount(null));
		assertFalse(ChequeAmountParser.isPositiveAmount("nope"));
	}

	@Test
	void constructorIsHidden() throws Exception {
		Constructor<ChequeAmountParser> constructor = ChequeAmountParser.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertEquals(IllegalStateException.class, thrown.getCause().getClass());
	}
}
