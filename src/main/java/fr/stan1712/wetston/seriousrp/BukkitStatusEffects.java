package fr.stan1712.wetston.seriousrp;

import fr.stan1712.wetston.seriousrp.commands.medics.EffectNameFormatter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;

import java.util.Objects;

/**
 * Potion registry calls that need a live Bukkit server. Kept out of unit-test coverage.
 */
public final class BukkitStatusEffects {
	private BukkitStatusEffects() {
		throw new IllegalStateException("Utility class");
	}

	public static void applyFall(LivingEntity entity, double damage) {
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, (int) Math.round(damage / 2.0D - 1.0D)));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 15, 1000));
	}

	public static void applyRtp(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 50, 100));
		player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 50, 100));
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 100));
	}

	public static void clearComa(Player player) {
		player.removePotionEffect(PotionEffectType.RESISTANCE);
		player.removePotionEffect(PotionEffectType.SLOWNESS);
		player.removePotionEffect(PotionEffectType.BLINDNESS);
		player.removePotionEffect(PotionEffectType.HUNGER);
	}

	public static String potionLabel(PotionEffect effect) {
		String effectCategoryLitteral = Objects.equals(effect.getType().getCategory(), PotionEffectTypeCategory.HARMFUL) ? "§4- §c" : "§2+ §a";
		return effectCategoryLitteral + EffectNameFormatter.formatNamespacedKey(String.valueOf(effect.getType().getKeyOrThrow()));
	}
}
