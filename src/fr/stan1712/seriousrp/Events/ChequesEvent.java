package fr.stan1712.seriousrp.Events;

import fr.stan1712.seriousrp.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ChequesEvent implements Listener {
    private final Main pl;

    public ChequesEvent(Main pl) {
        this.pl = pl;
        pl.getConfig();
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            //String[] lore = GetLoreFromItemInHand(player);

            ItemStack item = player.getInventory().getItemInMainHand() != null ? player.getInventory().getItemInMainHand() : null;

            ItemMeta imd = item.getItemMeta();
            if(imd == null) return;
            List<String> lore = imd.getLore();
            if(lore == null) return;


            if (lore != null) {
                for (String s : lore) {
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
}
