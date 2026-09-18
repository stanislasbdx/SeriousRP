package fr.stan1712.wetston.seriousrp.economy;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import fr.stan1712.wetston.seriousrp.Main;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
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
		config.set("Economy.Cash.Atm.CreateCost", 0);
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
		when(gui.getSize()).thenReturn(AtmListener.GUI_SIZE);
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
		verify(allowed).setLine(0, "§2§l[sATM]");
		verify(signPdc).set(any(NamespacedKey.class), eq(PersistentDataType.STRING), eq(playerId.toString()));
		verify(sign).update();
		assertEquals(100, account.get());
	}

	@Test
	void signCreateChargesConfiguredBankCost() {
		YamlConfiguration pricedConfig = new YamlConfiguration();
		pricedConfig.set("Economy.Cash.Enabled", true);
		pricedConfig.set("Economy.Cash.Atm.CreateCost", 150);
		pricedConfig.set("Economy.Cash.Denominations", List.of(
			Map.of("value", 1, "material", "GOLD_NUGGET"),
			Map.of("value", 10, "material", "PAPER")
		));
		AtmListener priced = new AtmListener(
			plugin,
			Cash.fromConfig(pricedConfig),
			walletItems,
			bank,
			Runnable::run,
			(h, s, t) -> gui
		);
		when(player.hasPermission(Atm.CREATE_PERM)).thenReturn(true);

		account.set(149);
		SignChangeEvent tooPoor = mock(SignChangeEvent.class);
		when(tooPoor.getLine(0)).thenReturn("[sATM]");
		when(tooPoor.getPlayer()).thenReturn(player);
		priced.onSignChange(tooPoor);
		verify(tooPoor).setCancelled(true);
		verify(tooPoor, never()).setLine(anyInt(), any());
		verify(player).sendMessage(contains("150"));
		assertEquals(149, account.get());

		account.set(200);
		SignChangeEvent paid = mock(SignChangeEvent.class);
		when(paid.getLine(0)).thenReturn("[sATM]");
		when(paid.getPlayer()).thenReturn(player);
		when(paid.getBlock()).thenReturn(block);
		priced.onSignChange(paid);
		verify(paid).setLine(0, "§2§l[sATM]");
		verify(player).sendMessage(contains("created for 150"));
		assertEquals(50, account.get());
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
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
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
		when(player.getOpenInventory()).thenReturn(view);
		when(gui.getSize()).thenReturn(AtmListener.GUI_SIZE);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.onClick(deposit);
		}
		assertEquals(110, account.get());
		verify(gui, atLeastOnce()).setItem(eq(AtmListener.ACCOUNT_SLOT), any());
		verify(gui, atLeastOnce()).setItem(eq(AtmListener.POCKET_SLOT), any());

		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn("WITHDRAW");
		InventoryClickEvent withdraw = mock(InventoryClickEvent.class);
		when(withdraw.getView()).thenReturn(view);
		when(withdraw.getWhoClicked()).thenReturn(player);
		when(withdraw.getCurrentItem()).thenReturn(button);
		when(playerInventory.getStorageContents()).thenReturn(new ItemStack[] {null, null});
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.onClick(withdraw);
		}
		assertEquals(100, account.get());

		when(gui.getHolder()).thenReturn(null);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.runOperation(player, Atm.Operation.WITHDRAW, 10, false);
		}
		when(gui.getHolder()).thenReturn(new AtmListener.Holder(location));

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

		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
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
		org.bukkit.block.BlockState otherState = mock(org.bukkit.block.BlockState.class);
		when(block.getState()).thenReturn(otherState);
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
		when(economy.withdrawPlayer(player, 4)).thenReturn(new EconomyResponse(4, 4, EconomyResponse.ResponseType.FAILURE, "no"));
		when(economy.depositPlayer(player, 5)).thenReturn(new EconomyResponse(5, 8, EconomyResponse.ResponseType.FAILURE, "no"));
		assertEquals(8D, vault.balance(player));
		assertTrue(vault.withdraw(player, 2));
		assertTrue(vault.deposit(player, 3));
		assertFalse(vault.withdraw(player, 4));
		assertFalse(vault.deposit(player, 5));
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

		Server server = mock(Server.class);
		BukkitScheduler scheduler = mock(BukkitScheduler.class);
		when(plugin.getServer()).thenReturn(server);
		when(server.getScheduler()).thenReturn(scheduler);
		when(scheduler.runTask(eq(plugin), any(Runnable.class))).thenAnswer(invocation -> {
			invocation.getArgument(1, Runnable.class).run();
			return mock(BukkitTask.class);
		});
		AtmListener live = new AtmListener(plugin, cash, walletItems);
		when(player.hasPermission(Atm.CREATE_PERM)).thenReturn(true);
		BlockState plain = mock(BlockState.class);
		Block notTile = mock(Block.class);
		when(notTile.getState()).thenReturn(plain);
		SignChangeEvent stamp = mock(SignChangeEvent.class);
		when(stamp.getLine(0)).thenReturn("[sATM]");
		when(stamp.getPlayer()).thenReturn(player);
		when(stamp.getBlock()).thenReturn(notTile);
		live.onSignChange(stamp);
		verify(scheduler).runTask(eq(plugin), any(Runnable.class));

		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(() -> Bukkit.createInventory(any(), anyInt(), any())).thenReturn(gui);
			Location location = mock(Location.class);
			assertEquals(gui, AtmListener.createInventory(new AtmListener.Holder(location), 27, "ATM"));
		}
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

		ItemStack air = mock(ItemStack.class);
		when(air.getType()).thenReturn(Material.AIR);
		ItemStack[] airy = new ItemStack[36];
		java.util.Arrays.fill(airy, air);
		when(playerInventory.getStorageContents()).thenReturn(airy);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			assertTrue(listener.withdraw(player, 10));
		}
		assertEquals(90, account.get());
	}

	@Test
	void breakInteractAndClickIgnoreUnrelatedTargets() {
		BlockBreakEvent notAtm = mock(BlockBreakEvent.class);
		when(notAtm.getBlock()).thenReturn(block);
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(null);
		listener.onBreak(notAtm);
		verify(notAtm, never()).getPlayer();

		PlayerInteractEvent missingBlock = mock(PlayerInteractEvent.class);
		when(missingBlock.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
		when(missingBlock.getClickedBlock()).thenReturn(null);
		listener.onInteract(missingBlock);
		verify(missingBlock, never()).setCancelled(true);

		YamlConfiguration disabled = new YamlConfiguration();
		disabled.set("Economy.Cash.Enabled", false);
		AtmListener off = new AtmListener(plugin, Cash.fromConfig(disabled), walletItems, bank, Runnable::run, (h, s, t) -> gui);
		PlayerInteractEvent disabledClick = mock(PlayerInteractEvent.class);
		when(disabledClick.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
		when(disabledClick.getClickedBlock()).thenReturn(block);
		off.onInteract(disabledClick);
		verify(disabledClick, never()).setCancelled(true);

		PlayerInteractEvent notAtmClick = mock(PlayerInteractEvent.class);
		when(notAtmClick.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
		when(notAtmClick.getClickedBlock()).thenReturn(block);
		listener.onInteract(notAtmClick);
		verify(notAtmClick, never()).setCancelled(true);

		when(view.getTopInventory()).thenReturn(gui);
		when(gui.getHolder()).thenReturn(null);
		InventoryClickEvent noHolder = mock(InventoryClickEvent.class);
		when(noHolder.getView()).thenReturn(view);
		listener.onClick(noHolder);
		verify(noHolder, never()).setCancelled(true);

		when(gui.getHolder()).thenReturn(new AtmListener.Holder(mock(Location.class)));
		org.bukkit.entity.HumanEntity human = mock(org.bukkit.entity.HumanEntity.class);
		InventoryClickEvent notPlayer = mock(InventoryClickEvent.class);
		when(notPlayer.getView()).thenReturn(view);
		when(notPlayer.getWhoClicked()).thenReturn(human);
		listener.onClick(notPlayer);
		verify(notPlayer, never()).setCancelled(true);

		InventoryClickEvent emptyClick = mock(InventoryClickEvent.class);
		when(emptyClick.getView()).thenReturn(view);
		when(emptyClick.getWhoClicked()).thenReturn(player);
		when(emptyClick.getCurrentItem()).thenReturn(null);
		listener.onClick(emptyClick);
		verify(emptyClick).setCancelled(true);

		ItemStack noMeta = mock(ItemStack.class);
		when(noMeta.getItemMeta()).thenReturn(null);
		InventoryClickEvent metaClick = mock(InventoryClickEvent.class);
		when(metaClick.getView()).thenReturn(view);
		when(metaClick.getWhoClicked()).thenReturn(player);
		when(metaClick.getCurrentItem()).thenReturn(noMeta);
		listener.onClick(metaClick);

		when(button.getItemMeta()).thenReturn(buttonMeta);
		when(buttonMeta.getPersistentDataContainer()).thenReturn(buttonPdc);
		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(null);
		InventoryClickEvent noOp = mock(InventoryClickEvent.class);
		when(noOp.getView()).thenReturn(view);
		when(noOp.getWhoClicked()).thenReturn(player);
		when(noOp.getCurrentItem()).thenReturn(button);
		listener.onClick(noOp);
	}

	@Test
	void proximitySkipsNullWorldAndNonAtmBlocks() {
		Location origin = mock(Location.class);
		when(player.getLocation()).thenReturn(origin);
		when(origin.getWorld()).thenReturn(null);
		listener.tickProximity(List.of(player));
		verify(origin, never()).getBlock();

		org.bukkit.World world = mock(org.bukkit.World.class);
		when(origin.getWorld()).thenReturn(world);
		when(origin.getBlock()).thenReturn(block);
		Block other = mock(Block.class);
		BlockState plain = mock(BlockState.class);
		when(other.getState()).thenReturn(plain);
		when(block.getRelative(anyInt(), anyInt(), anyInt())).thenReturn(other);
		when(block.getRelative(0, 0, 0)).thenReturn(block);
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(playerId.toString());
		when(block.getLocation()).thenReturn(origin);
		listener.tickProximity(List.of(player));
		verify(player, org.mockito.Mockito.atLeastOnce()).sendSignChange(eq(origin), any(String[].class));
	}

	@Test
	void promptReopensGuiWhenLastAtmIsStillValid() {
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(playerId.toString());
		Location location = mock(Location.class);
		when(location.getBlock()).thenReturn(block);
		when(view.getTopInventory()).thenReturn(gui);
		when(gui.getHolder()).thenReturn(new AtmListener.Holder(location));
		when(button.getItemMeta()).thenReturn(buttonMeta);
		when(buttonMeta.getPersistentDataContainer()).thenReturn(buttonPdc);
		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn("DEPOSIT");
		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(1);
		InventoryClickEvent custom = mock(InventoryClickEvent.class);
		when(custom.getView()).thenReturn(view);
		when(custom.getWhoClicked()).thenReturn(player);
		when(custom.getCurrentItem()).thenReturn(button);
		listener.onClick(custom);

		ItemStack cashItem = cashItem(10, 10);
		when(playerInventory.getStorageContents()).thenReturn(new ItemStack[] {cashItem, null});
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.handlePrompt(player, new Atm.Prompt(playerId, Atm.Operation.DEPOSIT, System.currentTimeMillis() + 5_000), "10");
			verify(player).openInventory(gui);
		}

		Location stale = mock(Location.class);
		when(stale.getBlock()).thenReturn(block);
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(null);
		when(view.getTopInventory()).thenReturn(gui);
		when(gui.getHolder()).thenReturn(new AtmListener.Holder(stale));
		when(buttonPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(1);
		InventoryClickEvent customAgain = mock(InventoryClickEvent.class);
		when(customAgain.getView()).thenReturn(view);
		when(customAgain.getWhoClicked()).thenReturn(player);
		when(customAgain.getCurrentItem()).thenReturn(button);
		listener.onClick(customAgain);
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(null);
		when(playerInventory.getStorageContents()).thenReturn(new ItemStack[36]);
		listener.handlePrompt(player, new Atm.Prompt(playerId, Atm.Operation.DEPOSIT, System.currentTimeMillis() + 5_000), "10");
	}

	@Test
	void depositWithdrawCoverBankSpaceAndWalletLedger() {
		ItemStack cashItem = cashItem(10, 10);
		ItemStack air = mock(ItemStack.class);
		when(air.getType()).thenReturn(Material.AIR);
		when(playerInventory.getStorageContents()).thenReturn(new ItemStack[] {cashItem, air});
		AtmListener.Bank refusing = new AtmListener.Bank() {
			@Override
			public double balance(Player ignored) {
				return 100;
			}

			@Override
			public boolean withdraw(Player ignored, int amount) {
				return false;
			}

			@Override
			public boolean deposit(Player ignored, int amount) {
				return false;
			}
		};
		AtmListener failing = new AtmListener(plugin, cash, walletItems, refusing, Runnable::run, (h, s, t) -> gui);
		assertFalse(failing.deposit(player, 10));
		assertFalse(failing.withdraw(player, 10));
		when(playerInventory.getStorageContents()).thenReturn(new ItemStack[36]);
		assertFalse(listener.deposit(player, 10));

		YamlConfiguration tens = new YamlConfiguration();
		tens.set("Economy.Cash.Enabled", true);
		tens.set("Economy.Cash.Denominations", List.of(Map.of("value", 10, "material", "PAPER")));
		AtmListener noChange = new AtmListener(
			plugin,
			Cash.fromConfig(tens),
			walletItems,
			bank,
			Runnable::run,
			(h, s, t) -> gui
		);
		assertFalse(noChange.withdraw(player, 15));

		ItemStack blocker = mock(ItemStack.class);
		when(blocker.getType()).thenReturn(Material.STONE);
		ItemStack packed = cashItem(1, 128);
		ItemStack[] tight = new ItemStack[36];
		java.util.Arrays.fill(tight, blocker);
		tight[0] = packed;
		when(playerInventory.getStorageContents()).thenReturn(tight);
		assertFalse(listener.deposit(player, 1));

		account.set(0);
		listener.runOperation(player, Atm.Operation.WITHDRAW, 10, false);
		verify(player).sendMessage(contains("account"));
		account.set(100);

		ItemStack wallet = walletHolding(10, 10);
		when(playerInventory.getStorageContents()).thenReturn(new ItemStack[] {wallet, null});
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			assertTrue(listener.deposit(player, 10));
			verify(playerInventory).setItem(eq(0), any(ItemStack.class));
		}

		when(view.getTopInventory()).thenReturn(gui);
		when(gui.getHolder()).thenReturn(new AtmListener.Holder(mock(Location.class)));
		when(button.getItemMeta()).thenReturn(buttonMeta);
		when(buttonMeta.getPersistentDataContainer()).thenReturn(buttonPdc);
		NamespacedKey op = new NamespacedKey(plugin, AtmListener.OP_KEY);
		NamespacedKey all = new NamespacedKey(plugin, AtmListener.ALL_KEY);
		NamespacedKey custom = new NamespacedKey(plugin, AtmListener.CUSTOM_KEY);
		NamespacedKey amount = new NamespacedKey(plugin, AtmListener.AMOUNT_KEY);
		when(buttonPdc.get(op, PersistentDataType.STRING)).thenReturn("WITHDRAW");
		when(buttonPdc.get(all, PersistentDataType.INTEGER)).thenReturn(1);
		when(buttonPdc.get(custom, PersistentDataType.INTEGER)).thenReturn(null);
		when(buttonPdc.get(amount, PersistentDataType.INTEGER)).thenReturn(null);
		InventoryClickEvent allOut = mock(InventoryClickEvent.class);
		when(allOut.getView()).thenReturn(view);
		when(allOut.getWhoClicked()).thenReturn(player);
		when(allOut.getCurrentItem()).thenReturn(button);
		ItemStack[] packedInv = new ItemStack[36];
		java.util.Arrays.fill(packedInv, blocker);
		when(playerInventory.getStorageContents()).thenReturn(packedInv);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.onClick(allOut);
		}
		verify(player).sendMessage(contains("full"));
	}

	@Test
	void openGuiRejectsNullItemMeta() {
		when(signPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(playerId.toString());
		when(player.hasPermission(Atm.USE_PERM)).thenReturn(true);
		Location guiLocation = mock(Location.class);
		when(block.getLocation()).thenReturn(guiLocation);
		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		when(event.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
		when(event.getClickedBlock()).thenReturn(block);
		when(event.getPlayer()).thenReturn(player);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class,
			(item, context) -> when(item.getItemMeta()).thenReturn(null))) {
			assertThrows(AssertionError.class, () -> listener.onInteract(event));
		}

		java.util.concurrent.atomic.AtomicInteger created = new java.util.concurrent.atomic.AtomicInteger();
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, (item, context) -> {
			if (created.getAndIncrement() < 2) {
				ItemMeta meta = mock(ItemMeta.class);
				PersistentDataContainer pdc = mock(PersistentDataContainer.class);
				when(item.getItemMeta()).thenReturn(meta);
				when(meta.getPersistentDataContainer()).thenReturn(pdc);
			}
			else {
				when(item.getItemMeta()).thenReturn(null);
			}
		})) {
			assertThrows(AssertionError.class, () -> listener.onInteract(event));
		}

		java.util.concurrent.atomic.AtomicInteger buttons = new java.util.concurrent.atomic.AtomicInteger();
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, (item, context) -> {
			if (buttons.getAndIncrement() < 4) {
				ItemMeta meta = mock(ItemMeta.class);
				PersistentDataContainer pdc = mock(PersistentDataContainer.class);
				when(item.getItemMeta()).thenReturn(meta);
				when(meta.getPersistentDataContainer()).thenReturn(pdc);
			}
			else {
				when(item.getItemMeta()).thenReturn(null);
			}
		})) {
			assertThrows(AssertionError.class, () -> listener.onInteract(event));
		}
	}

	@Test
	void stacksForWalletFallsBackWhenIndexIsOutOfRange() {
		CashTender.Ledger empty = new CashTender.Ledger(List.of(), List.of());
		assertEquals(List.of(), AtmListener.stacksForWallet(empty, 0));

		List<Cash.Stack> wallet = List.of(new Cash.Stack(10, 2));
		CashTender.Ledger oneWallet = new CashTender.Ledger(List.of(), List.of(wallet));
		assertEquals(wallet, AtmListener.stacksForWallet(oneWallet, 0));
		assertEquals(List.of(), AtmListener.stacksForWallet(oneWallet, 1));
	}

	@Test
	void holderStoresLocation() {
		Location location = mock(Location.class);
		AtmListener.Holder holder = new AtmListener.Holder(location);
		assertEquals(location, holder.location());
		assertThrows(UnsupportedOperationException.class, holder::getInventory);
	}

	private ItemStack cashItem(int denomination, int amount) {
		ItemStack cashItem = mock(ItemStack.class);
		ItemMeta cashMeta = mock(ItemMeta.class);
		PersistentDataContainer cashPdc = mock(PersistentDataContainer.class);
		when(cashItem.getType()).thenReturn(Material.PAPER);
		when(cashItem.getAmount()).thenReturn(amount);
		when(cashItem.getItemMeta()).thenReturn(cashMeta);
		when(cashMeta.getPersistentDataContainer()).thenReturn(cashPdc);
		when(cashPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(denomination);
		return cashItem;
	}

	private ItemStack walletHolding(int denomination, int amount) {
		ItemStack wallet = mock(ItemStack.class);
		ItemMeta meta = mock(ItemMeta.class);
		PersistentDataContainer pdc = mock(PersistentDataContainer.class);
		when(wallet.getType()).thenReturn(Material.BOOK);
		when(wallet.getItemMeta()).thenReturn(meta);
		when(meta.getPersistentDataContainer()).thenReturn(pdc);
		Wallet.Payload payload = new Wallet.Payload("w-atm", 9, List.of(new Cash.Stack(denomination, amount)));
		when(pdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(Wallet.toJson(payload));
		return wallet;
	}
}
