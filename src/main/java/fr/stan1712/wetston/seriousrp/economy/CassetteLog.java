package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public final class CassetteLog implements Listener {
	static final int SIZE = 54;
	private static final int LIST_SLOTS = 45;

	public interface InventoryFactory {
		Inventory create(InventoryHolder holder, int size, String title);
	}

	record Holder() implements InventoryHolder {
		@Override
		public Inventory getInventory() {
			throw new UnsupportedOperationException("Cassette log holder is a GUI marker");
		}
	}

	public static void open(Player player, InventoryFactory inventories, AtmLogger logger, String cassetteId) {
		if (!player.hasPermission(Atm.LOG_PERM)) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Cassette.LogDenied"));
			return;
		}
		String shortId = cassetteId == null || cassetteId.isBlank() ? null : Cassette.shortId(cassetteId);
		String title = shortId == null
			? getConfigString("Economy.Cash.Cassette.LogTitle")
			: getConfigString("Economy.Cash.Cassette.LogTitleOne").replace("%id%", shortId);
		Inventory inventory = inventories.create(new Holder(), SIZE, title);
		ItemStack edge = pane();
		for (int slot = LIST_SLOTS; slot < SIZE; slot++) {
			inventory.setItem(slot, edge);
		}
		String needle = shortId == null ? null : "#" + shortId;
		List<String> lines = logger == null ? List.of() : logger.recentCassetteLines(LIST_SLOTS, needle);
		if (lines.isEmpty()) {
			inventory.setItem(22, entry(getConfigString("Economy.Cash.Cassette.LogEmpty"), List.of()));
		}
		else {
			int slot = 0;
			for (String line : lines) {
				inventory.setItem(slot++, entry(actionName(line), loreLines(line)));
			}
		}
		player.openInventory(inventory);
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (event.getView().getTopInventory().getHolder() instanceof Holder) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		if (event.getView().getTopInventory().getHolder() instanceof Holder) {
			event.setCancelled(true);
		}
	}

	static String actionName(String line) {
		String[] parts = line.split(" ", 3);
		String action = parts.length > 1 ? parts[1] : line;
		return "§e" + action;
	}

	static List<String> loreLines(String line) {
		String[] parts = line.split(" ", 3);
		String when = parts.length > 0 ? parts[0] : "";
		if (when.length() > 19) {
			when = when.substring(0, 19);
		}
		List<String> lore = new ArrayList<>();
		lore.add("§7" + when);
		String details = parts.length > 2 ? parts[2] : "";
		StringBuilder current = new StringBuilder();
		for (String word : details.split(" ")) {
			if (word.isEmpty()) {
				continue;
			}
			if (current.length() > 0 && current.length() + 1 + word.length() > 36) {
				lore.add("§f" + current);
				current.setLength(0);
			}
			if (current.length() > 0) {
				current.append(' ');
			}
			current.append(word);
		}
		if (current.length() > 0) {
			lore.add("§f" + current);
		}
		return lore;
	}

	private static ItemStack pane() {
		ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(" ");
			item.setItemMeta(meta);
		}
		return item;
	}

	private static ItemStack entry(String name, List<String> lore) {
		ItemStack item = new ItemStack(Material.PAPER);
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(name);
			if (!lore.isEmpty()) {
				meta.setLore(lore);
			}
			item.setItemMeta(meta);
		}
		return item;
	}
}
