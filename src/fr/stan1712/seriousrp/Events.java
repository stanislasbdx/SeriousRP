package fr.stan1712.seriousrp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

@SuppressWarnings("unused")
public class Events implements Listener {
    private Main pl;
    
    public Events(Main pl) {
        this.pl = pl;
        pl.getConfig();
    }
    
	String[] locations = null;
    
    // Mouvement
    /*@EventHandler
    public void onDisplacement(PlayerMoveEvent event) throws InterruptedException {
    	Player player = event.getPlayer();
    	
    	//Bukkit.getScheduler().runTaskAsynchronously(this.pl, () -> {
    		for (int i = 0; i < 2; i++) {
    			//locations[i] = player.getLocation().toString();
    			
    			Location loc = player.getLocation();

    			TimeUnit.MILLISECONDS.sleep(500);
    			
    			Location loc1 = player.getLocation();
    			
    			if(loc.getX() == loc1.getX() && loc.getZ() == loc1.getZ()) { return; }
    			
    			player.sendMessage(ChatColor.GREEN + "x: " + Math.round(loc.getX()) + " ; y: " + Math.round(loc.getY()) + " ; z: " + Math.round(loc.getZ()));
            	player.sendMessage(ChatColor.RED + "x: " + Math.round(loc1.getX()) + " ; y: " + Math.round(loc1.getY()) + " ; z: " + Math.round(loc1.getZ()));
    			
    			//player.sendMessage("out: " + locations);
			}
    		
    		
    		/*Location loc = player.getLocation();    		
    		
        	player.sendMessage("----");
        	player.sendMessage("pos jX: " + loc.getX());
        	player.sendMessage("pos jY: " + loc.getY());
        	player.sendMessage("pos jZ: " + loc.getZ());
        	player.sendMessage(" ");
        	player.sendMessage("fall: " + player.getFallDistance());*/
        //});
    	
    	
    //}
    
    // Falls
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
	
	// Damages
	@EventHandler
	public void onSang(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		Location entityloc = entity.getLocation();
		if (entity.getType() != EntityType.ITEM_FRAME) {
			entity.getWorld().playEffect(entityloc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		}
	}
	
	// Economy - Cheques
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
        	if(event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore() != null) {
				for (String s : event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore()) {
					if(s.startsWith(this.pl.getConfig().getString("Economy.Cheque.Lores.Value").replace("&", "§"))) {						
						String price = s.replace(this.pl.getConfig().getString("Economy.Cheque.Lores.Value").replace("&", "§"), "").replaceAll("[^\\d.]", "");
					
						double dprice = Double.valueOf(price).doubleValue() * event.getPlayer().getInventory().getItemInMainHand().getAmount();
						String price1 = String.valueOf(dprice);
						
						Main.economy.depositPlayer(player, dprice);
						player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Economy.Cheque.Claimed").replace("&", "§").replace("%amount%", price1));
						player.getInventory().remove(player.getInventory().getItemInMainHand());
						
						event.setCancelled(true);
					}
				}
        	}
        }
	}
	
	// Chairs
	public Map<Player, Location> playerLocation = new HashMap<Player, Location>();
	public Map<Player, Entity> chairList = new HashMap<Player, Entity>();
	public Map<Player, Location> chairLocation = new HashMap<Player, Location>();
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(this.pl.getConfig().getBoolean("Core.Modules.Chairs") == true){
			Player player = event.getPlayer();
			    
			if (event.getClickedBlock() != null){
				Block block = event.getClickedBlock();
				if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) && (!player.isInsideVehicle()) && (player.getInventory().getItemInMainHand().getType() == Material.AIR)){;
					String blockMaterial = block.getType().name();
					for (String chairBlock : this.pl.getConfig().getStringList("Chairs")){
						if (blockMaterial.equalsIgnoreCase(chairBlock)){
							World world = player.getWorld();
							this.playerLocation.put(player, player.getLocation());
							Entity chair = world.spawnEntity(player.getLocation(), EntityType.ARROW);
							this.chairList.put(player, chair);
							chair.teleport(block.getLocation().add(0.5D, 0.2D, 0.5D));
							this.chairLocation.put(player, chair.getLocation());
							chair.addPassenger(player);
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
						chair.addPassenger(player);
					}
				}
			}
		}
	}
}
