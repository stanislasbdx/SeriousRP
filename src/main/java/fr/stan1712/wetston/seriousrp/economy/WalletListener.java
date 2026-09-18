package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public final class WalletListener implements Listener {
	static final String RECIPE_KEY = "srp_wallet";
	static final String SLOT_PERM_27 = "seriousrp.economy.wallet.slots.27";
	static final String SLOT_PERM_18 = "seriousrp.economy.wallet.slots.18";
	static final String SLOT_PERM_9 = "seriousrp.economy.wallet.slots.9";
	static final String NOT_CASH_MESSAGE = "Economy.Cash.Wallet.NotCash";

	private final Cash cash;
	private final Wallet wallets;
	private final NamespacedKey cashKey;
	private final NamespacedKey walletKey;
	private final NamespacedKey recipeKey;
	private final Consumer<ShapedRecipe> recipeSink;
	private final InventoryFactory inventoryFactory;

	@FunctionalInterface
	interface InventoryFactory {
		Inventory create(InventoryHolder holder, int size, String title);
	}

	public WalletListener(Plugin plugin, Cash cash, Wallet wallets) {
		this(plugin, cash, wallets, Bukkit::addRecipe, WalletListener::createInventory);
	}

	WalletListener(
		Plugin plugin,
		Cash cash,
		Wallet wallets,
		Consumer<ShapedRecipe> recipeSink,
		InventoryFactory inventoryFactory
	) {
		this.cash = cash;
		this.wallets = wallets;
		this.cashKey = new NamespacedKey(plugin, Cash.ITEM_PDC_KEY);
		this.walletKey = new NamespacedKey(plugin, Cash.WALLET_PDC_KEY);
		this.recipeKey = new NamespacedKey(plugin, RECIPE_KEY);
		this.recipeSink = recipeSink;
		this.inventoryFactory = inventoryFactory;
	}

	static Inventory createInventory(InventoryHolder holder, int size, String title) {
		return Bukkit.createInventory(holder, size, title);
	}

	public void registerRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(recipeKey, createWalletItem(Wallet.create(cash.walletSlots())));
		recipe.shape(cash.recipeShape().toArray(String[]::new));
		cash.recipeIngredients().forEach(recipe::setIngredient);
		recipeSink.accept(recipe);
	}

	static int slotsFor(Player player, Cash cash) {
		if (player.hasPermission(SLOT_PERM_27)) {
			return 27;
		}
		if (player.hasPermission(SLOT_PERM_18)) {
			return 18;
		}
		if (player.hasPermission(SLOT_PERM_9)) {
			return 9;
		}
		return cash.walletSlots();
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		if (!cash.isEnabled()) {
			return;
		}
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		Player player = event.getPlayer();
		Optional<Wallet.Payload> payload = readWalletItem(player.getInventory().getItemInMainHand());
		if (payload.isEmpty()) {
			return;
		}
		event.setCancelled(true);
		openWallet(player, payload.get(), player.getInventory().getHeldItemSlot());
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (!(event.getView().getTopInventory().getHolder() instanceof Holder)
			|| !(event.getWhoClicked() instanceof Player player)) {
			return;
		}
		Wallet.Session session = wallets.sessionOf(player.getUniqueId()).orElse(null);
		if (session == null) {
			event.setCancelled(true);
			return;
		}
		if (event.getClickedInventory() != null
			&& event.getClickedInventory().equals(player.getInventory())
			&& event.getSlot() == session.hotbarSlot()) {
			event.setCancelled(true);
			return;
		}
		if (event.getHotbarButton() == session.hotbarSlot()) {
			event.setCancelled(true);
			return;
		}
		if (isWalletItem(event.getCurrentItem()) || isWalletItem(event.getCursor())) {
			event.setCancelled(true);
			return;
		}
		boolean top = event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Holder;
		if (top && !isEmpty(event.getCursor()) && !isCashItem(event.getCursor())) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString(NOT_CASH_MESSAGE));
			return;
		}
		if (!top && event.isShiftClick() && !isEmpty(event.getCurrentItem()) && !isCashItem(event.getCurrentItem())) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString(NOT_CASH_MESSAGE));
		}
	}

	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		if (!(event.getView().getTopInventory().getHolder() instanceof Holder)
			|| !(event.getWhoClicked() instanceof Player player)) {
			return;
		}
		int topSize = event.getView().getTopInventory().getSize();
		boolean affectsTop = event.getRawSlots().stream().anyMatch(slot -> slot < topSize);
		if (affectsTop && !isCashItem(event.getOldCursor())) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString(NOT_CASH_MESSAGE));
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		if (!(event.getInventory().getHolder() instanceof Holder holder)
			|| !(event.getPlayer() instanceof Player player)) {
			return;
		}
		Optional<Wallet.Session> session = wallets.sessionOf(player.getUniqueId());
		if (session.isEmpty() || !session.get().walletId().equals(holder.walletId())) {
			return;
		}
		wallets.close(player.getUniqueId());
		Wallet.Payload updated = new Wallet.Payload(
			holder.walletId(),
			holder.slots(),
			collectCash(event.getInventory(), player)
		);
		ItemStack held = player.getInventory().getItem(session.get().hotbarSlot());
		Optional<Wallet.Payload> current = readWalletItem(held);
		if (current.isEmpty() || !current.get().id().equals(holder.walletId())) {
			return;
		}
		player.getInventory().setItem(session.get().hotbarSlot(), createWalletItem(updated));
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		Optional<Wallet.Session> session = wallets.sessionOf(event.getPlayer().getUniqueId());
		if (session.isEmpty()) {
			return;
		}
		Optional<Wallet.Payload> dropped = readWalletItem(event.getItemDrop().getItemStack());
		if (dropped.isPresent() && session.get().walletId().equals(dropped.get().id())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onHeld(PlayerItemHeldEvent event) {
		if (wallets.sessionOf(event.getPlayer().getUniqueId()).isEmpty()) {
			return;
		}
		event.getPlayer().closeInventory();
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		event.getPlayer().closeInventory();
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		event.getEntity().closeInventory();
	}

	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent event) {
		if (!isWalletRecipe(event.getRecipe())) {
			return;
		}
		int slots = cash.walletSlots();
		if (event.getView().getPlayer() instanceof Player player) {
			slots = slotsFor(player, cash);
		}
		event.getInventory().setResult(createWalletItem(Wallet.create(slots)));
	}

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		if (!isWalletRecipe(event.getRecipe()) || !(event.getWhoClicked() instanceof Player player)) {
			return;
		}
		if (event.isShiftClick()) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Wallet.ShiftCraft"));
			return;
		}
		event.setCurrentItem(createWalletItem(Wallet.create(slotsFor(player, cash))));
	}

	void openWallet(Player player, Wallet.Payload payload, int hotbarSlot) {
		if (!wallets.tryOpen(player.getUniqueId(), payload.id(), hotbarSlot)) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Wallet.AlreadyOpen"));
			return;
		}
		Holder holder = new Holder(payload.id(), payload.slots());
		Inventory inventory = inventoryFactory.create(holder, payload.slots(), cash.walletDisplayName());
		holder.attach(inventory);
		List<Cash.Stack> stacks = CashTender.toInventoryStacks(payload.stacks());
		for (int index = 0; index < stacks.size() && index < inventory.getSize(); index++) {
			inventory.setItem(index, createCashItem(stacks.get(index)));
		}
		player.openInventory(inventory);
	}

	boolean isWalletRecipe(Recipe recipe) {
		return recipe instanceof Keyed keyed && recipeKey.equals(keyed.getKey());
	}

	ItemStack createWalletItem(Wallet.Payload payload) {
		ItemStack item = new ItemStack(cash.walletMaterial());
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(cash.walletDisplayName());
		meta.setLore(Wallet.lore(payload, cash));
		Wallet.write(meta.getPersistentDataContainer(), walletKey, payload);
		item.setItemMeta(meta);
		return item;
	}

	ItemStack createCashItem(Cash.Stack stack) {
		Cash.Denomination denomination = cash.denomination(stack.denomination()).orElseThrow();
		ItemStack item = new ItemStack(denomination.material(), Math.min(stack.amount(), Cash.MAX_STACK_SIZE));
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(denomination.displayName());
		if (denomination.customModelData() != null) {
			meta.setCustomModelData(denomination.customModelData());
		}
		Cash.writeDenomination(meta.getPersistentDataContainer(), cashKey, stack.denomination());
		item.setItemMeta(meta);
		return item;
	}

	Optional<Wallet.Payload> readWalletItem(ItemStack item) {
		if (item == null || item.getType() != cash.walletMaterial()) {
			return Optional.empty();
		}
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return Optional.empty();
		}
		return Wallet.read(meta.getPersistentDataContainer(), walletKey);
	}

	boolean isWalletItem(ItemStack item) {
		return readWalletItem(item).isPresent();
	}

	boolean isCashItem(ItemStack item) {
		return readCashItem(item).isPresent();
	}

	Optional<Cash.Stack> readCashItem(ItemStack item) {
		if (isEmpty(item) || item.getItemMeta() == null) {
			return Optional.empty();
		}
		var denomination = Cash.readDenomination(item.getItemMeta().getPersistentDataContainer(), cashKey);
		if (denomination.isEmpty() || !cash.isDenomination(denomination.getAsInt())) {
			return Optional.empty();
		}
		return Optional.of(new Cash.Stack(denomination.getAsInt(), item.getAmount()));
	}

	private List<Cash.Stack> collectCash(Inventory inventory, Player player) {
		List<Cash.Stack> stacks = new ArrayList<>();
		for (ItemStack item : inventory.getContents()) {
			if (isEmpty(item)) {
				continue;
			}
			Optional<Cash.Stack> cashStack = readCashItem(item);
			if (cashStack.isPresent()) {
				stacks.add(cashStack.get());
			}
			else {
				player.getInventory().addItem(item);
			}
		}
		return stacks;
	}

	private static boolean isEmpty(ItemStack item) {
		return item == null || item.getType() == Material.AIR || item.getAmount() <= 0;
	}

	static final class Holder implements InventoryHolder {
		private final String walletId;
		private final int slots;
		private Inventory inventory;

		Holder(String walletId, int slots) {
			this.walletId = walletId;
			this.slots = slots;
		}

		void attach(Inventory inventory) {
			this.inventory = inventory;
		}

		String walletId() {
			return walletId;
		}

		int slots() {
			return slots;
		}

		@Override
		public Inventory getInventory() {
			return inventory;
		}
	}
}
