package fr.stan1712.wetston.seriousrp.events;

import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

public class Bleeding implements Listener {
	Plugin plugin;

	public Bleeding(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onBleeding(EntityDamageByEntityEvent event) {
		Entity eventEntity = event.getEntity();
		Location entityLocation = eventEntity.getLocation();

		if(eventEntity.getSpawnCategory() != SpawnCategory.MISC) playBleedingEffect(eventEntity, entityLocation);
		else if(eventEntity.getType() == EntityType.PLAYER || eventEntity.getType() == EntityType.VILLAGER) playBleedingEffect(eventEntity, entityLocation);
	}

	private static void playBleedingEffect(Entity entity, Location entityLocation) {
		entity.getWorld().playEffect(entityLocation, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
	}
}
