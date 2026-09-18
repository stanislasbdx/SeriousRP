package fr.stan1712.wetston.seriousrp.economy;

import java.util.OptionalInt;
import java.util.UUID;

public final class Atm {
	public static final String CREATE_PERM = "seriousrp.economy.atm.create";
	public static final String DELETE_PERM = "seriousrp.economy.atm.delete";
	public static final String DELETE_OTHERS_PERM = "seriousrp.economy.atm.delete.others";
	public static final String USE_PERM = "seriousrp.economy.atm.use";
	public static final long PROMPT_TIMEOUT_MILLIS = 20_000L;

	public enum Operation {
		DEPOSIT,
		WITHDRAW
	}

	public record Prompt(UUID playerId, Operation operation, long expiresAtMillis) {
		public Prompt {
			if (playerId == null || operation == null) {
				throw new IllegalArgumentException("prompt");
			}
		}

		public boolean isExpired(long nowMillis) {
			return nowMillis >= expiresAtMillis;
		}
	}

	private Atm() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isCreateAttempt(String firstLine, Cash cash) {
		return cash.isAtmHeader(firstLine);
	}

	public static boolean canBreak(UUID ownerId, UUID breakerId, boolean deleteOwn, boolean deleteOthers) {
		if (ownerId == null || breakerId == null) {
			return false;
		}
		if (ownerId.equals(breakerId)) {
			return deleteOwn || deleteOthers;
		}
		return deleteOthers;
	}

	public static String[] genericLines(Cash cash) {
		return new String[] {
			Cash.colorize("&2&l[sATM]"),
			Cash.colorize("&8&m              "),
			Cash.colorize("&aBanque &8· &ecash"),
			Cash.colorize("&8» &7Clic droit")
		};
	}

	public static String[] nearbyLines(Cash cash, int account, int pocket) {
		return new String[] {
			Cash.colorize("&2&l[sATM]"),
			trimLine(Cash.colorize("&aCompte &f" + account + cash.currency())),
			trimLine(Cash.colorize("&eCash &f" + pocket + cash.currency())),
			Cash.colorize("&8» &7Clic droit")
		};
	}

	public static OptionalInt resolveAmount(Operation operation, Integer requested, boolean all, int pocket, int account) {
		if (operation == null) {
			return OptionalInt.empty();
		}
		int available = operation == Operation.DEPOSIT ? pocket : account;
		if (all) {
			return available > 0 ? OptionalInt.of(available) : OptionalInt.empty();
		}
		if (requested == null || requested <= 0 || requested > available) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(requested);
	}

	public static OptionalInt parsePromptAmount(String message) {
		if (message == null) {
			return OptionalInt.empty();
		}
		String trimmed = message.trim();
		if (trimmed.equalsIgnoreCase("cancel")) {
			return OptionalInt.empty();
		}
		return Cash.parsePositiveInt(trimmed);
	}

	public static boolean isCancelPrompt(String message) {
		return message != null && message.trim().equalsIgnoreCase("cancel");
	}

	static String trimLine(String line) {
		if (line.length() <= 40) {
			return line;
		}
		return line.substring(0, 40);
	}
}
