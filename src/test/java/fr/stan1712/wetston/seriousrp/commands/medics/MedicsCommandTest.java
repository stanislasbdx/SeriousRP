package fr.stan1712.wetston.seriousrp.commands.medics;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicsCommandTest extends ConfigBackedTest {

	@Mock Main plugin;
	@Mock Command command;
	@Mock ConsoleCommandSender console;
	@Mock Player player;
	@Mock Player target;

	@Test
	void medinfoAndReviveRejectConsole() {
		assertFalse(new Medinfo(plugin).onCommand(console, command, "vitals", new String[] {"stan"}));
		assertFalse(new Revive(plugin).onCommand(console, command, "revive", new String[] {"stan"}));
		assertFalse(new HRPRevive(plugin).onCommand(console, command, "hrprevive", new String[0]));
	}

	@Test
	void medinfoDeniesWithoutPermission() {
		when(player.hasPermission("seriousrp.medics.info")).thenReturn(false);

		assertTrue(new Medinfo(plugin).onCommand(player, command, "vitals", new String[] {"stan"}));
		verify(player).sendMessage(contains("permission"));
	}

	@Test
	void medinfoUsageWhenNoArgs() {
		when(player.hasPermission("seriousrp.medics.info")).thenReturn(true);

		assertFalse(new Medinfo(plugin).onCommand(player, command, "vitals", new String[0]));
		verify(player).sendMessage(contains("/vitals"));
	}

	@Test
	void medinfoReportsUnknownTarget() {
		when(player.hasPermission("seriousrp.medics.info")).thenReturn(true);

		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(() -> Bukkit.getPlayer("ghost")).thenReturn(null);
			assertTrue(new Medinfo(plugin).onCommand(player, command, "vitals", new String[] {"ghost"}));
		}
		verify(player).sendMessage(contains("ghost"));
	}

	@Test
	void reviveDeniesWithoutPermissionAndShowsUsage() {
		when(player.hasPermission("seriousrp.medics.revive")).thenReturn(false);
		assertTrue(new Revive(plugin).onCommand(player, command, "revive", new String[] {"stan"}));
		verify(player).sendMessage(contains("permission"));

		when(player.hasPermission("seriousrp.medics.revive")).thenReturn(true);
		assertFalse(new Revive(plugin).onCommand(player, command, "revive", new String[0]));
		verify(player).sendMessage(contains("/revive"));
	}

	@Test
	void reviveReportsMissingTarget() {
		when(player.hasPermission("seriousrp.medics.revive")).thenReturn(true);

		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(() -> Bukkit.getPlayer("ghost")).thenReturn(null);
			assertTrue(new Revive(plugin).onCommand(player, command, "revive", new String[] {"ghost"}));
		}
		verify(player).sendMessage(contains("ghost"));
	}

	@Test
	void reviveSkipsHealthyTarget() {
		when(player.hasPermission("seriousrp.medics.revive")).thenReturn(true);
		when(target.getHealth()).thenReturn(20.0);

		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(() -> Bukkit.getPlayer("stan")).thenReturn(target);
			assertTrue(new Revive(plugin).onCommand(player, command, "revive", new String[] {"stan"}));
		}
		verify(player).sendMessage(contains("doesn't need"));
	}

	@Test
	void hrpReviveDeniesWithoutPermissionAndHealthyPlayer() {
		when(player.hasPermission("seriousrp.medics.hrprevive")).thenReturn(false);
		assertTrue(new HRPRevive(plugin).onCommand(player, command, "hrprevive", new String[0]));
		verify(player).sendMessage(contains("permission"));

		when(player.hasPermission("seriousrp.medics.hrprevive")).thenReturn(true);
		when(player.getHealth()).thenReturn(20.0);
		assertTrue(new HRPRevive(plugin).onCommand(player, command, "hrprevive", new String[0]));
		verify(player).sendMessage(contains("doesn't need"));
	}
}
