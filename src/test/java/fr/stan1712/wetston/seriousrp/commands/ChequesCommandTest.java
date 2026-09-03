package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import fr.stan1712.wetston.seriousrp.Main;
import fr.stan1712.wetston.seriousrp.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
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
}
