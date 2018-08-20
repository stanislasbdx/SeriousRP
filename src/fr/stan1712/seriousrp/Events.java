package fr.stan1712.seriousrp;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Events implements Listener {
	
	@EventHandler
	public void onFall(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
			if (e.getDamage() >= 2.0D) {
				LivingEntity entity = (LivingEntity)e.getEntity();
				entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 75, (int)Math.round(e.getDamage() / 2.0D - 1.0D)));
			}
		}
	}
	
	@EventHandler
	public void onSang(EntityDamageByEntityEvent e) {
		Entity entity = e.getEntity();
		Location entityloc = entity.getLocation();
		if (entity.getType() != EntityType.ITEM_FRAME) {
			entity.getWorld().playEffect(entityloc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		}
	}
}
