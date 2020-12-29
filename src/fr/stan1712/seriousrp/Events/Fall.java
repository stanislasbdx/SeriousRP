package fr.stan1712.seriousrp.Events;

import fr.stan1712.seriousrp.Main;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Fall implements Listener {

    public Fall(Main pl) {
        pl.getConfig();
    }

    @EventHandler
    public void onFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (event.getDamage() >= 2.0D) {
                LivingEntity entity = (LivingEntity)event.getEntity();
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 75, (int)Math.round(event.getDamage() / 2.0D - 1.0D)));
            }
        }
    }
}
