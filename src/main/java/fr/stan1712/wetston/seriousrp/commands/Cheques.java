package fr.stan1712.wetston.seriousrp.commands;

import com.google.gson.Gson;
import fr.stan1712.wetston.seriousrp.Main;
import fr.stan1712.wetston.seriousrp.pojo.PCheque;
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
import java.util.function.BiFunction;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.*;

public class Cheques implements CommandExecutor {
	private static final String AMOUNT_PLACEHOLDER = "%amount%";

	private final Plugin pl;
	private final BiFunction<Player, String, ItemStack> chequeFactory;

	public Cheques(Main pl) {
		this.pl = pl;
		this.chequeFactory = this::createChequeItem;
	}

	Cheques(Main pl, BiFunction<Player, String, ItemStack> chequeFactory) {
		this.pl = pl;
		this.chequeFactory = chequeFactory;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return true;

		if (!Boolean.TRUE.equals(getConfigBoolean("Core.Modules.Economy"))) {
			sendInactiveEconomyMessage(player);
			return true;
		}

		if (!player.hasPermission("seriousrp.economy.cheques")) {
			player.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
			return true;
		}

		if (args.length == 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cheque.Usage"));
			return false;
		}

		final String strValue = args[0];
		if (args.length != 1 || !ChequeAmountParser.isPositiveAmount(strValue)) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cheque.Usage"));
			return true;
		}

		giveChequeIfPossible(player, strValue, chequeFactory.apply(player, strValue));
		return true;
	}

	private void sendInactiveEconomyMessage(Player player) {
		if (Boolean.TRUE.equals(getConfigBoolean("Core.Modules.InactiveDebug"))) {
			player.sendMessage(getShortPrefixString() + getConfigString("Core.Modules.InactiveMessage").replace("%module%", "Economy"));
		}
	}

	ItemStack createChequeItem(Player player, String strValue) {
		ItemStack chequeItem = new ItemStack(Material.PAPER);
		ItemMeta chequeMeta = chequeItem.getItemMeta();
		assert chequeMeta != null;

		PCheque pCheque = new PCheque(player, Double.parseDouble(strValue));
		PersistentDataContainer chequeData = chequeMeta.getPersistentDataContainer();
		NamespacedKey namespacedKey = new NamespacedKey(this.pl, "srp-cheque");
		chequeData.set(namespacedKey, PersistentDataType.STRING, new Gson().toJson(pCheque));

		ArrayList<String> chequeLore = new ArrayList<>();
		chequeMeta.setDisplayName(getConfigString("Economy.Cheque.Lores.Title").replace(AMOUNT_PLACEHOLDER, strValue));
		chequeLore.add(getConfigString("Economy.Cheque.Lores.Value") + "§l" + strValue + getConfigString("Economy.Currency"));
		chequeLore.add(getConfigString("Economy.Cheque.Lores.Author") + "§7§o" + player.getDisplayName());
		chequeLore.add(getConfigString("Economy.Cheque.Lores.CreationDate") + "§7§o" + pCheque.getParsedCreationDate());
		chequeLore.add("");
		chequeLore.add(getConfigString("Economy.Cheque.Lores.Usage"));
		chequeMeta.setLore(chequeLore);

		chequeItem.setItemMeta(chequeMeta);
		return chequeItem;
	}

	void giveChequeIfPossible(Player player, String strValue, ItemStack chequeItem) {
		if (player.getInventory().contains(chequeItem)) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cheque.Already").replace(AMOUNT_PLACEHOLDER, strValue));
			return;
		}

		if (player.getInventory().firstEmpty() < 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cheque.InventoryFull"));
			return;
		}

		double value = Double.parseDouble(strValue);
		if (Main.economy.getBalance(player) < value) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.NotEnough").replace(AMOUNT_PLACEHOLDER, strValue));
			return;
		}

		player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cheque.Created").replace(AMOUNT_PLACEHOLDER, strValue));
		player.getInventory().addItem(chequeItem);
	}
}
