package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.mockito.MockedConstruction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class ItemStackMetaStubs {
	private ItemStackMetaStubs() {
	}

	static MockedConstruction.MockInitializer<ItemStack> persistentMeta() {
		return (item, context) -> {
			ItemMeta meta = mock(ItemMeta.class);
			PersistentDataContainer pdc = mock(PersistentDataContainer.class);
			when(item.getItemMeta()).thenReturn(meta);
			when(meta.getPersistentDataContainer()).thenReturn(pdc);
		};
	}
}
