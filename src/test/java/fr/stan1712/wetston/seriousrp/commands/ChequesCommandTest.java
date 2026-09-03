package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import fr.stan1712.wetston.seriousrp.Main;
import fr.stan1712.wetston.seriousrp.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChequesCommandTest extends ConfigBackedTest {

	@Mock Main plugin;
	@Mock Command command;
	@Mock ConsoleCommandSender console;
	@Mock Player player;
	@Mock FileConfiguration overrideConfig;
	@Mock PlayerInventory inventory;
	@Mock ItemStack chequeItem;
	@Mock Economy economy;

	@Test
	void consoleSenderIsIgnored() {
		assertTrue(new Cheques(plugin).onCommand(console, command, "cheque", new String[] {"10"}));
	}

	@Test
	void inactiveEconomyCanWarnWhenDebugIsOn() {
		when(overrideConfig.getBoolean("Core.Modules.Economy")).thenReturn(false);
		when(overrideConfig.getBoolean("Core.Modules.InactiveDebug")).thenReturn(true);
		when(overrideConfig.getString("ShortPrefix")).thenReturn("&7p ");
		when(overrideConfig.getString("Core.Modules.InactiveMessage")).thenReturn("&c'%module%' off");
		Utils.ConfigFactory.overrideConfig(overrideConfig);

		assertTrue(new Cheques(plugin).onCommand(player, command, "cheque", new String[] {"10"}));
		verify(player).sendMessage(contains("Economy"));
	}

	@Test
	void deniesPlayersWithoutChequePermission() {
		when(player.hasPermission("seriousrp.economy.cheques")).thenReturn(false);

		assertTrue(new Cheques(plugin).onCommand(player, command, "cheque", new String[] {"10"}));
		verify(player).sendMessage(contains("permission"));
	}

	@Test
	void missingAmountShowsUsageAndReturnsFalse() {
		when(player.hasPermission("seriousrp.economy.cheques")).thenReturn(true);

		assertFalse(new Cheques(plugin).onCommand(player, command, "cheque", new String[0]));
		verify(player).sendMessage(contains("/cheque"));
	}

	@Test
	void invalidAmountShowsUsage() {
		when(player.hasPermission("seriousrp.economy.cheques")).thenReturn(true);

		assertTrue(new Cheques(plugin).onCommand(player, command, "cheque", new String[] {"-1"}));
		verify(player).sendMessage(contains("/cheque"));
		verify(player, never()).getInventory();
	}

	@Test
	void extraArgumentsShowUsage() {
		when(player.hasPermission("seriousrp.economy.cheques")).thenReturn(true);

		assertTrue(new Cheques(plugin).onCommand(player, command, "cheque", new String[] {"10", "extra"}));
		verify(player).sendMessage(contains("/cheque"));
	}

	@Test
	void inactiveEconomyStaysSilentWhenDebugIsOff() {
		when(overrideConfig.getBoolean("Core.Modules.Economy")).thenReturn(false);
		when(overrideConfig.getBoolean("Core.Modules.InactiveDebug")).thenReturn(false);
		Utils.ConfigFactory.overrideConfig(overrideConfig);

		assertTrue(new Cheques(plugin).onCommand(player, command, "cheque", new String[] {"10"}));
		verify(player, never()).sendMessage(org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void giveChequeIfPossibleCoversInventoryBalanceAndSuccess() {
		Cheques cheques = new Cheques(plugin);
		when(player.getInventory()).thenReturn(inventory);

		when(inventory.contains(chequeItem)).thenReturn(true);
		cheques.giveChequeIfPossible(player, "10", chequeItem);
		verify(player).sendMessage(contains("already"));

		when(inventory.contains(chequeItem)).thenReturn(false);
		when(inventory.firstEmpty()).thenReturn(-1);
		cheques.giveChequeIfPossible(player, "10", chequeItem);
		verify(player).sendMessage(contains("full"));

		when(inventory.firstEmpty()).thenReturn(3);
		Main.economy = economy;
		when(economy.getBalance(player)).thenReturn(1.0);
		cheques.giveChequeIfPossible(player, "10", chequeItem);
		verify(player).sendMessage(contains("enough"));

		when(economy.getBalance(player)).thenReturn(100.0);
		cheques.giveChequeIfPossible(player, "10", chequeItem);
		verify(player).sendMessage(contains("created"));
		verify(inventory).addItem(chequeItem);
		Main.economy = null;
	}

	@Test
	void onCommandUsesFactoryThenGrantsCheque() {
		when(player.hasPermission("seriousrp.economy.cheques")).thenReturn(true);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.contains(chequeItem)).thenReturn(false);
		when(inventory.firstEmpty()).thenReturn(2);
		Main.economy = economy;
		when(economy.getBalance(player)).thenReturn(50.0);

		Cheques cheques = new Cheques(plugin, (ignoredPlayer, amount) -> chequeItem);
		assertTrue(cheques.onCommand(player, command, "cheque", new String[] {"10"}));
		verify(inventory).addItem(chequeItem);
		Main.economy = null;
	}

	@Test
	void createChequeItemRejectsNullItemMeta() {
		Cheques cheques = new Cheques(plugin);
		try (org.mockito.MockedConstruction<ItemStack> ignored = org.mockito.Mockito.mockConstruction(ItemStack.class,
			(mock, context) -> when(mock.getItemMeta()).thenReturn(null))) {
			assertThrows(AssertionError.class, () -> cheques.createChequeItem(player, "25"));
		}
	}

	@Test
	void createChequeItemWritesPersistentDataAndLore() {
		when(plugin.getName()).thenReturn("SeriousRP");
		when(player.getName()).thenReturn("stan");
		when(player.getDisplayName()).thenReturn("Stan");
		when(player.getUniqueId()).thenReturn(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"));

		org.bukkit.inventory.meta.ItemMeta chequeMeta = mock(org.bukkit.inventory.meta.ItemMeta.class);
		PersistentDataContainer pdc = mock(PersistentDataContainer.class);
		when(chequeMeta.getPersistentDataContainer()).thenReturn(pdc);

		try (org.mockito.MockedConstruction<ItemStack> mocked = org.mockito.Mockito.mockConstruction(ItemStack.class,
			(mock, context) -> when(mock.getItemMeta()).thenReturn(chequeMeta))) {
			ItemStack created = new Cheques(plugin).createChequeItem(player, "25");
			org.junit.jupiter.api.Assertions.assertNotNull(created);
		}

		verify(chequeMeta).setDisplayName(contains("25"));
		verify(chequeMeta).setLore(org.mockito.ArgumentMatchers.anyList());
		verify(pdc).set(any(NamespacedKey.class), eq(PersistentDataType.STRING), contains("stan"));
	}
}
