package fr.stan1712.wetston.seriousrp.economy;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PilotListenerTest extends ConfigBackedTest {

	@TempDir Path tempDir;
	@Mock Plugin plugin;
	@Mock Player player;
	@Mock Inventory gui;
	@Mock Block block;
	@Mock Sign sign;
	@Mock PersistentDataContainer signPdc;

	private PilotListener listener;
	private final UUID playerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@BeforeEach
	void setUp() {
		when(plugin.getName()).thenReturn("SeriousRP");
		when(player.getUniqueId()).thenReturn(playerId);
		YamlConfiguration config = new YamlConfiguration();
		config.set("Economy.Cash.Enabled", true);
		config.set("Economy.Cash.AdvancedBank", true);
		config.set("Economy.Cash.Denominations", List.of(Map.of("value", 1, "material", "GOLD_NUGGET")));
		when(plugin.getConfig()).thenReturn(config);
		Cash cash = Cash.fromConfig(config);
		AtmRegistry registry = new AtmRegistry(tempDir.resolve("atms.yml").toFile());
		listener = new PilotListener(plugin, cash, registry, Runnable::run, (h, s, t) -> gui);
		when(block.getState()).thenReturn(sign);
		when(sign.getPersistentDataContainer()).thenReturn(signPdc);
	}

	@Test
	void createRequiresPermissionAndStampsOwner() {
		SignChangeEvent denied = mock(SignChangeEvent.class);
		when(denied.getLine(0)).thenReturn("[sPilot]");
		when(denied.getPlayer()).thenReturn(player);
		when(player.hasPermission(Atm.PILOT_CREATE_PERM)).thenReturn(false);
		listener.onSignChange(denied);
		verify(denied).setCancelled(true);

		SignChangeEvent allowed = mock(SignChangeEvent.class);
		when(allowed.getLine(0)).thenReturn("[sPilot]");
		when(allowed.getPlayer()).thenReturn(player);
		when(allowed.getBlock()).thenReturn(block);
		when(player.hasPermission(Atm.PILOT_CREATE_PERM)).thenReturn(true);
		listener.onSignChange(allowed);
		verify(allowed).setLine(0, "§3§l[sPilot]");
		verify(signPdc).set(any(NamespacedKey.class), eq(PersistentDataType.STRING), eq(playerId.toString()));
	}
}
