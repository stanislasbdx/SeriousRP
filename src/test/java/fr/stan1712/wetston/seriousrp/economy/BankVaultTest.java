package fr.stan1712.wetston.seriousrp.economy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankVaultTest {

	@Test
	void feedAndTakeRespectCapacity() {
		assertTrue(BankVault.feed(0, 0, 100).isEmpty());
		assertTrue(BankVault.feed(90, 20, 100).isEmpty());
		assertEquals(100, BankVault.feed(80, 20, 100).orElseThrow());
		assertTrue(BankVault.take(10, 20).isEmpty());
		assertEquals(30, BankVault.take(50, 20).orElseThrow());
	}

	@Test
	void pinHelpers() {
		assertTrue(BankVault.needsPinSetup(null));
		assertTrue(BankVault.needsPinSetup(""));
		assertFalse(BankVault.needsPinSetup("1234"));
		assertTrue(BankVault.matchesPin("1234", "1234"));
		assertFalse(BankVault.matchesPin("1234", "0000"));
		assertFalse(BankVault.matchesPin(null, "1234"));
	}
}
