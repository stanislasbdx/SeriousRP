package fr.stan1712.wetston.seriousrp.economy;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BankListenerTest extends ConfigBackedTest {

	@Mock Plugin plugin;
	@Mock Player player;
	@Mock PlayerInventory playerInventory;
	@Mock Inventory gui;
	@Mock Block block;
	@Mock Sign sign;
	@Mock PersistentDataContainer signPdc;

	private Cash cash;
	private CassetteItems cassetteItems;
	private WalletListener walletItems;
	private BankListener listener;
	private final UUID playerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final AtomicInteger vault = new AtomicInteger(10_000);

	@BeforeEach
	void setUp() {
		when(plugin.getName()).thenReturn("SeriousRP");
		when(player.getUniqueId()).thenReturn(playerId);
		when(player.getInventory()).thenReturn(playerInventory);
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Cash.Enabled", true);
		config.set("Economy.Cash.Atm.PhysicalFloat", true);
		config.set("Economy.Cash.Atm.CassetteCapacity", 1000);
		config.set("Economy.Cash.Bank.VaultCapacity", 5000);
		config.set("Economy.Cash.Bank.VaultPlayer", "Banque");
		config.set("Economy.Cash.Denominations", List.of(
			Map.of("value", 1, "material", "GOLD_NUGGET"),
			Map.of("value", 10, "material", "PAPER")
		));
		when(plugin.getConfig()).thenReturn(config);
		cash = Cash.fromConfig(config);
		walletItems = new WalletListener(plugin, cash, new Wallet(), recipe -> {}, (h, s, t) -> gui);
		cassetteItems = new CassetteItems(plugin, cash, recipe -> {}, key -> {});
		listener = new BankListener(
			plugin,
			cash,
			cassetteItems,
			walletItems,
			null,
			(name, amount) -> {
				if (vault.get() < amount) {
					return false;
				}
				vault.addAndGet(-amount);
				return true;
			},
			Runnable::run,
			(h, s, t) -> gui
		);
		when(block.getState()).thenReturn(sign);
		when(sign.getPersistentDataContainer()).thenReturn(signPdc);
		when(gui.getSize()).thenReturn(BankListener.GUI_SIZE);
	}

	@Test
	void createRequiresPermission() {
		SignChangeEvent denied = mock(SignChangeEvent.class);
		when(denied.getLine(0)).thenReturn("[sBank]");
		when(denied.getPlayer()).thenReturn(player);
		when(player.hasPermission(Atm.BANK_CREATE_PERM)).thenReturn(false);
		listener.onSignChange(denied);
		verify(denied).setCancelled(true);

		SignChangeEvent allowed = mock(SignChangeEvent.class);
		when(allowed.getLine(0)).thenReturn("[sBank]");
		when(allowed.getPlayer()).thenReturn(player);
		when(allowed.getBlock()).thenReturn(block);
		when(player.hasPermission(Atm.BANK_CREATE_PERM)).thenReturn(true);
		listener.onSignChange(allowed);
		verify(allowed).setLine(0, "§2§l[sBank]");
		verify(signPdc).set(any(NamespacedKey.class), eq(PersistentDataType.STRING), eq(playerId.toString()));
	}

	@Test
	void createBlockedWhenCassetteModeOff() {
		plugin.getConfig().set("Economy.Cash.Atm.PhysicalFloat", false);
		SignChangeEvent event = mock(SignChangeEvent.class);
		when(event.getLine(0)).thenReturn("[sBank]");
		when(event.getPlayer()).thenReturn(player);
		listener.onSignChange(event);
		verify(event).setCancelled(true);
		verify(player).sendMessage(contains("PhysicalFloat"));
		verify(event, never()).setLine(anyInt(), any());
	}

	@Test
	void accessDeniedWithoutPermission() {
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(playerId.toString());
		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		when(event.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
		when(event.getClickedBlock()).thenReturn(block);
		when(event.getPlayer()).thenReturn(player);
		when(player.hasPermission(Atm.BANK_ACCESS_PERM)).thenReturn(false);
		listener.onInteract(event);
		verify(event).setCancelled(true);
		verify(player).sendMessage(contains("cannot access"));
	}

	@Test
	void bypassOpensGuiAndPinFlowWorks() {
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(playerId.toString());
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(0);
		Location location = mock(Location.class);
		when(block.getLocation()).thenReturn(location);
		when(location.getBlock()).thenReturn(block);
		when(player.hasPermission(Atm.BANK_ACCESS_PERM)).thenReturn(true);
		when(player.hasPermission(Atm.BANK_BYPASS_PERM)).thenReturn(true);

		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		when(event.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
		when(event.getClickedBlock()).thenReturn(block);
		when(event.getPlayer()).thenReturn(player);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.onInteract(event);
		}
		verify(player).openInventory(gui);

		when(player.hasPermission(Atm.BANK_BYPASS_PERM)).thenReturn(false);
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING)))
			.thenReturn(playerId.toString(), (String) null);
		listener.onInteract(event);
		verify(player).sendMessage(contains("4-digit"));

		BankListener.PinPrompt set = new BankListener.PinPrompt(
			playerId,
			location,
			BankListener.PinMode.SET,
			System.currentTimeMillis() + 5_000
		);
		listener.handlePin(player, set, "12a4");
		verify(player).sendMessage(contains("Invalid"));
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.handlePin(player, set, "1234");
		}
		verify(signPdc).set(any(NamespacedKey.class), eq(PersistentDataType.STRING), eq("1234"));
	}

	@Test
	void feedEmptyAndFillCassette() {
		Location location = mock(Location.class);
		when(location.getBlock()).thenReturn(block);
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(0);

		listener.feed(player, location, 100, false);
		assertEquals(9_900, vault.get());
		verify(signPdc).set(any(NamespacedKey.class), eq(PersistentDataType.INTEGER), eq(100));

		ItemStack hand = mock(ItemStack.class);
		ItemMeta meta = mock(ItemMeta.class);
		PersistentDataContainer pdc = mock(PersistentDataContainer.class);
		when(playerInventory.getItemInMainHand()).thenReturn(hand);
		when(hand.getType()).thenReturn(Material.CHEST);
		when(hand.getItemMeta()).thenReturn(meta);
		when(meta.getPersistentDataContainer()).thenReturn(pdc);
		Cassette.Payload empty = Cassette.empty();
		when(pdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(Cassette.toJson(empty));

		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(100);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.fillCassette(player, location);
		}
		verify(player).sendMessage(contains("filled"));
		verify(signPdc).set(any(NamespacedKey.class), eq(PersistentDataType.INTEGER), eq(0));

		Cassette.Payload filled = Cassette.addValue(empty, 50, cash.descendingValues(), 1000).orElseThrow();
		when(pdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(Cassette.toJson(filled));
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.emptyCassette(player);
		}
		verify(player).sendMessage(contains("emptied"));
	}
}
