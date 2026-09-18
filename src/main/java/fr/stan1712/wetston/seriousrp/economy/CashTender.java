package fr.stan1712.wetston.seriousrp.economy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

public final class CashTender {
	private CashTender() {
		throw new IllegalStateException("Utility class");
	}

	public record Ledger(List<Cash.Stack> inventory, List<List<Cash.Stack>> wallets) {
		public Ledger {
			inventory = List.copyOf(inventory);
			wallets = wallets.stream().map(List::copyOf).toList();
		}

		public int total() {
			int sum = CashTender.total(inventory);
			for (List<Cash.Stack> wallet : wallets) {
				sum = saturatingAdd(sum, CashTender.total(wallet));
			}
			return sum;
		}
	}

	public record TransformPlan(String playerName, int vaultDebit, List<Cash.Stack> stacks) {
		public TransformPlan {
			stacks = List.copyOf(stacks);
		}
	}

	public static int total(List<Cash.Stack> stacks) {
		long sum = 0L;
		for (Cash.Stack stack : stacks) {
			sum += stack.total();
			if (sum >= Integer.MAX_VALUE) {
				return Integer.MAX_VALUE;
			}
		}
		return (int) sum;
	}

	public static List<Cash.Stack> compact(List<Cash.Stack> stacks) {
		TreeMap<Integer, Integer> merged = new TreeMap<>(Comparator.reverseOrder());
		for (Cash.Stack stack : stacks) {
			merged.merge(stack.denomination(), stack.amount(), CashTender::saturatingAdd);
		}
		List<Cash.Stack> compacted = new ArrayList<>();
		for (var entry : merged.entrySet()) {
			compacted.add(new Cash.Stack(entry.getKey(), entry.getValue()));
		}
		return List.copyOf(compacted);
	}

	public static List<Cash.Stack> toInventoryStacks(List<Cash.Stack> stacks) {
		List<Cash.Stack> split = new ArrayList<>();
		for (Cash.Stack stack : compact(stacks)) {
			int remaining = stack.amount();
			while (remaining > 0) {
				int count = Math.min(remaining, Cash.MAX_STACK_SIZE);
				split.add(new Cash.Stack(stack.denomination(), count));
				remaining -= count;
			}
		}
		return List.copyOf(split);
	}

	public static Optional<List<Cash.Stack>> greedy(int amount, List<Integer> denomsDescending) {
		if (amount < 0 || denomsDescending.isEmpty()) {
			return Optional.empty();
		}
		if (amount == 0) {
			return Optional.of(List.of());
		}
		int remaining = amount;
		List<Cash.Stack> given = new ArrayList<>();
		for (Integer denom : denomsDescending) {
			if (denom == null || denom <= 0) {
				continue;
			}
			int count = remaining / denom;
			if (count > 0) {
				given.add(new Cash.Stack(denom, count));
				remaining -= count * denom;
			}
		}
		if (remaining != 0) {
			return Optional.empty();
		}
		return Optional.of(List.copyOf(given));
	}

	public static Optional<List<Cash.Stack>> extract(List<Cash.Stack> source, int amount, List<Integer> denomsDescending) {
		if (amount <= 0) {
			return Optional.empty();
		}
		int available = total(source);
		if (available < amount) {
			return Optional.empty();
		}
		return greedy(available - amount, denomsDescending);
	}

	public static Optional<Ledger> pay(Ledger ledger, int amount, List<Integer> denomsDescending) {
		if (amount <= 0 || ledger.total() < amount) {
			return Optional.empty();
		}
		int remaining = amount;
		List<Cash.Stack> inventory = ledger.inventory();
		List<List<Cash.Stack>> wallets = new ArrayList<>(ledger.wallets());

		int inventoryTotal = total(inventory);
		if (inventoryTotal > 0) {
			int take = Math.min(inventoryTotal, remaining);
			Optional<List<Cash.Stack>> extracted = extract(inventory, take, denomsDescending);
			if (extracted.isEmpty()) {
				return Optional.empty();
			}
			inventory = extracted.get();
			remaining -= take;
		}

		for (int index = 0; index < wallets.size() && remaining > 0; index++) {
			int walletTotal = total(wallets.get(index));
			if (walletTotal <= 0) {
				continue;
			}
			int take = Math.min(walletTotal, remaining);
			Optional<List<Cash.Stack>> extracted = extract(wallets.get(index), take, denomsDescending);
			if (extracted.isEmpty()) {
				return Optional.empty();
			}
			wallets.set(index, extracted.get());
			remaining -= take;
		}

		return Optional.of(new Ledger(inventory, wallets));
	}

	public static Optional<TransformPlan> parseTransform(String[] args, List<Integer> knownDenoms) {
		if (args == null || args.length < 2 || args.length > 3) {
			return Optional.empty();
		}
		String playerName = args[0];
		if (playerName == null || playerName.isBlank()) {
			return Optional.empty();
		}
		if (args.length == 2) {
			var amount = Cash.parsePositiveInt(args[1]);
			if (amount.isEmpty()) {
				return Optional.empty();
			}
			return greedy(amount.getAsInt(), knownDenoms)
				.map(stacks -> new TransformPlan(playerName, amount.getAsInt(), stacks));
		}

		var denomination = Cash.parsePositiveInt(args[1]);
		var quantity = Cash.parsePositiveInt(args[2]);
		if (denomination.isEmpty() || quantity.isEmpty() || !knownDenoms.contains(denomination.getAsInt())) {
			return Optional.empty();
		}
		try {
			int debit = Math.multiplyExact(denomination.getAsInt(), quantity.getAsInt());
			return Optional.of(new TransformPlan(
				playerName,
				debit,
				List.of(new Cash.Stack(denomination.getAsInt(), quantity.getAsInt()))
			));
		}
		catch (ArithmeticException overflow) {
			return Optional.empty();
		}
	}

	static int saturatingAdd(int left, int right) {
		long sum = (long) left + (long) right;
		if (sum >= Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) sum;
	}
}
