package fr.stan1712.wetston.seriousrp.economy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Wallet {
	private static final Gson GSON = new Gson();

	public record Payload(String id, int slots, List<Cash.Stack> stacks) {
		public Payload {
			Objects.requireNonNull(id, "id");
			if (id.isBlank()) {
				throw new IllegalArgumentException("id");
			}
			slots = Cash.clampSlots(slots);
			stacks = stacks == null ? List.of() : CashTender.compact(stacks);
		}

		public int total() {
			return CashTender.total(stacks);
		}
	}

	public record Session(UUID playerId, String walletId, int hotbarSlot) {
		public Session {
			Objects.requireNonNull(playerId, "playerId");
			Objects.requireNonNull(walletId, "walletId");
			if (walletId.isBlank() || hotbarSlot < 0 || hotbarSlot > 8) {
				throw new IllegalArgumentException("invalid wallet session");
			}
		}
	}

	private static final class PayloadJson {
		private String id;
		private int slots;
		private List<StackJson> stacks;
	}

	private static final class StackJson {
		private int denomination;
		private int amount;
	}

	private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
	private final Map<String, UUID> lockedWallets = new ConcurrentHashMap<>();

	public static Payload create(int slots) {
		return new Payload(UUID.randomUUID().toString(), slots, List.of());
	}

	public static String toJson(Payload payload) {
		PayloadJson json = new PayloadJson();
		json.id = payload.id();
		json.slots = payload.slots();
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
			return Optional.of(new Payload(parsed.id, parsed.slots, stacks));
		}
		catch (JsonSyntaxException exception) {
			return Optional.empty();
		}
	}

	public static void write(PersistentDataContainer container, NamespacedKey key, Payload payload) {
		container.set(key, PersistentDataType.STRING, toJson(payload));
	}

	public static Optional<Payload> read(PersistentDataContainer container, NamespacedKey key) {
		return fromJson(container.get(key, PersistentDataType.STRING));
	}

	public static List<String> lore(Payload payload, Cash cash) {
		return List.of(cash.formatTotal(payload.total()));
	}

	public static Optional<Payload> deposit(Payload payload, Cash.Stack incoming, Cash cash) {
		if (!cash.isDenomination(incoming.denomination())) {
			return Optional.empty();
		}
		List<Cash.Stack> next = new ArrayList<>(payload.stacks());
		next.add(incoming);
		if (CashTender.toInventoryStacks(next).size() > payload.slots()) {
			return Optional.empty();
		}
		return Optional.of(new Payload(payload.id(), payload.slots(), next));
	}

	public boolean tryOpen(UUID playerId, String walletId, int hotbarSlot) {
		Session session = new Session(playerId, walletId, hotbarSlot);
		if (sessions.containsKey(playerId) || lockedWallets.containsKey(walletId)) {
			return false;
		}
		sessions.put(playerId, session);
		lockedWallets.put(walletId, playerId);
		return true;
	}

	public Optional<Session> close(UUID playerId) {
		Session session = sessions.remove(playerId);
		if (session != null) {
			lockedWallets.remove(session.walletId(), playerId);
		}
		return Optional.ofNullable(session);
	}

	public Optional<Session> sessionOf(UUID playerId) {
		return Optional.ofNullable(sessions.get(playerId));
	}

	public boolean isHeld(UUID playerId, String walletId, int hotbarSlot) {
		Session session = sessions.get(playerId);
		return session != null
			&& session.walletId().equals(walletId)
			&& session.hotbarSlot() == hotbarSlot;
	}

	public boolean isLocked(String walletId) {
		return lockedWallets.containsKey(walletId);
	}
}
