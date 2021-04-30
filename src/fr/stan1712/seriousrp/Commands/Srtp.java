package fr.stan1712.seriousrp.Commands;

import fr.stan1712.seriousrp.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class Srtp implements CommandExecutor {
	private final Plugin pl;

	public Srtp(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return true;

		Player player = (Player) sender;

		if(player.hasPermission("serious.randomtp")) {
			Random r = new Random();

			player.sendMessage(ChatColor.AQUA + "+----- ♖ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ♖ -----+");
			player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("MicroModules.RandomTeleport").replace("&", "§"));
			player.sendMessage(ChatColor.AQUA + "+----- ----- ----- -----+");

			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 100));
			player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 50, 100));
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 100));

			int x = player.getLocation().getBlockX() + r.nextInt(this.pl.getConfig().getInt("MicroModules.RandomBlocks"));
			int z = player.getLocation().getBlockX() + r.nextInt(this.pl.getConfig().getInt("MicroModules.RandomBlocks"));

			int rtpcheck = player.getWorld().getHighestBlockYAt(x, z);
			int rtpdone = rtpcheck + 1;

			Location rtp = new Location(player.getWorld(), x, rtpdone, z);
			player.teleport(rtp);
		}
		else {
			player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
		}

		return true;
	}
}
