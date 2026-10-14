package fr.stan1712.wetston.seriousrp.economy;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CassetteItemsTest extends ConfigBackedTest {

	@Mock Plugin plugin;
	@Mock Player player;
	@Mock PlayerInventory inventory;
	@Mock ItemStack hand;
	@Mock ItemMeta handMeta;
	@Mock PersistentDataContainer handPdc;

	private CassetteItems items;

	@BeforeEach
	void setUp() {
		when(plugin.getName()).thenReturn("SeriousRP");
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Cash.Enabled", true);
		config.set("Economy.Cash.AdvancedBank", true);
		config.set("Economy.Cash.Denominations", List.of(
			java.util.Map.of("value", 10, "material", "PAPER")
		));
		when(plugin.getConfig()).thenReturn(config);
		items = new CassetteItems(plugin, Cash.fromConfig(config), recipe -> {}, key -> {});
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(hand);
		when(hand.getType()).thenReturn(Material.CHEST);
		when(hand.getItemMeta()).thenReturn(handMeta);
		when(handMeta.getPersistentDataContainer()).thenReturn(handPdc);
	}

	@Test
	void thirdWrongPinSaysTheCashWasDestroyed() {
		Cassette.Payload locked = Cassette.withPin(
			Cassette.addValue(Cassette.empty(), 40, List.of(10), 1000).orElseThrow(),
			"9999"
		);
		Cassette.Payload twoMisses = Cassette.tryPin(Cassette.tryPin(locked, "0000").payload(), "1111").payload();
		when(handPdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(Cassette.toJson(twoMisses));
		CassetteItems.PinPrompt prompt = new CassetteItems.PinPrompt(
			UUID.randomUUID(),
			EquipmentSlot.HAND,
			CassetteItems.PinMode.OPEN,
			System.currentTimeMillis() + 5_000
		);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			items.handlePin(player, prompt, "2222");
		}
		verify(player).sendMessage(contains("destroyed"));
		verify(player).sendMessage(contains("40"));
	}

	@Test
	void craftTellsThePlayerToLockTheCassette() {
		ShapedRecipe recipe = org.mockito.Mockito.mock(ShapedRecipe.class);
		NamespacedKey recipeKey = new NamespacedKey(plugin, CassetteItems.RECIPE_KEY);
		when(recipe.getKey()).thenReturn(recipeKey);
		CraftItemEvent event = org.mockito.Mockito.mock(CraftItemEvent.class);
		when(event.getRecipe()).thenReturn(recipe);
		when(event.getWhoClicked()).thenReturn(player);
		when(event.isShiftClick()).thenReturn(false);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			items.onCraft(event);
		}
		verify(player).sendMessage(contains("lock"));
	}
}
