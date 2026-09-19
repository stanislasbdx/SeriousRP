package fr.stan1712.wetston.seriousrp.economy;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WalletListenerTest extends ConfigBackedTest {

	@Mock Plugin plugin;
	@Mock Player player;
	@Mock PlayerInventory playerInventory;
	@Mock Inventory topInventory;
	@Mock InventoryView view;
	@Mock ItemStack walletStack;
	@Mock ItemStack cashStack;
	@Mock ItemStack otherStack;
	@Mock ItemMeta walletMeta;
	@Mock ItemMeta cashMeta;
	@Mock PersistentDataContainer walletPdc;
	@Mock PersistentDataContainer cashPdc;

	private Cash cash;
	private Wallet wallets;
	private WalletListener listener;
	private Wallet.Payload payload;

	@BeforeEach
	void setUpListener() {
		when(plugin.getName()).thenReturn("SeriousRP");
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Cash.Enabled", true);
		config.set("Economy.Cash.Wallet.DefaultSlots", 9);
		config.set("Economy.Cash.Denominations", List.of(
			Map.of("value", 1, "material", "GOLD_NUGGET"),
			Map.of("value", 20, "material", "PAPER", "custom-model-data", 20)
		));
		cash = Cash.fromConfig(config);
		wallets = new Wallet();
		listener = new WalletListener(plugin, cash, wallets, recipe -> {}, (holder, size, title) -> topInventory);
		payload = new Wallet.Payload("wallet-1", 9, List.of());
		when(player.getUniqueId()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));
		when(player.getInventory()).thenReturn(playerInventory);
	}

	@Test
	void slotsForPrefersHighestPermission() {
		when(player.hasPermission(WalletListener.SLOT_PERM_27)).thenReturn(true);
		assertEquals(27, WalletListener.slotsFor(player, cash));
		when(player.hasPermission(WalletListener.SLOT_PERM_27)).thenReturn(false);
		when(player.hasPermission(WalletListener.SLOT_PERM_18)).thenReturn(true);
		assertEquals(18, WalletListener.slotsFor(player, cash));
		when(player.hasPermission(WalletListener.SLOT_PERM_18)).thenReturn(false);
		when(player.hasPermission(WalletListener.SLOT_PERM_9)).thenReturn(true);
		assertEquals(9, WalletListener.slotsFor(player, cash));
		when(player.hasPermission(WalletListener.SLOT_PERM_9)).thenReturn(false);
		assertEquals(9, WalletListener.slotsFor(player, cash));
	}

	@Test
	void interactIgnoresDisabledLeftClickAndNonWallets() {
		YamlConfiguration disabled = new YamlConfiguration();
		disabled.set("Economy.Cash.Enabled", false);
		WalletListener off = new WalletListener(plugin, Cash.fromConfig(disabled), wallets, recipe -> {}, (h, s, t) -> topInventory);
		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		off.onInteract(event);
		verify(event, never()).getAction();

		when(event.getAction()).thenReturn(Action.LEFT_CLICK_AIR);
		listener.onInteract(event);
		verify(event, never()).getPlayer();

		when(event.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
		when(event.getHand()).thenReturn(EquipmentSlot.HAND);
		when(event.getPlayer()).thenReturn(player);
		when(playerInventory.getItemInMainHand()).thenReturn(null);
		listener.onInteract(event);
		verify(event, never()).setCancelled(true);

		when(event.getHand()).thenReturn(EquipmentSlot.OFF_HAND);
		when(playerInventory.getItemInMainHand()).thenReturn(walletStack);
		stubWalletItem();
		listener.onInteract(event);
		verify(event, never()).setCancelled(true);

		when(event.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
		when(event.getHand()).thenReturn(null);
		when(playerInventory.getItemInMainHand()).thenReturn(null);
		listener.onInteract(event);
		verify(event, never()).setCancelled(true);
	}

	@Test
	void interactOpensHeldWalletAndRefusesASecondOpen() {
		stubWalletItem();
		when(playerInventory.getItemInMainHand()).thenReturn(walletStack);
		when(playerInventory.getHeldItemSlot()).thenReturn(2);
		when(topInventory.getSize()).thenReturn(9);
		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		when(event.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
		when(event.getHand()).thenReturn(EquipmentSlot.HAND);
		when(event.isCancelled()).thenReturn(true);
		when(event.getPlayer()).thenReturn(player);

		listener.onInteract(event);

		verify(event).setCancelled(true);
		verify(player).openInventory(topInventory);
		assertTrue(wallets.isLocked("wallet-1"));

		listener.onInteract(event);
		verify(player).sendMessage(contains("already open"));
	}

	@Test
	void clickRulesProtectOpenWalletAndRejectNonCash() {
		stubWalletItem();
		wallets.tryOpen(player.getUniqueId(), "wallet-1", 2);
		when(view.getTopInventory()).thenReturn(topInventory);
		when(topInventory.getHolder()).thenReturn(new WalletListener.Holder("wallet-1", 9));

		InventoryClickEvent unrelated = mock(InventoryClickEvent.class);
		when(unrelated.getView()).thenReturn(view);
		when(topInventory.getHolder()).thenReturn(null);
		listener.onClick(unrelated);
		verify(unrelated, never()).setCancelled(true);

		when(topInventory.getHolder()).thenReturn(new WalletListener.Holder("wallet-1", 9));
		InventoryClickEvent noSession = mock(InventoryClickEvent.class);
		when(noSession.getView()).thenReturn(view);
		when(noSession.getWhoClicked()).thenReturn(player);
		UUID other = UUID.fromString("22222222-2222-2222-2222-222222222222");
		when(player.getUniqueId()).thenReturn(other);
		listener.onClick(noSession);
		verify(noSession).setCancelled(true);
		when(player.getUniqueId()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));

		InventoryClickEvent hotbar = mock(InventoryClickEvent.class);
		when(hotbar.getView()).thenReturn(view);
		when(hotbar.getWhoClicked()).thenReturn(player);
		when(hotbar.getClickedInventory()).thenReturn(playerInventory);
		when(hotbar.getSlot()).thenReturn(2);
		listener.onClick(hotbar);
		verify(hotbar).setCancelled(true);

		InventoryClickEvent numberKey = mock(InventoryClickEvent.class);
		when(numberKey.getView()).thenReturn(view);
		when(numberKey.getWhoClicked()).thenReturn(player);
		when(numberKey.getClickedInventory()).thenReturn(playerInventory);
		when(numberKey.getSlot()).thenReturn(1);
		when(numberKey.getHotbarButton()).thenReturn(2);
		listener.onClick(numberKey);
		verify(numberKey).setCancelled(true);

		InventoryClickEvent walletMove = mock(InventoryClickEvent.class);
		when(walletMove.getView()).thenReturn(view);
		when(walletMove.getWhoClicked()).thenReturn(player);
		when(walletMove.getClickedInventory()).thenReturn(topInventory);
		when(walletMove.getSlot()).thenReturn(0);
		when(walletMove.getHotbarButton()).thenReturn(-1);
		when(walletMove.getCurrentItem()).thenReturn(walletStack);
		when(walletMove.getCursor()).thenReturn(null);
		listener.onClick(walletMove);
		verify(walletMove).setCancelled(true);

		when(otherStack.getType()).thenReturn(Material.STONE);
		when(otherStack.getAmount()).thenReturn(1);
		when(otherStack.getItemMeta()).thenReturn(null);
		InventoryClickEvent nonCashTop = mock(InventoryClickEvent.class);
		when(nonCashTop.getView()).thenReturn(view);
		when(nonCashTop.getWhoClicked()).thenReturn(player);
		when(nonCashTop.getClickedInventory()).thenReturn(topInventory);
		when(nonCashTop.getSlot()).thenReturn(0);
		when(nonCashTop.getHotbarButton()).thenReturn(-1);
		when(nonCashTop.getCurrentItem()).thenReturn(null);
		when(nonCashTop.getCursor()).thenReturn(otherStack);
		when(topInventory.getHolder()).thenReturn(new WalletListener.Holder("wallet-1", 9));
		listener.onClick(nonCashTop);
		verify(nonCashTop).setCancelled(true);
		verify(player).sendMessage(contains("Only cash"));

		InventoryClickEvent shiftJunk = mock(InventoryClickEvent.class);
		when(shiftJunk.getView()).thenReturn(view);
		when(shiftJunk.getWhoClicked()).thenReturn(player);
		when(shiftJunk.getClickedInventory()).thenReturn(playerInventory);
		when(shiftJunk.getSlot()).thenReturn(1);
		when(shiftJunk.getHotbarButton()).thenReturn(-1);
		when(shiftJunk.getCurrentItem()).thenReturn(otherStack);
		when(shiftJunk.getCursor()).thenReturn(null);
		when(shiftJunk.isShiftClick()).thenReturn(true);
		listener.onClick(shiftJunk);
		verify(shiftJunk).setCancelled(true);
	}

	@Test
	void dragOntoTopRejectsNonCash() {
		when(view.getTopInventory()).thenReturn(topInventory);
		when(topInventory.getHolder()).thenReturn(new WalletListener.Holder("wallet-1", 9));
		when(topInventory.getSize()).thenReturn(9);
		when(otherStack.getType()).thenReturn(Material.STONE);
		when(otherStack.getAmount()).thenReturn(1);
		when(otherStack.getItemMeta()).thenReturn(null);

		InventoryDragEvent drag = mock(InventoryDragEvent.class);
		when(drag.getView()).thenReturn(view);
		when(drag.getWhoClicked()).thenReturn(player);
		when(drag.getRawSlots()).thenReturn(new HashSet<>(List.of(0, 20)));
		when(drag.getOldCursor()).thenReturn(otherStack);
		listener.onDrag(drag);
		verify(drag).setCancelled(true);

		InventoryDragEvent bottomOnly = mock(InventoryDragEvent.class);
		when(bottomOnly.getView()).thenReturn(view);
		when(bottomOnly.getWhoClicked()).thenReturn(player);
		when(bottomOnly.getRawSlots()).thenReturn(new HashSet<>(List.of(20)));
		listener.onDrag(bottomOnly);
		verify(bottomOnly, never()).setCancelled(true);
	}

	@Test
	void closeWritesPayloadBackToHeldWallet() {
		stubWalletItem();
		wallets.tryOpen(player.getUniqueId(), "wallet-1", 2);
		when(playerInventory.getItem(2)).thenReturn(walletStack);
		when(topInventory.getHolder()).thenReturn(new WalletListener.Holder("wallet-1", 9));
		when(topInventory.getContents()).thenReturn(new ItemStack[] {cashStack, otherStack, null});
		stubCashItem(20, 3);
		when(otherStack.getType()).thenReturn(Material.STONE);
		when(otherStack.getAmount()).thenReturn(1);
		when(otherStack.getItemMeta()).thenReturn(null);

		InventoryCloseEvent event = mock(InventoryCloseEvent.class);
		when(event.getInventory()).thenReturn(topInventory);
		when(event.getPlayer()).thenReturn(player);

		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, (mock, context) -> {
			ItemMeta meta = mock(ItemMeta.class);
			PersistentDataContainer pdc = mock(PersistentDataContainer.class);
			when(mock.getItemMeta()).thenReturn(meta);
			when(meta.getPersistentDataContainer()).thenReturn(pdc);
		})) {
			listener.onClose(event);
		}

		verify(playerInventory).addItem(otherStack);
		verify(playerInventory).setItem(eq(2), any(ItemStack.class));
		assertFalse(wallets.isLocked("wallet-1"));
	}

	@Test
	void closeDoesNothingWhenWalletLeftTheHand() {
		wallets.tryOpen(player.getUniqueId(), "wallet-1", 2);
		when(topInventory.getHolder()).thenReturn(new WalletListener.Holder("wallet-1", 9));
		when(topInventory.getContents()).thenReturn(new ItemStack[0]);
		when(playerInventory.getItem(2)).thenReturn(null);
		InventoryCloseEvent event = mock(InventoryCloseEvent.class);
		when(event.getInventory()).thenReturn(topInventory);
		when(event.getPlayer()).thenReturn(player);
		listener.onClose(event);
		verify(playerInventory, never()).setItem(anyInt(), any());
	}

	@Test
	void dropHeldAndHotbarChangeCloseTheGui() {
		stubWalletItem();
		wallets.tryOpen(player.getUniqueId(), "wallet-1", 2);
		Item dropped = mock(Item.class);
		when(dropped.getItemStack()).thenReturn(walletStack);
		PlayerDropItemEvent drop = mock(PlayerDropItemEvent.class);
		when(drop.getPlayer()).thenReturn(player);
		when(drop.getItemDrop()).thenReturn(dropped);
		listener.onDrop(drop);
		verify(drop).setCancelled(true);

		PlayerItemHeldEvent held = mock(PlayerItemHeldEvent.class);
		when(held.getPlayer()).thenReturn(player);
		listener.onHeld(held);
		verify(player).closeInventory();

		PlayerQuitEvent quit = mock(PlayerQuitEvent.class);
		when(quit.getPlayer()).thenReturn(player);
		listener.onQuit(quit);
		PlayerDeathEvent death = mock(PlayerDeathEvent.class);
		when(death.getEntity()).thenReturn(player);
		listener.onDeath(death);
		verify(player, times(3)).closeInventory();
	}

	@Test
	void dropAndHeldIgnorePlayersWithoutSession() {
		PlayerDropItemEvent drop = mock(PlayerDropItemEvent.class);
		when(drop.getPlayer()).thenReturn(player);
		listener.onDrop(drop);
		verify(drop, never()).setCancelled(true);
		PlayerItemHeldEvent held = mock(PlayerItemHeldEvent.class);
		when(held.getPlayer()).thenReturn(player);
		listener.onHeld(held);
		verify(player, never()).closeInventory();
	}

	@Test
	void craftReplacesResultAndBlocksShiftClick() {
		ShapedRecipe recipe = mock(ShapedRecipe.class);
		NamespacedKey key = new NamespacedKey(plugin, WalletListener.RECIPE_KEY);
		when(recipe.getKey()).thenReturn(key);
		CraftingInventory crafting = mock(CraftingInventory.class);
		when(view.getPlayer()).thenReturn(player);

		PrepareItemCraftEvent prepare = mock(PrepareItemCraftEvent.class);
		when(prepare.getRecipe()).thenReturn(recipe);
		when(prepare.getView()).thenReturn(view);
		when(prepare.getInventory()).thenReturn(crafting);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.onPrepareCraft(prepare);
			verify(crafting).setResult(any(ItemStack.class));
		}

		CraftItemEvent shift = mock(CraftItemEvent.class);
		when(shift.getRecipe()).thenReturn(recipe);
		when(shift.getWhoClicked()).thenReturn(player);
		when(shift.isShiftClick()).thenReturn(true);
		listener.onCraft(shift);
		verify(shift).setCancelled(true);

		CraftItemEvent single = mock(CraftItemEvent.class);
		when(single.getRecipe()).thenReturn(recipe);
		when(single.getWhoClicked()).thenReturn(player);
		when(single.isShiftClick()).thenReturn(false);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.onCraft(single);
			verify(single).setCurrentItem(any(ItemStack.class));
		}
	}

	@Test
	void prepareCraftIgnoresUnknownRecipes() {
		PrepareItemCraftEvent event = mock(PrepareItemCraftEvent.class);
		when(event.getRecipe()).thenReturn(null);
		listener.onPrepareCraft(event);
		verify(event, never()).getInventory();
		assertFalse(listener.isWalletRecipe(mock(Recipe.class)));
	}

	@Test
	void createWalletItemRejectsNullMeta() {
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class,
			(mock, context) -> when(mock.getItemMeta()).thenReturn(null))) {
			assertThrows(AssertionError.class, () -> listener.createWalletItem(payload));
		}
	}

	@Test
	void createWalletItemAppliesDefaultCustomModelData() {
		ItemMeta meta = mock(ItemMeta.class);
		PersistentDataContainer pdc = mock(PersistentDataContainer.class);
		when(meta.getPersistentDataContainer()).thenReturn(pdc);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class,
			(mock, context) -> when(mock.getItemMeta()).thenReturn(meta))) {
			listener.createWalletItem(payload);
		}
		verify(meta).setCustomModelData(1);
		verify(meta).setEnchantmentGlintOverride(true);
	}

	@Test
	void createWalletItemSkipsCustomModelDataWhenDisabled() {
		YamlConfiguration disabled = new YamlConfiguration();
		disabled.set("Economy.Cash.Enabled", true);
		disabled.set("Economy.Cash.Wallet.CustomModelData", 0);
		disabled.set("Economy.Cash.Denominations", List.of(Map.of("value", 1, "material", "GOLD_NUGGET")));
		WalletListener noModel = new WalletListener(
			plugin,
			Cash.fromConfig(disabled),
			wallets,
			recipe -> {},
			(h, s, t) -> topInventory
		);
		ItemMeta meta = mock(ItemMeta.class);
		PersistentDataContainer pdc = mock(PersistentDataContainer.class);
		when(meta.getPersistentDataContainer()).thenReturn(pdc);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class,
			(mock, context) -> when(mock.getItemMeta()).thenReturn(meta))) {
			noModel.createWalletItem(payload);
		}
		verify(meta, never()).setCustomModelData(anyInt());
	}

	@Test
	void createCashItemWritesFaceValueCustomModelData() {
		ItemMeta meta = mock(ItemMeta.class);
		PersistentDataContainer pdc = mock(PersistentDataContainer.class);
		when(meta.getPersistentDataContainer()).thenReturn(pdc);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, (mock, context) -> when(mock.getItemMeta()).thenReturn(meta))) {
			listener.createCashItem(new Cash.Stack(20, 2));
			listener.createCashItem(new Cash.Stack(1, 1));
		}
		verify(meta).setCustomModelData(20);
		verify(meta).setCustomModelData(1);
		verify(meta, times(2)).setEnchantmentGlintOverride(true);
		assertThrows(java.util.NoSuchElementException.class, () -> listener.createCashItem(new Cash.Stack(50, 1)));
	}

	@Test
	void createCashItemSkipsCustomModelDataWhenDisabled() {
		YamlConfiguration disabled = new YamlConfiguration();
		disabled.set("Economy.Cash.Enabled", true);
		disabled.set("Economy.Cash.Denominations", List.of(
			Map.of("value", 1, "material", "GOLD_NUGGET", "custom-model-data", 0)
		));
		WalletListener noModel = new WalletListener(
			plugin,
			Cash.fromConfig(disabled),
			wallets,
			recipe -> {},
			(h, s, t) -> topInventory
		);
		ItemMeta meta = mock(ItemMeta.class);
		PersistentDataContainer pdc = mock(PersistentDataContainer.class);
		when(meta.getPersistentDataContainer()).thenReturn(pdc);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class,
			(mock, context) -> when(mock.getItemMeta()).thenReturn(meta))) {
			noModel.createCashItem(new Cash.Stack(1, 1));
		}
		verify(meta, never()).setCustomModelData(anyInt());
	}

	@Test
	void readHelpersRejectWrongItems() {
		when(walletStack.getType()).thenReturn(Material.STONE);
		assertTrue(listener.readWalletItem(walletStack).isEmpty());
		when(walletStack.getType()).thenReturn(cash.walletMaterial());
		when(walletStack.getItemMeta()).thenReturn(null);
		assertTrue(listener.readWalletItem(walletStack).isEmpty());
		assertTrue(listener.readCashItem(null).isEmpty());
		when(cashStack.getType()).thenReturn(Material.PAPER);
		when(cashStack.getAmount()).thenReturn(2);
		when(cashStack.getItemMeta()).thenReturn(null);
		assertTrue(listener.readCashItem(cashStack).isEmpty());
		when(cashStack.getItemMeta()).thenReturn(cashMeta);
		when(cashMeta.getPersistentDataContainer()).thenReturn(cashPdc);
		when(cashPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(null);
		assertTrue(listener.readCashItem(cashStack).isEmpty());
		when(cashPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(99);
		assertTrue(listener.readCashItem(cashStack).isEmpty());
	}

	@Test
	void registerRecipePushesConfiguredShape() {
		AtomicReference<NamespacedKey> removed = new AtomicReference<>();
		WalletListener crafting = new WalletListener(
			plugin,
			cash,
			wallets,
			recipe -> {},
			removed::set,
			(h, s, t) -> topInventory
		);
		try (MockedConstruction<ItemStack> items = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta());
			MockedConstruction<ShapedRecipe> recipes = mockConstruction(ShapedRecipe.class, (mock, context) -> {
			})) {
			crafting.registerRecipe();
			assertEquals(1, recipes.constructed().size());
			assertEquals(new NamespacedKey(plugin, WalletListener.RECIPE_KEY), removed.get());
			verify(recipes.constructed().get(0)).shape(" L ", "LPL", " L ");
			verify(recipes.constructed().get(0)).setIngredient('L', Material.LEATHER);
			verify(recipes.constructed().get(0)).setIngredient('P', Material.PAPER);
			crafting.unregisterRecipe();
			assertEquals(new NamespacedKey(plugin, WalletListener.RECIPE_KEY), removed.get());
		}
	}

	@Test
	void remainingListenerBranches() {
		org.bukkit.entity.HumanEntity human = mock(org.bukkit.entity.HumanEntity.class);
		InventoryClickEvent notPlayer = mock(InventoryClickEvent.class);
		when(notPlayer.getView()).thenReturn(view);
		when(topInventory.getHolder()).thenReturn(new WalletListener.Holder("wallet-1", 9));
		when(view.getTopInventory()).thenReturn(topInventory);
		when(notPlayer.getWhoClicked()).thenReturn(human);
		listener.onClick(notPlayer);
		verify(notPlayer, never()).setCancelled(true);

		InventoryDragEvent notHolder = mock(InventoryDragEvent.class);
		when(notHolder.getView()).thenReturn(view);
		when(topInventory.getHolder()).thenReturn(null);
		listener.onDrag(notHolder);
		verify(notHolder, never()).setCancelled(true);

		CraftItemEvent notRecipe = mock(CraftItemEvent.class);
		when(notRecipe.getRecipe()).thenReturn(null);
		listener.onCraft(notRecipe);
		verify(notRecipe, never()).setCancelled(true);

		PlayerDropItemEvent dropOther = mock(PlayerDropItemEvent.class);
		wallets.tryOpen(player.getUniqueId(), "wallet-1", 2);
		Item dropped = mock(Item.class);
		when(dropped.getItemStack()).thenReturn(otherStack);
		when(otherStack.getType()).thenReturn(Material.STONE);
		when(dropOther.getPlayer()).thenReturn(player);
		when(dropOther.getItemDrop()).thenReturn(dropped);
		listener.onDrop(dropOther);
		verify(dropOther, never()).setCancelled(true);

		when(cashStack.getType()).thenReturn(Material.AIR);
		assertTrue(listener.readCashItem(cashStack).isEmpty());
		when(cashStack.getType()).thenReturn(Material.PAPER);
		when(cashStack.getAmount()).thenReturn(0);
		assertTrue(listener.readCashItem(cashStack).isEmpty());

		WalletListener.Holder holder = new WalletListener.Holder("wallet-1", 9);
		holder.attach(topInventory);
		assertEquals(topInventory, holder.getInventory());
	}

	@Test
	void openWalletFillsCashAndPublicConstructorCreatesInventory() {
		Wallet.Payload loaded = new Wallet.Payload("wallet-1", 9, List.of(new Cash.Stack(20, 2)));
		when(topInventory.getSize()).thenReturn(9);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.openWallet(player, loaded, 1);
			verify(topInventory).setItem(eq(0), any(ItemStack.class));
		}
		wallets.close(player.getUniqueId());

		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(() -> Bukkit.createInventory(any(), anyInt(), any())).thenReturn(topInventory);
			WalletListener hooked = new WalletListener(plugin, cash, wallets);
			hooked.openWallet(player, payload, 0);
			bukkit.verify(() -> Bukkit.createInventory(any(), eq(9), any()));
		}
	}

	@Test
	void createCashItemRejectsNullMeta() {
		Cash.Stack stack = new Cash.Stack(1, 1);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class,
			(mock, context) -> when(mock.getItemMeta()).thenReturn(null))) {
			assertThrows(AssertionError.class, () -> listener.createCashItem(stack));
		}
	}

	@Test
	void closeIgnoresNonWalletInventories() {
		InventoryCloseEvent event = mock(InventoryCloseEvent.class);
		when(event.getInventory()).thenReturn(topInventory);
		when(topInventory.getHolder()).thenReturn(null);
		listener.onClose(event);
		verify(event, never()).getPlayer();
	}

	@Test
	void collectCashHandlesMismatchedWalletHolder() {
		wallets.tryOpen(player.getUniqueId(), "wallet-1", 2);
		when(topInventory.getHolder()).thenReturn(new WalletListener.Holder("other", 9));
		when(topInventory.getContents()).thenReturn(new ItemStack[0]);
		InventoryCloseEvent event = mock(InventoryCloseEvent.class);
		when(event.getInventory()).thenReturn(topInventory);
		when(event.getPlayer()).thenReturn(player);
		listener.onClose(event);
		verify(playerInventory, never()).setItem(anyInt(), any());
	}

	@Test
	void clickDragCloseAndCraftCoverRemainingGuards() {
		stubWalletItem();
		stubCashItem(20, 2);
		wallets.tryOpen(player.getUniqueId(), "wallet-1", 2);
		when(view.getTopInventory()).thenReturn(topInventory);
		when(topInventory.getHolder()).thenReturn(new WalletListener.Holder("wallet-1", 9));
		when(topInventory.getSize()).thenReturn(9);

		InventoryClickEvent nullClicked = mock(InventoryClickEvent.class);
		when(nullClicked.getView()).thenReturn(view);
		when(nullClicked.getWhoClicked()).thenReturn(player);
		when(nullClicked.getClickedInventory()).thenReturn(null);
		when(nullClicked.getHotbarButton()).thenReturn(-1);
		when(nullClicked.getCurrentItem()).thenReturn(null);
		when(nullClicked.getCursor()).thenReturn(null);
		listener.onClick(nullClicked);
		verify(nullClicked, never()).setCancelled(true);

		InventoryClickEvent cursorWallet = mock(InventoryClickEvent.class);
		when(cursorWallet.getView()).thenReturn(view);
		when(cursorWallet.getWhoClicked()).thenReturn(player);
		when(cursorWallet.getClickedInventory()).thenReturn(topInventory);
		when(cursorWallet.getHotbarButton()).thenReturn(-1);
		when(cursorWallet.getCurrentItem()).thenReturn(null);
		when(cursorWallet.getCursor()).thenReturn(walletStack);
		listener.onClick(cursorWallet);
		verify(cursorWallet).setCancelled(true);

		InventoryClickEvent cashOnTop = mock(InventoryClickEvent.class);
		when(cashOnTop.getView()).thenReturn(view);
		when(cashOnTop.getWhoClicked()).thenReturn(player);
		when(cashOnTop.getClickedInventory()).thenReturn(topInventory);
		when(cashOnTop.getHotbarButton()).thenReturn(-1);
		when(cashOnTop.getCurrentItem()).thenReturn(null);
		when(cashOnTop.getCursor()).thenReturn(cashStack);
		listener.onClick(cashOnTop);
		verify(cashOnTop, never()).setCancelled(true);

		InventoryClickEvent emptyCursorTop = mock(InventoryClickEvent.class);
		when(emptyCursorTop.getView()).thenReturn(view);
		when(emptyCursorTop.getWhoClicked()).thenReturn(player);
		when(emptyCursorTop.getClickedInventory()).thenReturn(topInventory);
		when(emptyCursorTop.getHotbarButton()).thenReturn(-1);
		when(emptyCursorTop.getCurrentItem()).thenReturn(null);
		when(emptyCursorTop.getCursor()).thenReturn(null);
		listener.onClick(emptyCursorTop);
		verify(emptyCursorTop, never()).setCancelled(true);

		when(otherStack.getType()).thenReturn(Material.STONE);
		when(otherStack.getAmount()).thenReturn(1);
		when(otherStack.getItemMeta()).thenReturn(null);
		InventoryClickEvent junkNoShift = mock(InventoryClickEvent.class);
		when(junkNoShift.getView()).thenReturn(view);
		when(junkNoShift.getWhoClicked()).thenReturn(player);
		when(junkNoShift.getClickedInventory()).thenReturn(playerInventory);
		when(junkNoShift.getSlot()).thenReturn(1);
		when(junkNoShift.getHotbarButton()).thenReturn(-1);
		when(junkNoShift.getCurrentItem()).thenReturn(otherStack);
		when(junkNoShift.getCursor()).thenReturn(null);
		when(junkNoShift.isShiftClick()).thenReturn(false);
		listener.onClick(junkNoShift);
		verify(junkNoShift, never()).setCancelled(true);

		InventoryClickEvent shiftEmpty = mock(InventoryClickEvent.class);
		when(shiftEmpty.getView()).thenReturn(view);
		when(shiftEmpty.getWhoClicked()).thenReturn(player);
		when(shiftEmpty.getClickedInventory()).thenReturn(playerInventory);
		when(shiftEmpty.getSlot()).thenReturn(1);
		when(shiftEmpty.getHotbarButton()).thenReturn(-1);
		when(shiftEmpty.getCurrentItem()).thenReturn(null);
		when(shiftEmpty.getCursor()).thenReturn(null);
		when(shiftEmpty.isShiftClick()).thenReturn(true);
		listener.onClick(shiftEmpty);
		verify(shiftEmpty, never()).setCancelled(true);

		InventoryClickEvent shiftCash = mock(InventoryClickEvent.class);
		when(shiftCash.getView()).thenReturn(view);
		when(shiftCash.getWhoClicked()).thenReturn(player);
		when(shiftCash.getClickedInventory()).thenReturn(playerInventory);
		when(shiftCash.getSlot()).thenReturn(1);
		when(shiftCash.getHotbarButton()).thenReturn(-1);
		when(shiftCash.getCurrentItem()).thenReturn(cashStack);
		when(shiftCash.getCursor()).thenReturn(null);
		when(shiftCash.isShiftClick()).thenReturn(true);
		listener.onClick(shiftCash);
		verify(shiftCash, never()).setCancelled(true);

		InventoryDragEvent cashDrag = mock(InventoryDragEvent.class);
		when(cashDrag.getView()).thenReturn(view);
		when(cashDrag.getWhoClicked()).thenReturn(player);
		when(cashDrag.getRawSlots()).thenReturn(new HashSet<>(List.of(0)));
		when(cashDrag.getOldCursor()).thenReturn(cashStack);
		listener.onDrag(cashDrag);
		verify(cashDrag, never()).setCancelled(true);

		org.bukkit.entity.HumanEntity human = mock(org.bukkit.entity.HumanEntity.class);
		InventoryDragEvent dragHuman = mock(InventoryDragEvent.class);
		when(dragHuman.getView()).thenReturn(view);
		when(dragHuman.getWhoClicked()).thenReturn(human);
		listener.onDrag(dragHuman);
		verify(dragHuman, never()).setCancelled(true);

		InventoryCloseEvent closeHuman = mock(InventoryCloseEvent.class);
		when(closeHuman.getInventory()).thenReturn(topInventory);
		when(closeHuman.getPlayer()).thenReturn(human);
		listener.onClose(closeHuman);
		verify(closeHuman, never()).getView();

		wallets.close(player.getUniqueId());
		InventoryCloseEvent closeNoSession = mock(InventoryCloseEvent.class);
		when(closeNoSession.getInventory()).thenReturn(topInventory);
		when(closeNoSession.getPlayer()).thenReturn(player);
		listener.onClose(closeNoSession);
		verify(playerInventory, never()).setItem(anyInt(), any());

		wallets.tryOpen(player.getUniqueId(), "wallet-1", 2);
		ItemStack otherWallet = mock(ItemStack.class);
		ItemMeta otherMeta = mock(ItemMeta.class);
		PersistentDataContainer otherPdc = mock(PersistentDataContainer.class);
		when(otherWallet.getType()).thenReturn(cash.walletMaterial());
		when(otherWallet.getItemMeta()).thenReturn(otherMeta);
		when(otherMeta.getPersistentDataContainer()).thenReturn(otherPdc);
		when(otherPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING)))
			.thenReturn(Wallet.toJson(new Wallet.Payload("other-id", 9, List.of())));
		when(playerInventory.getItem(2)).thenReturn(otherWallet);
		when(topInventory.getContents()).thenReturn(new ItemStack[0]);
		InventoryCloseEvent wrongHand = mock(InventoryCloseEvent.class);
		when(wrongHand.getInventory()).thenReturn(topInventory);
		when(wrongHand.getPlayer()).thenReturn(player);
		listener.onClose(wrongHand);
		verify(playerInventory, never()).setItem(eq(2), any());

		wallets.tryOpen(player.getUniqueId(), "wallet-1", 2);
		Item dropped = mock(Item.class);
		when(dropped.getItemStack()).thenReturn(otherWallet);
		PlayerDropItemEvent dropOtherId = mock(PlayerDropItemEvent.class);
		when(dropOtherId.getPlayer()).thenReturn(player);
		when(dropOtherId.getItemDrop()).thenReturn(dropped);
		listener.onDrop(dropOtherId);
		verify(dropOtherId, never()).setCancelled(true);

		ShapedRecipe recipe = mock(ShapedRecipe.class);
		NamespacedKey key = new NamespacedKey(plugin, WalletListener.RECIPE_KEY);
		when(recipe.getKey()).thenReturn(key);
		when(view.getPlayer()).thenReturn(human);
		CraftingInventory crafting = mock(CraftingInventory.class);
		PrepareItemCraftEvent prepareHuman = mock(PrepareItemCraftEvent.class);
		when(prepareHuman.getRecipe()).thenReturn(recipe);
		when(prepareHuman.getView()).thenReturn(view);
		when(prepareHuman.getInventory()).thenReturn(crafting);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.onPrepareCraft(prepareHuman);
			verify(crafting).setResult(any(ItemStack.class));
		}

		CraftItemEvent craftHuman = mock(CraftItemEvent.class);
		when(craftHuman.getRecipe()).thenReturn(recipe);
		when(craftHuman.getWhoClicked()).thenReturn(human);
		listener.onCraft(craftHuman);
		verify(craftHuman, never()).setCancelled(true);

		ShapedRecipe otherRecipe = mock(ShapedRecipe.class);
		NamespacedKey otherKey = new NamespacedKey(plugin, "other");
		when(otherRecipe.getKey()).thenReturn(otherKey);
		assertFalse(listener.isWalletRecipe(otherRecipe));

		wallets.close(player.getUniqueId());
		when(topInventory.getSize()).thenReturn(9);
		Wallet.Payload oversized = new Wallet.Payload("big", 9, List.of(new Cash.Stack(1, 577)));
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			listener.openWallet(player, oversized, 1);
			verify(topInventory).setItem(eq(8), any(ItemStack.class));
			verify(topInventory, never()).setItem(eq(9), any(ItemStack.class));
		}
	}

	private void stubWalletItem() {
		when(walletStack.getType()).thenReturn(cash.walletMaterial());
		when(walletStack.getItemMeta()).thenReturn(walletMeta);
		when(walletMeta.getPersistentDataContainer()).thenReturn(walletPdc);
		when(walletPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(Wallet.toJson(payload));
	}

	private void stubCashItem(int denomination, int amount) {
		when(cashStack.getType()).thenReturn(Material.PAPER);
		when(cashStack.getAmount()).thenReturn(amount);
		when(cashStack.getItemMeta()).thenReturn(cashMeta);
		when(cashMeta.getPersistentDataContainer()).thenReturn(cashPdc);
		when(cashPdc.get(any(NamespacedKey.class), eq(PersistentDataType.INTEGER))).thenReturn(denomination);
	}
}
