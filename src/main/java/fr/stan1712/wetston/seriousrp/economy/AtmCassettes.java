package fr.stan1712.wetston.seriousrp.economy;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class AtmCassettes {
	private static final Gson GSON = new Gson();

	private AtmCassettes() {
		throw new IllegalStateException("Utility class");
	}

	public static String toJson(List<Optional<Cassette.Payload>> slots) {
		JsonArray array = new JsonArray();
		for (Optional<Cassette.Payload> slot : slots) {
			if (slot.isEmpty()) {
				array.add(JsonNull.INSTANCE);
			}
			else {
				array.add(Cassette.toJson(slot.get()));
			}
		}
		return GSON.toJson(array);
	}

	public static List<Optional<Cassette.Payload>> fromJson(String raw, int slots) {
		List<Optional<Cassette.Payload>> list = emptySlots(slots);
		if (raw == null || raw.isBlank()) {
			return list;
		}
		try {
			JsonArray array = JsonParser.parseString(raw).getAsJsonArray();
			for (int index = 0; index < list.size() && index < array.size(); index++) {
				JsonElement element = array.get(index);
				if (element == null || element.isJsonNull()) {
					continue;
				}
				String encoded = element.isJsonPrimitive() ? element.getAsString() : element.toString();
				list.set(index, Cassette.fromJson(encoded));
			}
			return List.copyOf(list);
		}
		catch (JsonSyntaxException | IllegalStateException | ClassCastException exception) {
			return emptySlots(slots);
		}
	}

	public static List<Optional<Cassette.Payload>> emptySlots(int slots) {
		int size = Math.max(1, slots);
		List<Optional<Cassette.Payload>> list = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			list.add(Optional.empty());
		}
		return list;
	}

	public static int total(List<Optional<Cassette.Payload>> slots) {
		int sum = 0;
		for (Optional<Cassette.Payload> slot : slots) {
			if (slot.isPresent()) {
				sum = CashTender.saturatingAdd(sum, slot.get().total());
			}
		}
		return sum;
	}

	public static int freeCapacity(List<Optional<Cassette.Payload>> slots, int perCassetteCapacity) {
		int free = 0;
		for (Optional<Cassette.Payload> slot : slots) {
			if (slot.isPresent()) {
				free += Math.max(0, perCassetteCapacity - slot.get().total());
			}
		}
		return free;
	}

	public static boolean hasCassette(List<Optional<Cassette.Payload>> slots) {
		for (Optional<Cassette.Payload> slot : slots) {
			if (slot.isPresent()) {
				return true;
			}
		}
		return false;
	}

	public static Optional<List<Optional<Cassette.Payload>>> deposit(
		List<Optional<Cassette.Payload>> slots,
		int amount,
		List<Integer> denoms,
		int perCassetteCapacity
	) {
		if (amount <= 0 || !hasCassette(slots) || freeCapacity(slots, perCassetteCapacity) < amount) {
			return Optional.empty();
		}
		List<Optional<Cassette.Payload>> next = copy(slots);
		int remaining = amount;
		for (int index = 0; index < next.size() && remaining > 0; index++) {
			Optional<Cassette.Payload> slot = next.get(index);
			if (slot.isEmpty()) {
				continue;
			}
			int room = perCassetteCapacity - slot.get().total();
			if (room <= 0) {
				continue;
			}
			int put = Math.min(room, remaining);
			Optional<Cassette.Payload> updated = Cassette.addValue(slot.get(), put, denoms, perCassetteCapacity);
			if (updated.isEmpty()) {
				return Optional.empty();
			}
			next.set(index, updated);
			remaining -= put;
		}
		return remaining == 0 ? Optional.of(List.copyOf(next)) : Optional.empty();
	}

	public static Optional<List<Optional<Cassette.Payload>>> withdraw(
		List<Optional<Cassette.Payload>> slots,
		int amount,
		List<Integer> denoms
	) {
		if (amount <= 0 || total(slots) < amount) {
			return Optional.empty();
		}
		List<Optional<Cassette.Payload>> next = copy(slots);
		int remaining = amount;
		for (int index = 0; index < next.size() && remaining > 0; index++) {
			Optional<Cassette.Payload> slot = next.get(index);
			if (slot.isEmpty() || slot.get().total() <= 0) {
				continue;
			}
			int take = Math.min(slot.get().total(), remaining);
			Optional<Cassette.Payload> updated = Cassette.takeValue(slot.get(), take, denoms);
			if (updated.isEmpty()) {
				return Optional.empty();
			}
			next.set(index, updated);
			remaining -= take;
		}
		return remaining == 0 ? Optional.of(List.copyOf(next)) : Optional.empty();
	}

	public static Optional<List<Optional<Cassette.Payload>>> install(
		List<Optional<Cassette.Payload>> slots,
		int index,
		Cassette.Payload cassette
	) {
		if (slots == null || cassette == null || index < 0 || index >= slots.size() || slots.get(index).isPresent()) {
			return Optional.empty();
		}
		List<Optional<Cassette.Payload>> next = copy(slots);
		next.set(index, Optional.of(cassette));
		return Optional.of(List.copyOf(next));
	}

	public static Optional<Eject> eject(List<Optional<Cassette.Payload>> slots, int index) {
		if (slots == null || index < 0 || index >= slots.size() || slots.get(index).isEmpty()) {
			return Optional.empty();
		}
		List<Optional<Cassette.Payload>> next = copy(slots);
		Cassette.Payload removed = next.get(index).orElseThrow();
		next.set(index, Optional.empty());
		return Optional.of(new Eject(List.copyOf(next), removed));
	}

	public record Eject(List<Optional<Cassette.Payload>> slots, Cassette.Payload cassette) {
	}

	private static List<Optional<Cassette.Payload>> copy(List<Optional<Cassette.Payload>> slots) {
		return new ArrayList<>(slots == null ? Collections.emptyList() : slots);
	}
}
