package fr.stan1712.wetston.seriousrp.economy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Cassette {
	public static final int MAX_PIN_ATTEMPTS = 3;
	private static final Gson GSON = new Gson();

	public record Payload(String id, List<Cash.Stack> stacks, String pin, int failedAttempts) {
		public Payload {
			if (id == null || id.isBlank()) {
				throw new IllegalArgumentException("id");
			}
			stacks = stacks == null ? List.of() : CashTender.compact(stacks);
			if (!isPin(pin)) {
				pin = null;
			}
			failedAttempts = Math.max(0, failedAttempts);
		}

		public Payload(String id, List<Cash.Stack> stacks) {
			this(id, stacks, null, 0);
		}

		public int total() {
			return CashTender.total(stacks);
		}
	}

	public record PinAttempt(Payload payload, boolean unlocked, boolean wiped) {
	}

	public record Fit(Payload kept, List<Cash.Stack> overflow) {
	}

	public static String shortId(String id) {
		if (id == null || id.isBlank()) {
			return "";
		}
		String compact = id.replace("-", "").toLowerCase();
		return compact.length() <= 5 ? compact : compact.substring(0, 5);
	}

	public static Fit fit(Payload payload, int capacity, List<Integer> denoms) {
		if (payload == null || capacity < 0 || payload.total() <= capacity) {
			return new Fit(payload, List.of());
		}
		int excess = payload.total() - capacity;
		List<Cash.Stack> kept = CashTender.extract(payload.stacks(), excess, denoms).orElse(payload.stacks());
		List<Cash.Stack> overflow = CashTender.greedy(excess, denoms).orElse(List.of());
		return new Fit(copy(payload, kept), overflow);
	}

	private static final class PayloadJson {
		private String id;
		private List<StackJson> stacks;
		private String pin;
		private int failedAttempts;
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
		json.pin = payload.pin();
		json.failedAttempts = payload.failedAttempts();
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
			String pin = isPin(parsed.pin) ? parsed.pin : null;
			return Optional.of(new Payload(parsed.id, stacks, pin, parsed.failedAttempts));
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
		return Optional.of(copy(payload, merged));
	}

	public static Optional<Payload> takeValue(Payload payload, int amount, List<Integer> denoms) {
		if (payload == null || amount <= 0 || payload.total() < amount) {
			return Optional.empty();
		}
		return CashTender.extract(payload.stacks(), amount, denoms)
			.map(left -> copy(payload, left));
	}

	public static Payload withPin(Payload payload, String pin) {
		if (payload == null || !isPin(pin)) {
			throw new IllegalArgumentException("pin");
		}
		return new Payload(payload.id(), payload.stacks(), pin, 0);
	}

	public static PinAttempt tryPin(Payload payload, String pin) {
		if (payload == null || payload.pin() == null || !isPin(pin)) {
			return new PinAttempt(payload, false, false);
		}
		if (payload.pin().equals(pin)) {
			return new PinAttempt(new Payload(payload.id(), payload.stacks(), payload.pin(), 0), true, false);
		}
		int next = payload.failedAttempts() + 1;
		if (next >= MAX_PIN_ATTEMPTS) {
			return new PinAttempt(new Payload(payload.id(), List.of(), payload.pin(), 0), false, true);
		}
		return new PinAttempt(new Payload(payload.id(), payload.stacks(), payload.pin(), next), false, false);
	}

	private static Payload copy(Payload payload, List<Cash.Stack> stacks) {
		return new Payload(payload.id(), stacks, payload.pin(), payload.failedAttempts());
	}

	public static boolean isPin(String raw) {
		return raw != null && raw.matches("\\d{4}");
	}
}
