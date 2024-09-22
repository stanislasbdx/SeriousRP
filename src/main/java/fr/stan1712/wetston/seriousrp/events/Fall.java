package fr.stan1712.wetston.seriousrp.events;

import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Fall implements Listener {
	Plugin plugin;

	public Fall(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onFall(EntityDamageEvent event) {
		Entity eventEntity = event.getEntity();

		if(eventEntity.getType() != EntityType.PLAYER) return;

		if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
			if (event.getDamage() >= 2.0D) {
				LivingEntity playerEntity = (LivingEntity)event.getEntity();

				playerEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, (int)Math.round(event.getDamage() / 2.0D - 1.0D)));
				playerEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 15, 1000));
			}
		}
	}
}
