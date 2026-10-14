package fr.stan1712.wetston.seriousrp.economy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CassetteTest {

	private static final List<Integer> EURO = List.of(500, 200, 100, 50, 20, 10, 5, 2, 1);

	@Test
	void jsonRoundTripAndPinValidation() {
		Cassette.Payload original = Cassette.addValue(Cassette.empty(), 70, EURO, 1000).orElseThrow();
		Cassette.Payload parsed = Cassette.fromJson(Cassette.toJson(original)).orElseThrow();
		assertEquals(original.id(), parsed.id());
		assertEquals(70, parsed.total());
		assertTrue(Cassette.fromJson("").isEmpty());
		assertTrue(Cassette.fromJson("{").isEmpty());
		assertTrue(Cassette.isPin("1234"));
		assertFalse(Cassette.isPin("12a4"));
		assertFalse(Cassette.isPin("123"));
		assertFalse(Cassette.isPin(null));
	}

	@Test
	void addAndTakeRespectCapacity() {
		Cassette.Payload empty = Cassette.empty();
		assertTrue(Cassette.addValue(empty, 0, EURO, 100).isEmpty());
		assertTrue(Cassette.addValue(empty, 150, EURO, 100).isEmpty());
		Cassette.Payload filled = Cassette.addValue(empty, 100, EURO, 100).orElseThrow();
		assertEquals(100, filled.total());
		assertTrue(Cassette.addValue(filled, 1, EURO, 100).isEmpty());
		Cassette.Payload left = Cassette.takeValue(filled, 40, EURO).orElseThrow();
		assertEquals(60, left.total());
		assertTrue(Cassette.takeValue(left, 100, EURO).isEmpty());
	}

	@Test
	void multiSlotDepositWithdrawInstallEject() {
		var slots = AtmCassettes.emptySlots(2);
		assertFalse(AtmCassettes.hasCassette(slots));
		assertEquals(0, AtmCassettes.total(slots));

		Cassette.Payload first = Cassette.empty();
		Cassette.Payload second = Cassette.empty();
		slots = AtmCassettes.install(slots, 0, first).orElseThrow();
		slots = AtmCassettes.install(slots, 1, second).orElseThrow();
		assertTrue(AtmCassettes.install(slots, 0, Cassette.empty()).isEmpty());

		slots = AtmCassettes.deposit(slots, 120, EURO, 100).orElseThrow();
		assertEquals(120, AtmCassettes.total(slots));
		assertEquals(100, slots.get(0).orElseThrow().total());
		assertEquals(20, slots.get(1).orElseThrow().total());
		assertTrue(AtmCassettes.deposit(slots, 100, EURO, 100).isEmpty());

		slots = AtmCassettes.withdraw(slots, 50, EURO).orElseThrow();
		assertEquals(70, AtmCassettes.total(slots));
		assertTrue(AtmCassettes.withdraw(slots, 1000, EURO).isEmpty());

		AtmCassettes.Eject ejected = AtmCassettes.eject(slots, 0).orElseThrow();
		assertTrue(ejected.slots().get(0).isEmpty());
		assertTrue(ejected.slots().get(1).isPresent());
		assertTrue(AtmCassettes.eject(ejected.slots(), 0).isEmpty());
	}
}
