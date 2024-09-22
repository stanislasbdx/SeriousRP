package fr.stan1712.wetston.seriousrp.commands;

import com.google.gson.Gson;
import fr.stan1712.wetston.seriousrp.Main;
import fr.stan1712.wetston.seriousrp.pojo.PCheque;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigBoolean;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;

public class Cheques implements CommandExecutor {
	private final Plugin pl;

	public Cheques(Main pl) {
		this.pl = pl;
	}

	private static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
		}
		catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private static boolean isFloat(String s) {
		try {
			Float.parseFloat(s);
		}
		catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return true;

		if(Boolean.TRUE.equals(getConfigBoolean("Core.Modules.Economy"))) {
			if(player.hasPermission("seriousrp.economy.cheques")) {

				if (args.length == 0) {
					player.sendMessage(getConfigString("Economy.Cheque.Usage"));
					return false;
				}

				final String strValue = args[0];

				if(args.length == 1 && ((isInt(strValue) && Integer.parseInt(strValue) > 0) || (isFloat(strValue) && Float.parseFloat(strValue) > 0))) {
					ItemStack chequeItem = new ItemStack(Material.PAPER);

					ItemMeta chequeMeta = chequeItem.getItemMeta();
					ArrayList<String> chequeLore = new ArrayList<>();
					assert chequeMeta != null;
					PersistentDataContainer chequeData = chequeMeta.getPersistentDataContainer();
					NamespacedKey namespacedKey = new NamespacedKey(this.pl, "srp-cheque");

					PCheque pCheque = new PCheque(player, Double.parseDouble(strValue));
					chequeData.set(namespacedKey, PersistentDataType.STRING, new Gson().toJson(pCheque));

					chequeMeta.setDisplayName(getConfigString("Economy.Cheque.Lores.Title").replace("%amount%", strValue));
					chequeLore.add(getConfigString("Economy.Cheque.Lores.Value") + "§l" + strValue + getConfigString("Economy.Currency"));
					chequeLore.add(getConfigString("Economy.Cheque.Lores.Author") + "§7§o" + player.getDisplayName());
					chequeLore.add(getConfigString("Economy.Cheque.Lores.CreationDate") + "§7§o" + pCheque.getParsedCreationDate());
					chequeLore.add("");
					chequeLore.add(getConfigString("Economy.Cheque.Lores.Usage"));
					chequeMeta.setLore(chequeLore);

					chequeItem.setItemMeta(chequeMeta);

					if (player.getInventory().contains(chequeItem)) {
						player.sendMessage(getConfigString("Economy.Cheque.Already").replace("%amount%", strValue));
					}
					else {
						if(player.getInventory().firstEmpty() >= 0){
							double value = Double.parseDouble(strValue);

							if (Main.economy.getBalance(player) < value) {
								player.sendMessage(ChatColor.GOLD + "» " + getConfigString("Economy.NotEnough").replace("%amount%", strValue));
							}
							else {
								player.sendMessage(ChatColor.GOLD + "» " + getConfigString("Economy.Cheque.Created").replace("%amount%", strValue));

								player.getInventory().addItem(chequeItem);
							}
						}
						else {
							player.sendMessage(getConfigString("Economy.Cheque.InventoryFull"));
						}


					}
				}
				else {
					player.sendMessage(ChatColor.GOLD + "» " + getConfigString("Economy.Cheque.Usage"));
				}

			}
			else {
				player.sendMessage("[" + getConfigString("Prefix") + "]" + getConfigString("Core.NoPerms"));
			}
		}
		else {
			if(Boolean.TRUE.equals(getConfigBoolean("Core.Modules.InactiveDebug"))) {
				player.sendMessage(ChatColor.GOLD + "» " + getConfigString("Core.Modules.InactiveMessage").replace("%module%", "Economy"));
			}
		}

		return true;
	}
}
