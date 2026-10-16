package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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
	public static final String ATM_NAME_PDC_KEY = "srp-atm-name";
	public static final String CASSETTE_ITEM_PDC_KEY = "srp-atm-cassette";
	public static final String ATM_CASSETTES_PDC_KEY = "srp-atm-cassettes";
	public static final String BANK_OWNER_PDC_KEY = "srp-bank-owner";
	public static final String BANK_STOCK_PDC_KEY = "srp-bank-stock";
	public static final String BANK_PIN_PDC_KEY = "srp-bank-pin";
	public static final int MAX_STACK_SIZE = 64;
	private static final int[] DEFAULT_VALUES = {1, 2, 5, 10, 20, 50, 100, 200, 500};
	private static final List<Integer> DEFAULT_PRESETS = List.of(10, 25, 50, 100);
	private static final int DEFAULT_CREATE_COST = 150;
	private static final int DEFAULT_BREAK_PIECE_CAP = 64;
	private static final int DEFAULT_BREAK_MAX_DENOMINATION = 20;
	public static final int MAX_CASSETTE_SLOTS = 8;
	private static final int DEFAULT_CASSETTE_SLOTS = 2;
	private static final int DEFAULT_CASSETTE_CAPACITY = 50_000;
	private static final int DEFAULT_BANK_CAPACITY = 500_000;
	private static final List<String> DEFAULT_RECIPE_SHAPE = List.of(" L ", "LPL", " L ");
	private static final List<String> DEFAULT_CASSETTE_SHAPE = List.of("BIB", "IRI", "BIB");
	private static final Map<Character, Material> DEFAULT_CASSETTE_INGREDIENTS = Map.of(
		'B', Material.IRON_BLOCK,
		'I', Material.IRON_INGOT,
		'R', Material.BARREL
	);
	private static final Pattern COLOR_CODE = Pattern.compile("§[0-9a-fk-orxA-FK-ORX]");

	public record Denomination(int value, Material material, String displayName, Integer customModelData) {
		public Denomination {
			customModelData = customModelData != null && customModelData > 0 ? customModelData : null;
		}
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

	public record WalletSettings(
		int slots,
		Material material,
		String displayName,
		String loreTotal,
		List<String> recipeShape,
		Map<Character, Material> recipeIngredients,
		Integer customModelData
	) {
		public WalletSettings {
			slots = clampSlots(slots);
			recipeShape = List.copyOf(recipeShape);
			recipeIngredients = Map.copyOf(recipeIngredients);
			customModelData = customModelData != null && customModelData > 0 ? customModelData : null;
		}
	}

	public record PlacedSign(String title, String separator, String subtitle, String hint) {
	}

	public record AtmSettings(
		String header,
		int viewRadius,
		List<Integer> presets,
		int createCost,
		int cassetteSlots,
		int cassetteCapacity,
		PlacedSign sign,
		String accountPrefix,
		String pocketPrefix
	) {
		public AtmSettings {
			viewRadius = Math.max(1, viewRadius);
			presets = List.copyOf(presets);
			createCost = Math.max(0, createCost);
			cassetteSlots = Math.min(MAX_CASSETTE_SLOTS, Math.max(1, cassetteSlots));
			cassetteCapacity = Math.max(1, cassetteCapacity);
		}
	}

	public record CassetteSettings(
		Material material,
		String displayName,
		String loreTotal,
		List<String> recipeShape,
		Map<Character, Material> recipeIngredients,
		Integer customModelData
	) {
		public CassetteSettings {
			recipeShape = List.copyOf(recipeShape);
			recipeIngredients = Map.copyOf(recipeIngredients);
			customModelData = customModelData != null && customModelData > 0 ? customModelData : null;
		}
	}

	public record BankSettings(String header, String vaultPlayer, int vaultCapacity, PlacedSign sign) {
		public BankSettings {
			vaultCapacity = Math.max(1, vaultCapacity);
			vaultPlayer = vaultPlayer == null || vaultPlayer.isBlank() ? "Bank" : vaultPlayer;
		}
	}

	public record PilotSettings(String header, PlacedSign sign) {
	}

	public record ChangeSettings(int breakPieceCap, int breakMaxDenomination) {
		public ChangeSettings {
			breakPieceCap = Math.max(1, breakPieceCap);
			breakMaxDenomination = Math.max(1, breakMaxDenomination);
		}
	}

	public static final String AMOUNT_PLACEHOLDER = "%amount%";
	public static final String CURRENCY_PLACEHOLDER = "%currency%";

	private final boolean enabled;
	private final boolean advancedBank;
	private final String currency;
	private final List<Denomination> denominations;
	private final Map<Integer, Denomination> byValue;
	private final WalletSettings wallet;
	private final AtmSettings atm;
	private final CassetteSettings cassette;
	private final BankSettings bank;
	private final PilotSettings pilot;
	private final ChangeSettings change;

	Cash(
		boolean enabled,
		boolean advancedBank,
		String currency,
		List<Denomination> denominations,
		WalletSettings wallet,
		AtmSettings atm,
		CassetteSettings cassette,
		BankSettings bank,
		PilotSettings pilot,
		ChangeSettings change
	) {
		this.enabled = enabled;
		this.advancedBank = advancedBank;
		this.currency = currency;
		this.denominations = List.copyOf(denominations);
		Map<Integer, Denomination> index = new LinkedHashMap<>();
		for (Denomination denomination : this.denominations) {
			index.putIfAbsent(denomination.value(), denomination);
		}
		this.byValue = Map.copyOf(index);
		this.wallet = wallet;
		this.atm = atm;
		this.cassette = cassette;
		this.bank = bank;
		this.pilot = pilot;
		this.change = change;
	}

	public static Cash fromConfig(FileConfiguration config) {
		boolean enabled = config.getBoolean("Economy.Cash.Enabled", true);
		boolean advancedBank = config.getBoolean(
			"Economy.Cash.AdvancedBank",
			config.getBoolean("Economy.Cash.Atm.PhysicalFloat", false)
		);
		String currency = colorize(nullable(config.getString("Economy.Currency"), "€"));
		List<Denomination> denoms = parseDenominations(config.getMapList("Economy.Cash.Denominations"));
		if (denoms.isEmpty()) {
			denoms = defaultDenominations(currency);
		}
		denoms.sort(Comparator.comparingInt(Denomination::value).reversed());

		int slots = config.getInt("Economy.Cash.Wallet.DefaultSlots", 18);
		Material walletMaterial = materialOr(config.getString("Economy.Cash.Wallet.Material"), Material.BOOK);
		String walletName = colorize(nullable(config.getString("Economy.Cash.Wallet.DisplayName"), "&6Wallet"));
		String walletLore = colorize(nullable(
			config.getString("Economy.Cash.Wallet.LoreTotal"),
			"&7Content : &e%amount%%currency%"
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
		int walletModel = config.getInt("Economy.Cash.Wallet.CustomModelData", 1);
		Integer walletCustomModelData = walletModel > 0 ? walletModel : null;

		String header = nullable(config.getString("Economy.Cash.Atm.SignHeader"), "[sATM]");
		int radius = config.getInt("Economy.Cash.Atm.ViewRadius", 5);
		int createCost = config.getInt("Economy.Cash.Atm.CreateCost", DEFAULT_CREATE_COST);
		List<Integer> presets = parsePresets(config.getIntegerList("Economy.Cash.Atm.Presets"));
		int cassetteSlots = config.getInt("Economy.Cash.Atm.CassetteSlots", DEFAULT_CASSETTE_SLOTS);
		int cassetteCapacity = config.getInt("Economy.Cash.Atm.CassetteCapacity", DEFAULT_CASSETTE_CAPACITY);
		int pieceCap = config.getInt("Economy.Cash.Change.BreakPieceCap", DEFAULT_BREAK_PIECE_CAP);
		int maxDenom = config.getInt("Economy.Cash.Change.BreakMaxDenomination", DEFAULT_BREAK_MAX_DENOMINATION);

		Material cassetteMaterial = materialOr(config.getString("Economy.Cash.Cassette.Material"), Material.CHEST);
		String cassetteName = colorize(nullable(config.getString("Economy.Cash.Cassette.DisplayName"), "&8Cassette"));
		String cassetteLore = colorize(nullable(
			config.getString("Economy.Cash.Cassette.LoreTotal"),
			"&7Content : &e%amount%%currency%"
		));
		List<String> cassetteShape = config.getStringList("Economy.Cash.Cassette.Recipe.Shape");
		if (cassetteShape.size() != 3) {
			cassetteShape = DEFAULT_CASSETTE_SHAPE;
		}
		Map<Character, Material> cassetteIngredients = parseIngredients(
			config.getConfigurationSection("Economy.Cash.Cassette.Recipe.Ingredients")
		);
		if (cassetteIngredients.isEmpty()) {
			cassetteIngredients = DEFAULT_CASSETTE_INGREDIENTS;
		}
		int cassetteModel = config.getInt("Economy.Cash.Cassette.CustomModelData", 1);
		Integer cassetteCustomModelData = cassetteModel > 0 ? cassetteModel : null;

		String bankHeader = nullable(config.getString("Economy.Cash.Bank.SignHeader"), "[sBank]");
		String vaultPlayer = nullable(config.getString("Economy.Cash.Bank.VaultPlayer"), "Bank");
		int bankCapacity = config.getInt("Economy.Cash.Bank.VaultCapacity", DEFAULT_BANK_CAPACITY);
		String pilotHeader = nullable(config.getString("Economy.Cash.Pilot.SignHeader"), "[sPilot]");
		PlacedSign atmSign = readSign(config, "Economy.Cash.Atm.Sign", new PlacedSign(
			"&b&l[sATM]",
			"&8&m              ",
			"&7Bank &8· &6cash",
			"&8» &fRight click"
		));
		String accountPrefix = colorize(nullable(config.getString("Economy.Cash.Atm.Sign.Account"), "&7Acc &a"));
		String pocketPrefix = colorize(nullable(config.getString("Economy.Cash.Atm.Sign.Pocket"), "&7Cash &6"));
		PlacedSign bankSign = readSign(config, "Economy.Cash.Bank.Sign", new PlacedSign(
			"&2&l[sBank]",
			"&8&m              ",
			"&7Vault",
			"&8» &fRight click"
		));
		PlacedSign pilotSign = readSign(config, "Economy.Cash.Pilot.Sign", new PlacedSign(
			"&3&l[sPilot]",
			"&8&m              ",
			"&7ATM network",
			"&8» &fRight click"
		));

		return new Cash(
			enabled,
			advancedBank,
			currency,
			denoms,
			new WalletSettings(slots, walletMaterial, walletName, walletLore, shape, ingredients, walletCustomModelData),
			new AtmSettings(header, radius, presets, createCost, cassetteSlots, cassetteCapacity, atmSign, accountPrefix, pocketPrefix),
			new CassetteSettings(
				cassetteMaterial,
				cassetteName,
				cassetteLore,
				cassetteShape,
				cassetteIngredients,
				cassetteCustomModelData
			),
			new BankSettings(bankHeader, vaultPlayer, bankCapacity, bankSign),
			new PilotSettings(pilotHeader, pilotSign),
			new ChangeSettings(pieceCap, maxDenom)
		);
	}

	public static void applyGlow(ItemMeta meta) {
		meta.setEnchantmentGlintOverride(true);
	}

	public static void applyCustomModelData(ItemMeta meta, int value) {
		if (value <= 0) {
			return;
		}
		meta.setCustomModelData(value);
		CustomModelDataComponent component = meta.getCustomModelDataComponent();
		if (component == null) {
			return;
		}
		component.setFloats(List.of((float) value));
		meta.setCustomModelDataComponent(component);
	}

	public static OptionalInt readDenomination(PersistentDataContainer container, NamespacedKey key) {
		Integer value = container.get(key, PersistentDataType.INTEGER);
		if (value == null || value <= 0) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(value);
	}

	public static void writeDenomination(PersistentDataContainer container, NamespacedKey key, int denomination) {
		container.set(key, PersistentDataType.INTEGER, denomination);
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
		return wallet.slots();
	}

	public Material walletMaterial() {
		return wallet.material();
	}

	public OptionalInt walletCustomModelData() {
		return wallet.customModelData() == null ? OptionalInt.empty() : OptionalInt.of(wallet.customModelData());
	}

	public String walletDisplayName() {
		return wallet.displayName();
	}

	public String walletLoreTotal() {
		return wallet.loreTotal();
	}

	public List<String> recipeShape() {
		return wallet.recipeShape();
	}

	public Map<Character, Material> recipeIngredients() {
		return wallet.recipeIngredients();
	}

	public String atmHeader() {
		return atm.header();
	}

	public boolean isAtmHeader(String line) {
		return line != null && atm.header().equalsIgnoreCase(stripColorCodes(line).trim());
	}

	public int viewRadius() {
		return atm.viewRadius();
	}

	public List<Integer> atmPresets() {
		return atm.presets();
	}

	public int atmCreateCost() {
		return atm.createCost();
	}

	public boolean advancedBank() {
		return advancedBank;
	}

	public int cassetteSlots() {
		return atm.cassetteSlots();
	}

	public int cassetteCapacity() {
		return atm.cassetteCapacity();
	}

	public Material cassetteMaterial() {
		return cassette.material();
	}

	public String cassetteDisplayName() {
		return cassette.displayName();
	}

	public String cassetteLoreTotal() {
		return cassette.loreTotal();
	}

	public List<String> cassetteRecipeShape() {
		return cassette.recipeShape();
	}

	public Map<Character, Material> cassetteRecipeIngredients() {
		return cassette.recipeIngredients();
	}

	public OptionalInt cassetteCustomModelData() {
		return cassette.customModelData() == null ? OptionalInt.empty() : OptionalInt.of(cassette.customModelData());
	}

	public PlacedSign atmSign() {
		return atm.sign();
	}

	public String atmAccountPrefix() {
		return atm.accountPrefix();
	}

	public String atmPocketPrefix() {
		return atm.pocketPrefix();
	}

	public PlacedSign bankSign() {
		return bank.sign();
	}

	public PlacedSign pilotSign() {
		return pilot.sign();
	}

	public String bankHeader() {
		return bank.header();
	}

	public boolean isBankHeader(String line) {
		return line != null && bank.header().equalsIgnoreCase(stripColorCodes(line).trim());
	}

	public String bankVaultPlayer() {
		return bank.vaultPlayer();
	}

	public int bankVaultCapacity() {
		return bank.vaultCapacity();
	}

	public String pilotHeader() {
		return pilot.header();
	}

	public boolean isPilotHeader(String line) {
		return line != null && pilot.header().equalsIgnoreCase(stripColorCodes(line).trim());
	}

	public int breakPieceCap() {
		return change.breakPieceCap();
	}

	public int breakMaxDenomination() {
		return change.breakMaxDenomination();
	}

	public String applyAmount(String template, int amount) {
		return template
			.replace(AMOUNT_PLACEHOLDER, Integer.toString(amount))
			.replace(CURRENCY_PLACEHOLDER, currency);
	}

	public String formatTotal(int amount) {
		return applyAmount(wallet.loreTotal(), amount);
	}

	static String stripColorCodes(String line) {
		return COLOR_CODE.matcher(line.replace('&', '§')).replaceAll("");
	}

	private static List<Denomination> parseDenominations(List<Map<?, ?>> rows) {
		List<Denomination> parsed = new ArrayList<>();
		for (Map<?, ?> row : rows) {
			int value = toPositiveInt(row.get("value"));
			Material material = materialOr(stringOrNull(row.get("material")), null);
			boolean unique = parsed.stream().noneMatch(existing -> existing.value() == value);
			if (value > 0 && material != null && unique) {
				String name = colorize(nullable(stringOrNull(row.get("name")), value + "€"));
				Integer model = parseCustomModelData(row, value);
				parsed.add(new Denomination(value, material, name, model));
			}
		}
		return parsed;
	}

	private static List<Denomination> defaultDenominations(String currency) {
		List<Denomination> defaults = new ArrayList<>();
		for (int value : DEFAULT_VALUES) {
			Material material = defaultMaterial(value);
			String name = colorize("&e" + value + currency);
			defaults.add(new Denomination(value, material, name, value));
		}
		defaults.sort(Comparator.comparingInt(Denomination::value).reversed());
		return defaults;
	}

	private static Material defaultMaterial(int value) {
		if (value == 1) {
			return Material.IRON_NUGGET;
		}
		if (value == 2) {
			return Material.GOLD_NUGGET;
		}
		return Material.RESIN_BRICK;
	}

	private static Integer parseCustomModelData(Map<?, ?> row, int fallback) {
		Object raw = row.get("custom-model-data");
		if (raw == null) {
			raw = row.get("CustomModelData");
		}
		if (raw == null) {
			raw = row.get("custom_model_data");
		}
		if (raw == null) {
			return fallback;
		}
		return toNullableInt(raw);
	}

	private static PlacedSign readSign(FileConfiguration config, String path, PlacedSign fallback) {
		return new PlacedSign(
			colorize(nullable(config.getString(path + ".Title"), fallback.title())),
			colorize(nullable(config.getString(path + ".Separator"), fallback.separator())),
			colorize(nullable(config.getString(path + ".Subtitle"), fallback.subtitle())),
			colorize(nullable(config.getString(path + ".Hint"), fallback.hint()))
		);
	}

	private static Map<Character, Material> parseIngredients(ConfigurationSection section) {
		if (section == null) {
			return Map.of();
		}
		Map<Character, Material> ingredients = new LinkedHashMap<>();
		for (String key : section.getKeys(false)) {
			if (key.length() != 1) {
				continue;
			}
			Material material = materialOr(section.getString(key), null);
			if (material != null) {
				ingredients.put(key.charAt(0), material);
			}
		}
		return ingredients;
	}

	static List<Integer> parsePresets(List<Integer> configured) {
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
