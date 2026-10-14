package fr.stan1712.wetston.seriousrp.economy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Cassette {
	private static final Gson GSON = new Gson();

	public record Payload(String id, List<Cash.Stack> stacks) {
		public Payload {
			if (id == null || id.isBlank()) {
				throw new IllegalArgumentException("id");
			}
			stacks = stacks == null ? List.of() : CashTender.compact(stacks);
		}

		public int total() {
			return CashTender.total(stacks);
		}
	}

	private static final class PayloadJson {
		private String id;
		private List<StackJson> stacks;
	}

	private static final class StackJson {
		private int denomination;
		private int amount;
	}

	private Cassette() {
		throw new IllegalStateException("Utility class");
	}

	public static Payload empty() {
		return new Payload(UUID.randomUUID().toString(), List.of());
	}

	public static String toJson(Payload payload) {
		PayloadJson json = new PayloadJson();
		json.id = payload.id();
		json.stacks = payload.stacks().stream().map(stack -> {
			StackJson row = new StackJson();
			row.denomination = stack.denomination();
			row.amount = stack.amount();
			return row;
		}).toList();
		return GSON.toJson(json);
	}

	public static Optional<Payload> fromJson(String raw) {
		if (raw == null || raw.isBlank()) {
			return Optional.empty();
		}
		try {
			PayloadJson parsed = GSON.fromJson(raw, PayloadJson.class);
			if (parsed == null || parsed.id == null || parsed.id.isBlank()) {
				return Optional.empty();
			}
			List<Cash.Stack> stacks = new ArrayList<>();
			if (parsed.stacks != null) {
				for (StackJson row : parsed.stacks) {
					if (row == null || row.denomination <= 0 || row.amount <= 0) {
						continue;
					}
					stacks.add(new Cash.Stack(row.denomination, row.amount));
				}
			}
			return Optional.of(new Payload(parsed.id, stacks));
		}
		catch (JsonSyntaxException exception) {
			return Optional.empty();
		}
	}

	public static Optional<Payload> addValue(Payload payload, int amount, List<Integer> denoms, int capacity) {
		if (payload == null || amount <= 0 || capacity <= 0) {
			return Optional.empty();
		}
		if ((long) payload.total() + amount > capacity) {
			return Optional.empty();
		}
		Optional<List<Cash.Stack>> added = CashTender.greedy(amount, denoms);
		if (added.isEmpty()) {
			return Optional.empty();
		}
		List<Cash.Stack> merged = new ArrayList<>(payload.stacks());
		merged.addAll(added.get());
		return Optional.of(new Payload(payload.id(), merged));
	}

	public static Optional<Payload> takeValue(Payload payload, int amount, List<Integer> denoms) {
		if (payload == null || amount <= 0 || payload.total() < amount) {
			return Optional.empty();
		}
		return CashTender.extract(payload.stacks(), amount, denoms)
			.map(left -> new Payload(payload.id(), left));
	}

	public static boolean isPin(String raw) {
		return raw != null && raw.matches("\\d{4}");
	}
}
