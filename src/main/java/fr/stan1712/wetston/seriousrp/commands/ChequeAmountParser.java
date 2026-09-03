package fr.stan1712.wetston.seriousrp.commands;

public final class ChequeAmountParser {
	private ChequeAmountParser() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
		}
		catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean isFloat(String s) {
		try {
			Float.parseFloat(s);
		}
		catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean isPositiveAmount(String strValue) {
		if (strValue == null) {
			return false;
		}
		return (isInt(strValue) && Integer.parseInt(strValue) > 0)
			|| (isFloat(strValue) && Float.parseFloat(strValue) > 0);
	}
}
