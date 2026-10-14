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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public final class PilotListener implements Listener {
	static final String OWNER_KEY = "srp-pilot-owner";
	static final int GUI_SIZE = 54;

	record Holder() implements InventoryHolder {
		@Override
		public Inventory getInventory() {
			throw new UnsupportedOperationException("Pilot holder is a GUI marker");
		}
	}

	interface InventoryFactory {
		Inventory create(InventoryHolder holder, int size, String title);
	}

	interface MainThread {
		void run(Runnable task);
	}

	private final Cash cash;
	private final AtmRegistry registry;
	private final NamespacedKey ownerKey;
	private final NamespacedKey cassettesKey;
	private final MainThread mainThread;
	private final InventoryFactory inventories;

	public PilotListener(Plugin plugin, Cash cash, AtmRegistry registry) {
		this(
			plugin,
			cash,
			registry,
			task -> plugin.getServer().getScheduler().runTask(plugin, task),
			AtmListener::createInventory
		);
	}

	PilotListener(
		Plugin plugin,
		Cash cash,
		AtmRegistry registry,
		MainThread mainThread,
		InventoryFactory inventories
	) {
		this.cash = cash;
		this.registry = registry;
		this.ownerKey = new NamespacedKey(plugin, OWNER_KEY);
		this.cassettesKey = new NamespacedKey(plugin, Cash.ATM_CASSETTES_PDC_KEY);
		this.mainThread = mainThread;
		this.inventories = inventories;
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (!cash.isEnabled() || !cash.physicalFloat() || !cash.isPilotHeader(event.getLine(0))) {
			return;
		}
		Player player = event.getPlayer();
		if (!player.hasPermission(Atm.PILOT_CREATE_PERM)) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Pilot.DeniedCreate"));
			return;
		}
		event.setLine(0, Cash.colorize("&3&l[sPilot]"));
		event.setLine(1, Cash.colorize("&8&m              "));
		event.setLine(2, Cash.colorize("&7Réseau ATM"));
		event.setLine(3, Cash.colorize("&8» &fClic droit"));
		UUID owner = player.getUniqueId();
		mainThread.run(() -> stampOwner(event.getBlock(), owner));
		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Pilot.Created"));
	}

	@EventHandler(ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		Optional<UUID> owner = pilotOwner(event.getBlock());
		if (owner.isEmpty()) {
			return;
		}
		Player player = event.getPlayer();
		if (!Atm.canBreak(
			owner.get(),
			player.getUniqueId(),
			player.hasPermission(Atm.PILOT_CREATE_PERM),
			player.hasPermission(Atm.PILOT_OTHERS_PERM)
		)) {
			event.setCancelled(true);
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Pilot.DeniedBreak"));
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		if (!cash.isEnabled() || !cash.physicalFloat() || event.getAction() != Action.RIGHT_CLICK_BLOCK
			|| event.getClickedBlock() == null) {
			return;
		}
		if (pilotOwner(event.getClickedBlock()).isEmpty()) {
			return;
		}
		event.setCancelled(true);
		openGui(event.getPlayer());
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (event.getView().getTopInventory().getHolder() instanceof Holder) {
			event.setCancelled(true);
		}
	}

	void openGui(Player player) {
		Inventory inventory = inventories.create(new Holder(), GUI_SIZE, getConfigString("Economy.Cash.Pilot.Title"));
		List<AtmRegistry.Entry> entries = player.hasPermission(Atm.PILOT_OTHERS_PERM)
			? registry.all()
			: registry.ownedBy(player.getUniqueId());
		int slot = 0;
		for (AtmRegistry.Entry entry : entries) {
			if (slot >= GUI_SIZE) {
				break;
			}
			inventory.setItem(slot++, lineItem(entry));
		}
		player.openInventory(inventory);
	}

	private ItemStack lineItem(AtmRegistry.Entry entry) {
		Optional<Location> location = entry.location();
		int total = location.map(this::floatTotal).orElse(0);
		boolean hasCassette = location.map(this::hasCassette).orElse(false);
		String template = hasCassette
			? getConfigString("Economy.Cash.Pilot.Line")
			: getConfigString("Economy.Cash.Pilot.EmptyAtm");
		String name = template
			.replace("%world%", entry.world())
			.replace("%x%", Integer.toString(entry.x()))
			.replace("%y%", Integer.toString(entry.y()))
			.replace("%z%", Integer.toString(entry.z()));
		name = cash.applyAmount(name, total);
		ItemStack item = new ItemStack(hasCassette ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}

	private int floatTotal(Location location) {
		return AtmCassettes.total(readCassettes(location.getBlock()));
	}

	private boolean hasCassette(Location location) {
		return AtmCassettes.hasCassette(readCassettes(location.getBlock()));
	}

	private List<Optional<Cassette.Payload>> readCassettes(Block block) {
		if (!(block.getState() instanceof TileState state)) {
			return AtmCassettes.emptySlots(cash.cassetteSlots());
		}
		String raw = state.getPersistentDataContainer().get(cassettesKey, PersistentDataType.STRING);
		return AtmCassettes.fromJson(raw, cash.cassetteSlots());
	}

	private void stampOwner(Block block, UUID owner) {
		if (!(block.getState() instanceof TileState state)) {
			return;
		}
		state.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, owner.toString());
		state.update();
	}

	Optional<UUID> pilotOwner(Block block) {
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
}
