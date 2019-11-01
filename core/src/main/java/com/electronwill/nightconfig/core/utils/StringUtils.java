package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.impl.Charray;

import java.util.*;

/**
 * Fast string utilities.
 *
 * @author TheElectronWill
 */
public final class StringUtils {

	private StringUtils() {}// Utility class that can't be constructed

	/** @return an array containing the last element of the given array. */
	public static String[] last(String[] path) {
		return new String[]{path[path.length - 1]};
	}

	/** @return an array containing only one string */
	public static String[] single(String path) {
		return new String[]{path};
	}

	/**
	 * Splits a String at each dot. Shortcut for {@code split(path, '.')}
	 * @param path the string to split
	 * @return a list containing the splitted parts
	 */
	public static String[] splitPath(String path) {
		return split(path, '.');
	}

	/**
	 * Splits a String around each occurence of the specified character. The result is <b>not</b>
	 * the same as {@link String#split(String)}. In particular, this method never returns an
	 * empty list.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>{@code split("a.b.c", '.')} gives {@code ["a", "b", "c"]}
	 * <li>{@code split("", '.')} gives {@code [""]} (a list containing 1 empty string)
	 * <li>{@code split(".", '.')} gives {@code ["", ""]} (a list containing 2 empty strings)
	 * <li>{@code split("..", '.')} gives {@code ["", "", ""]} (a list containing 3 empty strings)
	 * <li>{@code split(".a...b.", '.')} gives {@code ["", "a", "", "", "b", ""]}
	 * </ul>
	 *
	 * @param str the String to split
	 * @param sep the separator to use
	 * @return a non-empty list of strings
	 */
	public static String[] split(String str, char sep) {
		String[] parts = new String[8];
		int partIdx = 0;
		int partBegin = 0;
		int partEnd;
		while ((partEnd = str.indexOf(sep, partBegin)) != -1) {
			String part = str.substring(partBegin, partEnd);
			if (partIdx == parts.length) {
				parts = Arrays.copyOf(parts, parts.length << 1);
			}
			parts[partIdx++] = part;
			partBegin = partEnd + 1;
		}
		String[] result = Arrays.copyOf(parts, partIdx+1); // +1 for the last part
		result[result.length-1] = str.substring(partBegin); // add the last part
		return result;
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
	 * @param dst the list where to put the results
	 */
	public static void split(String str, char sep, List<String> dst) {
		int pos0 = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == sep) {// separator found
				dst.add(str.substring(pos0, i));
				pos0 = i + 1;
			}
		}
		dst.add(str.substring(pos0));// adds the last part
	}

	/**
	 * Joins an array of strings with dots.
	 * This is the opposite of {@link #splitPath(String)}.
	 *
	 * @return the result of the concatenation of each element with a '.' between 2 elements
	 */
	public static String joinPath(String[] strings) {
		return join(strings, '.');
	}

	/**
	 * Joins the first elements of an array of strings, with the delimiter '.' (dot).
	 * This is the opposite of {@link #splitPath(String)}.
	 *
	 * @param strings the strings to join
	 * @param len the number of strings to join, starting at strings[0]
	 * @return the result of the concatenation of each element with a '.' between 2 elements
	 */
	public static String joinPath(String[] strings, int len) {
		return join(strings, len, '.');
	}

	/**
	 * Like {@link java.lang.String#join(java.lang.CharSequence, java.lang.CharSequence...)} but
	 * optimized for single-char delimiters.
	 */
	public static String join(String[] strings, char delimiter) {
		return join(strings, strings.length, delimiter);
	}

	/**
	 * Join the first elements of an array of strings with the given delimiter.
	 */
	public static String join(String[] strings, int len, char delimiter) {
		Charray builder = new Charray(32);
		join(strings, len, delimiter, builder);
		return builder.toString();
	}

	public static void join(String[] strings, int len, char delimiter, Charray dst) {
		if (len <= 0) return;
		for (int i = 0; i < len; i++) {
			dst.append(strings[i]).append(delimiter);
		}
		dst.append(strings[len-1]);
	}

	/**
	 * Iterates over the lines of a String.
	 * The lines may be separated by '\n' (LF) or "\r\n" (CRLF).
	 *
	 * @param str the string to iterate over
	 * @return an iterator over the lines of the string
	 */
	public static Iterator<String> iterateLines(String str) {
		return new Iterator<String>() {
			private int lineStart = 0;
			private int lineEnd = 0;
			private int sepSize = 0;

			{ findNextEnd(); }

			private void findNextEnd() {
				lineEnd = str.indexOf('\n', lineStart);
				if (lineEnd >= 1 && str.charAt(lineEnd - 1) == '\r') {
					// Handle CRLF
					lineEnd -= 1;
					sepSize = 2;
				} else {
					// Handle LF
					sepSize = 1;
				}
			}

			@Override
			public boolean hasNext() {
				return lineEnd != -1;
			}

			@Override
			public String next() {
				String line = str.substring(lineStart, lineEnd);
				lineStart = lineEnd + sepSize;
				findNextEnd();
				return line;
			}
		};
	}
}
