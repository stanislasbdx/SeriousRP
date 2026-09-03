package fr.stan1712.wetston.seriousrp.commands.medics;

public final class EffectNameFormatter {
	private EffectNameFormatter() {
		throw new IllegalStateException("Utility class");
	}

	public static String formatNamespacedKey(String effectKey) {
		if (effectKey == null || !effectKey.contains(":")) {
			return "";
		}

		String[] keyParts = effectKey.split(":");
		if (keyParts.length < 2 || keyParts[1].isEmpty()) {
			return "";
		}

		String[] parts = keyParts[1].split("_");
		StringBuilder result = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			result
				.append(Character.toUpperCase(part.charAt(0)))
				.append(part.substring(1))
				.append(" ");
		}
		return result.toString().trim();
	}
}
