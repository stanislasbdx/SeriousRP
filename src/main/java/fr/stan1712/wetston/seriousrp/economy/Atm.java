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
		WITHDRAW,
		COMPACT,
		BREAK
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
			Cash.colorize("&b&l[sATM]"),
			Cash.colorize("&8&m              "),
			Cash.colorize("&7Banque &8· &6cash"),
			Cash.colorize("&8» &fClic droit")
		};
	}

	public static String[] nearbyLines(Cash cash, int account, int pocket) {
		String currency = Cash.stripColorCodes(cash.currency());
		return new String[] {
			Cash.colorize("&b&l[sATM]"),
			trimLine(Cash.colorize("&7Cpt &a") + formatSignAmount(account) + currency),
			trimLine(Cash.colorize("&7Cash &6") + formatSignAmount(pocket) + currency),
			Cash.colorize("&8» &fClic droit")
		};
	}

	static String formatSignAmount(int amount) {
		long value = Math.max(0, amount);
		if (value < 10_000L) {
			return Long.toString(value);
		}
		if (value < 1_000_000L) {
			return formatCompact(value, 1_000L, "k");
		}
		if (value < 1_000_000_000L) {
			return formatCompact(value, 1_000_000L, "M");
		}
		return formatCompact(value, 1_000_000_000L, "B");
	}

	private static String formatCompact(long value, long unit, String suffix) {
		long whole = value / unit;
		long tenth = (value % unit) / (unit / 10L);
		if (tenth == 0L || whole >= 100L) {
			return whole + suffix;
		}
		return whole + "." + tenth + suffix;
	}

	public static OptionalInt resolveAmount(Operation operation, Integer requested, boolean all, int pocket, int account) {
		if (operation == null || (operation != Operation.DEPOSIT && operation != Operation.WITHDRAW)) {
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
		if (line == null) {
			return "";
		}
		if (Cash.stripColorCodes(line).length() <= 15) {
			return line;
		}
		StringBuilder out = new StringBuilder();
		int visible = 0;
		for (int index = 0; index < line.length(); index++) {
			char current = line.charAt(index);
			if (current == '§' && index + 1 < line.length()) {
				out.append(current).append(line.charAt(index + 1));
				index++;
				continue;
			}
			if (visible >= 15) {
				break;
			}
			out.append(current);
			visible++;
		}
		return out.toString();
	}
}
