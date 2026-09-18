package fr.stan1712.wetston.seriousrp.economy;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import fr.stan1712.wetston.seriousrp.Main;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class AtmListenerTest extends ConfigBackedTest {

	@Mock Plugin plugin;
	@Mock Player player;
	@Mock PlayerInventory playerInventory;
	@Mock Inventory gui;
	@Mock InventoryView view;
	@Mock Block block;
	@Mock Sign sign;
	@Mock PersistentDataContainer signPdc;
	@Mock ItemStack button;
	@Mock ItemMeta buttonMeta;
	@Mock PersistentDataContainer buttonPdc;

	private Cash cash;
	private WalletListener walletItems;
	private AtmListener.Bank bank;
	private AtmListener listener;
	private final UUID playerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final AtomicInteger account = new AtomicInteger(100);

	@BeforeEach
	void setUp() {
		when(plugin.getName()).thenReturn("SeriousRP");
		when(player.getUniqueId()).thenReturn(playerId);
		when(player.getInventory()).thenReturn(playerInventory);
		when(playerInventory.getStorageContents()).thenReturn(new ItemStack[36]);
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Cash.Enabled", true);
		config.set("Economy.Cash.Atm.ViewRadius", 1);
		config.set("Economy.Cash.Denominations", List.of(
			Map.of("value", 1, "material", "GOLD_NUGGET"),
			Map.of("value", 10, "material", "PAPER")
		));
		cash = Cash.fromConfig(config);
		walletItems = new WalletListener(plugin, cash, new Wallet(), recipe -> {}, (h, s, t) -> gui);
		bank = new AtmListener.Bank() {
			@Override
			public double balance(Player ignored) {
				return account.get();
			}

			@Override
			public boolean withdraw(Player ignored, int amount) {
				if (account.get() < amount) {
					return false;
				}
				account.addAndGet(-amount);
				return true;
			}

			@Override
			public boolean deposit(Player ignored, int amount) {
				account.addAndGet(amount);
				return true;
			}
		};
		listener = new AtmListener(plugin, cash, walletItems, bank, Runnable::run, (h, s, t) -> gui);
		when(block.getState()).thenReturn(sign);
		when(sign.getPersistentDataContainer()).thenReturn(signPdc);
	}

	@Test
	void signCreateRequiresPermissionAndStampsOwner() {
		SignChangeEvent denied = mock(SignChangeEvent.class);
		when(denied.getLine(0)).thenReturn("[sATM]");
		when(denied.getPlayer()).thenReturn(player);
		when(player.hasPermission(Atm.CREATE_PERM)).thenReturn(false);
		listener.onSignChange(denied);
		verify(denied).setCancelled(true);

		SignChangeEvent allowed = mock(SignChangeEvent.class);
		when(allowed.getLine(0)).thenReturn("[sATM]");
		when(allowed.getPlayer()).thenReturn(player);
		when(allowed.getBlock()).thenReturn(block);
		when(player.hasPermission(Atm.CREATE_PERM)).thenReturn(true);
		listener.onSignChange(allowed);
		verify(allowed).setLine(0, "[sATM]");
		verify(signPdc).set(any(NamespacedKey.class), eq(PersistentDataType.STRING), eq(playerId.toString()));
		verify(sign).update();
	}

	@Test
	void signChangeIgnoresOtherLines() {
		SignChangeEvent event = mock(SignChangeEvent.class);
		when(event.getLine(0)).thenReturn("[shop]");
		listener.onSignChange(event);
		verify(event, never()).getPlayer();
	}

	@Test
	void breakRequiresOwnerPermissions() {
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(playerId.toString());
		BlockBreakEvent event = mock(BlockBreakEvent.class);
		when(event.getBlock()).thenReturn(block);
		when(event.getPlayer()).thenReturn(player);
		when(player.hasPermission(Atm.DELETE_PERM)).thenReturn(false);
		when(player.hasPermission(Atm.DELETE_OTHERS_PERM)).thenReturn(false);
		listener.onBreak(event);
		verify(event).setCancelled(true);

		when(player.hasPermission(Atm.DELETE_PERM)).thenReturn(true);
		BlockBreakEvent allowed = mock(BlockBreakEvent.class);
		when(allowed.getBlock()).thenReturn(block);
		when(allowed.getPlayer()).thenReturn(player);
		listener.onBreak(allowed);
		verify(allowed, never()).setCancelled(true);
	}

	@Test
	void interactOpensGuiForUsers() {
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(playerId.toString());
		when(player.hasPermission(Atm.USE_PERM)).thenReturn(true);
		Location location = mock(Location.class);
		when(block.getLocation()).thenReturn(location);
		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		when(event.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
		when(event.getClickedBlock()).thenReturn(block);
		when(event.getPlayer()).thenReturn(player);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, (mock, context) -> {
			ItemMeta meta = mock(ItemMeta.class);
			when(mock.getItemMeta()).thenReturn(meta);
			when(meta.getPersistentDataContainer()).thenReturn(mock(PersistentDataContainer.class));
		})) {
			listener.onInteract(event);
		}
		verify(event).setCancelled(true);
		verify(player).openInventory(gui);
	}

	@Test
	void clickRunsDepositWithdrawAndCustomPrompt() {
		Location location = mock(Location.class);
		when(view.getTopInventory()).thenReturn(gui);
		when(gui.getHolder()).thenReturn(new AtmListener.Holder(location));
		when(button.getItemMeta()).thenReturn(buttonMeta);
		when(buttonMeta.getPersistentDataContainer()).thenReturn(buttonPdc);

		InventoryClickEvent deposit = mock(InventoryClickEvent.class);
		when(deposit.getView()).thenReturn(view);
		when(deposit.getWhoClicked()).thenReturn(player);
		when(deposit.getCurrentItem()).thenReturn(button);
		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn("DEPOSIT");
		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(null);
		listener.onClick(deposit);
		verify(deposit).setCancelled(true);
		verify(player).sendMessage(contains("cash"));

		ItemStack cashItem = mock(ItemStack.class);
		ItemMeta cashMeta = mock(ItemMeta.class);
		PersistentDataContainer cashPdc = mock(PersistentDataContainer.class);
		when(cashItem.getType()).thenReturn(Material.PAPER);
		when(cashItem.getAmount()).thenReturn(10);
		when(cashItem.getItemMeta()).thenReturn(cashMeta);
		when(cashMeta.getPersistentDataContainer()).thenReturn(cashPdc);
		when(cashPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(10);
		when(playerInventory.getStorageContents()).thenReturn(new ItemStack[] {cashItem, null});
		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(10);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, (mock, context) -> {
			ItemMeta meta = mock(ItemMeta.class);
			when(mock.getItemMeta()).thenReturn(meta);
			when(meta.getPersistentDataContainer()).thenReturn(mock(PersistentDataContainer.class));
		})) {
			listener.onClick(deposit);
		}
		assertEquals(110, account.get());

		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn("WITHDRAW");
		InventoryClickEvent withdraw = mock(InventoryClickEvent.class);
		when(withdraw.getView()).thenReturn(view);
		when(withdraw.getWhoClicked()).thenReturn(player);
		when(withdraw.getCurrentItem()).thenReturn(button);
		when(playerInventory.getStorageContents()).thenReturn(new ItemStack[] {null, null});
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, (mock, context) -> {
			ItemMeta meta = mock(ItemMeta.class);
			when(mock.getItemMeta()).thenReturn(meta);
			when(meta.getPersistentDataContainer()).thenReturn(mock(PersistentDataContainer.class));
		})) {
			listener.onClick(withdraw);
		}
		assertEquals(100, account.get());

		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn("DEPOSIT");
		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(1);
		InventoryClickEvent custom = mock(InventoryClickEvent.class);
		when(custom.getView()).thenReturn(view);
		when(custom.getWhoClicked()).thenReturn(player);
		when(custom.getCurrentItem()).thenReturn(button);
		listener.onClick(custom);
		verify(player).closeInventory();
	}

	@Test
	void chatPromptDepositsTypedAmount() {
		ItemStack cashItem = mock(ItemStack.class);
		ItemMeta cashMeta = mock(ItemMeta.class);
		PersistentDataContainer cashPdc = mock(PersistentDataContainer.class);
		when(cashItem.getType()).thenReturn(Material.PAPER);
		when(cashItem.getAmount()).thenReturn(10);
		when(cashItem.getItemMeta()).thenReturn(cashMeta);
		when(cashMeta.getPersistentDataContainer()).thenReturn(cashPdc);
		when(cashPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(10);
		when(playerInventory.getStorageContents()).thenReturn(new ItemStack[] {cashItem, null});

		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, (mock, context) -> {
			ItemMeta meta = mock(ItemMeta.class);
			when(mock.getItemMeta()).thenReturn(meta);
			when(meta.getPersistentDataContainer()).thenReturn(mock(PersistentDataContainer.class));
		})) {
			listener.handlePrompt(player, new Atm.Prompt(playerId, Atm.Operation.DEPOSIT, System.currentTimeMillis() + 5_000), "10");
		}
		assertEquals(110, account.get());

		listener.handlePrompt(player, new Atm.Prompt(playerId, Atm.Operation.DEPOSIT, 0L), "10");
		verify(player).sendMessage(contains("Cancelled"));
		listener.handlePrompt(player, new Atm.Prompt(playerId, Atm.Operation.DEPOSIT, System.currentTimeMillis() + 5_000), "cancel");
		listener.handlePrompt(player, new Atm.Prompt(playerId, Atm.Operation.DEPOSIT, System.currentTimeMillis() + 5_000), "nope");
		verify(player).sendMessage(contains("Invalid"));

		AsyncPlayerChatEvent chat = mock(AsyncPlayerChatEvent.class);
		when(chat.getPlayer()).thenReturn(player);
		listener.onChat(chat);
		verify(chat, never()).setCancelled(true);
	}

	@Test
	void tickProximitySendsPersonalizedLines() {
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(playerId.toString());
		Location origin = mock(Location.class);
		org.bukkit.World world = mock(org.bukkit.World.class);
		when(player.getLocation()).thenReturn(origin);
		when(origin.getWorld()).thenReturn(world);
		when(origin.getBlock()).thenReturn(block);
		when(block.getRelative(anyInt(), anyInt(), anyInt())).thenReturn(block);
		when(block.getLocation()).thenReturn(origin);
		listener.tickProximity(List.of(player));
		verify(player, org.mockito.Mockito.atLeastOnce()).sendSignChange(eq(origin), any(String[].class));
	}

	@Test
	void atmOwnerAndVaultBankEdgeCases() {
		when(block.getState()).thenReturn(mock(org.bukkit.block.BlockState.class));
		assertTrue(listener.atmOwner(block).isEmpty());
		when(block.getState()).thenReturn(sign);
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn("nope");
		assertTrue(listener.atmOwner(block).isEmpty());
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(null);
		assertTrue(listener.atmOwner(block).isEmpty());

		Main.economy = null;
		AtmListener.Bank vault = AtmListener.vaultBank();
		assertEquals(0D, vault.balance(player));
		assertFalse(vault.withdraw(player, 1));
		assertFalse(vault.deposit(player, 1));
		Economy economy = mock(Economy.class);
		Main.economy = economy;
		when(economy.getBalance(player)).thenReturn(8D);
		when(economy.withdrawPlayer(player, 2)).thenReturn(new EconomyResponse(2, 6, EconomyResponse.ResponseType.SUCCESS, ""));
		when(economy.depositPlayer(player, 3)).thenReturn(new EconomyResponse(3, 9, EconomyResponse.ResponseType.SUCCESS, ""));
		assertEquals(8D, vault.balance(player));
		assertTrue(vault.withdraw(player, 2));
		assertTrue(vault.deposit(player, 3));
		Main.economy = null;
	}

	@Test
	void publicConstructorAndDisabledModule() {
		YamlConfiguration disabled = new YamlConfiguration();
		disabled.set("Economy.Cash.Enabled", false);
		AtmListener off = new AtmListener(
			plugin,
			Cash.fromConfig(disabled),
			walletItems,
			bank,
			Runnable::run,
			(h, s, t) -> gui
		);
		SignChangeEvent event = mock(SignChangeEvent.class);
		off.onSignChange(event);
		verify(event, never()).getLine(anyInt());

		new AtmListener(plugin, cash, walletItems);
	}

	@Test
	void interactAndChatIgnoreUnrelatedCases() {
		PlayerInteractEvent left = mock(PlayerInteractEvent.class);
		when(left.getAction()).thenReturn(Action.LEFT_CLICK_BLOCK);
		listener.onInteract(left);
		verify(left, never()).setCancelled(true);

		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(playerId.toString());
		PlayerInteractEvent noPerm = mock(PlayerInteractEvent.class);
		when(noPerm.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
		when(noPerm.getClickedBlock()).thenReturn(block);
		when(noPerm.getPlayer()).thenReturn(player);
		when(player.hasPermission(Atm.USE_PERM)).thenReturn(false);
		listener.onInteract(noPerm);
		verify(noPerm, never()).setCancelled(true);

		AsyncPlayerChatEvent chat = mock(AsyncPlayerChatEvent.class);
		when(chat.getPlayer()).thenReturn(player);
		when(chat.getMessage()).thenReturn("cancel");
		listener.handlePrompt(player, new Atm.Prompt(playerId, Atm.Operation.WITHDRAW, System.currentTimeMillis() + 1000), "cancel");
		// seed a prompt via custom click then chat
		Location location = mock(Location.class);
		when(view.getTopInventory()).thenReturn(gui);
		when(gui.getHolder()).thenReturn(new AtmListener.Holder(location));
		when(button.getItemMeta()).thenReturn(buttonMeta);
		when(buttonMeta.getPersistentDataContainer()).thenReturn(buttonPdc);
		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn("WITHDRAW");
		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(1);
		InventoryClickEvent custom = mock(InventoryClickEvent.class);
		when(custom.getView()).thenReturn(view);
		when(custom.getWhoClicked()).thenReturn(player);
		when(custom.getCurrentItem()).thenReturn(button);
		listener.onClick(custom);
		when(chat.getPlayer()).thenReturn(player);
		when(chat.getMessage()).thenReturn("cancel");
		listener.onChat(chat);
		verify(chat).setCancelled(true);
	}

	@Test
	void withdrawFailsWhenInventoryIsFull() {
		account.set(100);
		ItemStack blocker = mock(ItemStack.class);
		when(blocker.getType()).thenReturn(Material.STONE);
		ItemStack[] full = new ItemStack[36];
		java.util.Arrays.fill(full, blocker);
		when(playerInventory.getStorageContents()).thenReturn(full);
		assertFalse(listener.withdraw(player, 10));
		assertEquals(100, account.get());
	}

	@Test
	void holderStoresLocation() {
		Location location = mock(Location.class);
		AtmListener.Holder holder = new AtmListener.Holder(location);
		assertEquals(location, holder.location());
		assertEquals(null, holder.getInventory());
	}
}
