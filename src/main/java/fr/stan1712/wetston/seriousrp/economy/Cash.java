package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;

public final class Cash {
	public static final String ITEM_PDC_KEY = "srp-cash";
	public static final String WALLET_PDC_KEY = "srp-wallet";
	public static final String ATM_OWNER_PDC_KEY = "srp-atm-owner";
	public static final int MAX_STACK_SIZE = 64;
	private static final int[] DEFAULT_VALUES = {1, 2, 5, 10, 20, 50, 100, 200, 500};
	private static final List<Integer> DEFAULT_PRESETS = List.of(10, 25, 50, 100);
	private static final List<String> DEFAULT_RECIPE_SHAPE = List.of(" L ", "LPL", " L ");
	private static final Pattern COLOR_CODE = Pattern.compile("§[0-9a-fk-orxA-FK-ORX]");

	public record Denomination(int value, Material material, String displayName, Integer customModelData) {
	}

	public record Stack(int denomination, int amount) {
		public Stack {
			if (denomination <= 0 || amount <= 0) {
				throw new IllegalArgumentException("denomination and amount must be positive");
			}
		}

		public long total() {
			return (long) denomination * (long) amount;
		}
	}

	private final boolean enabled;
	private final String currency;
	private final List<Denomination> denominations;
	private final Map<Integer, Denomination> byValue;
	private final int walletSlots;
	private final Material walletMaterial;
	private final String walletDisplayName;
	private final String walletLoreTotal;
	private final List<String> recipeShape;
	private final Map<Character, Material> recipeIngredients;
	private final String atmHeader;
	private final int viewRadius;
	private final List<Integer> atmPresets;

	Cash(
		boolean enabled,
		String currency,
		List<Denomination> denominations,
		int walletSlots,
		Material walletMaterial,
		String walletDisplayName,
		String walletLoreTotal,
		List<String> recipeShape,
		Map<Character, Material> recipeIngredients,
		String atmHeader,
		int viewRadius,
		List<Integer> atmPresets
	) {
		this.enabled = enabled;
		this.currency = currency;
		this.denominations = List.copyOf(denominations);
		Map<Integer, Denomination> index = new LinkedHashMap<>();
		for (Denomination denomination : this.denominations) {
			index.putIfAbsent(denomination.value(), denomination);
		}
		this.byValue = Map.copyOf(index);
		this.walletSlots = clampSlots(walletSlots);
		this.walletMaterial = walletMaterial;
		this.walletDisplayName = walletDisplayName;
		this.walletLoreTotal = walletLoreTotal;
		this.recipeShape = List.copyOf(recipeShape);
		this.recipeIngredients = Map.copyOf(recipeIngredients);
		this.atmHeader = atmHeader;
		this.viewRadius = Math.max(1, viewRadius);
		this.atmPresets = List.copyOf(atmPresets);
	}

	public static Cash fromConfig(FileConfiguration config) {
		boolean enabled = config.getBoolean("Economy.Cash.Enabled", true);
		String currency = colorize(nullable(config.getString("Economy.Currency"), "€"));
		List<Denomination> denoms = parseDenominations(config.getMapList("Economy.Cash.Denominations"));
		if (denoms.isEmpty()) {
			denoms = defaultDenominations(currency);
		}
		denoms.sort(Comparator.comparingInt(Denomination::value).reversed());

		int slots = config.getInt("Economy.Cash.Wallet.DefaultSlots", 18);
		Material walletMaterial = materialOr(config.getString("Economy.Cash.Wallet.Material"), Material.LEATHER);
		String walletName = colorize(nullable(config.getString("Economy.Cash.Wallet.DisplayName"), "&6Portefeuille"));
		String walletLore = colorize(nullable(
			config.getString("Economy.Cash.Wallet.LoreTotal"),
			"&7Contenu : &e%amount%%currency%"
		));

		List<String> shape = config.getStringList("Economy.Cash.Wallet.Recipe.Shape");
		if (shape.size() != 3) {
			shape = DEFAULT_RECIPE_SHAPE;
		}
		Map<Character, Material> ingredients = parseIngredients(
			config.getConfigurationSection("Economy.Cash.Wallet.Recipe.Ingredients")
		);
		if (ingredients.isEmpty()) {
			ingredients = Map.of('L', Material.LEATHER, 'P', Material.PAPER);
		}

		String header = nullable(config.getString("Economy.Cash.Atm.SignHeader"), "[sATM]");
		int radius = config.getInt("Economy.Cash.Atm.ViewRadius", 5);
		List<Integer> presets = parsePresets(config.getIntegerList("Economy.Cash.Atm.Presets"));

		return new Cash(
			enabled,
			currency,
			denoms,
			slots,
			walletMaterial,
			walletName,
			walletLore,
			shape,
			ingredients,
			header,
			radius,
			presets
		);
	}

	public static int clampSlots(int slots) {
		if (slots == 9 || slots == 18 || slots == 27) {
			return slots;
		}
		return 18;
	}

