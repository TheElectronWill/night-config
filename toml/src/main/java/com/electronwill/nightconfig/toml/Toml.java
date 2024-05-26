package com.electronwill.nightconfig.toml;

import java.util.List;

import com.electronwill.nightconfig.core.io.*;

/**
 * @author TheElectronWill
 */
final class Toml {

	private static final char[] WHITESPACE_OR_NEWLINE = {'\t', ' ', '\n', '\r'};
	private static final char[] WHITESPACE = {'\t', ' '};
	private static final char[] NEWLINE = {'\n'};
	private static final char[] FORBIDDEN_IN_ALL_BARE_KEYS = {'.', '[', ']', '#', '='};

	/**
	 * Returns the next "useful" character. Skips comments, spaces and newlines.
	 */
	static char readUsefulChar(CharacterInput input) {
		char next = input.readCharAndSkip(WHITESPACE_OR_NEWLINE);
		while (next == '#') {
			input.readCharsUntil(NEWLINE);
			next = input.readCharAndSkip(WHITESPACE_OR_NEWLINE);
		}
		return next;
	}

	/**
	 * Returns the next "useful" character. Skips comments, spaces and newlines.
	 */
	static int readUseful(CharacterInput input, List<CharsWrapper> commentsList) {
		int next = input.readAndSkip(WHITESPACE_OR_NEWLINE);
		while (next == '#') {
			CharsWrapper comment = readLine(input);
			commentsList.add(comment);
			next = input.readAndSkip(WHITESPACE_OR_NEWLINE);
		}
		return next;
	}

	/**
	 * Reads the next non-space character. Doesn't skip comments.
	 */
	static char readNonSpaceChar(CharacterInput input, boolean skipNewlines) {
		return skipNewlines ? input.readCharAndSkip(WHITESPACE_OR_NEWLINE)
							: input.readCharAndSkip(WHITESPACE);
	}

	/**
	 * Reads the next non-space character. Doesn't skip comments.
	 */
	static int readNonSpace(CharacterInput input, boolean skipNewlines) {
		return skipNewlines ? input.readAndSkip(WHITESPACE_OR_NEWLINE)
							: input.readAndSkip(WHITESPACE);
	}

	/**
	 * Reads all the characters before the next newline or the end of the data.
	 */
	static CharsWrapper readLine(CharacterInput input) {
		CharsWrapper chars = input.readUntil(NEWLINE);
		int lastIndex = chars.length() - 1;
		if (lastIndex >= 0 && chars.get(lastIndex) == '\r') {
			return chars.subView(0, lastIndex);
		}
		return chars;
	}

	static boolean isControlChar(char c) {
		return (c <= 0x1F || c == 0x7F) && !Character.isSurrogate(c);
	}

	static boolean isControlChar(int c) {
		return (c <= 0x1F || c == 0x7F);
	}

	static boolean isValidCodePoint(int c) {
		return (c <= 0xD7FF || (c >= 0xE000 && c <= 0x10FFFF));
	}

	static boolean isValidInBareKey(char c, boolean lenient) {
		if (lenient) { return c > ' ' && !Utils.arrayContains(FORBIDDEN_IN_ALL_BARE_KEYS, c) && !isControlChar(c); }
		return (c >= 'a' && c <= 'z')
			   || (c >= 'A' && c <= 'Z')
			   || (c >= '0' && c <= '9')
			   || c == '-'
			   || c == '_';
	}

	static boolean isValidBareKey(CharSequence csq, boolean lenient) {
		int len = csq.length();
		if (len == 0) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if (!isValidInBareKey(csq.charAt(i), lenient)) { return false; }
		}
		return true;
	}

	static boolean isKeyValueSeparator(char c, boolean lenient) {
		return c == '=' || (lenient && c == ':');
	}

	private Toml() {}
}