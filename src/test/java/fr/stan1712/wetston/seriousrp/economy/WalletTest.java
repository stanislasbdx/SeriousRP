package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletTest {

	private static Cash euroCash() {
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Cash.Denominations", List.of(
			Map.of("value", 1, "material", "GOLD_NUGGET"),
			Map.of("value", 20, "material", "PAPER")
		));
		return Cash.fromConfig(config);
	}

	@Test
	void jsonRoundTripSkipsInvalidStacksAndClampsSlots() {
		Wallet.Payload original = new Wallet.Payload("abc", 27, List.of(new Cash.Stack(20, 2), new Cash.Stack(1, 3)));
		String json = Wallet.toJson(original);
		Wallet.Payload restored = Wallet.fromJson(json).orElseThrow();
		assertEquals("abc", restored.id());
		assertEquals(27, restored.slots());
		assertEquals(43, restored.total());
		assertEquals(original.stacks(), restored.stacks());

		assertEquals(18, new Wallet.Payload("id", 12, List.of()).slots());
		assertEquals(0, new Wallet.Payload("id", 9, null).total());
		assertTrue(Wallet.fromJson(null).isEmpty());
		assertTrue(Wallet.fromJson(" ").isEmpty());
		assertTrue(Wallet.fromJson("{not-json").isEmpty());
		assertTrue(Wallet.fromJson("null").isEmpty());
		assertTrue(Wallet.fromJson("{\"slots\":9}").isEmpty());
		assertTrue(Wallet.fromJson("{\"id\":\"  \",\"slots\":9}").isEmpty());
		assertEquals("w1", Wallet.fromJson("{\"id\":\"w1\",\"slots\":9}").orElseThrow().id());

		Wallet.Payload messy = Wallet.fromJson(
			"{\"id\":\"w1\",\"slots\":9,\"stacks\":[null,{\"denomination\":0,\"amount\":1},{\"denomination\":1,\"amount\":0},{\"denomination\":1,\"amount\":4}]}"
		).orElseThrow();
		assertEquals(List.of(new Cash.Stack(1, 4)), messy.stacks());
	}

	@Test
	void payloadRejectsBlankIds() {
		assertThrows(NullPointerException.class, () -> new Wallet.Payload(null, 9, List.of()));
		assertThrows(IllegalArgumentException.class, () -> new Wallet.Payload(" ", 9, List.of()));
	}

	@Test
	void createAssignsUniqueIds() {
		assertNotEquals(Wallet.create(9).id(), Wallet.create(9).id());
	}

	@Test
	void loreShowsFormattedTotal() {
		Cash cash = euroCash();
		Wallet.Payload payload = new Wallet.Payload("w", 9, List.of(new Cash.Stack(20, 2)));
		assertEquals(List.of(cash.formatTotal(40)), Wallet.lore(payload, cash));
	}

	@Test
	void depositAcceptsKnownCashUntilSlotLimit() {
		Cash cash = euroCash();
		Wallet.Payload empty = new Wallet.Payload("w", 9, List.of());
		Wallet.Payload deposited = Wallet.deposit(empty, new Cash.Stack(20, 3), cash).orElseThrow();
		assertEquals(60, deposited.total());
		assertTrue(Wallet.deposit(empty, new Cash.Stack(50, 1), cash).isEmpty());

		List<Cash.Stack> full = new java.util.ArrayList<>();
		for (int i = 0; i < 9; i++) {
			full.add(new Cash.Stack(1, 64));
		}
		Wallet.Payload packed = new Wallet.Payload("w", 9, full);
		assertTrue(Wallet.deposit(packed, new Cash.Stack(1, 1), cash).isEmpty());
		assertTrue(Wallet.deposit(packed, new Cash.Stack(1, 64), cash).isEmpty());
	}

	@Test
	void writeAndReadUsePersistentData() {
		PersistentDataContainer container = mock(PersistentDataContainer.class);
		NamespacedKey key = new NamespacedKey("seriousrp", "srp-wallet");
		Wallet.Payload payload = new Wallet.Payload("w", 9, List.of(new Cash.Stack(1, 2)));
		Wallet.write(container, key, payload);
		verify(container).set(key, PersistentDataType.STRING, Wallet.toJson(payload));

		when(container.get(key, PersistentDataType.STRING)).thenReturn(Wallet.toJson(payload));
		assertEquals("w", Wallet.read(container, key).orElseThrow().id());
	}

	@Test
	void sessionsLockWalletIdsAndHotbarSlot() {
		Wallet wallets = new Wallet();
		UUID player = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID other = UUID.fromString("22222222-2222-2222-2222-222222222222");

		assertTrue(wallets.tryOpen(player, "w1", 3));
		assertFalse(wallets.tryOpen(player, "w2", 4));
		assertFalse(wallets.tryOpen(other, "w1", 0));
		assertTrue(wallets.isLocked("w1"));
		assertTrue(wallets.isHeld(player, "w1", 3));
		assertFalse(wallets.isHeld(player, "w1", 4));
		assertFalse(wallets.isHeld(player, "nope", 3));
		assertFalse(wallets.isHeld(other, "w1", 3));
		assertEquals("w1", wallets.sessionOf(player).orElseThrow().walletId());

		assertEquals("w1", wallets.close(player).orElseThrow().walletId());
		assertTrue(wallets.close(player).isEmpty());
		assertFalse(wallets.isLocked("w1"));
		assertTrue(wallets.tryOpen(other, "w1", 8));
	}

	@Test
	void sessionRejectsInvalidHotbarSlots() {
		UUID player = UUID.fromString("11111111-1111-1111-1111-111111111111");
		assertThrows(NullPointerException.class, () -> new Wallet.Session(null, "w", 0));
		assertThrows(NullPointerException.class, () -> new Wallet.Session(player, null, 0));
		assertThrows(IllegalArgumentException.class, () -> new Wallet.Session(player, " ", 0));
		assertThrows(IllegalArgumentException.class, () -> new Wallet.Session(player, "w", -1));
		assertThrows(IllegalArgumentException.class, () -> new Wallet.Session(player, "w", 9));
	}

	@Test
	void unusedWalletMaterialConstantStillLoads() {
		assertEquals(Material.BOOK, euroCash().walletMaterial());
	}
}
