package fr.stan1712.wetston.seriousrp.economy;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

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
		Cassette.Payload locked = Cassette.withPin(original, "1234");
		Cassette.Payload kept = Cassette.fromJson(Cassette.toJson(locked)).orElseThrow();
		assertEquals("1234", kept.pin());
		assertEquals(60, Cassette.takeValue(kept, 10, EURO).orElseThrow().total());
		assertEquals("1234", Cassette.takeValue(kept, 10, EURO).orElseThrow().pin());
	}

	@Test
	void threeWrongPinsWipeContentsAndKeepTheCode() {
		Cassette.Payload locked = Cassette.withPin(
			Cassette.addValue(Cassette.empty(), 40, EURO, 100).orElseThrow(),
			"9999"
		);
		Cassette.PinAttempt first = Cassette.tryPin(locked, "0000");
		assertFalse(first.unlocked());
		assertFalse(first.wiped());
		assertEquals(1, first.payload().failedAttempts());
		assertEquals(40, first.payload().total());

		Cassette.PinAttempt second = Cassette.tryPin(first.payload(), "1111");
		assertEquals(2, second.payload().failedAttempts());

		Cassette.PinAttempt third = Cassette.tryPin(second.payload(), "2222");
		assertTrue(third.wiped());
		assertEquals(0, third.payload().total());
		assertEquals("9999", third.payload().pin());
		assertEquals(0, third.payload().failedAttempts());

		Cassette.PinAttempt opened = Cassette.tryPin(third.payload(), "9999");
		assertTrue(opened.unlocked());
		assertEquals(0, opened.payload().failedAttempts());
	}

	@Test
	void shortIdAndFitKeepTheCode() {
		assertEquals("f4de5", Cassette.shortId("f4de5abc-1111-2222-3333-444444444444"));
		Cassette.Payload locked = Cassette.withPin(
			Cassette.addValue(Cassette.empty(), 80, EURO, 100).orElseThrow(),
			"1234"
		);
		Cassette.Fit fit = Cassette.fit(locked, 50, EURO);
		assertEquals(50, fit.kept().total());
		assertEquals("1234", fit.kept().pin());
		assertEquals(30, CashTender.total(fit.overflow()));
		assertEquals(80, Cassette.fit(locked, 80, EURO).kept().total());
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

	@Test
	void movementTagsRecordTheDeltaOnEachCassette() {
		Cassette.Payload first = Cassette.addValue(Cassette.empty(), 80, EURO, 100).orElseThrow();
		Cassette.Payload second = Cassette.addValue(Cassette.empty(), 40, EURO, 100).orElseThrow();
		List<Optional<Cassette.Payload>> before = AtmCassettes.install(
			AtmCassettes.install(AtmCassettes.emptySlots(2), 0, first).orElseThrow(),
			1,
			second
		).orElseThrow();
		List<Optional<Cassette.Payload>> after = AtmCassettes.withdraw(before, 100, EURO).orElseThrow();
		assertEquals(
			"#" + Cassette.shortId(first.id()) + ":-80,#" + Cassette.shortId(second.id()) + ":-20",
			AtmCassettes.movementTags(before, after)
		);
	}
}
