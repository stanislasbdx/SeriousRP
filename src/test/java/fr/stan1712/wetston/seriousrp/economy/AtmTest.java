package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
			"§b§l[sATM]",
			"§8§m              ",
			"§7Bank §8· §6cash",
			"§8» §fRight click"
		}, Atm.genericLines(cash));
		String[] nearby = Atm.nearbyLines(cash, 12, 4);
		assertEquals("§b§l[sATM]", nearby[0]);
		assertEquals("Square", Atm.nearbyLines(cash, 12, 4, "Square")[0]);
		assertEquals("Square", Atm.genericLines(cash, "Square")[0]);
		assertNull(Atm.cleanAtmName("   "));
		assertNull(Atm.cleanAtmName("this name is way too long"));
		assertEquals("Square", Atm.cleanAtmName(" &cSquare "));
		assertTrue(Atm.canRename(OWNER, OWNER, false));
		assertFalse(Atm.canRename(OWNER, UUID.randomUUID(), false));
		assertTrue(Atm.canRename(OWNER, UUID.randomUUID(), true));
		assertEquals("§7Acc §a12€", nearby[1]);
		assertEquals("§7Cash §64€", nearby[2]);
		assertEquals("§8» §fRight click", nearby[3]);

		String[] rich = Atm.nearbyLines(cash, 996_999_081, 2_650);
		assertEquals("§7Acc §a996M€", rich[1]);
		assertEquals("§7Cash §62650€", rich[2]);
		assertTrue(Cash.stripColorCodes(rich[1]).length() <= 15);
		assertTrue(Cash.stripColorCodes(rich[2]).length() <= 15);

		assertEquals("9999", Atm.formatSignAmount(9_999));
		assertEquals("10k", Atm.formatSignAmount(10_000));
		assertEquals("12.5k", Atm.formatSignAmount(12_500));
		assertEquals("150k", Atm.formatSignAmount(150_400));
		assertEquals("999k", Atm.formatSignAmount(999_999));
		assertEquals("1M", Atm.formatSignAmount(1_000_000));
		assertEquals("1.5M", Atm.formatSignAmount(1_500_000));
		assertEquals("996M", Atm.formatSignAmount(996_999_081));
		assertEquals("2.1B", Atm.formatSignAmount(Integer.MAX_VALUE));
		assertEquals("0", Atm.formatSignAmount(-3));

		assertEquals(15, Cash.stripColorCodes(Atm.trimLine("§aabcdefghijklmnopqrstuvwxyz")).length());
		assertEquals("short", Atm.trimLine("short"));
		assertEquals("", Atm.trimLine(null));

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
