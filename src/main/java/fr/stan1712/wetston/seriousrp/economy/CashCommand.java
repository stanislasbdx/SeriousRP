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
	static final String TRANSFORM = "transform";
	static final String COMPACT = "compact";
	static final String BREAK = "break";
	static final String TRANSFORM_PERM = "seriousrp.economy.cash.transform";
	static final String CHANGE_PERM = "seriousrp.economy.cash.change";
	private static final String TARGET_PLACEHOLDER = "%target%";

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
		if (args.length == 0) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.Usage"));
			return false;
		}
		if (TRANSFORM.equalsIgnoreCase(args[0])) {
			return transform(sender, args);
		}
		if (COMPACT.equalsIgnoreCase(args[0]) || BREAK.equalsIgnoreCase(args[0])) {
			return change(sender, args);
		}
		sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.Usage"));
		return true;
	}

	private boolean transform(CommandSender sender, String[] args) {
		if (!sender.hasPermission(TRANSFORM_PERM)) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
			return true;
		}
		if (args.length < 2) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.Usage"));
			return true;
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
				.replace(TARGET_PLACEHOLDER, plan.playerName()));
			return true;
		}
		giveTransformedCash(sender, target, plan);
		return true;
	}

	private boolean change(CommandSender sender, String[] args) {
		if (!sender.hasPermission(CHANGE_PERM)) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Core.NoPerms"));
			return true;
		}
		if (!(sender instanceof Player player)) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Change.PlayerOnly"));
			return true;
		}
		if (args.length != 1) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Change.Usage"));
			return true;
		}
		List<Cash.Stack> loose = walletItems.readLooseCash(player);
		int total = CashTender.total(loose);
		if (total <= 0) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Change.NoCash"));
			return true;
		}
		boolean breaking = BREAK.equalsIgnoreCase(args[0]);
		Optional<List<Cash.Stack>> next = breaking
			? CashTender.breakSmall(total, cash.descendingValues(), cash.breakMaxDenomination(), cash.breakPieceCap())
			: CashTender.greedy(total, cash.descendingValues());
		if (next.isEmpty()) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Change.NoCash"));
			return true;
		}
		if (CashTender.sameStacks(loose, next.get())) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Change.AlreadyChanged"));
			return true;
		}
		if (!walletItems.canFitLoose(player, next.get())) {
			player.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Change.InventoryFull"));
			return true;
		}
		walletItems.replaceLooseCash(player, next.get());
		player.sendMessage(getShortPrefixString() + getConfigString(
			breaking ? "Economy.Cash.Change.Broken" : "Economy.Cash.Change.Compacted"
		));
		return true;
	}

	void giveTransformedCash(CommandSender sender, Player target, CashTender.TransformPlan plan) {
		List<Cash.Stack> stacks = CashTender.toInventoryStacks(plan.stacks());
		int empty = 0;
		for (ItemStack item : target.getInventory().getStorageContents()) {
			if (item == null || item.getType() == Material.AIR) {
				empty++;
			}
		}
		if (empty < stacks.size()) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.InventoryFull")
				.replace(TARGET_PLACEHOLDER, target.getName()));
			return;
		}
		if (Main.economy == null || Main.economy.getBalance(target) < plan.vaultDebit()) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.NotEnough")
				.replace(TARGET_PLACEHOLDER, target.getName()));
			return;
		}
		if (!Main.economy.withdrawPlayer(target, plan.vaultDebit()).transactionSuccess()) {
			sender.sendMessage(getShortPrefixString() + getConfigString("Economy.Cash.Transform.NotEnough")
				.replace(TARGET_PLACEHOLDER, target.getName()));
			return;
		}
		for (Cash.Stack stack : stacks) {
			target.getInventory().addItem(walletItems.createCashItem(stack));
		}
		sender.sendMessage(getShortPrefixString() + cash.applyAmount(
			getConfigString("Economy.Cash.Transform.Done"),
			plan.vaultDebit()
		).replace(TARGET_PLACEHOLDER, target.getName()));
	}
}
