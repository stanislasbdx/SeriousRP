package fr.stan1712.wetston.seriousrp.commands;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
class SeriousrpCommandTest extends ConfigBackedTest {

	@Mock Main plugin;
	@Mock FileConfiguration pluginConfig;
	@Mock Command command;
	@Mock CommandSender sender;
	@Mock Player player;

	@Test
	void deniesSendersWithoutInfoPermission() {
		when(sender.hasPermission("seriousrp.info")).thenReturn(false);

		assertTrue(new Seriousrp(plugin).onCommand(sender, command, "seriousrp", new String[0]));
		verify(sender).sendMessage(contains("permission"));
	}

	@Test
	void emptyArgsShowsUsageAndReturnsFalse() {
		when(sender.hasPermission("seriousrp.info")).thenReturn(true);

		assertFalse(new Seriousrp(plugin).onCommand(sender, command, "seriousrp", new String[0]));
		verify(sender).sendMessage(contains("/seriousrp help"));
	}

	@Test
	void versionSubcommandPrintsConfiguredVersion() {
		when(sender.hasPermission("seriousrp.info")).thenReturn(true);

		assertTrue(new Seriousrp(plugin).onCommand(sender, command, "seriousrp", new String[] {"version"}));
		verify(sender).sendMessage(contains("Version"));
	}

	@Test
	void helpIncludesMedicsAndEconomyWhenEnabled() {
		when(sender.hasPermission("seriousrp.info")).thenReturn(true);
		when(plugin.getConfig()).thenReturn(pluginConfig);
		when(pluginConfig.getBoolean("Core.Modules.Medics")).thenReturn(true);
		when(pluginConfig.getBoolean("Core.Modules.Economy")).thenReturn(true);

		assertTrue(new Seriousrp(plugin).onCommand(sender, command, "seriousrp", new String[] {"help"}));
		verify(sender).sendMessage(contains("/medinfo"));
		verify(sender).sendMessage(contains("/cheque"));
	}

	@Test
	void modulesSubcommandReportsEachToggle() {
		when(sender.hasPermission("seriousrp.info")).thenReturn(true);
		when(plugin.getConfig()).thenReturn(pluginConfig);
		when(pluginConfig.getBoolean("Core.Modules.CustomRecipes")).thenReturn(true);
		when(pluginConfig.getBoolean("Core.Modules.RPDeath")).thenReturn(false);
		when(pluginConfig.getBoolean("Core.Modules.Medics")).thenReturn(false);
		when(pluginConfig.getBoolean("Core.Modules.Chairs")).thenReturn(true);
		when(pluginConfig.getBoolean("Core.Modules.Economy")).thenReturn(true);

		assertTrue(new Seriousrp(plugin).onCommand(sender, command, "seriousrp", new String[] {"status"}));
		verify(sender).sendMessage(contains("CustomRecipes"));
		verify(sender).sendMessage(contains("Economy"));
	}

	@Test
	void reloadWithoutAdminPermissionIsDenied() {
		when(sender.hasPermission("seriousrp.info")).thenReturn(true);
		when(sender.hasPermission("seriousrp.admin.reload")).thenReturn(false);

		assertTrue(new Seriousrp(plugin).onCommand(sender, command, "seriousrp", new String[] {"reload"}));
		verify(sender).sendMessage(contains("permission"));
		verify(plugin, never()).saveConfig();
	}

	@Test
	void unknownSubcommandReturnsFalse() {
		when(sender.hasPermission("seriousrp.info")).thenReturn(true);

		assertFalse(new Seriousrp(plugin).onCommand(sender, command, "seriousrp", new String[] {"nope"}));
	}

	@Test
	void helpOmitsDisabledModules() {
		when(sender.hasPermission("seriousrp.info")).thenReturn(true);
		when(plugin.getConfig()).thenReturn(pluginConfig);
		when(pluginConfig.getBoolean("Core.Modules.Medics")).thenReturn(false);
		when(pluginConfig.getBoolean("Core.Modules.Economy")).thenReturn(false);

		assertTrue(new Seriousrp(plugin).onCommand(sender, command, "seriousrp", new String[] {"help"}));
		verify(sender, never()).sendMessage(contains("/medinfo"));
		verify(sender, never()).sendMessage(contains("/cheque"));
		verify(sender).sendMessage(contains("/srtp"));
	}

	@Test
	void modulesAliasUsesSameHandlerAsStatus() {
		when(sender.hasPermission("seriousrp.info")).thenReturn(true);
		when(plugin.getConfig()).thenReturn(pluginConfig);

		assertTrue(new Seriousrp(plugin).onCommand(sender, command, "seriousrp", new String[] {"modules"}));
		verify(sender).sendMessage(contains("CustomRecipes"));
	}

	@Test
	void sendMessageStatusModuleReportsOn() {
		when(plugin.getConfig()).thenReturn(pluginConfig);
		when(pluginConfig.getBoolean("Core.Modules.Chairs")).thenReturn(true);

		new Seriousrp(plugin).sendMessageStatusModule("Chairs", player);
		verify(player).sendMessage(contains("ON"));
	}
}
