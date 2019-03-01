package com.electronwill.nightconfig.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fast string utilities.
 *
 * @author TheElectronWill
 */
public final class StringUtils {

	private StringUtils() {}// Utility class that can't be constructed

	/**
	 * Splits a String around each occurence of the specified character. The result is <b>not</b>
	 * the same as {@link String#split(String)}. In particular, this method never returns an
	 * empty list.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>{@code split("a.b.c", '.')} gives {@code ["a", "b", "c"]}
	 * <li>{@code split("", '.')} gives {@code [""]} (a list containing the empty string)
	 * <li>{@code split(".", '.')} gives {@code ["", ""]} (a list containing two empty strings)
	 * <li>{@code split("..", '.')} gives {@code ["", "", ""]} (a list containing three empty
	 * strings)
	 * <li>{@code split(".a...b.", '.')} gives {@code ["", "a", "", "", "b", ""]} (a list containing
	 * an empty string, the string "a", two empty strings, the string "b", and an empty string)
	 * </ul>
	 *
	 * @param str the String to split
	 * @param sep the separator to use
	 * @return a non-empty list of strings
	 */
	public static List<String> split(String str, char sep) {
		List<String> list = new ArrayList<>(8);// usually the paths are small
		split(str, sep, list);
		return list;
	}

	/**
	 * Splits a String around each occurence of the specified character, and puts the result in the
	 * given List. The result is <b>not</b> the same as {@link String#split(String)}. In
	 * particular, this method always add at least one element to the list.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>{@code split("a.b.c", '.')} gives {@code ["a", "b", "c"]}
	 * <li>{@code split("", '.')} gives {@code [""]} (a list containing the empty string)
	 * <li>{@code split(".", '.')} gives {@code ["", ""]} (a list containing two empty strings)
	 * <li>{@code split("..", '.')} gives {@code ["", "", ""]} (a list containing three empty
	 * strings)
	 * <li>{@code split(".a...b.", '.')} gives {@code ["", "a", "", "", "b", ""]} (a list containing
	 * an empty string, the string "a", two empty strings, the string "b", and an empty string)
	 * </ul>
	 *
	 * @param str  the String to split
	 * @param sep  the separator to use
	 * @param list the list where to put the results
	 */
	public static void split(String str, char sep, List<String> list) {
		int pos0 = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == sep) {// separator found
				list.add(str.substring(pos0, i));
				pos0 = i + 1;
			}
		}
		list.add(str.substring(pos0));// adds the last part
	}

	/**
	 * Splits a String around each occurence of LF and CRLF. If the String is null or empty, then
	 * an empty list is returned.
	 *
	 * @param str the String to split
	 * @return a list of strings, may be empty
	 */
	public static List<String> splitLines(String str) {
		if (str == null || str.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> list = new ArrayList<>(8);
		splitLines(str, list);
		return list;
	}

	/**
	 * Splits a String around each occurence of LF and CRLF, and puts the result in the given list.
	 *
	 * @param str  the String to split
	 * @param list the list where to put the result
	 */
	public static void splitLines(String str, List<String> list) {
		int pos0 = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ('\r')) {// CRLF
				list.add(str.substring(pos0, i));
				pos0 = i + 2;// Skips LF
			}
			if (str.charAt(i) == '\n') {// LF
				list.add(str.substring(pos0, i));
				pos0 = i + 1;
			}
		}
		list.add(str.substring(pos0));// adds the last part
	}
}