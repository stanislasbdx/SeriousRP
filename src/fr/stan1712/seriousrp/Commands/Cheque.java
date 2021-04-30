package fr.stan1712.seriousrp.Commands;

import fr.stan1712.seriousrp.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Random;

public class Cheque implements CommandExecutor {
	private final Plugin pl;

	public Cheque(Main pl) {
		this.pl = pl;
		pl.getConfig();
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

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return true;

		Player player = (Player) sender;

		if(this.pl.getConfig().getBoolean("Core.Modules.Economy")) {
			if(player.hasPermission("seriousrp.economy.cheques")) {

				if(args.length == 1 && isInt(args[0]) && Integer.parseInt(args[0]) > 1) {
					ItemStack chequeItem = new ItemStack(Material.PAPER);
					ItemMeta inHandMeta = chequeItem.getItemMeta();
					ArrayList<String> inHandLore = new ArrayList<>();

					inHandMeta.setDisplayName(this.pl.getConfig().getString("Economy.Cheque.Lores.Title").replace("&", "§").replace("%amount%", args[0]));
					inHandLore.add(this.pl.getConfig().getString("Economy.Cheque.Lores.Value").replace("&", "§") + "§l" + args[0] + this.pl.getConfig().getString("Economy.Currency"));
					inHandLore.add(this.pl.getConfig().getString("Economy.Cheque.Lores.Author").replace("&", "§") + "§7§o" + player.getDisplayName());
					inHandLore.add("");
					inHandLore.add(this.pl.getConfig().getString("Economy.Cheque.Lores.Usage").replace("&", "§"));
					inHandMeta.setLore(inHandLore);
					chequeItem.setItemMeta(inHandMeta);

					if (player.getInventory().contains(chequeItem)) {
						player.sendMessage(this.pl.getConfig().getString("Economy.Cheque.Already").replace("&", "§").replace("%amount%", args[0]));
					}
					else {
						if(player.getInventory().firstEmpty() >= 0){
							String price = args[0];
							double dprice = Double.parseDouble(price);

							if (Main.economy.getBalance(player) < dprice) {
								player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Economy.NotEnough").replace("&", "§").replace("%amount%", args[0]));
							}
							else {
								player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Economy.Cheque.Created").replace("&", "§").replace("%amount%", args[0]));


								Main.economy.withdrawPlayer(player, dprice);
								player.getInventory().addItem(chequeItem);
							}
						}
						else {
							player.sendMessage(this.pl.getConfig().getString("Economy.Cheque.InventoryFull").replace("&", "§"));
						}


					}
				}
				else {
					player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Economy.Cheque.Usage").replace("&", "§"));
				}

			}
			else {
				player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
			}
		}
		else {
			if(this.pl.getConfig().getBoolean("Core.Modules.InactiveDebug")) {
				player.sendMessage(ChatColor.GOLD + "» " + this.pl.getConfig().getString("Core.Modules.InactiveMessage").replace("&", "§").replace("%module%", "Economy"));
			}
		}

		return true;
	}
}
