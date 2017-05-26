package fr.stan1712.srp;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class falldamage
  implements Listener
{
  srp plugin;
  
  public falldamage()
  {
    this.plugin = srp.getInstance();
  }
  
  @EventHandler
  public void onFall(EntityDamageEvent e)
  {
    if (!(e.getEntity() instanceof Player)) {
      return;
    }
    if (e.getCause() == EntityDamageEvent.DamageCause.FALL)
    {
      if (e.getDamage() >= 2.0D)
      {
        LivingEntity entity = (LivingEntity)e.getEntity();
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 75, (int)Math.round(e.getDamage() / 2.0D - 1.0D)));
      }
    }
  }
}
