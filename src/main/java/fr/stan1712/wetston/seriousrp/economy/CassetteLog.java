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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public final class CassetteLog implements Listener {
	static final int SIZE = 54;
	private static final int LIST_SLOTS = 45;
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
		.withZone(ZoneId.systemDefault());

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
		String currency = getConfigString("Economy.Cash.Currency");
		if (lines.isEmpty()) {
			inventory.setItem(22, entry(getConfigString("Economy.Cash.Cassette.LogEmpty"), List.of()));
		}
		else {
			int slot = 0;
			for (String line : lines) {
				inventory.setItem(slot++, entry(actionName(line), loreLines(line, currency)));
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

	static String actor(Player player) {
		if (player == null) {
			return "unknown";
		}
		return actor(player.getName(), player.getDisplayName());
	}

	static String actor(String name, String displayName) {
		String safeName = name == null || name.isBlank() ? "unknown" : name;
		String display = Cash.stripColorCodes(displayName == null ? "" : displayName).trim();
		if (display.isEmpty() || display.equals(safeName)) {
			return safeName;
		}
		return safeName + " display=" + URLEncoder.encode(display, StandardCharsets.UTF_8);
	}

	static String actionName(String line) {
		String[] parts = line.split(" ", 3);
		String action = parts.length > 1 ? parts[1] : line;
		return "§e" + action;
	}

	static List<String> loreLines(String line) {
		return loreLines(line, "€");
	}

	static List<String> loreLines(String line, String currency) {
		Parsed parsed = parse(line);
		String money = currency == null || currency.isBlank() ? "€" : currency;
		List<String> lore = new ArrayList<>();
		String shown = parsed.display == null || parsed.display.isBlank() ? parsed.user : parsed.display;
		if (shown != null && !shown.isBlank()) {
			lore.add("§7User: §f" + shown);
		}
		if (parsed.amount != null) {
			lore.add("§7Amount: " + money(signedAmount(parsed), money));
		}
		if (!parsed.cassettes.isEmpty()) {
			lore.add("§7Cassettes: " + cassettes(parsed, money));
		}
		if (parsed.date != null && !parsed.date.isBlank()) {
			lore.add("§7Date: §f" + parsed.date);
		}
		return lore;
	}

	private static String cassettes(Parsed parsed, String currency) {
		StringBuilder text = new StringBuilder();
		for (CassetteMove move : parsed.cassettes) {
			if (!text.isEmpty()) {
				text.append("§7, ");
			}
			text.append("§f#").append(move.id);
			Integer delta = move.delta;
			if (delta == null && parsed.cassettes.size() == 1 && parsed.amount != null) {
				delta = signedAmount(parsed);
			}
			if (delta != null && delta != 0) {
				text.append(' ').append(money(delta, currency));
			}
		}
		return text.toString();
	}

	private static int signedAmount(Parsed parsed) {
		if (parsed.delta != null) {
			return parsed.delta;
		}
		int amount = parsed.amount == null ? 0 : parsed.amount;
		return switch (parsed.action) {
			case "WITHDRAW", "CASSETTE_EMPTY", "CASSETTE_WIPE", "EJECT" -> -Math.abs(amount);
			case "DEPOSIT", "CASSETTE_FILL", "INSTALL" -> Math.abs(amount);
			default -> amount;
		};
	}

	private static String money(int signed, String currency) {
		if (signed > 0) {
			return "§a+" + signed + currency;
		}
		if (signed < 0) {
			return "§c" + signed + currency;
		}
		return "§f0" + currency;
	}

	private static Parsed parse(String line) {
		String[] parts = line == null ? new String[0] : line.split(" ");
		String action = parts.length > 1 ? parts[1] : "";
		String user = null;
		String display = null;
		Integer amount = null;
		Integer delta = null;
		List<CassetteMove> cassettes = new ArrayList<>();
		for (int index = 2; index < parts.length; index++) {
			String token = parts[index];
			if (token.isBlank()) {
				continue;
			}
			int eq = token.indexOf('=');
			if (eq < 0) {
				if (user == null) {
					user = token;
				}
				continue;
			}
			String key = token.substring(0, eq);
			String value = token.substring(eq + 1);
			switch (key) {
				case "user", "player" -> user = value;
				case "display" -> display = decode(value);
				case "amount" -> amount = parseInt(value);
				case "delta" -> delta = parseInt(value);
				case "cassette", "cassettes" -> cassettes.addAll(parseCassettes(value));
				default -> {
					// cash, stock, slot stay out of the readable view
				}
			}
		}
		return new Parsed(action, user, display, amount, delta, cassettes, formatDate(parts.length > 0 ? parts[0] : ""));
	}

	private static List<CassetteMove> parseCassettes(String value) {
		List<CassetteMove> moves = new ArrayList<>();
		if (value == null || value.isBlank()) {
			return moves;
		}
		for (String piece : value.split(",")) {
			if (piece.isBlank()) {
				continue;
			}
			String body = piece.startsWith("#") ? piece.substring(1) : piece;
			int colon = body.lastIndexOf(':');
			if (colon > 0) {
				moves.add(new CassetteMove(body.substring(0, colon), parseInt(body.substring(colon + 1))));
			}
			else {
				moves.add(new CassetteMove(body, null));
			}
		}
		return moves;
	}

	private static String formatDate(String raw) {
		if (raw == null || raw.isBlank()) {
			return "";
		}
		try {
			return DATE.format(Instant.parse(raw));
		}
		catch (DateTimeParseException exception) {
			return raw.length() > 19 ? raw.substring(0, 19).replace('T', ' ') : raw.replace('T', ' ');
		}
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	private static Integer parseInt(String value) {
		try {
			return Integer.valueOf(value);
		}
		catch (NumberFormatException exception) {
			return null;
		}
	}

	private record CassetteMove(String id, Integer delta) {
	}

	private record Parsed(
		String action,
		String user,
		String display,
		Integer amount,
		Integer delta,
		List<CassetteMove> cassettes,
		String date
	) {
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
