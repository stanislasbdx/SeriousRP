package fr.stan1712.seriousrp;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Events implements Listener {
    private Main pl;
    
    public Events(Main pl) {
        this.pl = pl;
        pl.getConfig();
    }
	
    // Chutes
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
	
	// Dégâts
	@EventHandler
	public void onSang(EntityDamageByEntityEvent e) {
		Entity entity = e.getEntity();
		Location entityloc = entity.getLocation();
		if (entity.getType() != EntityType.ITEM_FRAME) {
			entity.getWorld().playEffect(entityloc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		}
	}
	
	public Map<Player, Location> playerLocation = new HashMap<Player, Location>();
	public Map<Player, Entity> chairList = new HashMap<Player, Entity>();
	public Map<Player, Location> chairLocation = new HashMap<Player, Location>();
	Logger log = Logger.getLogger("Minecraft");
		
		  
	// Chaises
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(this.pl.getConfig().getBoolean("Core.Modules.Chairs") == true){
			Player player = event.getPlayer();
			    
			if (event.getClickedBlock() != null){
				Block block = event.getClickedBlock();
				if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) && (!player.isInsideVehicle()) && (player.getItemInHand().getType() == Material.AIR)){
					String blockMaterial = block.getType().name();
					for (String chairBlock : this.pl.getConfig().getStringList("Chairs")){
						if (blockMaterial.equalsIgnoreCase(chairBlock)){
							World world = player.getWorld();
							this.playerLocation.put(player, player.getLocation());
							Entity chair = world.spawnEntity(player.getLocation(), EntityType.ARROW);
							this.chairList.put(player, chair);
							chair.teleport(block.getLocation().add(0.5D, 0.2D, 0.5D));
							this.chairLocation.put(player, chair.getLocation());
							chair.setPassenger(player);
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if(this.pl.getConfig().getBoolean("Core.Modules.Chairs") == true){
			if (this.playerLocation.containsKey(event.getPlayer())){
				if (event.getPlayer().isSneaking()){
					Player player = event.getPlayer();
					final Player sit_player = player;
					final Location stand_location = (Location)this.playerLocation.get(player);
					Entity sit_chair = (Entity)this.chairList.get(player);
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.pl, new Runnable(){
						public void run(){
							sit_player.teleport(stand_location);
							sit_player.setSneaking(false);
						}
					}, 1L);
					event.setCancelled(true);
					this.playerLocation.remove(player);
					sit_chair.remove();
					this.chairList.remove(player);
					event.setCancelled(true);
					return;
				}
				event.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		if(this.pl.getConfig().getBoolean("Core.Modules.Chairs") == true){
			if (this.playerLocation.containsKey(event.getPlayer())){
				Player player = event.getPlayer();
				if ((player.getLocation() != this.playerLocation.get(player)) && (!player.isInsideVehicle())){
					World world = player.getWorld();
					if (this.chairLocation.containsKey(player)){
						Entity chair = world.spawnEntity((Location)this.chairLocation.get(player), EntityType.ARROW);
						chair.setPassenger(player);
					}
				}
			}
		}
	}
}