	public static String colorize(String value) {
		if (value == null) {
			return "";
		}
		return value.replace('&', '§');
	}

	public static OptionalInt parsePositiveInt(String raw) {
		if (raw == null) {
			return OptionalInt.empty();
		}
		try {
			int parsed = Integer.parseInt(raw);
			if (parsed <= 0) {
				return OptionalInt.empty();
			}
			return OptionalInt.of(parsed);
		}
		catch (NumberFormatException exception) {
			return OptionalInt.empty();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String currency() {
		return currency;
	}

	public List<Denomination> denominations() {
		return denominations;
	}

	public List<Integer> descendingValues() {
		return denominations.stream().map(Denomination::value).toList();
	}

	public Optional<Denomination> denomination(int value) {
		return Optional.ofNullable(byValue.get(value));
	}

	public boolean isDenomination(int value) {
		return byValue.containsKey(value);
	}

	public int walletSlots() {
		return walletSlots;
	}

	public Material walletMaterial() {
		return walletMaterial;
	}

	public String walletDisplayName() {
		return walletDisplayName;
	}

	public String walletLoreTotal() {
		return walletLoreTotal;
	}

	public List<String> recipeShape() {
		return recipeShape;
	}

	public Map<Character, Material> recipeIngredients() {
		return recipeIngredients;
	}

	public String atmHeader() {
		return atmHeader;
	}

	public boolean isAtmHeader(String line) {
		return line != null && atmHeader.equalsIgnoreCase(stripColorCodes(line).trim());
	}

	public int viewRadius() {
		return viewRadius;
	}

	public List<Integer> atmPresets() {
		return atmPresets;
	}

	public String formatTotal(int amount) {
		return walletLoreTotal
			.replace("%amount%", Integer.toString(amount))
			.replace("%currency%", currency);
	}

	static String stripColorCodes(String line) {
		return COLOR_CODE.matcher(line.replace('&', '§')).replaceAll("");
	}

	private static List<Denomination> parseDenominations(List<Map<?, ?>> rows) {
		List<Denomination> parsed = new ArrayList<>();
		for (Map<?, ?> row : rows) {
			int value = toPositiveInt(row.get("value"));
			if (value <= 0) {
				continue;
			}
			Material material = materialOr(stringOrNull(row.get("material")), null);
			if (material == null) {
				continue;
			}
			if (parsed.stream().anyMatch(existing -> existing.value() == value)) {
				continue;
			}
			String name = colorize(nullable(stringOrNull(row.get("name")), value + "€"));
			Integer model = toNullableInt(row.get("custom-model-data"));
			parsed.add(new Denomination(value, material, name, model));
		}
		return parsed;
	}

	private static List<Denomination> defaultDenominations(String currency) {
		List<Denomination> defaults = new ArrayList<>();
		for (int value : DEFAULT_VALUES) {
			Material material = value <= 2 ? Material.GOLD_NUGGET : Material.PAPER;
			if (value == 2) {
				material = Material.GOLD_INGOT;
			}
			String name = colorize("&e" + value + currency);
			Integer model = value >= 5 ? value : null;
			defaults.add(new Denomination(value, material, name, model));
		}
		defaults.sort(Comparator.comparingInt(Denomination::value).reversed());
		return defaults;
	}

	private static Map<Character, Material> parseIngredients(ConfigurationSection section) {
		if (section == null) {
			return Map.of();
		}
		Map<Character, Material> ingredients = new LinkedHashMap<>();
		for (String key : section.getKeys(false)) {
			if (key == null || key.length() != 1) {
				continue;
			}
			Material material = materialOr(section.getString(key), null);
			if (material != null) {
				ingredients.put(key.charAt(0), material);
			}
		}
		return ingredients;
	}

	private static List<Integer> parsePresets(List<Integer> configured) {
		List<Integer> presets = new ArrayList<>();
		for (Integer preset : configured) {
			if (preset != null && preset > 0 && !presets.contains(preset)) {
				presets.add(preset);
			}
		}
		if (presets.isEmpty()) {
			return DEFAULT_PRESETS;
		}
		return presets;
	}

	private static Material materialOr(String raw, Material fallback) {
		if (raw == null || raw.isBlank()) {
			return fallback;
		}
		try {
			return Material.valueOf(raw.trim().toUpperCase());
		}
		catch (IllegalArgumentException exception) {
			return fallback;
		}
	}

	private static int toPositiveInt(Object raw) {
		Integer parsed = toNullableInt(raw);
		return parsed == null ? 0 : parsed;
	}

	private static Integer toNullableInt(Object raw) {
		if (raw instanceof Number number) {
			int value = number.intValue();
			return value > 0 ? value : null;
		}
		if (raw instanceof String text) {
			OptionalInt parsed = parsePositiveInt(text);
			return parsed.isPresent() ? parsed.getAsInt() : null;
		}
		return null;
	}

	private static String stringOrNull(Object raw) {
		return raw instanceof String text ? text : null;
	}

	private static String nullable(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}
}
