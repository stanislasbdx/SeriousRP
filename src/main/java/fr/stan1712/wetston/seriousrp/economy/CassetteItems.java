package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public final class CassetteItems implements Listener {
	static final String RECIPE_KEY = "srp_cassette";

	private final Plugin plugin;
	private final Cash cash;
	private final NamespacedKey itemKey;
	private final NamespacedKey recipeKey;
	private final Consumer<ShapedRecipe> recipeSink;
	private final Consumer<NamespacedKey> recipeRemover;
	private final Consumer<Runnable> mainThread;
	private final Map<UUID, PinPrompt> prompts = new ConcurrentHashMap<>();
	private WalletListener walletItems;
	private AtmLogger logger;

	enum PinMode {
		SET,
		OPEN
	}

	record PinPrompt(UUID playerId, EquipmentSlot hand, PinMode mode, long expiresAtMillis) {
		boolean expired(long now) {
			return now >= expiresAtMillis;
		}
	}

	record ContentsHolder(String id, String pin, int failedAttempts, int slot, List<Cash.Stack> hidden, int previousTotal)
		implements InventoryHolder {
		@Override
		public Inventory getInventory() {
			throw new UnsupportedOperationException("Cassette contents are a GUI marker");
		}
	}

	public void use(WalletListener walletItems, AtmLogger logger) {
		this.walletItems = walletItems;
		this.logger = logger;
	}

	public CassetteItems(Plugin plugin, Cash cash) {
		this(
			plugin,
			cash,
			org.bukkit.Bukkit::addRecipe,
			org.bukkit.Bukkit::removeRecipe,
			task -> plugin.getServer().getScheduler().runTask(plugin, task)
		);
	}

	CassetteItems(
		Plugin plugin,
		Cash cash,
		Consumer<ShapedRecipe> recipeSink,
		Consumer<NamespacedKey> recipeRemover
	) {
		this(plugin, cash, recipeSink, recipeRemover, Runnable::run);
	}

	CassetteItems(
		Plugin plugin,
		Cash cash,
		Consumer<ShapedRecipe> recipeSink,
		Consumer<NamespacedKey> recipeRemover,
		Consumer<Runnable> mainThread
	) {
		this.plugin = plugin;
		this.cash = cash;
		this.itemKey = new NamespacedKey(plugin, Cash.CASSETTE_ITEM_PDC_KEY);
		this.recipeKey = new NamespacedKey(plugin, RECIPE_KEY);
		this.recipeSink = recipeSink;
		this.recipeRemover = recipeRemover;
		this.mainThread = mainThread;
	}

	public void registerRecipe() {
		unregisterRecipe();
		if (!cash.isEnabled()) {
			return;
		}
		ShapedRecipe recipe = new ShapedRecipe(recipeKey, create(Cassette.empty()));
		recipe.shape(cash.cassetteRecipeShape().toArray(String[]::new));
		cash.cassetteRecipeIngredients().forEach(recipe::setIngredient);
		recipeSink.accept(recipe);
	}

	boolean advancedBankLive() {
		if (plugin.getConfig().contains("Economy.Cash.AdvancedBank")) {
			return plugin.getConfig().getBoolean("Economy.Cash.AdvancedBank");
		}
		return cash.advancedBank();
	}

	public void unregisterRecipe() {
		recipeRemover.accept(recipeKey);
	}

	ItemStack create(Cassette.Payload payload) {
		ItemStack item = new ItemStack(cash.cassetteMaterial());
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(labeled(cash.cassetteDisplayName(), payload.id()));
		if (payload.pin() == null) {
			meta.setLore(List.of(cash.applyAmount(cash.cassetteLoreTotal(), payload.total())));
		}
		else {
			meta.setLore(List.of(
				cash.applyAmount(cash.cassetteLoreTotal(), payload.total()),
				getConfigString("Economy.Cash.Cassette.Locked")
			));
		}
		cash.cassetteCustomModelData().ifPresent(model -> Cash.applyCustomModelData(meta, model));
		if (payload.total() > 0) {
			Cash.applyGlow(meta);
		}
		meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, Cassette.toJson(payload));
		item.setItemMeta(meta);
		return item;
	}

	Optional<Cassette.Payload> read(ItemStack item) {
		if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) {
			return Optional.empty();
		}
		String raw = item.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
		return Cassette.fromJson(raw);
	}

	boolean isCassetteRecipe(Recipe recipe) {
		return recipe instanceof Keyed keyed && recipeKey.equals(keyed.getKey());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (advancedBankLive()) {
			event.getPlayer().discoverRecipe(recipeKey);
		}
	}

	@EventHandler
	public void onUse(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (event.getHand() == null) {
			return;
		}
		Optional<Cassette.Payload> payload = read(event.getItem());
		if (payload.isEmpty()) {
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
			&& event.getClickedBlock() != null
			&& event.getClickedBlock().getState() instanceof Sign) {
			return;
		}
		event.setCancelled(true);
		Player player = event.getPlayer();
		PinMode mode = payload.get().pin() == null ? PinMode.SET : PinMode.OPEN;
		prompts.put(player.getUniqueId(), new PinPrompt(
			player.getUniqueId(),
			event.getHand(),
			mode,
			System.currentTimeMillis() + Atm.PROMPT_TIMEOUT_MILLIS
		));
		String key = mode == PinMode.SET ? "Economy.Cash.Cassette.PinSet" : "Economy.Cash.Cassette.PinAsk";
		player.sendMessage(getShortPrefixString() + getConfigString(key));
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
		mainThread.accept(() -> handlePin(player, prompt, message));
	}

	@EventHandler
	public void onContentsClick(InventoryClickEvent event) {
		if (!(event.getView().getTopInventory().getHolder() instanceof ContentsHolder holder)
			|| !(event.getWhoClicked() instanceof Player player)) {
			return;
		}
		if (lockedContentsClick(event, player, holder)) {
			event.setCancelled(true);
			return;
		}
		guardContentsClick(event, player);
	}

	private boolean lockedContentsClick(InventoryClickEvent event, Player player, ContentsHolder holder) {
		if (walletItems == null || isSourceSlot(event, player, holder.slot())) {
			return true;
		}
		if (event.getHotbarButton() == holder.slot()) {
			return true;
		}
		return read(event.getCurrentItem()).isPresent() || read(event.getCursor()).isPresent();
	}

	private void guardContentsClick(InventoryClickEvent event, Player player) {
		if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
			guardTopContents(event, player);
			return;
		}
		guardShiftContents(event, player);
	}

	private void guardTopContents(InventoryClickEvent event, Player player) {
		if (event.getClick() == ClickType.NUMBER_KEY) {
			int hotbar = event.getHotbarButton();
			ItemStack swapped = hotbar < 0 ? null : player.getInventory().getItem(hotbar);
			refuseUnlessCash(event, player, swapped);
			return;
		}
		refuseUnlessCash(event, player, event.getCursor());
	}

	private void guardShiftContents(InventoryClickEvent event, Player player) {
		if (event.isShiftClick()) {
			refuseUnlessCash(event, player, event.getCurrentItem());
		}
	}

	private void refuseUnlessCash(InventoryClickEvent event, Player player, ItemStack item) {
		if (isAir(item) || walletItems.isCashItem(item)) {
			return;
		}
		event.setCancelled(true);
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Wallet.NotCash"));
	}

	@EventHandler
	public void onContentsDrag(InventoryDragEvent event) {
		if (!(event.getView().getTopInventory().getHolder() instanceof ContentsHolder)
			|| !(event.getWhoClicked() instanceof Player player)
			|| walletItems == null) {
			if (event.getView().getTopInventory().getHolder() instanceof ContentsHolder) {
				event.setCancelled(true);
			}
			return;
		}
		int topSize = event.getView().getTopInventory().getSize();
		boolean affectsTop = event.getRawSlots().stream().anyMatch(slot -> slot < topSize);
		if (affectsTop && !walletItems.isCashItem(event.getOldCursor())) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Wallet.NotCash"));
		}
	}

	@EventHandler
	public void onContentsClose(InventoryCloseEvent event) {
		if (!(event.getInventory().getHolder() instanceof ContentsHolder holder)
			|| !(event.getPlayer() instanceof Player player)
			|| walletItems == null) {
			return;
		}
		List<Cash.Stack> stacks = new ArrayList<>();
		if (holder.hidden() != null) {
			stacks.addAll(holder.hidden());
		}
		for (ItemStack item : event.getInventory().getContents()) {
			if (isAir(item)) {
				continue;
			}
			Optional<Cash.Stack> cashStack = walletItems.readCashItem(item);
			if (cashStack.isPresent()) {
				stacks.add(cashStack.get());
			}
			else {
				player.getInventory().addItem(item);
			}
		}
		Cassette.Payload draft = new Cassette.Payload(holder.id(), stacks, holder.pin(), holder.failedAttempts());
		Cassette.Fit fit = Cassette.fit(draft, cash.cassetteCapacity(), cash.descendingValues());
		if (!fit.overflow().isEmpty()) {
			for (Cash.Stack stack : CashTender.toInventoryStacks(fit.overflow())) {
				player.getInventory().addItem(walletItems.createCashItem(stack));
			}
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.TooMuch"));
		}
		replaceCassette(player, holder, fit.kept());
		if (fit.kept().total() != holder.previousTotal()) {
			log("CASSETTE_EDIT", player.getName()
				+ " cassette=#" + Cassette.shortId(holder.id())
				+ " amount=" + fit.kept().total());
		}
	}

	void handlePin(Player player, PinPrompt prompt, String message) {
		if (prompt.expired(System.currentTimeMillis()) || Atm.isCancelPrompt(message)) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.PinCancel"));
			return;
		}
		ItemStack hand = itemIn(player, prompt.hand());
		Optional<Cassette.Payload> payload = read(hand);
		if (payload.isEmpty()) {
			return;
		}
		String typed = message.trim();
		if (!Cassette.isPin(typed)) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.PinInvalid"));
			return;
		}
		if (prompt.mode() == PinMode.SET) {
			Cassette.Payload locked = Cassette.withPin(payload.get(), typed);
			setHand(player, prompt.hand(), create(locked));
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.PinSaved"));
			openContents(player, locked, prompt.hand());
			return;
		}
		Cassette.PinAttempt attempt = Cassette.tryPin(payload.get(), typed);
		setHand(player, prompt.hand(), create(attempt.payload()));
		if (attempt.unlocked()) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.PinOk"));
			openContents(player, attempt.payload(), prompt.hand());
			return;
		}
		if (attempt.wiped()) {
			player.sendMessage(getShortPrefixString() + cash.applyAmount(
				getConfigString("Economy.Cash.Cassette.PinLost"),
				payload.get().total()
			));
			log("CASSETTE_WIPE", player.getName()
				+ " cassette=#" + Cassette.shortId(payload.get().id())
				+ " amount=" + payload.get().total());
			return;
		}
		int left = Cassette.MAX_PIN_ATTEMPTS - attempt.payload().failedAttempts();
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.PinWrong")
			.replace("%tries%", Integer.toString(left)));
	}

	private void openContents(Player player, Cassette.Payload payload, EquipmentSlot hand) {
		List<Cash.Stack> shown = new ArrayList<>();
		List<Cash.Stack> hidden = new ArrayList<>();
		int limit = 54;
		for (Cash.Stack stack : CashTender.toInventoryStacks(payload.stacks())) {
			if (shown.size() < limit) {
				shown.add(stack);
			}
			else {
				hidden.add(stack);
			}
		}
		int sourceSlot = hand == EquipmentSlot.OFF_HAND ? 40 : player.getInventory().getHeldItemSlot();
		Inventory inventory = org.bukkit.Bukkit.createInventory(
			new ContentsHolder(payload.id(), payload.pin(), payload.failedAttempts(), sourceSlot, hidden, payload.total()),
			limit,
			labeled(getConfigString("Economy.Cash.Cassette.OpenedTitle"), payload.id())
		);
		for (int index = 0; index < shown.size(); index++) {
			inventory.setItem(index, cashItem(shown.get(index)));
		}
		player.openInventory(inventory);
		if (!hidden.isEmpty()) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.MoreInside"));
		}
	}

	private ItemStack cashItem(Cash.Stack stack) {
		if (walletItems != null) {
			return walletItems.createCashItem(stack);
		}
		Cash.Denomination denomination = cash.denomination(stack.denomination()).orElse(null);
		Material material = denomination == null ? Material.PAPER : denomination.material();
		ItemStack item = new ItemStack(material, Math.min(stack.amount(), Cash.MAX_STACK_SIZE));
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(denomination == null
				? stack.denomination() + cash.currency()
				: denomination.displayName());
			if (denomination != null && denomination.customModelData() != null) {
				Cash.applyCustomModelData(meta, denomination.customModelData());
			}
			item.setItemMeta(meta);
		}
		return item;
	}

	private void replaceCassette(Player player, ContentsHolder holder, Cassette.Payload payload) {
		ItemStack updated = create(payload);
		ItemStack current = player.getInventory().getItem(holder.slot());
		if (matches(current, holder.id())) {
			player.getInventory().setItem(holder.slot(), updated);
			return;
		}
		ItemStack[] contents = player.getInventory().getStorageContents();
		if (contents != null) {
			for (int index = 0; index < contents.length; index++) {
				if (matches(contents[index], holder.id())) {
					player.getInventory().setItem(index, updated);
					return;
				}
			}
		}
		if (matches(player.getInventory().getItemInOffHand(), holder.id())) {
			player.getInventory().setItemInOffHand(updated);
		}
	}

	private boolean matches(ItemStack item, String id) {
		return read(item).filter(payload -> payload.id().equals(id)).isPresent();
	}

	private static boolean isSourceSlot(InventoryClickEvent event, Player player, int slot) {
		return event.getClickedInventory() != null
			&& event.getClickedInventory().equals(player.getInventory())
			&& event.getSlot() == slot;
	}

	private static boolean isAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR || item.getAmount() <= 0;
	}

	static String labeled(String template, String id) {
		String tag = Cassette.shortId(id);
		if (template != null && template.contains("%id%")) {
			return template.replace("%id%", tag);
		}
		String base = template == null ? "" : template;
		return base.isBlank() ? "#" + tag : base + " #" + tag;
	}

	private void log(String action, String details) {
		if (logger != null) {
			logger.log(action, details);
		}
	}

	private static ItemStack itemIn(Player player, EquipmentSlot hand) {
		return hand == EquipmentSlot.OFF_HAND
			? player.getInventory().getItemInOffHand()
			: player.getInventory().getItemInMainHand();
	}

	private static void setHand(Player player, EquipmentSlot hand, ItemStack item) {
		if (hand == EquipmentSlot.OFF_HAND) {
			player.getInventory().setItemInOffHand(item);
		}
		else {
			player.getInventory().setItemInMainHand(item);
		}
	}

	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent event) {
		if (!isCassetteRecipe(event.getRecipe())) {
			return;
		}
		if (!(event.getView().getPlayer() instanceof Player player) || !canCraft(player)) {
			event.getInventory().setResult(null);
			return;
		}
		player.discoverRecipe(recipeKey);
		event.getInventory().setResult(create(Cassette.empty()));
	}

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		if (!isCassetteRecipe(event.getRecipe()) || !(event.getWhoClicked() instanceof Player player)) {
			return;
		}
		if (!canCraft(player)) {
			event.setCancelled(true);
			String key = advancedBankLive()
				? "Economy.Cash.Cassette.DeniedCraft"
				: "Economy.Cash.NeedAdvancedBank";
			player.sendMessage(getShortPrefixString() + getConfigString(key));
			return;
		}
		if (event.isShiftClick()) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.ShiftCraft"));
			return;
		}
		event.setCurrentItem(create(Cassette.empty()));
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.Crafted"));
	}

	private boolean canCraft(Player player) {
		return player != null && advancedBankLive();
	}
}
