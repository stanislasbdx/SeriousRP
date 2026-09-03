package fr.stan1712.wetston.seriousrp.events;

import fr.stan1712.wetston.seriousrp.ConfigBackedTest;
import fr.stan1712.wetston.seriousrp.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventEarlyExitTest extends ConfigBackedTest {

	@Mock Main plugin;
	@Mock EntityDamageEvent damageEvent;
	@Mock EntityDamageByEntityEvent damageByEntityEvent;
	@Mock PlayerInteractEvent interactEvent;
	@Mock Entity cow;
	@Mock LivingEntity playerEntity;
	@Mock Player player;
	@Mock PlayerInventory inventory;
	@Mock ItemStack item;
	@Mock ItemMeta meta;
	@Mock PersistentDataContainer pdc;

	@Test
	void fallIgnoresNonPlayersAndLightFalls() {
		Fall fall = new Fall(plugin);

		when(damageEvent.getEntity()).thenReturn(cow);
		when(cow.getType()).thenReturn(EntityType.COW);
		fall.onFall(damageEvent);
		verify(damageEvent, never()).getCause();

		when(damageEvent.getEntity()).thenReturn(playerEntity);
		when(playerEntity.getType()).thenReturn(EntityType.PLAYER);
		when(damageEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FIRE);
		fall.onFall(damageEvent);
		verify(playerEntity, never()).addPotionEffect(any());

		when(damageEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
		when(damageEvent.getDamage()).thenReturn(1.0);
		fall.onFall(damageEvent);
		verify(playerEntity, never()).addPotionEffect(any());
	}

	@Test
	void bleedingSkipsMiscNonPlayerEntities() {
		when(damageByEntityEvent.getEntity()).thenReturn(cow);
		when(cow.getLocation()).thenReturn(null);
		when(cow.getSpawnCategory()).thenReturn(SpawnCategory.MISC);
		when(cow.getType()).thenReturn(EntityType.ARMOR_STAND);

		new Bleeding(plugin).onBleeding(damageByEntityEvent);

		verify(cow, never()).getWorld();
	}

	@Test
	void chequeIgnoresLeftClickNonPaperAndPaperWithoutTag() {
		Cheque cheque = new Cheque(plugin);
		when(interactEvent.getPlayer()).thenReturn(player);

		when(interactEvent.getAction()).thenReturn(Action.LEFT_CLICK_AIR);
		cheque.onPlayerUse(interactEvent);
		verify(player, never()).getInventory();

		when(interactEvent.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(item);
		when(item.getType()).thenReturn(Material.STONE);
		cheque.onPlayerUse(interactEvent);
		verify(item, never()).getItemMeta();

		when(item.getType()).thenReturn(Material.PAPER);
		when(item.getItemMeta()).thenReturn(meta);
		when(meta.getPersistentDataContainer()).thenReturn(pdc);
		when(plugin.getName()).thenReturn("SeriousRP");
		when(pdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(null);
		cheque.onPlayerUse(interactEvent);
		verify(interactEvent, never()).setCancelled(true);
	}

	@Test
	void chequeCancelsWhenIssuerCannotCoverTheValue() {
		when(plugin.getName()).thenReturn("SeriousRP");
		when(interactEvent.getPlayer()).thenReturn(player);
		when(interactEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(item);
		when(item.getType()).thenReturn(Material.PAPER);
		when(item.getItemMeta()).thenReturn(meta);
		when(item.getAmount()).thenReturn(2);
		when(meta.getPersistentDataContainer()).thenReturn(pdc);

		UUID issuerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		String payload = "{\"author\":\"bank\",\"authorUUID\":\"" + issuerId
			+ "\",\"authorDisplayName\":\"Bank\",\"value\":40.0,\"creationDate\":1700000000000}";
		when(pdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(payload);

		Economy economy = mock(Economy.class);
		Main.economy = economy;
		when(economy.getBalance(org.mockito.ArgumentMatchers.nullable(OfflinePlayer.class))).thenReturn(10.0);

		try (MockedStatic<Bukkit> ignored = mockStatic(Bukkit.class, invocation -> null)) {
			new Cheque(plugin).onPlayerUse(interactEvent);
		}

		verify(interactEvent).setCancelled(true);
		verify(player).sendMessage(org.mockito.ArgumentMatchers.contains("issuer"));
		Main.economy = null;
	}

	@Test
	void chequeClaimsWhenIssuerHasFunds() {
		when(plugin.getName()).thenReturn("SeriousRP");
		when(interactEvent.getPlayer()).thenReturn(player);
		when(interactEvent.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(item);
		when(item.getType()).thenReturn(Material.PAPER);
		when(item.getItemMeta()).thenReturn(meta);
		when(item.getAmount()).thenReturn(1);
		when(meta.getPersistentDataContainer()).thenReturn(pdc);

		UUID issuerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		String payload = "{\"author\":\"bank\",\"authorUUID\":\"" + issuerId
			+ "\",\"authorDisplayName\":\"Bank\",\"value\":15.0,\"creationDate\":1700000000000}";
		when(pdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn(payload);

		Economy economy = mock(Economy.class);
		Main.economy = economy;
		when(economy.getBalance(org.mockito.ArgumentMatchers.nullable(OfflinePlayer.class))).thenReturn(100.0);

		try (MockedStatic<Bukkit> ignored = mockStatic(Bukkit.class, invocation -> null)) {
			new Cheque(plugin).onPlayerUse(interactEvent);
		}

		verify(economy).withdrawPlayer(org.mockito.ArgumentMatchers.nullable(OfflinePlayer.class), eq(15.0));
		verify(economy).depositPlayer(player, 15.0);
		verify(inventory).remove(item);
		verify(interactEvent).setCancelled(true);
		verify(player).sendMessage(org.mockito.ArgumentMatchers.contains("Bank"));
		Main.economy = null;
	}

	@Test
	void chequeSwallowsInvalidJsonPayload() {
		when(plugin.getName()).thenReturn("SeriousRP");
		when(interactEvent.getPlayer()).thenReturn(player);
		when(interactEvent.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(item);
		when(item.getType()).thenReturn(Material.PAPER);
		when(item.getItemMeta()).thenReturn(meta);
		when(meta.getPersistentDataContainer()).thenReturn(pdc);
		when(pdc.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenReturn("{not-json");

		new Cheque(plugin).onPlayerUse(interactEvent);

		verify(interactEvent, never()).setCancelled(true);
	}
}
