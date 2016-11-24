package com.electronwill.nightconfig.core;

import java.util.ArrayList;
import java.util.List;

/**
 * String utilities.
 *
 * @author TheElectronWill
 */
public final class StringUtils {

	private StringUtils() {}

	/**
	 * Splits a String around occurences of a character. The result is similar to
	 * {@link String#split(String)}.
	 */
	public static List<String> split(String str, char sep) {
		List<String> list = new ArrayList<>(4);
		split(str, sep, list);
		return list;
	}

	/**
	 * Splits a String around occurences of a character, and put the result in a List. The result is similar
	 * to {@link String#split(String)}.
	 */
	public static void split(String str, char sep, List<String> list) {
		int pos0 = 0;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == sep) {
				list.add(str.substring(pos0, i));
				pos0 = i + 1;
			}
		}
		if (pos0 < str.length()) {
			list.add(str.substring(pos0, str.length()));
		}
	}
}
