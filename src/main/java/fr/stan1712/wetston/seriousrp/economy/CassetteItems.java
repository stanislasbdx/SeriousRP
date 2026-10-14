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
import org.bukkit.event.inventory.InventoryClickEvent;
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

	enum PinMode {
		SET,
		OPEN
	}

	record PinPrompt(UUID playerId, EquipmentSlot hand, PinMode mode, long expiresAtMillis) {
		boolean expired(long now) {
			return now >= expiresAtMillis;
		}
	}

	record ContentsHolder(String id) implements InventoryHolder {
		@Override
		public Inventory getInventory() {
			throw new UnsupportedOperationException("Cassette contents are read-only");
		}
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
		meta.setDisplayName(cash.cassetteDisplayName());
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
		if (event.getView().getTopInventory().getHolder() instanceof ContentsHolder) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onContentsDrag(InventoryDragEvent event) {
		if (event.getView().getTopInventory().getHolder() instanceof ContentsHolder) {
			event.setCancelled(true);
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
			openContents(player, locked);
			return;
		}
		Cassette.PinAttempt attempt = Cassette.tryPin(payload.get(), typed);
		setHand(player, prompt.hand(), create(attempt.payload()));
		if (attempt.unlocked()) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.PinOk"));
			openContents(player, attempt.payload());
			return;
		}
		if (attempt.wiped()) {
			player.sendMessage(getShortPrefixString() + cash.applyAmount(
				getConfigString("Economy.Cash.Cassette.PinLost"),
				payload.get().total()
			));
			return;
		}
		int left = Cassette.MAX_PIN_ATTEMPTS - attempt.payload().failedAttempts();
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.PinWrong")
			.replace("%tries%", Integer.toString(left)));
	}

	private void openContents(Player player, Cassette.Payload payload) {
		Inventory inventory = org.bukkit.Bukkit.createInventory(
			new ContentsHolder(payload.id()),
			27,
			getConfigString("Economy.Cash.Cassette.OpenedTitle")
		);
		int slot = 0;
		for (Cash.Stack stack : CashTender.toInventoryStacks(payload.stacks())) {
			if (slot >= inventory.getSize()) {
				break;
			}
			inventory.setItem(slot++, displayStack(stack));
		}
		player.openInventory(inventory);
	}

	private ItemStack displayStack(Cash.Stack stack) {
		Cash.Denomination denomination = cash.denomination(stack.denomination()).orElse(null);
		Material material = denomination == null ? Material.PAPER : denomination.material();
		ItemStack item = new ItemStack(material, Math.min(stack.amount(), Cash.MAX_STACK_SIZE));
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(denomination == null
				? stack.denomination() + cash.currency()
				: denomination.displayName());
			item.setItemMeta(meta);
		}
		return item;
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
