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
import org.bukkit.event.inventory.InventoryClickEvent;
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
	static final int GUI_SIZE = 36;
	static final int ACCOUNT_SLOT = 3;
	static final int POCKET_SLOT = 5;
	static final int DEPOSIT_START = 10;
	static final int WITHDRAW_START = 19;

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
	private final NamespacedKey ownerKey;
	private final NamespacedKey opKey;
	private final NamespacedKey amountKey;
	private final NamespacedKey allKey;
	private final NamespacedKey customKey;
	private final Bank bank;
	private final MainThread mainThread;
	private final InventoryFactory inventories;
	private final Map<UUID, Atm.Prompt> prompts = new ConcurrentHashMap<>();
	private final Map<UUID, Location> lastAtm = new ConcurrentHashMap<>();

	public AtmListener(Plugin plugin, Cash cash, WalletListener walletItems) {
		this(
			plugin,
			cash,
			walletItems,
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
		this.cash = cash;
		this.walletItems = walletItems;
		this.ownerKey = new NamespacedKey(plugin, Cash.ATM_OWNER_PDC_KEY);
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
		mainThread.run(() -> stampOwner(event.getBlock(), owner));
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
		}
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
		openGui(player, event.getClickedBlock().getLocation());
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (!(event.getView().getTopInventory().getHolder() instanceof Holder)
			|| !(event.getWhoClicked() instanceof Player player)) {
			return;
		}
		event.setCancelled(true);
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
		boolean done = operation == Atm.Operation.DEPOSIT
			? deposit(player, amount.getAsInt())
			: withdraw(player, amount.getAsInt());
		if (done) {
			String key = operation == Atm.Operation.DEPOSIT
				? "Economy.Cash.AtmGui.Deposited"
				: "Economy.Cash.AtmGui.Withdrawn";
			player.sendMessage(getShortPrefixString() + cash.applyAmount(getConfigString(key), amount.getAsInt()));
			refreshOpenGui(player);
		}
		else {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cheque.InventoryFull"));
		}
	}

	boolean deposit(Player player, int amount) {
		Optional<CashTender.Ledger> paid = CashTender.pay(readLedger(player), amount, cash.descendingValues());
		if (paid.isEmpty() || !canFitLoose(player, paid.get().inventory()) || !bank.deposit(player, amount)) {
			return false;
		}
		writeLedger(player, paid.get());
		return true;
	}

	boolean withdraw(Player player, int amount) {
		Optional<List<Cash.Stack>> stacks = CashTender.greedy(amount, cash.descendingValues());
		if (stacks.isEmpty() || emptySlots(player.getInventory()) < CashTender.toInventoryStacks(stacks.get()).size()) {
			return false;
		}
		if (!bank.withdraw(player, amount)) {
			return false;
		}
		for (Cash.Stack stack : CashTender.toInventoryStacks(stacks.get())) {
			player.getInventory().addItem(walletItems.createCashItem(stack));
		}
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
		fillFrame(inventory);
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
	}

	private void fillFrame(Inventory inventory) {
		ItemStack edge = pane(Material.BLACK_STAINED_GLASS_PANE);
		ItemStack inner = pane(Material.GRAY_STAINED_GLASS_PANE);
		int lastRow = inventory.getSize() / 9 - 1;
		for (int slot = 0; slot < inventory.getSize(); slot++) {
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
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(name);
		Cash.applyGlow(meta);
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
		Cash.applyGlow(meta);
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

	private void stampOwner(Block block, UUID owner) {
		if (!(block.getState() instanceof TileState state)) {
			return;
		}
		state.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, owner.toString());
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
				continue;
			}
			if (walletItems.readCashItem(item).isPresent()) {
				inventory.setItem(slot, null);
			}
		}
		for (Cash.Stack stack : CashTender.toInventoryStacks(ledger.inventory())) {
			inventory.addItem(walletItems.createCashItem(stack));
		}
	}

	static List<Cash.Stack> stacksForWallet(CashTender.Ledger ledger, int walletIndex) {
		if (walletIndex < ledger.wallets().size()) {
			return ledger.wallets().get(walletIndex);
		}
		return List.of();
	}

	private boolean canFitLoose(Player player, List<Cash.Stack> loose) {
		int needed = CashTender.toInventoryStacks(loose).size();
		int free = 0;
		for (ItemStack item : contents(player.getInventory())) {
			if (item == null || item.getType() == Material.AIR || walletItems.readCashItem(item).isPresent()) {
				free++;
			}
		}
		return needed <= free;
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
