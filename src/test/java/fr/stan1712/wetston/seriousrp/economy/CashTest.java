package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashTest {

	@Test
	void fromConfigReadsDenominationsWalletAndAtm() {
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Currency", "&6€");
		config.set("Economy.Cash.Enabled", true);
		config.set("Economy.Cash.Wallet.DefaultSlots", 27);
		config.set("Economy.Cash.Wallet.Material", "BUNDLE");
		config.set("Economy.Cash.Wallet.DisplayName", "&6Wallet");
		config.set("Economy.Cash.Wallet.LoreTotal", "Total %amount%%currency%");
		config.set("Economy.Cash.Wallet.Recipe.Shape", List.of("LLL", "LPL", "LLL"));
		config.set("Economy.Cash.Wallet.Recipe.Ingredients.L", "LEATHER");
		config.set("Economy.Cash.Wallet.Recipe.Ingredients.P", "PAPER");
		config.set("Economy.Cash.Wallet.Recipe.Ingredients.XX", "STONE");
		config.set("Economy.Cash.Wallet.Recipe.Ingredients.Z", "NOT_A_MATERIAL");
		config.set("Economy.Cash.Wallet.CustomModelData", 42);
		config.set("Economy.Cash.Atm.SignHeader", "[sATM]");
		config.set("Economy.Cash.Atm.ViewRadius", 8);
		config.set("Economy.Cash.Atm.CreateCost", 80);
		config.set("Economy.Cash.Atm.Presets", java.util.Arrays.asList(10, null, 25, 10, -5, 0));
		config.set("Economy.Cash.Denominations", List.of(
			Map.of("value", 20, "material", "PAPER", "name", "&a20", "custom-model-data", 20),
			Map.of("value", "1", "material", "GOLD_NUGGET", "name", "1€"),
			Map.of("value", 7, "material", "PAPER", "name", "seven", "custom-model-data", "7"),
			Map.of("value", 8, "material", "   "),
			Map.of("value", 20, "material", "GOLD_INGOT", "name", "dup"),
			Map.of("value", 0, "material", "PAPER"),
			Map.of("value", 5, "material", "NOPE"),
			Map.of("material", "PAPER")
		));

		Cash cash = Cash.fromConfig(config);

		assertTrue(cash.isEnabled());
		assertEquals("§6€", cash.currency());
		assertEquals(27, cash.walletSlots());
		assertEquals(Material.BUNDLE, cash.walletMaterial());
		assertEquals(42, cash.walletCustomModelData().orElse(0));
		assertEquals("§6Wallet", cash.walletDisplayName());
		assertEquals(List.of(20, 7, 1), cash.descendingValues());
		assertEquals(7, cash.denomination(7).orElseThrow().customModelData());
		assertEquals(1, cash.denomination(1).orElseThrow().customModelData());
		assertFalse(cash.isDenomination(8));
		assertTrue(cash.isDenomination(20));
		assertFalse(cash.isDenomination(50));
		assertEquals(Material.PAPER, cash.denomination(20).orElseThrow().material());
		assertEquals(20, cash.denomination(20).orElseThrow().customModelData());
		assertEquals(8, cash.viewRadius());
		assertEquals(80, cash.atmCreateCost());
		assertEquals(List.of(10, 25), cash.atmPresets());
		assertEquals("[sATM]", cash.atmHeader());
		assertTrue(cash.isAtmHeader("§a[sATM]"));
		assertTrue(cash.isAtmHeader(" [satm] "));
		assertFalse(cash.isAtmHeader("[ATM]"));
		assertFalse(cash.isAtmHeader(null));
		assertEquals("Total 12§6€", cash.formatTotal(12));
		assertEquals(3, cash.recipeShape().size());
		assertEquals(Material.LEATHER, cash.recipeIngredients().get('L'));
		assertFalse(cash.recipeIngredients().containsKey('Z'));
		assertEquals(3, cash.denominations().size());
	}

	@Test
	void fromConfigFallsBackWhenSectionsAreMissingOrInvalid() {
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Cash.Enabled", false);
		config.set("Economy.Cash.Wallet.DefaultSlots", 12);
		config.set("Economy.Cash.Wallet.Material", "NOT_REAL");
		config.set("Economy.Cash.Wallet.DisplayName", "   ");
		config.set("Economy.Cash.Atm.ViewRadius", 0);
		config.set("Economy.Cash.Atm.Presets", List.of(-5, 0));
		config.set("Economy.Cash.Wallet.Recipe.Shape", List.of("X"));
		config.set("Economy.Cash.Denominations", List.of(Map.of("value", "nope")));

		Cash cash = Cash.fromConfig(config);

		assertFalse(cash.isEnabled());
		assertEquals("€", cash.currency());
		assertEquals(18, cash.walletSlots());
		assertEquals(Material.BOOK, cash.walletMaterial());
		assertEquals(1, cash.walletCustomModelData().orElse(0));
		assertEquals("§6Portefeuille", cash.walletDisplayName());
		assertEquals(1, cash.viewRadius());
		assertEquals(150, cash.atmCreateCost());
		assertEquals(List.of(10, 25, 50, 100), cash.atmPresets());
		assertEquals(List.of(500, 200, 100, 50, 20, 10, 5, 2, 1), cash.descendingValues());
		assertFalse(cash.walletLoreTotal().isBlank());
		assertEquals(Material.GOLD_NUGGET, cash.denomination(2).orElseThrow().material());
		assertEquals(Material.RESIN_BRICK, cash.denomination(5).orElseThrow().material());
		assertEquals(Material.IRON_NUGGET, cash.denomination(1).orElseThrow().material());
		assertEquals(1, cash.denomination(1).orElseThrow().customModelData());
		assertEquals(2, cash.denomination(2).orElseThrow().customModelData());
		assertEquals(5, cash.denomination(5).orElseThrow().customModelData());
		assertEquals(List.of(" L ", "LPL", " L "), cash.recipeShape());
		assertEquals(Material.PAPER, cash.recipeIngredients().get('P'));
	}

	@Test
	void walletCustomModelDataDefaultsToOneAndDisablesWhenNonPositive() {
		assertEquals(1, Cash.fromConfig(new YamlConfiguration()).walletCustomModelData().orElse(0));

		YamlConfiguration disabled = new YamlConfiguration();
		disabled.set("Economy.Cash.Wallet.CustomModelData", 0);
		assertTrue(Cash.fromConfig(disabled).walletCustomModelData().isEmpty());
		disabled.set("Economy.Cash.Wallet.CustomModelData", -4);
		assertTrue(Cash.fromConfig(disabled).walletCustomModelData().isEmpty());

		Cash.WalletSettings compacted = new Cash.WalletSettings(
			18,
			Material.BOOK,
			"n",
			"l",
			List.of(" L ", "LPL", " L "),
			Map.of('L', Material.LEATHER),
			0
		);
		assertNull(compacted.customModelData());
	}

	@Test
	void denominationCustomModelDataDefaultsToFaceValueAndDisablesWhenNonPositive() {
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Cash.Denominations", List.of(
			Map.of("value", 1, "material", "IRON_NUGGET"),
			Map.of("value", 2, "material", "GOLD_NUGGET", "custom-model-data", 0),
			Map.of("value", 5, "material", "RESIN_BRICK", "CustomModelData", 50),
			Map.of("value", 10, "material", "RESIN_BRICK", "custom_model_data", 10)
		));
		Cash cash = Cash.fromConfig(config);
		assertEquals(1, cash.denomination(1).orElseThrow().customModelData());
		assertNull(cash.denomination(2).orElseThrow().customModelData());
		assertEquals(50, cash.denomination(5).orElseThrow().customModelData());
		assertEquals(10, cash.denomination(10).orElseThrow().customModelData());
		assertNull(new Cash.Denomination(10, Material.RESIN_BRICK, "n", 0).customModelData());
	}

	@Test
	void applyCustomModelDataWritesLegacyValueAndFloatComponent() {
		ItemMeta meta = mock(ItemMeta.class);
		CustomModelDataComponent component = mock(CustomModelDataComponent.class);
		when(meta.getCustomModelDataComponent()).thenReturn(component);

		Cash.applyCustomModelData(meta, 20);
		verify(meta).setCustomModelData(20);
		verify(component).setFloats(List.of(20f));
		verify(meta).setCustomModelDataComponent(component);

		Cash.applyCustomModelData(meta, 0);
		verify(meta, never()).setCustomModelData(0);
	}

	@Test
	void clampSlotsOnlyAllowsChestSizes() {
		assertEquals(9, Cash.clampSlots(9));
		assertEquals(18, Cash.clampSlots(18));
		assertEquals(27, Cash.clampSlots(27));
		assertEquals(18, Cash.clampSlots(1));
		assertEquals(18, Cash.clampSlots(36));
	}

	@Test
	void colorizeAndParsePositiveIntCoverNulls() {
		assertEquals("", Cash.colorize(null));
		assertEquals("§aHi", Cash.colorize("&aHi"));
		assertTrue(Cash.parsePositiveInt(null).isEmpty());
		assertTrue(Cash.parsePositiveInt("0").isEmpty());
		assertTrue(Cash.parsePositiveInt("-2").isEmpty());
		assertTrue(Cash.parsePositiveInt("no").isEmpty());
		assertEquals(4, Cash.parsePositiveInt("4").orElse(0));
	}

	@Test
	void atmCreateCostClampsNegativeValuesToZero() {
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Cash.Atm.CreateCost", -20);
		assertEquals(0, Cash.fromConfig(config).atmCreateCost());
		config.set("Economy.Cash.Atm.CreateCost", 0);
		assertEquals(0, Cash.fromConfig(config).atmCreateCost());
	}

	@Test
	void parsePresetsKeepsUniquePositiveValuesAndSkipsNulls() {
		assertEquals(List.of(10, 25), Cash.parsePresets(java.util.Arrays.asList(10, null, 25, 10, -5, 0)));
		assertEquals(List.of(10, 25, 50, 100), Cash.parsePresets(java.util.Arrays.asList(null, -1, 0)));
	}

	@Test
	void stackRejectsNonPositiveValuesAndComputesTotal() {
		assertThrows(IllegalArgumentException.class, () -> new Cash.Stack(0, 1));
		assertThrows(IllegalArgumentException.class, () -> new Cash.Stack(1, 0));
		assertEquals(40L, new Cash.Stack(10, 4).total());
	}

	@Test
	void denominationPersistentDataIgnoresMissingAndNonPositive() {
		PersistentDataContainer container = mock(PersistentDataContainer.class);
		NamespacedKey key = new NamespacedKey("seriousrp", "srp-cash");
		when(container.get(key, PersistentDataType.INTEGER)).thenReturn(null);
		assertTrue(Cash.readDenomination(container, key).isEmpty());
		when(container.get(key, PersistentDataType.INTEGER)).thenReturn(0);
		assertTrue(Cash.readDenomination(container, key).isEmpty());
		when(container.get(key, PersistentDataType.INTEGER)).thenReturn(20);
		assertEquals(20, Cash.readDenomination(container, key).orElse(0));
		Cash.writeDenomination(container, key, 5);
		verify(container).set(key, PersistentDataType.INTEGER, 5);
	}
}
