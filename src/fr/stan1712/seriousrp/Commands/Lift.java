package fr.stan1712.seriousrp.Commands;

import fr.stan1712.seriousrp.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class Lift implements CommandExecutor {
	private final Plugin pl;

	public Lift(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return true;

		Player player = (Player) sender;

		if(player.hasPermission("seriousrp.lift")) {
			List<Entity> playersCarried = player.getPassengers();

			if(playersCarried.size() > 0){
				for(Entity entity : playersCarried){
					Player player2 = getPlayerFromEntity(entity);

					player.sendMessage(this.pl.getConfig().getString("MicroModules.NoMoreLifting").replace("&", "§").replace("%player%", player2.getDisplayName()));

					player.removePassenger(entity);
				}
			}
			else {
				List<Entity> nearbyEntities = player.getNearbyEntities(2, 2, 2);

				if(nearbyEntities.size() == 0){
					player.sendMessage(this.pl.getConfig().getString("MicroModules.NobodyToLift").replace("&", "§"));
				}
				else if(nearbyEntities.get(0).getType() != EntityType.PLAYER) {
					player.sendMessage(this.pl.getConfig().getString("MicroModules.NobodyToLift").replace("&", "§"));
				}
				else {
					Entity entity = nearbyEntities.get(0);
					Player player2 = getPlayerFromEntity(entity);

					player.sendMessage(this.pl.getConfig().getString("MicroModules.LiftPlayer").replace("&", "§").replace("%player%", player2.getDisplayName()));
					entity.sendMessage(this.pl.getConfig().getString("MicroModules.LiftedPlayer").replace("&", "§").replace("%player%", player.getDisplayName()));

					player.addPassenger(entity);
				}
			}
		}
		else {
			player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
		}

		return true;
	}

	public Player getPlayerFromEntity(Entity entity) {
		return this.pl.getServer().getPlayer(entity.getName());
	}
}
