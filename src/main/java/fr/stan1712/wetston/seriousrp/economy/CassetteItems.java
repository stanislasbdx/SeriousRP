package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;
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

	public CassetteItems(Plugin plugin, Cash cash) {
		this(plugin, cash, org.bukkit.Bukkit::addRecipe, org.bukkit.Bukkit::removeRecipe);
	}

	CassetteItems(
		Plugin plugin,
		Cash cash,
		Consumer<ShapedRecipe> recipeSink,
		Consumer<NamespacedKey> recipeRemover
	) {
		this.plugin = plugin;
		this.cash = cash;
		this.itemKey = new NamespacedKey(plugin, Cash.CASSETTE_ITEM_PDC_KEY);
		this.recipeKey = new NamespacedKey(plugin, RECIPE_KEY);
		this.recipeSink = recipeSink;
		this.recipeRemover = recipeRemover;
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
		meta.setLore(List.of(cash.applyAmount(cash.cassetteLoreTotal(), payload.total())));
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
		Player player = event.getPlayer();
		if (advancedBankLive() && player.hasPermission(Atm.SERVICE_PERM)) {
			player.discoverRecipe(recipeKey);
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
	}

	private boolean canCraft(Player player) {
		return advancedBankLive() && player.hasPermission(Atm.SERVICE_PERM);
	}
}
