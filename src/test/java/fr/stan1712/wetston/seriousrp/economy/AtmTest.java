package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtmTest {

	private static final UUID OWNER = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID OTHER = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Test
	void createAndBreakPermissions() {
		Cash cash = Cash.fromConfig(new YamlConfiguration());
		assertTrue(Atm.isCreateAttempt("[sATM]", cash));
		assertTrue(Atm.isCreateAttempt("§a[sATM]", cash));
		assertFalse(Atm.isCreateAttempt("[ATM]", cash));
		assertFalse(Atm.isCreateAttempt(null, cash));

		assertFalse(Atm.canBreak(null, OWNER, true, true));
		assertFalse(Atm.canBreak(OWNER, null, true, true));
		assertTrue(Atm.canBreak(OWNER, OWNER, true, false));
		assertTrue(Atm.canBreak(OWNER, OWNER, false, true));
		assertFalse(Atm.canBreak(OWNER, OWNER, false, false));
		assertTrue(Atm.canBreak(OWNER, OTHER, false, true));
		assertFalse(Atm.canBreak(OWNER, OTHER, true, false));
	}

	@Test
	void signLinesAndAmountResolution() {
		Cash cash = Cash.fromConfig(new YamlConfiguration());
		assertArrayEquals(new String[] {
			"§2§l[sATM]",
			"§8§m              ",
			"§aBanque §8· §ecash",
			"§8» §7Clic droit"
		}, Atm.genericLines(cash));
		assertEquals("§2§l[sATM]", Atm.nearbyLines(cash, 12, 4)[0]);
		assertTrue(Atm.nearbyLines(cash, 12, 4)[1].contains("12"));
		assertTrue(Atm.nearbyLines(cash, 12, 4)[2].contains("4"));
		assertEquals(40, Atm.trimLine("abcdefghijklmnopqrstuvwxyz0123456789XXXXX").length());
		assertEquals("short", Atm.trimLine("short"));

		assertEquals(10, Atm.resolveAmount(Atm.Operation.DEPOSIT, 10, false, 40, 5).orElse(0));
		assertTrue(Atm.resolveAmount(Atm.Operation.DEPOSIT, 10, false, 5, 100).isEmpty());
		assertEquals(40, Atm.resolveAmount(Atm.Operation.DEPOSIT, null, true, 40, 5).orElse(0));
		assertTrue(Atm.resolveAmount(Atm.Operation.DEPOSIT, null, true, 0, 5).isEmpty());
		assertEquals(80, Atm.resolveAmount(Atm.Operation.WITHDRAW, null, true, 1, 80).orElse(0));
		assertTrue(Atm.resolveAmount(Atm.Operation.WITHDRAW, 50, false, 100, 20).isEmpty());
		assertTrue(Atm.resolveAmount(null, 10, false, 10, 10).isEmpty());
		assertTrue(Atm.resolveAmount(Atm.Operation.WITHDRAW, 0, false, 10, 10).isEmpty());
		assertTrue(Atm.resolveAmount(Atm.Operation.COMPACT, 10, false, 10, 10).isEmpty());
		assertTrue(Atm.resolveAmount(Atm.Operation.BREAK, null, true, 10, 10).isEmpty());
	}

	@Test
	void promptParsingAndExpiry() {
		Atm.Prompt prompt = new Atm.Prompt(OWNER, Atm.Operation.DEPOSIT, 100L);
		assertFalse(prompt.isExpired(99L));
		assertTrue(prompt.isExpired(100L));
		assertTrue(Atm.isCancelPrompt(" cancel "));
		assertFalse(Atm.isCancelPrompt("10"));
		assertFalse(Atm.isCancelPrompt(null));
		assertTrue(Atm.parsePromptAmount(" cancel ").isEmpty());
		assertTrue(Atm.parsePromptAmount(null).isEmpty());
		assertEquals(25, Atm.parsePromptAmount(" 25 ").orElse(0));
		assertThrows(IllegalArgumentException.class, () -> new Atm.Prompt(null, Atm.Operation.DEPOSIT, 1L));
		assertThrows(IllegalArgumentException.class, () -> new Atm.Prompt(OWNER, null, 1L));
	}

	@Test
	void constructorIsHidden() throws Exception {
		Constructor<Atm> constructor = Atm.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertEquals(IllegalStateException.class, thrown.getCause().getClass());
	}
}
