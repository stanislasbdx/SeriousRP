package fr.stan1712.wetston.seriousrp.events;

import fr.stan1712.wetston.seriousrp.BukkitStatusEffects;
import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

import java.util.function.BiConsumer;

public class Fall implements Listener {
	Plugin plugin;
	private final BiConsumer<LivingEntity, Double> fallEffects;

	public Fall(Main plugin) {
		this(plugin, BukkitStatusEffects::applyFall);
	}

	Fall(Main plugin, BiConsumer<LivingEntity, Double> fallEffects) {
		this.plugin = plugin;
		this.fallEffects = fallEffects;
	}

	@EventHandler
	public void onFall(EntityDamageEvent event) {
		Entity eventEntity = event.getEntity();

		if(eventEntity.getType() != EntityType.PLAYER) return;

		if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getDamage() >= 2.0D) {
			LivingEntity playerEntity = (LivingEntity)event.getEntity();
			fallEffects.accept(playerEntity, event.getDamage());
		}
	}
}
