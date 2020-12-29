package fr.stan1712.seriousrp.Events;

import fr.stan1712.seriousrp.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class Displacement implements Listener {

    public Displacement(Main pl) {
        pl.getConfig();
    }

    /*@EventHandler
    public void onDisplacement (PlayerMoveEvent event){
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        Vector vector = loc.getDirection();

        player.sendMessage(String.valueOf(player.getVelocity()));
        player.sendMessage(String.valueOf(player.getWalkSpeed()));
        player.sendMessage(String.valueOf(player.getFlySpeed()));

        if (loc.subtract(0, 1, 0).getBlock().getType().equals(Material.BEACON)) {
            vector.setY(2);

            player.setVelocity(vector);
        }

    	Bukkit.getScheduler().runTaskAsynchronously(this.pl, () -> {

        });

		for (int i = 0; i < 2; i++) {
			locations[i] = player.getLocation().toString();

			Location loc = player.getLocation();

			Location loc1 = player.getLocation();

			if(loc.getX() == loc1.getX() && loc.getZ() == loc1.getZ()) { return; }

			player.sendMessage(ChatColor.GREEN + "x: " + Math.round(loc.getX()) + " ; y: " + Math.round(loc.getY()) + " ; z: " + Math.round(loc.getZ()));
			player.sendMessage(ChatColor.RED + "x: " + Math.round(loc1.getX()) + " ; y: " + Math.round(loc1.getY()) + " ; z: " + Math.round(loc1.getZ()));

			player.sendMessage("out: " + locations);
		}


		Location loc = player.getLocation();

		player.sendMessage("----");
		player.sendMessage("pos jX: " + loc.getX());
		player.sendMessage("pos jY: " + loc.getY());
		player.sendMessage("pos jZ: " + loc.getZ());
		player.sendMessage(" ");
		player.sendMessage("fall: " + player.getFallDistance());

		double speed;

		if (!player.getWorld().equals(player.getLocation().getWorld())) speed = 0.0D;


		double d = player.getLocation().distance(player.getLocation());
		long l = System.currentTimeMillis() - Long.valueOf(System.currentTimeMillis()).longValue();
		speed = (int)(d / l * 100.0D * 6.0D * 6.0D * 100.0D) / 100.0D;

		player.sendMessage("d: " + d);
		player.sendMessage("l: " + l);

		player.sendMessage("speed: " + speed);
    }*/
}