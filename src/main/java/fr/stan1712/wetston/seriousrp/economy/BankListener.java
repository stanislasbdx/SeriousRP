package fr.stan1712.wetston.seriousrp.economy;

import fr.stan1712.wetston.seriousrp.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;
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
	static final int FEED_START = 10;
	static final int EMPTY_SLOT = 21;
	static final int FILL_SLOT = 23;

	enum Op {
		FEED,
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

	interface NamedVault {
		boolean withdraw(String name, int amount);
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
	private final NamedVault namedVault;
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
			vaultNamed(),
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
		NamedVault namedVault,
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
		this.namedVault = namedVault;
		this.mainThread = mainThread;
		this.inventories = inventories;
	}

	boolean cassetteMode() {
		return plugin.getConfig().getBoolean("Economy.Cash.Atm.PhysicalFloat", cash.physicalFloat());
	}

	static NamedVault vaultNamed() {
		return (name, amount) -> {
			Economy economy = Main.economy;
			if (economy == null || name == null || amount <= 0) {
				return false;
			}
			OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
			return economy.withdrawPlayer(offline, amount).transactionSuccess();
		};
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (!cash.isEnabled() || !cash.isBankHeader(event.getLine(0))) {
			return;
		}
		Player player = event.getPlayer();
		if (!cassetteMode()) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Atm.NeedCassetteMode"));
			return;
		}
		if (!player.hasPermission(Atm.BANK_CREATE_PERM)) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.DeniedCreate"));
			return;
		}
		event.setLine(0, Cash.colorize("&2&l[sBank]"));
		event.setLine(1, Cash.colorize("&8&m              "));
		event.setLine(2, Cash.colorize("&7Coffre banque"));
		event.setLine(3, Cash.colorize("&8» &fClic droit"));
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
		if (!cash.isEnabled() || !cassetteMode() || event.getAction() != Action.RIGHT_CLICK_BLOCK
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
		event.setCancelled(true);
		ItemStack current = event.getCurrentItem();
		if (current == null || current.getItemMeta() == null) {
			return;
		}
		String opRaw = current.getItemMeta().getPersistentDataContainer().get(opKey, PersistentDataType.STRING);
		if (opRaw == null) {
			return;
		}
		Op op = Op.valueOf(opRaw);
		if (op == Op.EMPTY) {
			emptyCassette(player);
			refresh(player, holder.location());
			return;
		}
		if (op == Op.FILL) {
			fillCassette(player, holder.location());
			refresh(player, holder.location());
			return;
		}
		boolean all = Integer.valueOf(1).equals(
			current.getItemMeta().getPersistentDataContainer().get(allKey, PersistentDataType.INTEGER)
		);
		Integer amount = current.getItemMeta().getPersistentDataContainer().get(amountKey, PersistentDataType.INTEGER);
		feed(player, holder.location(), amount, all);
		refresh(player, holder.location());
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

	void feed(Player player, Location location, Integer requested, boolean all) {
		int stock = readStock(location.getBlock());
		int room = cash.bankVaultCapacity() - stock;
		if (room <= 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.CassetteFull"));
			return;
		}
		int amount = all ? room : (requested == null ? 0 : requested);
		if (amount <= 0 || amount > room) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.NoStock"));
			return;
		}
		OptionalInt next = BankVault.feed(stock, amount, cash.bankVaultCapacity());
		if (next.isEmpty() || !namedVault.withdraw(cash.bankVaultPlayer(), amount)) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.NoStock"));
			return;
		}
		writeStock(location.getBlock(), next.getAsInt());
		log("BANK_FEED", player.getName() + " amount=" + amount + " stock=" + next.getAsInt());
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.Fed"));
	}

	void emptyCassette(Player player) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		Optional<Cassette.Payload> held = cassetteItems.read(hand);
		if (held.isEmpty() || held.get().total() <= 0) {
			return;
		}
		replaceHand(player, cassetteItems.create(new Cassette.Payload(held.get().id(), List.of())));
		log("CASSETTE_VOID", player.getName() + " amount=" + held.get().total());
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.Emptied"));
	}

	void fillCassette(Player player, Location location) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		Optional<Cassette.Payload> held = cassetteItems.read(hand);
		if (held.isEmpty()) {
			return;
		}
		int stock = readStock(location.getBlock());
		if (stock <= 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.NoStock"));
			return;
		}
		int room = cash.cassetteCapacity() - held.get().total();
		int put = Math.min(room, stock);
		if (put <= 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.CassetteFull"));
			return;
		}
		Optional<Cassette.Payload> updated = Cassette.addValue(
			held.get(),
			put,
			cash.descendingValues(),
			cash.cassetteCapacity()
		);
		OptionalInt nextStock = BankVault.take(stock, put);
		if (updated.isEmpty() || nextStock.isEmpty()) {
			return;
		}
		writeStock(location.getBlock(), nextStock.getAsInt());
		replaceHand(player, cassetteItems.create(updated.get()));
		log("CASSETTE_FILL", player.getName() + " amount=" + put + " stock=" + nextStock.getAsInt());
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Bank.Filled"));
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
		ItemStack edge = pane(Material.BLACK_STAINED_GLASS_PANE);
		for (int slot = 0; slot < inventory.getSize(); slot++) {
			inventory.setItem(slot, edge);
		}
		inventory.setItem(STOCK_SLOT, info(Material.GOLD_BLOCK, cash.applyAmount(
			getConfigString("Economy.Cash.Bank.Stock"), stock)));
		int slot = FEED_START;
		for (int preset : cash.atmPresets()) {
			inventory.setItem(slot++, action(Material.EMERALD, Op.FEED, preset, false,
				cash.applyAmount(getConfigString("Economy.Cash.Bank.Feed"), preset)));
		}
		inventory.setItem(slot, action(Material.SUNFLOWER, Op.FEED, null, true,
			getConfigString("Economy.Cash.Bank.FeedAll")));
		inventory.setItem(EMPTY_SLOT, action(Material.LAVA_BUCKET, Op.EMPTY, null, false,
			getConfigString("Economy.Cash.Bank.EmptyCassette")));
		inventory.setItem(FILL_SLOT, action(Material.HOPPER, Op.FILL, null, false,
			getConfigString("Economy.Cash.Bank.FillCassette")));
	}

	private ItemStack action(Material material, Op op, Integer amount, boolean all, String name) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(name);
		Cash.applyGlow(meta);
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

	private ItemStack info(Material material, String name) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(name);
		Cash.applyGlow(meta);
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

	private static void replaceHand(Player player, ItemStack item) {
		player.getInventory().setItemInMainHand(item);
	}

	private void log(String action, String details) {
		if (logger != null) {
			logger.log(action, details);
		}
	}
}
