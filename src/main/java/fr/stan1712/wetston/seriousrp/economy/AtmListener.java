package fr.stan1712.wetston.seriousrp.economy;

import fr.stan1712.wetston.seriousrp.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
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
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public final class AtmListener implements Listener {
	static final String OP_KEY = "srp-atm-op";
	static final String AMOUNT_KEY = "srp-atm-amount";
	static final String ALL_KEY = "srp-atm-all";
	static final String CUSTOM_KEY = "srp-atm-custom";
	static final int GUI_SIZE = 45;
	static final int ACCOUNT_SLOT = 3;
	static final int POCKET_SLOT = 5;
	static final int DEPOSIT_START = 10;
	static final int WITHDRAW_START = 19;
	static final int COMPACT_SLOT = 30;
	static final int BREAK_SLOT = 32;
	static final int MACHINE_SLOT = 44;
	static final int CASSETTE_START = 36;

	interface Bank {
		double balance(Player player);
		boolean withdraw(Player player, int amount);
		boolean deposit(Player player, int amount);
	}

	interface MainThread {
		void run(Runnable task);
	}

	interface InventoryFactory {
		Inventory create(InventoryHolder holder, int size, String title);
	}

	private final Cash cash;
	private final WalletListener walletItems;
	private final CassetteItems cassetteItems;
	private final AtmRegistry registry;
	private final AtmLogger logger;
	private final NamespacedKey ownerKey;
	private final NamespacedKey cassettesKey;
	private final NamespacedKey opKey;
	private final NamespacedKey amountKey;
	private final NamespacedKey allKey;
	private final NamespacedKey customKey;
	private final Bank bank;
	private final MainThread mainThread;
	private final InventoryFactory inventories;
	private final Map<UUID, Atm.Prompt> prompts = new ConcurrentHashMap<>();
	private final Map<UUID, Location> lastAtm = new ConcurrentHashMap<>();
	private final Map<String, Long> breakConfirmUntil = new ConcurrentHashMap<>();

	public AtmListener(Plugin plugin, Cash cash, WalletListener walletItems, CassetteItems cassetteItems) {
		this(plugin, cash, walletItems, cassetteItems, new AtmRegistry(plugin), new AtmLogger(plugin));
	}

	public AtmListener(
		Plugin plugin,
		Cash cash,
		WalletListener walletItems,
		CassetteItems cassetteItems,
		AtmRegistry registry,
		AtmLogger logger
	) {
		this(
			plugin,
			cash,
			walletItems,
			cassetteItems,
			registry,
			logger,
			vaultBank(),
			task -> plugin.getServer().getScheduler().runTask(plugin, task),
			AtmListener::createInventory
		);
	}

	AtmListener(
		Plugin plugin,
		Cash cash,
		WalletListener walletItems,
		Bank bank,
		MainThread mainThread,
		InventoryFactory inventories
	) {
		this(plugin, cash, walletItems, null, null, null, bank, mainThread, inventories);
	}

	AtmListener(
		Plugin plugin,
		Cash cash,
		WalletListener walletItems,
		CassetteItems cassetteItems,
		AtmRegistry registry,
		AtmLogger logger,
		Bank bank,
		MainThread mainThread,
		InventoryFactory inventories
	) {
		this.cash = cash;
		this.walletItems = walletItems;
		this.cassetteItems = cassetteItems;
		this.registry = registry;
		this.logger = logger;
		this.ownerKey = new NamespacedKey(plugin, Cash.ATM_OWNER_PDC_KEY);
		this.cassettesKey = new NamespacedKey(plugin, Cash.ATM_CASSETTES_PDC_KEY);
		this.opKey = new NamespacedKey(plugin, OP_KEY);
		this.amountKey = new NamespacedKey(plugin, AMOUNT_KEY);
		this.allKey = new NamespacedKey(plugin, ALL_KEY);
		this.customKey = new NamespacedKey(plugin, CUSTOM_KEY);
		this.bank = bank;
		this.mainThread = mainThread;
		this.inventories = inventories;
	}

	static Inventory createInventory(InventoryHolder holder, int size, String title) {
		return Bukkit.createInventory(holder, size, title);
	}

	static Bank vaultBank() {
		return new Bank() {
			@Override
			public double balance(Player player) {
				Economy economy = Main.economy;
				return economy == null ? 0D : economy.getBalance(player);
			}

			@Override
			public boolean withdraw(Player player, int amount) {
				Economy economy = Main.economy;
				return economy != null && economy.withdrawPlayer(player, amount).transactionSuccess();
			}

			@Override
			public boolean deposit(Player player, int amount) {
				Economy economy = Main.economy;
				return economy != null && economy.depositPlayer(player, amount).transactionSuccess();
			}
		};
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (!cash.isEnabled() || !Atm.isCreateAttempt(event.getLine(0), cash)) {
			return;
		}
		Player player = event.getPlayer();
		if (!player.hasPermission(Atm.CREATE_PERM)) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.DeniedCreate"));
			return;
		}
		int cost = cash.atmCreateCost();
		if (cost > 0 && !bank.withdraw(player, cost)) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + cash.applyAmount(
				getConfigString("Economy.Cash.AtmGui.CreateNotEnough"), cost));
			return;
		}
		String[] generic = Atm.genericLines(cash);
		event.setLine(0, generic[0]);
		event.setLine(1, generic[1]);
		event.setLine(2, generic[2]);
		event.setLine(3, generic[3]);
		UUID owner = player.getUniqueId();
		mainThread.run(() -> {
			stampNewAtm(event.getBlock(), owner);
			if (registry != null) {
				registry.register(event.getBlock().getLocation(), owner);
			}
		});
		String created = cost > 0
			? cash.applyAmount(getConfigString("Economy.Cash.AtmGui.CreatedPaid"), cost)
			: getConfigString("Economy.Cash.AtmGui.Created");
		player.sendMessage(getShortPrefixString() + created);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		Optional<UUID> owner = atmOwner(event.getBlock());
		if (owner.isEmpty()) {
			return;
		}
		Player player = event.getPlayer();
		if (!Atm.canBreak(
			owner.get(),
			player.getUniqueId(),
			player.hasPermission(Atm.DELETE_PERM),
			player.hasPermission(Atm.DELETE_OTHERS_PERM)
		)) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.DeniedBreak"));
			return;
		}
		String confirmKey = player.getUniqueId() + ":" + blockKey(event.getBlock().getLocation());
		long now = System.currentTimeMillis();
		Long until = breakConfirmUntil.get(confirmKey);
		if (until == null || until < now) {
			event.setCancelled(true);
			breakConfirmUntil.put(confirmKey, now + Atm.BREAK_CONFIRM_MILLIS);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.BreakConfirm"));
			return;
		}
		breakConfirmUntil.remove(confirmKey);
		dropInstalledCassettes(event.getBlock(), event.getBlock().getLocation());
		if (registry != null) {
			registry.unregister(event.getBlock().getLocation());
		}
		log("BREAK", player.getName() + " " + blockKey(event.getBlock().getLocation()));
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		if (!cash.isEnabled() || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
			return;
		}
		if (atmOwner(event.getClickedBlock()).isEmpty()) {
			return;
		}
		Player player = event.getPlayer();
		if (!player.hasPermission(Atm.USE_PERM)) {
			return;
		}
		event.setCancelled(true);
		Location location = event.getClickedBlock().getLocation();
		lastAtm.put(player.getUniqueId(), location);
		log("OPEN", player.getName()
			+ " account=" + (int) Math.floor(bank.balance(player))
			+ " pocket=" + pocketTotal(player)
			+ " cash=" + AtmCassettes.total(readCassettes(event.getClickedBlock())));
		openGui(player, location);
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (!(event.getView().getTopInventory().getHolder() instanceof Holder)
			|| !(event.getWhoClicked() instanceof Player player)) {
			return;
		}
		if (handleCassetteClick(event, player)) {
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
		var pdc = current.getItemMeta().getPersistentDataContainer();
		String opRaw = pdc.get(opKey, PersistentDataType.STRING);
		if (opRaw == null) {
			return;
		}
		Atm.Operation operation = Atm.Operation.valueOf(opRaw);
		if (operation == Atm.Operation.COMPACT || operation == Atm.Operation.BREAK) {
			runChange(player, operation);
			return;
		}
		boolean custom = Integer.valueOf(1).equals(pdc.get(customKey, PersistentDataType.INTEGER));
		if (custom) {
			prompts.put(player.getUniqueId(), new Atm.Prompt(
				player.getUniqueId(),
				operation,
				System.currentTimeMillis() + Atm.PROMPT_TIMEOUT_MILLIS
			));
			lastAtm.put(player.getUniqueId(), ((Holder) event.getView().getTopInventory().getHolder()).location());
			player.closeInventory();
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.Prompt"));
			return;
		}
		boolean all = Integer.valueOf(1).equals(pdc.get(allKey, PersistentDataType.INTEGER));
		Integer requested = pdc.get(amountKey, PersistentDataType.INTEGER);
		runOperation(player, operation, requested, all);
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
			if (!isCassetteSlot(raw) || !canService(player)) {
				event.setCancelled(true);
				return;
			}
		}
		if (!touchesTop) {
			return;
		}
		if (cassetteItems == null || cassetteItems.read(event.getOldCursor()).isEmpty()) {
			event.setCancelled(true);
			return;
		}
		mainThread.run(() -> persistCassettes(player));
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player player && event.getInventory().getHolder() instanceof Holder) {
			persistCassettes(player, event.getInventory());
		}
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Atm.Prompt prompt = prompts.remove(event.getPlayer().getUniqueId());
		if (prompt == null) {
			return;
		}
		event.setCancelled(true);
		Player player = event.getPlayer();
		String message = event.getMessage();
		mainThread.run(() -> handlePrompt(player, prompt, message));
	}

	public void tickProximity(Collection<? extends Player> players) {
		int radius = cash.viewRadius();
		for (Player player : players) {
			Location origin = player.getLocation();
			if (origin.getWorld() == null) {
				continue;
			}
			scanRadius(player, origin, radius);
		}
	}

	private void scanRadius(Player player, Location origin, int radius) {
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					overlayIfAtm(player, origin.getBlock().getRelative(dx, dy, dz));
				}
			}
		}
	}

	private void overlayIfAtm(Player player, Block block) {
		if (atmOwner(block).isPresent()) {
			int account = (int) Math.floor(bank.balance(player));
			player.sendSignChange(block.getLocation(), Atm.nearbyLines(cash, account, pocketTotal(player)));
		}
	}

	void handlePrompt(Player player, Atm.Prompt prompt, String message) {
		if (prompt.isExpired(System.currentTimeMillis())) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.PromptCancelled"));
			return;
		}
		if (Atm.isCancelPrompt(message)) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.PromptCancelled"));
			return;
		}
		OptionalInt amount = Atm.parsePromptAmount(message);
		if (amount.isEmpty()) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.InvalidAmount"));
			return;
		}
		runOperation(player, prompt.operation(), amount.getAsInt(), false);
		Location location = lastAtm.remove(player.getUniqueId());
		if (location != null && atmOwner(location.getBlock()).isPresent()) {
			openGui(player, location);
		}
	}

	void runOperation(Player player, Atm.Operation operation, Integer requested, boolean all) {
		int pocket = pocketTotal(player);
		int account = (int) Math.floor(bank.balance(player));
		OptionalInt amount = Atm.resolveAmount(operation, requested, all, pocket, account);
		if (amount.isEmpty()) {
			String key = operation == Atm.Operation.DEPOSIT
				? "Economy.Cash.AtmGui.NotEnoughCash"
				: "Economy.Cash.AtmGui.NotEnoughAccount";
			player.sendMessage(getShortPrefixString() + getConfigString(key));
			return;
		}
		Location location = openAtmLocation(player).orElse(lastAtm.get(player.getUniqueId()));
		boolean done = operation == Atm.Operation.DEPOSIT
			? deposit(player, amount.getAsInt(), location)
			: withdraw(player, amount.getAsInt(), location);
		if (done) {
			String key = operation == Atm.Operation.DEPOSIT
				? "Economy.Cash.AtmGui.Deposited"
				: "Economy.Cash.AtmGui.Withdrawn";
			player.sendMessage(getShortPrefixString() + cash.applyAmount(getConfigString(key), amount.getAsInt()));
			refreshOpenGui(player);
		}
		else if (cash.advancedBank() && location != null && !AtmCassettes.hasCassette(readCassettes(location.getBlock()))) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.NoCassette"));
		}
		else if (cash.advancedBank() && operation == Atm.Operation.DEPOSIT) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.CassetteFull"));
		}
		else if (cash.advancedBank() && operation == Atm.Operation.WITHDRAW) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.CassetteEmpty"));
		}
		else {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cheque.InventoryFull"));
		}
	}

	void runChange(Player player, Atm.Operation operation) {
		CashTender.Ledger ledger = readLedger(player);
		int total = CashTender.total(ledger.inventory());
		if (total <= 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.NotEnoughCash"));
			return;
		}
		Optional<List<Cash.Stack>> next = operation == Atm.Operation.BREAK
			? CashTender.breakSmall(total, cash.descendingValues(), cash.breakMaxDenomination(), cash.breakPieceCap())
			: CashTender.greedy(total, cash.descendingValues());
		if (next.isEmpty()) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.NotEnoughCash"));
			return;
		}
		if (CashTender.sameStacks(ledger.inventory(), next.get())) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.AlreadyChanged"));
			return;
		}
		if (!walletItems.canFitLoose(player, next.get())) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.InventoryFull"));
			return;
		}
		writeLedger(player, new CashTender.Ledger(next.get(), ledger.wallets()));
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.AtmGui.Changed"));
		refreshOpenGui(player);
	}

	boolean deposit(Player player, int amount) {
		return deposit(player, amount, lastAtm.get(player.getUniqueId()));
	}

	boolean deposit(Player player, int amount, Location location) {
		Optional<CashTender.Ledger> paid = CashTender.pay(readLedger(player), amount, cash.descendingValues());
		if (paid.isEmpty() || !canFitLoose(player, paid.get().inventory())) {
			return false;
		}
		if (cash.advancedBank()) {
			if (location == null || location.getWorld() == null) {
				return false;
			}
			Optional<List<Optional<Cassette.Payload>>> next = AtmCassettes.deposit(
				readCassettes(location.getBlock()),
				amount,
				cash.descendingValues(),
				cash.cassetteCapacity()
			);
			if (next.isEmpty() || !bank.deposit(player, amount)) {
				return false;
			}
			writeCassettes(location.getBlock(), next.get());
			writeLedger(player, paid.get());
			log("DEPOSIT", player.getName()
				+ " amount=" + amount
				+ " cash=" + AtmCassettes.total(next.get())
				+ " cassettes=" + cassetteTags(next.get()));
			return true;
		}
		if (!bank.deposit(player, amount)) {
			return false;
		}
		writeLedger(player, paid.get());
		log("DEPOSIT", player.getName() + " amount=" + amount);
		return true;
	}

	boolean withdraw(Player player, int amount) {
		return withdraw(player, amount, lastAtm.get(player.getUniqueId()));
	}

	boolean withdraw(Player player, int amount, Location location) {
		Optional<List<Cash.Stack>> stacks = CashTender.greedy(amount, cash.descendingValues());
		if (stacks.isEmpty() || emptySlots(player.getInventory()) < CashTender.toInventoryStacks(stacks.get()).size()) {
			return false;
		}
		List<Optional<Cassette.Payload>> previous = List.of();
		if (cash.advancedBank()) {
			if (location == null || location.getWorld() == null) {
				return false;
			}
			previous = readCassettes(location.getBlock());
			Optional<List<Optional<Cassette.Payload>>> next = AtmCassettes.withdraw(
				previous,
				amount,
				cash.descendingValues()
			);
			if (next.isEmpty()) {
				return false;
			}
			writeCassettes(location.getBlock(), next.get());
			if (!bank.withdraw(player, amount)) {
				writeCassettes(location.getBlock(), previous);
				return false;
			}
			for (Cash.Stack stack : CashTender.toInventoryStacks(stacks.get())) {
				player.getInventory().addItem(walletItems.createCashItem(stack));
			}
			log("WITHDRAW", player.getName()
				+ " amount=" + amount
				+ " cash=" + AtmCassettes.total(next.get())
				+ " cassettes=" + cassetteTags(next.get()));
			return true;
		}
		if (!bank.withdraw(player, amount)) {
			return false;
		}
		for (Cash.Stack stack : CashTender.toInventoryStacks(stacks.get())) {
			player.getInventory().addItem(walletItems.createCashItem(stack));
		}
		log("WITHDRAW", player.getName() + " amount=" + amount);
		return true;
	}

	int pocketTotal(Player player) {
		return readLedger(player).total();
	}

	private void openGui(Player player, Location location) {
		Holder holder = new Holder(location);
		Inventory inventory = inventories.create(holder, GUI_SIZE, getConfigString("Economy.Cash.AtmGui.Title"));
		fillGui(inventory, player);
		player.openInventory(inventory);
	}

	private void refreshOpenGui(Player player) {
		InventoryView view = player.getOpenInventory();
		if (view == null) {
			return;
		}
		Inventory top = view.getTopInventory();
		if (top.getHolder() instanceof Holder) {
			fillGui(top, player);
		}
	}

	private void fillGui(Inventory inventory, Player player) {
		int account = (int) Math.floor(bank.balance(player));
		int pocket = pocketTotal(player);
		fillFrame(inventory, serviceCassetteUi(player));
		inventory.setItem(ACCOUNT_SLOT, infoItem(Material.GOLD_INGOT, cash.applyAmount(
			getConfigString("Economy.Cash.AtmGui.Account"), account)));
		inventory.setItem(POCKET_SLOT, infoItem(Material.SUNFLOWER, cash.applyAmount(
			getConfigString("Economy.Cash.AtmGui.Pocket"), pocket)));
		placeActionRow(inventory, DEPOSIT_START, Atm.Operation.DEPOSIT, Material.EMERALD,
			getConfigString("Economy.Cash.AtmGui.Deposit"),
			cash.applyAmount(getConfigString("Economy.Cash.AtmGui.DepositAll"), pocket));
		placeActionRow(inventory, WITHDRAW_START, Atm.Operation.WITHDRAW, Material.REDSTONE,
			getConfigString("Economy.Cash.AtmGui.Withdraw"),
			cash.applyAmount(getConfigString("Economy.Cash.AtmGui.WithdrawAll"), account));
		inventory.setItem(COMPACT_SLOT, button(Material.PAPER, Atm.Operation.COMPACT, null, false, false,
			getConfigString("Economy.Cash.AtmGui.Compact")));
		inventory.setItem(BREAK_SLOT, button(Material.IRON_NUGGET, Atm.Operation.BREAK, null, false, false,
			getConfigString("Economy.Cash.AtmGui.Break")));
		if (inventory.getHolder() instanceof Holder holder) {
			fillCassetteSlots(inventory, player, holder.location());
		}
	}

	private void fillCassetteSlots(Inventory inventory, Player player, Location location) {
		if (!cash.advancedBank() || location == null || location.getWorld() == null) {
			return;
		}
		List<Optional<Cassette.Payload>> slots = readCassettes(location.getBlock());
		int total = AtmCassettes.total(slots);
		inventory.setItem(MACHINE_SLOT, infoItem(
			Material.CHEST,
			cash.applyAmount(getConfigString("Economy.Cash.Cassette.Installed"), total),
			total > 0
		));
		if (!serviceCassetteUi(player)) {
			return;
		}
		int shown = Math.min(shownCassetteSlots(), slots.size());
		for (int index = 0; index < shown; index++) {
			inventory.setItem(
				CASSETTE_START + index,
				slots.get(index).map(cassetteItems::create).orElse(null)
			);
		}
	}

	private boolean handleCassetteClick(InventoryClickEvent event, Player player) {
		if (!serviceCassetteUi(player)) {
			return false;
		}
		int raw = event.getRawSlot();
		int size = event.getView().getTopInventory().getSize();
		boolean slot = isCassetteSlot(raw);
		boolean shiftIn = event.isShiftClick() && raw >= size && cassetteItems.read(event.getCurrentItem()).isPresent();
		if (!slot && !shiftIn) {
			return false;
		}
		if (!cassetteClickAllowed(event, player)) {
			event.setCancelled(true);
			return true;
		}
		event.setCancelled(false);
		mainThread.run(() -> persistCassettes(player));
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
		return cassetteItems != null && cassetteItems.read(item).isPresent();
	}

	private boolean isCassetteSlot(int rawSlot) {
		return rawSlot >= CASSETTE_START && rawSlot < CASSETTE_START + shownCassetteSlots();
	}

	private int shownCassetteSlots() {
		return Math.min(Cash.MAX_CASSETTE_SLOTS, Math.max(1, cash.cassetteSlots()));
	}

	private boolean serviceCassetteUi(Player player) {
		return cash.advancedBank()
			&& cassetteItems != null
			&& player.hasPermission(Atm.SERVICE_PERM);
	}

	private boolean canService(Player player) {
		return serviceCassetteUi(player);
	}

	private void persistCassettes(Player player) {
		InventoryView view = player.getOpenInventory();
		if (view != null) {
			persistCassettes(player, view.getTopInventory());
		}
	}

	private void persistCassettes(Player player, Inventory top) {
		if (!(top.getHolder() instanceof Holder holder) || !serviceCassetteUi(player)) {
			return;
		}
		Location location = holder.location();
		if (location == null || location.getWorld() == null) {
			return;
		}
		int count = shownCassetteSlots();
		List<Optional<Cassette.Payload>> previous = readCassettes(location.getBlock());
		List<Optional<Cassette.Payload>> next = AtmCassettes.emptySlots(count);
		for (int index = 0; index < count; index++) {
			ItemStack item = top.getItem(CASSETTE_START + index);
			Optional<Cassette.Payload> payload = cassetteItems.read(item);
			if (payload.isEmpty()) {
				if (item != null && item.getType() != Material.AIR) {
					top.setItem(CASSETTE_START + index, null);
					giveOrDrop(player, location, item);
				}
				continue;
			}
			if (item.getAmount() > 1) {
				ItemStack extra = item.clone();
				extra.setAmount(item.getAmount() - 1);
				item.setAmount(1);
				giveOrDrop(player, location, extra);
			}
			next.set(index, payload);
		}
		writeCassettes(location.getBlock(), next);
		logCassetteMoves(player, previous, next);
	}

	private static String cassetteTags(List<Optional<Cassette.Payload>> slots) {
		StringBuilder tags = new StringBuilder();
		for (Optional<Cassette.Payload> slot : slots) {
			if (slot.isEmpty()) {
				continue;
			}
			if (!tags.isEmpty()) {
				tags.append(',');
			}
			tags.append('#').append(Cassette.shortId(slot.get().id()));
		}
		return tags.toString();
	}

	private void logCassetteMoves(
		Player player,
		List<Optional<Cassette.Payload>> previous,
		List<Optional<Cassette.Payload>> next
	) {
		int size = Math.max(previous.size(), next.size());
		for (int index = 0; index < size; index++) {
			Optional<Cassette.Payload> before = index < previous.size() ? previous.get(index) : Optional.empty();
			Optional<Cassette.Payload> after = index < next.size() ? next.get(index) : Optional.empty();
			if (before.isEmpty() && after.isPresent()) {
				log("INSTALL", player.getName()
					+ " slot=" + index
					+ " cassette=#" + Cassette.shortId(after.get().id())
					+ " amount=" + after.get().total());
			}
			else if (before.isPresent() && after.isEmpty()) {
				log("EJECT", player.getName()
					+ " slot=" + index
					+ " cassette=#" + Cassette.shortId(before.get().id())
					+ " amount=" + before.get().total());
			}
		}
	}

	private static void giveOrDrop(Player player, Location location, ItemStack item) {
		Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
		if (location.getWorld() == null) {
			return;
		}
		leftover.values().forEach(extra -> location.getWorld().dropItemNaturally(location, extra));
	}

	List<Optional<Cassette.Payload>> readCassettes(Block block) {
		if (!(block.getState() instanceof TileState state)) {
			return AtmCassettes.emptySlots(cash.cassetteSlots());
		}
		String raw = state.getPersistentDataContainer().get(cassettesKey, PersistentDataType.STRING);
		return AtmCassettes.fromJson(raw, cash.cassetteSlots());
	}

	void writeCassettes(Block block, List<Optional<Cassette.Payload>> slots) {
		if (!(block.getState() instanceof TileState state)) {
			return;
		}
		state.getPersistentDataContainer().set(cassettesKey, PersistentDataType.STRING, AtmCassettes.toJson(slots));
		state.update();
	}

	private void dropInstalledCassettes(Block block, Location location) {
		if (cassetteItems == null || location == null || location.getWorld() == null) {
			return;
		}
		for (Optional<Cassette.Payload> slot : readCassettes(block)) {
			slot.ifPresent(payload -> location.getWorld().dropItemNaturally(location, cassetteItems.create(payload)));
		}
	}

	private void log(String action, String details) {
		if (logger != null) {
			logger.log(action, details);
		}
	}

	private Optional<Location> openAtmLocation(Player player) {
		InventoryView view = player.getOpenInventory();
		if (view != null && view.getTopInventory().getHolder() instanceof Holder holder) {
			return Optional.ofNullable(holder.location());
		}
		return Optional.empty();
	}

	static String blockKey(Location location) {
		if (location == null || location.getWorld() == null) {
			return "unknown";
		}
		return location.getWorld().getName()
			+ ";" + location.getBlockX()
			+ ";" + location.getBlockY()
			+ ";" + location.getBlockZ();
	}

	private void fillFrame(Inventory inventory, boolean openCassetteRow) {
		ItemStack edge = pane(Material.BLACK_STAINED_GLASS_PANE);
		ItemStack inner = pane(Material.GRAY_STAINED_GLASS_PANE);
		int lastRow = inventory.getSize() / 9 - 1;
		int cassetteCount = openCassetteRow ? shownCassetteSlots() : 0;
		for (int slot = 0; slot < inventory.getSize(); slot++) {
			if (cassetteCount > 0 && slot >= CASSETTE_START && slot < CASSETTE_START + cassetteCount) {
				inventory.setItem(slot, null);
				continue;
			}
			int column = slot % 9;
			int row = slot / 9;
			boolean border = row == 0 || row == lastRow || column == 0 || column == 8;
			inventory.setItem(slot, border ? edge : inner);
		}
	}

	private void placeActionRow(
		Inventory inventory,
		int start,
		Atm.Operation operation,
		Material material,
		String presetTemplate,
		String allName
	) {
		int slot = start;
		for (int preset : cash.atmPresets()) {
			inventory.setItem(slot++, button(material, operation, preset, false, false,
				cash.applyAmount(presetTemplate, preset)));
		}
		inventory.setItem(slot++, button(Material.SUNFLOWER, operation, null, true, false, allName));
		inventory.setItem(slot, button(Material.NAME_TAG, operation, null, false, true,
			getConfigString("Economy.Cash.AtmGui.CustomAmount")));
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

	private ItemStack infoItem(Material material, String name) {
		return infoItem(material, name, false);
	}

	private ItemStack infoItem(Material material, String name, boolean glow) {
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

	private ItemStack button(
		Material material,
		Atm.Operation operation,
		Integer amount,
		boolean all,
		boolean custom,
		String name
	) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(name);
		meta.getPersistentDataContainer().set(opKey, PersistentDataType.STRING, operation.name());
		if (amount != null) {
			meta.getPersistentDataContainer().set(amountKey, PersistentDataType.INTEGER, amount);
		}
		if (all) {
			meta.getPersistentDataContainer().set(allKey, PersistentDataType.INTEGER, 1);
		}
		if (custom) {
			meta.getPersistentDataContainer().set(customKey, PersistentDataType.INTEGER, 1);
		}
		item.setItemMeta(meta);
		return item;
	}

	private void stampNewAtm(Block block, UUID owner) {
		if (!(block.getState() instanceof TileState state)) {
			return;
		}
		state.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, owner.toString());
		state.getPersistentDataContainer().set(
			cassettesKey,
			PersistentDataType.STRING,
			AtmCassettes.toJson(AtmCassettes.emptySlots(cash.cassetteSlots()))
		);
		state.update();
	}

	Optional<UUID> atmOwner(Block block) {
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

	CashTender.Ledger readLedger(Player player) {
		List<Cash.Stack> loose = new ArrayList<>();
		List<List<Cash.Stack>> wallets = new ArrayList<>();
		for (ItemStack item : contents(player.getInventory())) {
			Optional<Wallet.Payload> wallet = walletItems.readWalletItem(item);
			if (wallet.isPresent()) {
				wallets.add(wallet.get().stacks());
				continue;
			}
			walletItems.readCashItem(item).ifPresent(loose::add);
		}
		return new CashTender.Ledger(loose, wallets);
	}

	private void writeLedger(Player player, CashTender.Ledger ledger) {
		PlayerInventory inventory = player.getInventory();
		ItemStack[] contents = contents(inventory);
		int walletIndex = 0;
		for (int slot = 0; slot < contents.length; slot++) {
			ItemStack item = contents[slot];
			Optional<Wallet.Payload> wallet = walletItems.readWalletItem(item);
			if (wallet.isPresent()) {
				List<Cash.Stack> stacks = stacksForWallet(ledger, walletIndex);
				walletIndex++;
				inventory.setItem(slot, walletItems.createWalletItem(new Wallet.Payload(
					wallet.get().id(),
					wallet.get().slots(),
					stacks
				)));
			}
		}
		walletItems.replaceLooseCash(player, ledger.inventory());
	}

	static List<Cash.Stack> stacksForWallet(CashTender.Ledger ledger, int walletIndex) {
		if (walletIndex < ledger.wallets().size()) {
			return ledger.wallets().get(walletIndex);
		}
		return List.of();
	}

	private boolean canFitLoose(Player player, List<Cash.Stack> loose) {
		return walletItems.canFitLoose(player, loose);
	}

	private static int emptySlots(PlayerInventory inventory) {
		int empty = 0;
		for (ItemStack item : contents(inventory)) {
			if (item == null || item.getType() == Material.AIR) {
				empty++;
			}
		}
		return empty;
	}

	private static ItemStack[] contents(PlayerInventory inventory) {
		return inventory.getStorageContents();
	}

	record Holder(Location location) implements InventoryHolder {
		@Override
		public Inventory getInventory() {
			throw new UnsupportedOperationException("ATM holder is a GUI marker");
		}
	}
}
