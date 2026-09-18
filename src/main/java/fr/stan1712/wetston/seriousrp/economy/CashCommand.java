package fr.stan1712.wetston.seriousrp.economy;

import fr.stan1712.wetston.seriousrp.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigBoolean;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getConfigString;
import static fr.stan1712.wetston.seriousrp.Utils.ConfigFactory.getShortPrefixString;

public final class CashCommand implements CommandExecutor {
	private static final String TRANSFORM = "transform";

	private final Cash cash;
	private final WalletListener walletItems;
	private final Function<String, Player> playerLookup;

	public CashCommand(Plugin plugin, Cash cash, WalletListener walletItems) {
		this(cash, walletItems, Bukkit::getPlayerExact);
		Objects.requireNonNull(plugin);
	}

	CashCommand(Cash cash, WalletListener walletItems, Function<String, Player> playerLookup) {
		this.cash = cash;
		this.walletItems = walletItems;
		this.playerLookup = playerLookup;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!Boolean.TRUE.equals(getConfigBoolean("Core.Modules.Economy")) || !cash.isEnabled()) {
			if (sender instanceof Player player && Boolean.TRUE.equals(getConfigBoolean("Core.Modules.InactiveDebug"))) {
				player.sendMessage(getShortPrefixString() + getConfigString("Core.Modules.InactiveMessage").replace("%module%", "Economy"));
			}
			return true;
		}
		if (!sender.hasPermission("seriousrp.economy.cash.transform")) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
			return true;
		}
		if (args.length < 2 || !TRANSFORM.equalsIgnoreCase(args[0])) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.Usage"));
			return args.length != 0;
		}
		String[] transformArgs = new String[args.length - 1];
		System.arraycopy(args, 1, transformArgs, 0, transformArgs.length);
		Optional<CashTender.TransformPlan> parsed = CashTender.parseTransform(transformArgs, cash.descendingValues());
		if (parsed.isEmpty()) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.Usage"));
			return true;
		}
		CashTender.TransformPlan plan = parsed.get();
		Player target = playerLookup.apply(plan.playerName());
		if (target == null) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.UnknownPlayer")
				.replace("%target%", plan.playerName()));
			return true;
		}
		giveTransformedCash(sender, target, plan);
		return true;
	}

	void giveTransformedCash(CommandSender sender, Player target, CashTender.TransformPlan plan) {
		List<Cash.Stack> stacks = CashTender.toInventoryStacks(plan.stacks());
		int empty = 0;
		ItemStack[] storage = target.getInventory().getStorageContents();
		if (storage != null) {
			for (ItemStack item : storage) {
				if (item == null || item.getType() == Material.AIR) {
					empty++;
				}
			}
		}
		if (empty < stacks.size()) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.InventoryFull")
				.replace("%target%", target.getName()));
			return;
		}
		if (Main.economy == null || Main.economy.getBalance(target) < plan.vaultDebit()) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.NotEnough")
				.replace("%target%", target.getName()));
			return;
		}
		if (!Main.economy.withdrawPlayer(target, plan.vaultDebit()).transactionSuccess()) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.NotEnough")
				.replace("%target%", target.getName()));
			return;
		}
		for (Cash.Stack stack : stacks) {
			target.getInventory().addItem(walletItems.createCashItem(stack));
		}
		sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.Done")
			.replace("%amount%", Integer.toString(plan.vaultDebit()))
			.replace("%currency%", cash.currency())
			.replace("%target%", target.getName()));
	}
}
