package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import fr.stan1712.wetston.seriousrp.Main;
import fr.stan1712.wetston.seriousrp.defaults.EnumModules;
import org.bukkit.World;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerOnlyCommandTest extends ConfigBackedTest {

	@Mock Main plugin;
	@Mock FileConfiguration pluginConfig;
	@Mock Command command;
	@Mock ConsoleCommandSender console;
	@Mock Player player;
	@Mock World world;

	@Test
	void inactiveCommandIgnoresConsole() {
		when(plugin.getConfig()).thenReturn(pluginConfig);
		assertFalse(new InactiveCommand(plugin, EnumModules.MEDICS).onCommand(console, command, "vitals", new String[0]));
	}

	@Test
	void inactiveCommandNotifiesWhenDebugEnabled() {
		when(plugin.getConfig()).thenReturn(pluginConfig);
		when(pluginConfig.getBoolean("Core.Modules.InactiveDebug")).thenReturn(true);

		assertTrue(new InactiveCommand(plugin, EnumModules.MEDICS).onCommand(player, command, "vitals", new String[0]));
		verify(player).sendMessage(contains("MEDICS"));
	}

	@Test
	void inactiveCommandStaysSilentWhenDebugDisabled() {
		when(plugin.getConfig()).thenReturn(pluginConfig);
		when(pluginConfig.getBoolean("Core.Modules.InactiveDebug")).thenReturn(false);

		assertTrue(new InactiveCommand(plugin, EnumModules.MEDICS).onCommand(player, command, "vitals", new String[0]));
		verify(player, never()).sendMessage(org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void randomTeleportRejectsConsoleAndMissingPermission() {
		when(plugin.getConfig()).thenReturn(pluginConfig);
		when(pluginConfig.getInt("MicroModules.RandomBlocks")).thenReturn(1000);
		RandomTeleportation rtp = new RandomTeleportation(plugin);

		assertFalse(rtp.onCommand(console, command, "srtp", new String[0]));

		when(player.hasPermission("serious.randomtp")).thenReturn(false);
		assertTrue(rtp.onCommand(player, command, "srtp", new String[0]));
		verify(player).sendMessage(contains("permission"));
	}

	@Test
	void randomTeleportRejectsNonOverworld() {
		when(plugin.getConfig()).thenReturn(pluginConfig);
		when(pluginConfig.getInt("MicroModules.RandomBlocks")).thenReturn(1000);
		when(player.hasPermission("serious.randomtp")).thenReturn(true);
		when(player.getWorld()).thenReturn(world);
		when(world.getEnvironment()).thenReturn(World.Environment.NETHER);

		assertTrue(new RandomTeleportation(plugin).onCommand(player, command, "srtp", new String[0]));
		verify(player).sendMessage(contains("disabled"));
	}

	@Test
	void randomTeleportMovesPlayerInOverworld() {
		when(plugin.getConfig()).thenReturn(pluginConfig);
		when(pluginConfig.getInt("MicroModules.RandomBlocks")).thenReturn(1000);
		when(player.hasPermission("serious.randomtp")).thenReturn(true);
		when(player.getWorld()).thenReturn(world);
		when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
		org.bukkit.Location origin = mock(org.bukkit.Location.class);
		when(player.getLocation()).thenReturn(origin);
		when(origin.getBlockX()).thenReturn(0);
		when(origin.getBlockZ()).thenReturn(0);
		when(world.getHighestBlockYAt(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(64);

		java.util.concurrent.atomic.AtomicBoolean boosted = new java.util.concurrent.atomic.AtomicBoolean();
		RandomTeleportation rtp = new RandomTeleportation(plugin, p -> boosted.set(true), new java.util.Random(42));

		assertTrue(rtp.onCommand(player, command, "srtp", new String[0]));
		org.junit.jupiter.api.Assertions.assertTrue(boosted.get());
		verify(player).teleport(org.mockito.ArgumentMatchers.any(org.bukkit.Location.class));
		verify(player).sendMessage(contains("random"));
	}
}
