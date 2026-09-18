package fr.stan1712.wetston.seriousrp.economy;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import fr.stan1712.wetston.seriousrp.Main;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CashCommandTest extends ConfigBackedTest {

	@Mock Plugin plugin;
	@Mock Command command;
	@Mock Player player;
	@Mock PlayerInventory inventory;
	@Mock ConsoleCommandSender console;
	@Mock Economy economy;

	private Cash cash;
	private WalletListener walletItems;
	private CashCommand commandExecutor;

	@BeforeEach
	void setUp() {
		when(plugin.getName()).thenReturn("SeriousRP");
		when(player.getName()).thenReturn("stan");
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getStorageContents()).thenReturn(new ItemStack[36]);
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Cash.Enabled", true);
		config.set("Economy.Cash.Denominations", List.of(
			Map.of("value", 1, "material", "GOLD_NUGGET"),
			Map.of("value", 50, "material", "PAPER")
		));
		cash = Cash.fromConfig(config);
		walletItems = new WalletListener(plugin, cash, new Wallet(), recipe -> {}, (h, s, t) -> mock(org.bukkit.inventory.Inventory.class));
		commandExecutor = new CashCommand(cash, walletItems, name -> "stan".equalsIgnoreCase(name) ? player : null);
		when(player.hasPermission("seriousrp.economy.cash.transform")).thenReturn(true);
	}

	@Test
	void rejectsInactiveEconomyMissingPermsAndBadUsage() {
		YamlConfiguration override = new YamlConfiguration();
		override.set("Core.Modules.Economy", false);
		override.set("Core.Modules.InactiveDebug", true);
		override.set("ShortPrefix", "&7p ");
		override.set("Core.Modules.InactiveMessage", "&c'%module%' off");
		fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.overrideConfig(override);
		assertTrue(commandExecutor.onCommand(player, command, "cash", new String[] {"transform", "stan", "10"}));
		verify(player).sendMessage(contains("Economy"));

		override.set("Core.Modules.Economy", true);
		override.set("Core.NoPerms", "nope");
		fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.overrideConfig(override);
		when(player.hasPermission("seriousrp.economy.cash.transform")).thenReturn(false);
		assertTrue(commandExecutor.onCommand(player, command, "cash", new String[] {"transform", "stan", "10"}));
		verify(player).sendMessage(contains("nope"));
	}

	@Test
	void usageUnknownPlayerAndDisabledCash() {
		assertFalse(commandExecutor.onCommand(player, command, "cash", new String[0]));
		assertTrue(commandExecutor.onCommand(player, command, "cash", new String[] {"nope"}));
		assertTrue(commandExecutor.onCommand(player, command, "cash", new String[] {"transform", "stan", "7"}));
		assertTrue(commandExecutor.onCommand(player, command, "cash", new String[] {"transform", "missing", "10"}));
		verify(player).sendMessage(contains("missing"));

		YamlConfiguration disabled = new YamlConfiguration();
		disabled.set("Economy.Cash.Enabled", false);
		CashCommand off = new CashCommand(Cash.fromConfig(disabled), walletItems, name -> player);
		assertTrue(off.onCommand(console, command, "cash", new String[] {"transform", "stan", "10"}));
	}

	@Test
	void transformWithdrawsAndGivesCash() {
		Main.economy = economy;
		when(economy.getBalance(player)).thenReturn(200D);
		when(economy.withdrawPlayer(player, 150D)).thenReturn(
			new EconomyResponse(150D, 50D, EconomyResponse.ResponseType.SUCCESS, "")
		);
		try (MockedConstruction<ItemStack> ignored = mockConstruction(ItemStack.class, ItemStackMetaStubs.persistentMeta())) {
			assertTrue(commandExecutor.onCommand(player, command, "cash", new String[] {"transform", "stan", "50", "3"}));
		}
		verify(economy).withdrawPlayer(player, 150D);
		verify(inventory).addItem(any(ItemStack.class));
		verify(player).sendMessage(contains("150"));
		Main.economy = null;
	}

	@Test
	void transformFailsWhenInventoryOrBankCannotCover() {
		Main.economy = economy;
		ItemStack blocker = mock(ItemStack.class);
		when(blocker.getType()).thenReturn(Material.STONE);
		ItemStack[] full = new ItemStack[36];
		java.util.Arrays.fill(full, blocker);
		when(inventory.getStorageContents()).thenReturn(full);
		assertTrue(commandExecutor.onCommand(player, command, "cash", new String[] {"transform", "stan", "10"}));
		verify(player, org.mockito.Mockito.atLeastOnce()).sendMessage(contains("full"));

		when(inventory.getStorageContents()).thenReturn(new ItemStack[36]);
		when(economy.getBalance(player)).thenReturn(1D);
		assertTrue(commandExecutor.onCommand(player, command, "cash", new String[] {"transform", "stan", "10"}));
		verify(player).sendMessage(contains("enough"));

		when(economy.getBalance(player)).thenReturn(100D);
		when(economy.withdrawPlayer(player, 10D)).thenReturn(
			new EconomyResponse(10D, 90D, EconomyResponse.ResponseType.FAILURE, "no")
		);
		assertTrue(commandExecutor.onCommand(player, command, "cash", new String[] {"transform", "stan", "10"}));
		Main.economy = null;
	}

	@Test
	void publicConstructorBindsPlayerLookup() {
		assertDoesNotThrow(() -> new CashCommand(plugin, cash, walletItems));
	}
}
