package fr.stan1712.wetston.seriousrp.economy;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CashTenderTest {

	private static final List<Integer> EURO = List.of(500, 200, 100, 50, 20, 10, 5, 2, 1);

	@Test
	void totalCompactsAndSplitsStacks() {
		List<Cash.Stack> stacks = List.of(new Cash.Stack(20, 3), new Cash.Stack(20, 2), new Cash.Stack(1, 70));
		assertEquals(170, CashTender.total(stacks));
		assertEquals(List.of(new Cash.Stack(20, 5), new Cash.Stack(1, 70)), CashTender.compact(stacks));
		assertEquals(
			List.of(new Cash.Stack(20, 5), new Cash.Stack(1, 64), new Cash.Stack(1, 6)),
			CashTender.toInventoryStacks(stacks)
		);
		assertEquals(
			List.of(new Cash.Stack(1, 64), new Cash.Stack(1, 64)),
			CashTender.toInventoryStacks(List.of(new Cash.Stack(1, 128)))
		);
	}

	@Test
	void totalSaturatesAtIntegerMax() {
		List<Cash.Stack> huge = List.of(new Cash.Stack(500, Integer.MAX_VALUE));
		assertEquals(Integer.MAX_VALUE, CashTender.total(huge));
		assertEquals(Integer.MAX_VALUE, CashTender.saturatingAdd(Integer.MAX_VALUE, 10));
		assertEquals(3, CashTender.saturatingAdd(1, 2));
	}

	@Test
	void greedyMakesCanonicalChange() {
		assertEquals(
			List.of(new Cash.Stack(100, 1), new Cash.Stack(20, 2), new Cash.Stack(5, 1)),
			CashTender.greedy(145, EURO).orElseThrow()
		);
		assertTrue(CashTender.greedy(0, EURO).orElseThrow().isEmpty());
		assertTrue(CashTender.greedy(-1, EURO).isEmpty());
		assertTrue(CashTender.greedy(10, List.of()).isEmpty());
		assertTrue(CashTender.greedy(3, java.util.Arrays.asList(2, 0, null, -5)).isEmpty());
		assertEquals(List.of(new Cash.Stack(2, 1)), CashTender.greedy(2, java.util.Arrays.asList(2, 0, null, -5)).orElseThrow());
	}

	@Test
	void extractRewritesLeftoverAsGreedyChange() {
		List<Cash.Stack> source = List.of(new Cash.Stack(500, 1));
		assertEquals(List.of(new Cash.Stack(200, 2)), CashTender.extract(source, 100, EURO).orElseThrow());
		assertTrue(CashTender.extract(source, 0, EURO).isEmpty());
		assertTrue(CashTender.extract(source, 600, EURO).isEmpty());
		assertTrue(CashTender.extract(List.of(new Cash.Stack(100, 1)), 100, EURO).orElseThrow().isEmpty());
	}

	@Test
	void payTakesInventoryThenWallets() {
		CashTender.Ledger ledger = new CashTender.Ledger(
			List.of(new Cash.Stack(50, 1)),
			List.of(List.of(), List.of(new Cash.Stack(100, 2)))
		);
		assertEquals(250, ledger.total());

		CashTender.Ledger paid = CashTender.pay(ledger, 150, EURO).orElseThrow();
		assertTrue(paid.inventory().isEmpty());
		assertEquals(List.of(List.of(), List.of(new Cash.Stack(100, 1))), paid.wallets());

		assertTrue(CashTender.pay(
			new CashTender.Ledger(List.of(new Cash.Stack(3, 1)), List.of()),
			1,
			List.of(3)
		).isEmpty());
		assertTrue(CashTender.pay(ledger, 999, EURO).isEmpty());
		assertTrue(CashTender.pay(ledger, 0, EURO).isEmpty());
	}

	@Test
	void payFromWalletsOnlyAndStopsWhenWalletCannotMakeChange() {
		CashTender.Ledger walletsOnly = new CashTender.Ledger(
			List.of(),
			List.of(List.of(new Cash.Stack(50, 2)))
		);
		CashTender.Ledger paid = CashTender.pay(walletsOnly, 50, EURO).orElseThrow();
		assertTrue(paid.inventory().isEmpty());
		assertEquals(List.of(List.of(new Cash.Stack(50, 1))), paid.wallets());

		assertTrue(CashTender.pay(
			new CashTender.Ledger(List.of(), List.of(List.of(new Cash.Stack(3, 1)))),
			1,
			List.of(3)
		).isEmpty());
	}

	@Test
	void payCanDrainInventoryOnly() {
		CashTender.Ledger ledger = new CashTender.Ledger(
			List.of(new Cash.Stack(10, 5)),
			List.of(List.of(new Cash.Stack(20, 1)))
		);
		CashTender.Ledger paid = CashTender.pay(ledger, 30, EURO).orElseThrow();
		assertEquals(List.of(new Cash.Stack(20, 1)), paid.inventory());
		assertEquals(List.of(List.of(new Cash.Stack(20, 1))), paid.wallets());
	}

	@Test
	void parseTransformSupportsGreedyAndExactDenomination() {
		CashTender.TransformPlan greedy = CashTender.parseTransform(new String[] {"stan", "70"}, EURO).orElseThrow();
		assertEquals("stan", greedy.playerName());
		assertEquals(70, greedy.vaultDebit());
		assertEquals(List.of(new Cash.Stack(50, 1), new Cash.Stack(20, 1)), greedy.stacks());

		CashTender.TransformPlan exact = CashTender.parseTransform(new String[] {"stan", "50", "3"}, EURO).orElseThrow();
		assertEquals(150, exact.vaultDebit());
		assertEquals(List.of(new Cash.Stack(50, 3)), exact.stacks());

		assertTrue(CashTender.parseTransform(null, EURO).isEmpty());
		assertTrue(CashTender.parseTransform(new String[] {"stan"}, EURO).isEmpty());
		assertTrue(CashTender.parseTransform(new String[] {null, "10"}, EURO).isEmpty());
		assertTrue(CashTender.parseTransform(new String[] {"", "10"}, EURO).isEmpty());
		assertTrue(CashTender.parseTransform(new String[] {"stan", "nope"}, EURO).isEmpty());
		assertTrue(CashTender.parseTransform(new String[] {"stan", "7", "1"}, EURO).isEmpty());
		assertTrue(CashTender.parseTransform(new String[] {"stan", "50", "no"}, EURO).isEmpty());
		assertTrue(CashTender.parseTransform(new String[] {"stan", "nope", "1"}, EURO).isEmpty());
		assertTrue(CashTender.parseTransform(new String[] {"stan", "50", "3", "extra"}, EURO).isEmpty());
		assertTrue(CashTender.parseTransform(new String[] {"stan", "50", String.valueOf(Integer.MAX_VALUE)}, EURO).isEmpty());
	}

	@Test
	void constructorIsHidden() throws Exception {
		Constructor<CashTender> constructor = CashTender.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertEquals(IllegalStateException.class, thrown.getCause().getClass());
	}
}
