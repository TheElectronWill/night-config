package com.electronwill.nightconfig.core.serialization;

/**
 * @author TheElectronWill
 */
public final class Utils {
	private static final char[] EMPTY_CHAR_ARRAY = new char[0];

	private Utils() {
	}

	public static boolean arrayContains(char[] array, char element) {
		for (char c : array) {
			if (c == element)
				return true;
		}
		return false;
	}

	public static int arrayIndexOf(char[] array, char element) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == element)
				return i;
		}
		return -1;
	}

	public static long parseLong(CharsWrapper chars, int base) {
		return parseLong(chars.getChars(), base);
	}

	public static long parseLong(char[] chars, int base) {
		//Optimized lightweight parsing
		boolean negative = (chars[0] == '-');
		long value = 0, coefficient = 1;
		for (int i = 0; i < chars.length; i++) {
			int digitValue = Character.digit(chars[i], base);
			value += digitValue * coefficient;
			coefficient *= base;
		}
		return negative ? -value : value;
	}

	public static int parseInt(CharsWrapper chars, int base) {
		return parseInt(chars.getChars(), base);
	}

	public static int parseInt(char[] chars, int base) {
		//Optimized lightweight parsing
		boolean negative = (chars[0] == '-');
		int value = 0, coefficient = 1;
		for (int i = 0; i < chars.length; i++) {
			int digitValue = Character.digit(chars[i], base);
			value += digitValue * coefficient;
			coefficient *= base;
		}
		return negative ? -value : value;
	}

	public static double parseDouble(CharsWrapper chars) {
		return Double.parseDouble(chars.toString());
	}

	public static char[] noChars() {
		return EMPTY_CHAR_ARRAY;
	}
}
