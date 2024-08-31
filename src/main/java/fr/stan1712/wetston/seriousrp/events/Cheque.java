package fr.stan1712.wetston.seriousrp.events;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.stan1712.wetston.seriousrp.Main;
import fr.stan1712.wetston.seriousrp.pojo.PCheque;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;

public class Cheque implements Listener {
	Plugin plugin;

	public Cheque(Main plugin) {
		this.plugin = plugin;
	}

	private static final Logger _log = LoggerFactory.getLogger("SeriousRP - ChequeEvent");

	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			ItemStack usedItem = player.getInventory().getItemInMainHand();

			if (usedItem.getType() != Material.PAPER) return;

			ItemMeta itemChequeMeta = usedItem.getItemMeta();

			assert itemChequeMeta != null;
			PersistentDataContainer chequeData = itemChequeMeta.getPersistentDataContainer();
			NamespacedKey namespacedKey = new NamespacedKey(plugin, "srp-cheque");

			if (chequeData.get(namespacedKey, PersistentDataType.STRING) == null) return;

			try {
				String chequeDataStr = chequeData.get(namespacedKey, PersistentDataType.STRING);

				PCheque parsedCheque = new Gson().fromJson(chequeDataStr, PCheque.class);

				double chequeValue = parsedCheque.getValue() * event.getPlayer().getInventory().getItemInMainHand().getAmount();

				OfflinePlayer chequeIssuerPlayer = Bukkit.getPlayer(parsedCheque.getAuthorUUID());
				Main.economy.withdrawPlayer(chequeIssuerPlayer, chequeValue);

				Main.economy.depositPlayer(player, chequeValue);

				player.sendMessage(ChatColor.GOLD + "» " + getConfigString("Economy.Cheque.Claimed")
					.replace("%amount%", String.valueOf(chequeValue))
					.replace("%issuer%", parsedCheque.getAuthorDisplayName())
				);

				player.getInventory().remove(player.getInventory().getItemInMainHand());

				event.setCancelled(true);
			} catch (JsonSyntaxException e) {
				_log.error("Unable to process the json", e);
			}
		}
	}
}
