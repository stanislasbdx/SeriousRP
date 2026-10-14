package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public final class BankListener implements Listener {
	static final String OP_KEY = "srp-bank-op";
	static final String AMOUNT_KEY = "srp-bank-amount";
	static final String ALL_KEY = "srp-bank-all";
	static final int GUI_SIZE = 27;
	static final int STOCK_SLOT = 4;
	static final int CASSETTE_SLOT = 13;
	static final int EMPTY_SLOT = 15;
	static final int FILL_START = 19;

	enum Op {
		EMPTY,
		FILL
	}

	enum PinMode {
		SET,
		ASK
	}

	record PinPrompt(UUID playerId, Location location, PinMode mode, long expiresAtMillis) {
		boolean expired(long now) {
			return now >= expiresAtMillis;
		}
	}

	record Holder(Location location) implements InventoryHolder {
		@Override
		public Inventory getInventory() {
			throw new UnsupportedOperationException("Bank holder is a GUI marker");
		}
	}

	interface InventoryFactory {
		Inventory create(InventoryHolder holder, int size, String title);
	}

	interface MainThread {
		void run(Runnable task);
	}

	private final Plugin plugin;
	private final Cash cash;
	private final CassetteItems cassetteItems;
	private final WalletListener walletItems;
	private final AtmLogger logger;
	private final NamespacedKey ownerKey;
	private final NamespacedKey stockKey;
	private final NamespacedKey pinKey;
	private final NamespacedKey opKey;
	private final NamespacedKey amountKey;
	private final NamespacedKey allKey;
	private final MainThread mainThread;
	private final InventoryFactory inventories;
	private final Map<UUID, PinPrompt> prompts = new ConcurrentHashMap<>();
	private final Map<String, Long> breakConfirmUntil = new ConcurrentHashMap<>();

	public BankListener(Plugin plugin, Cash cash, CassetteItems cassetteItems, WalletListener walletItems, AtmLogger logger) {
		this(
			plugin,
			cash,
			cassetteItems,
			walletItems,
			logger,
			task -> plugin.getServer().getScheduler().runTask(plugin, task),
			AtmListener::createInventory
		);
	}

	BankListener(
		Plugin plugin,
		Cash cash,
		CassetteItems cassetteItems,
		WalletListener walletItems,
		AtmLogger logger,
		MainThread mainThread,
		InventoryFactory inventories
	) {
		this.plugin = plugin;
		this.cash = cash;
		this.cassetteItems = cassetteItems;
		this.walletItems = walletItems;
		this.logger = logger;
		this.ownerKey = new NamespacedKey(plugin, Cash.BANK_OWNER_PDC_KEY);
		this.stockKey = new NamespacedKey(plugin, Cash.BANK_STOCK_PDC_KEY);
		this.pinKey = new NamespacedKey(plugin, Cash.BANK_PIN_PDC_KEY);
		this.opKey = new NamespacedKey(plugin, OP_KEY);
		this.amountKey = new NamespacedKey(plugin, AMOUNT_KEY);
		this.allKey = new NamespacedKey(plugin, ALL_KEY);
		this.mainThread = mainThread;
		this.inventories = inventories;
	}

	boolean advancedBank() {
		if (plugin.getConfig().contains("Economy.Cash.AdvancedBank")) {
			return plugin.getConfig().getBoolean("Economy.Cash.AdvancedBank");
		}
		if (plugin.getConfig().contains("Economy.Cash.Atm.PhysicalFloat")) {
			return plugin.getConfig().getBoolean("Economy.Cash.Atm.PhysicalFloat");
		}
		return cash.advancedBank();
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (!cash.isEnabled() || !cash.isBankHeader(event.getLine(0))) {
			return;
		}
		Player player = event.getPlayer();
		if (!advancedBank()) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.NeedAdvancedBank"));
			return;
		}
		if (!player.hasPermission(Atm.BANK_CREATE_PERM)) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.DeniedCreate"));
			return;
		}
		Cash.PlacedSign sign = cash.bankSign();
		event.setLine(0, sign.title());
		event.setLine(1, sign.separator());
		event.setLine(2, sign.subtitle());
		event.setLine(3, sign.hint());
		UUID owner = player.getUniqueId();
		mainThread.run(() -> stampOwner(event.getBlock(), owner));
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.Created"));
	}

	@EventHandler(ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		Optional<UUID> owner = bankOwner(event.getBlock());
		if (owner.isEmpty()) {
			return;
		}
		Player player = event.getPlayer();
		if (!Atm.canBreak(
			owner.get(),
			player.getUniqueId(),
			player.hasPermission(Atm.BANK_DELETE_PERM),
			player.hasPermission(Atm.BANK_DELETE_OTHERS_PERM)
		)) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.DeniedBreak"));
			return;
		}
		String confirmKey = player.getUniqueId() + ":" + AtmListener.blockKey(event.getBlock().getLocation());
		long now = System.currentTimeMillis();
		Long until = breakConfirmUntil.get(confirmKey);
		if (until == null || until < now) {
			event.setCancelled(true);
			breakConfirmUntil.put(confirmKey, now + Atm.BREAK_CONFIRM_MILLIS);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.BreakConfirm"));
			return;
		}
		breakConfirmUntil.remove(confirmKey);
		dropStock(event.getBlock(), event.getBlock().getLocation());
		log("BREAK_BANK", player.getName() + " " + AtmListener.blockKey(event.getBlock().getLocation()));
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		if (!cash.isEnabled() || !advancedBank() || event.getAction() != Action.RIGHT_CLICK_BLOCK
			|| event.getClickedBlock() == null) {
			return;
		}
		if (bankOwner(event.getClickedBlock()).isEmpty()) {
			return;
		}
		Player player = event.getPlayer();
		if (!player.hasPermission(Atm.BANK_ACCESS_PERM)) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.DeniedAccess"));
			event.setCancelled(true);
			return;
		}
		event.setCancelled(true);
		Location location = event.getClickedBlock().getLocation();
		if (player.hasPermission(Atm.BANK_BYPASS_PERM)) {
			openGui(player, location);
			log("BANK_OPEN", player.getName() + " bypass");
			return;
		}
		String pin = readPin(event.getClickedBlock());
		if (BankVault.needsPinSetup(pin)) {
			prompts.put(player.getUniqueId(), new PinPrompt(
				player.getUniqueId(),
				location,
				PinMode.SET,
				System.currentTimeMillis() + Atm.PROMPT_TIMEOUT_MILLIS
			));
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.PinSet"));
			return;
		}
		prompts.put(player.getUniqueId(), new PinPrompt(
			player.getUniqueId(),
			location,
			PinMode.ASK,
			System.currentTimeMillis() + Atm.PROMPT_TIMEOUT_MILLIS
		));
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.PinAsk"));
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		PinPrompt prompt = prompts.remove(event.getPlayer().getUniqueId());
		if (prompt == null) {
			return;
		}
		event.setCancelled(true);
		Player player = event.getPlayer();
		String message = event.getMessage();
		mainThread.run(() -> handlePin(player, prompt, message));
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (!(event.getView().getTopInventory().getHolder() instanceof Holder holder)
			|| !(event.getWhoClicked() instanceof Player player)) {
			return;
		}
		if (handleCassetteClick(event, player, holder)) {
			return;
		}
		event.setCancelled(true);
		if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
			return;
		}
		ItemStack current = event.getCurrentItem();
		if (current == null || current.getItemMeta() == null) {
			return;
		}
		String opRaw = current.getItemMeta().getPersistentDataContainer().get(opKey, PersistentDataType.STRING);
		if (opRaw == null) {
			return;
		}
		Op op = Op.valueOf(opRaw);
		Inventory top = event.getView().getTopInventory();
		Optional<Cassette.Payload> held = cassetteItems.read(top.getItem(CASSETTE_SLOT));
		if (held.isEmpty()) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.NoCassette"));
			return;
		}
		if (op == Op.EMPTY) {
			emptyIntoBank(player, holder.location(), held.get())
				.ifPresent(next -> top.setItem(CASSETTE_SLOT, cassetteItems.create(next)));
		}
		else {
			boolean all = Integer.valueOf(1).equals(
				current.getItemMeta().getPersistentDataContainer().get(allKey, PersistentDataType.INTEGER)
			);
			Integer amount = current.getItemMeta().getPersistentDataContainer().get(amountKey, PersistentDataType.INTEGER);
			fillFromBank(player, holder.location(), held.get(), amount, all)
				.ifPresent(next -> top.setItem(CASSETTE_SLOT, cassetteItems.create(next)));
		}
		refresh(player, holder.location());
	}

	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		if (!(event.getView().getTopInventory().getHolder() instanceof Holder)
			|| !(event.getWhoClicked() instanceof Player player)) {
			return;
		}
		int size = event.getView().getTopInventory().getSize();
		boolean touchesTop = false;
		for (int raw : event.getRawSlots()) {
			if (raw >= size) {
				continue;
			}
			touchesTop = true;
			if (raw != CASSETTE_SLOT) {
				event.setCancelled(true);
				return;
			}
		}
		if (!touchesTop) {
			return;
		}
		if (cassetteItems.read(event.getOldCursor()).isEmpty()) {
			event.setCancelled(true);
			return;
		}
		Location location = ((Holder) event.getView().getTopInventory().getHolder()).location();
		mainThread.run(() -> refresh(player, location));
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player player) || !(event.getInventory().getHolder() instanceof Holder holder)) {
			return;
		}
		ItemStack item = event.getInventory().getItem(CASSETTE_SLOT);
		if (cassetteItems.read(item).isEmpty()) {
			return;
		}
		event.getInventory().setItem(CASSETTE_SLOT, null);
		Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
		Location location = holder.location();
		if (location != null && location.getWorld() != null) {
			leftover.values().forEach(extra -> location.getWorld().dropItemNaturally(location, extra));
		}
	}

	void handlePin(Player player, PinPrompt prompt, String message) {
		if (prompt.expired(System.currentTimeMillis()) || Atm.isCancelPrompt(message)) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.PinCancel"));
			return;
		}
		if (!Cassette.isPin(message.trim())) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.PinInvalid"));
			return;
		}
		Block block = prompt.location().getBlock();
		if (prompt.mode() == PinMode.SET) {
			writePin(block, message.trim());
			openGui(player, prompt.location());
			log("BANK_OPEN", player.getName() + " pin-set");
			return;
		}
		if (!BankVault.matchesPin(readPin(block), message.trim())) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.PinInvalid"));
			return;
		}
		openGui(player, prompt.location());
		log("BANK_OPEN", player.getName() + " pin-ok");
	}

	Optional<Cassette.Payload> emptyIntoBank(Player player, Location location, Cassette.Payload payload) {
		if (payload == null || payload.total() <= 0) {
			return Optional.empty();
		}
		int stock = readStock(location.getBlock());
		int room = cash.bankVaultCapacity() - stock;
		int move = Math.min(payload.total(), room);
		if (move <= 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.StockFull"));
			return Optional.empty();
		}
		Optional<Cassette.Payload> left = Cassette.takeValue(payload, move, cash.descendingValues());
		if (left.isEmpty()) {
			return Optional.empty();
		}
		int nextStock = stock + move;
		writeStock(location.getBlock(), nextStock);
		log("CASSETTE_EMPTY", player.getName()
			+ " cassette=#" + Cassette.shortId(payload.id())
			+ " amount=" + move
			+ " stock=" + nextStock);
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.Emptied"));
		return left;
	}

	Optional<Cassette.Payload> fillFromBank(
		Player player,
		Location location,
		Cassette.Payload payload,
		Integer requested,
		boolean all
	) {
		if (payload == null) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.NoCassette"));
			return Optional.empty();
		}
		int stock = readStock(location.getBlock());
		if (stock <= 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.NoStock"));
			return Optional.empty();
		}
		int room = cash.cassetteCapacity() - payload.total();
		if (room <= 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.CassetteFull"));
			return Optional.empty();
		}
		int want = all ? room : (requested == null ? 0 : requested);
		int put = Math.min(want, Math.min(room, stock));
		if (put <= 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.NoStock"));
			return Optional.empty();
		}
		Optional<Cassette.Payload> updated = Cassette.addValue(
			payload,
			put,
			cash.descendingValues(),
			cash.cassetteCapacity()
		);
		OptionalInt nextStock = BankVault.take(stock, put);
		if (updated.isEmpty() || nextStock.isEmpty()) {
			return Optional.empty();
		}
		writeStock(location.getBlock(), nextStock.getAsInt());
		log("CASSETTE_FILL", player.getName()
			+ " cassette=#" + Cassette.shortId(updated.get().id())
			+ " amount=" + put
			+ " stock=" + nextStock.getAsInt());
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.Filled"));
		return updated;
	}

	private boolean handleCassetteClick(InventoryClickEvent event, Player player, Holder holder) {
		int raw = event.getRawSlot();
		int size = event.getView().getTopInventory().getSize();
		boolean slot = raw == CASSETTE_SLOT;
		boolean shiftIn = event.isShiftClick()
			&& raw >= size
			&& cassetteItems.read(event.getCurrentItem()).isPresent();
		if (!slot && !shiftIn) {
			return false;
		}
		if (!cassetteClickAllowed(event, player)) {
			event.setCancelled(true);
			return true;
		}
		event.setCancelled(false);
		mainThread.run(() -> refresh(player, holder.location()));
		return true;
	}

	private boolean cassetteClickAllowed(InventoryClickEvent event, Player player) {
		ClickType click = event.getClick();
		if (click == ClickType.DOUBLE_CLICK
			|| click == ClickType.CREATIVE
			|| click == ClickType.UNKNOWN
			|| click == ClickType.DROP
			|| click == ClickType.CONTROL_DROP
			|| click == ClickType.SWAP_OFFHAND
			|| click == ClickType.WINDOW_BORDER_LEFT
			|| click == ClickType.WINDOW_BORDER_RIGHT) {
			return false;
		}
		ItemStack current = event.getCurrentItem();
		if (click == ClickType.NUMBER_KEY) {
			int hotbar = event.getHotbarButton();
			ItemStack swapped = hotbar < 0 ? null : player.getInventory().getItem(hotbar);
			return cassetteOrAir(current) && cassetteOrAir(swapped);
		}
		if (event.isShiftClick()) {
			return cassetteItems.read(current).isPresent();
		}
		return cassetteOrAir(event.getCursor()) && cassetteOrAir(current);
	}

	private boolean cassetteOrAir(ItemStack item) {
		if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
			return true;
		}
		return cassetteItems.read(item).isPresent();
	}

	private void openGui(Player player, Location location) {
		Holder holder = new Holder(location);
		Inventory inventory = inventories.create(holder, GUI_SIZE, getConfigString("Economy.Cash.Bank.Title"));
		fillGui(inventory, location);
		player.openInventory(inventory);
	}

	private void refresh(Player player, Location location) {
		if (player.getOpenInventory().getTopInventory().getHolder() instanceof Holder) {
			fillGui(player.getOpenInventory().getTopInventory(), location);
		}
	}

	private void fillGui(Inventory inventory, Location location) {
		int stock = readStock(location.getBlock());
		ItemStack sitting = inventory.getItem(CASSETTE_SLOT);
		Optional<Cassette.Payload> payload = cassetteItems.read(sitting);
		if (payload.isEmpty()) {
			sitting = null;
		}
		ItemStack edge = pane(Material.BLACK_STAINED_GLASS_PANE);
		for (int slot = 0; slot < inventory.getSize(); slot++) {
			inventory.setItem(slot, edge);
		}
		inventory.setItem(CASSETTE_SLOT, sitting);
		boolean hasCassette = payload.isPresent();
		boolean hasCash = hasCassette && payload.get().total() > 0;
		boolean roomInBank = stock < cash.bankVaultCapacity();
		boolean roomInCassette = hasCassette && payload.get().total() < cash.cassetteCapacity();
		inventory.setItem(STOCK_SLOT, info(Material.GOLD_BLOCK, cash.applyAmount(
			getConfigString("Economy.Cash.Bank.Stock"), stock), stock > 0));
		inventory.setItem(EMPTY_SLOT, action(Material.HOPPER, Op.EMPTY, null, false, hasCash && roomInBank,
			getConfigString("Economy.Cash.Bank.EmptyIntoBank")));
		int slot = FILL_START;
		boolean canFill = hasCassette && stock > 0 && roomInCassette;
		for (int preset : cash.atmPresets()) {
			if (slot >= inventory.getSize() - 1) {
				break;
			}
			inventory.setItem(slot++, action(Material.EMERALD, Op.FILL, preset, false, canFill,
				cash.applyAmount(getConfigString("Economy.Cash.Bank.Fill"), preset)));
		}
		if (slot < inventory.getSize()) {
			inventory.setItem(slot, action(Material.SUNFLOWER, Op.FILL, null, true, canFill,
				getConfigString("Economy.Cash.Bank.FillAll")));
		}
	}

	private ItemStack action(Material material, Op op, Integer amount, boolean all, boolean glow, String name) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(name);
		if (glow) {
			Cash.applyGlow(meta);
		}
		meta.getPersistentDataContainer().set(opKey, PersistentDataType.STRING, op.name());
		if (amount != null) {
			meta.getPersistentDataContainer().set(amountKey, PersistentDataType.INTEGER, amount);
		}
		if (all) {
			meta.getPersistentDataContainer().set(allKey, PersistentDataType.INTEGER, 1);
		}
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack info(Material material, String name, boolean glow) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(name);
		if (glow) {
			Cash.applyGlow(meta);
		}
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack pane(Material material) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(" ");
		meta.setHideTooltip(true);
		item.setItemMeta(meta);
		return item;
	}

	private void stampOwner(Block block, UUID owner) {
		if (!(block.getState() instanceof TileState state)) {
			return;
		}
		state.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, owner.toString());
		state.getPersistentDataContainer().set(stockKey, PersistentDataType.INTEGER, 0);
		state.update();
	}

	Optional<UUID> bankOwner(Block block) {
		if (!(block.getState() instanceof Sign sign)) {
			return Optional.empty();
		}
		String stored = sign.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
		if (stored == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(UUID.fromString(stored));
		}
		catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	int readStock(Block block) {
		if (!(block.getState() instanceof TileState state)) {
			return 0;
		}
		Integer stock = state.getPersistentDataContainer().get(stockKey, PersistentDataType.INTEGER);
		return stock == null ? 0 : Math.max(0, stock);
	}

	void writeStock(Block block, int stock) {
		if (!(block.getState() instanceof TileState state)) {
			return;
		}
		state.getPersistentDataContainer().set(stockKey, PersistentDataType.INTEGER, Math.max(0, stock));
		state.update();
	}

	String readPin(Block block) {
		if (!(block.getState() instanceof TileState state)) {
			return null;
		}
		return state.getPersistentDataContainer().get(pinKey, PersistentDataType.STRING);
	}

	void writePin(Block block, String pin) {
		if (!(block.getState() instanceof TileState state)) {
			return;
		}
		state.getPersistentDataContainer().set(pinKey, PersistentDataType.STRING, pin);
		state.update();
	}

	private void dropStock(Block block, Location location) {
		int stock = readStock(block);
		if (stock <= 0 || location.getWorld() == null) {
			return;
		}
		CashTender.greedy(stock, cash.descendingValues()).ifPresent(stacks -> {
			for (Cash.Stack stack : CashTender.toInventoryStacks(stacks)) {
				location.getWorld().dropItemNaturally(location, walletItems.createCashItem(stack));
			}
		});
	}

	private void log(String action, String details) {
		if (logger != null) {
			logger.log(action, details);
		}
	}
}
