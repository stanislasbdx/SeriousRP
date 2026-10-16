package fr.stan1712.wetston.seriousrp.economy;

import java.util.OptionalInt;

public final class BankVault {
	private BankVault() {
		throw new IllegalStateException("Utility class");
	}

	public static OptionalInt feed(int stock, int amount, int capacity) {
		if (amount <= 0 || stock < 0 || capacity <= 0) {
			return OptionalInt.empty();
		}
		long next = (long) stock + amount;
		if (next > capacity) {
			return OptionalInt.empty();
		}
		return OptionalInt.of((int) next);
	}

	public static OptionalInt take(int stock, int amount) {
		if (amount <= 0 || stock < amount) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(stock - amount);
	}

	public static boolean matchesPin(String expected, String provided) {
		return expected != null && expected.equals(provided);
	}

	public static boolean needsPinSetup(String storedPin) {
		return storedPin == null || storedPin.isBlank();
	}
}
