package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.impl.CharacterInput;
import com.electronwill.nightconfig.core.impl.CharsWrapper;
import com.electronwill.nightconfig.core.impl.Utils;
import java.util.List;

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
		char next = input.readCharSkipping(WHITESPACE_OR_NEWLINE);
		while (next == '#') {
			input.readCharsUntil(NEWLINE);
			next = input.readCharSkipping(WHITESPACE_OR_NEWLINE);
		}
		return next;
	}

	/**
	 * Returns the next "useful" character. Skips comments, spaces and newlines.
	 */
	static int readUseful(CharacterInput input, List<CharsWrapper> commentsList) {
		int next = input.readSkipping(WHITESPACE_OR_NEWLINE);
		while (next == '#') {
			CharsWrapper comment = readLine(input);
			commentsList.add(comment);
			next = input.readSkipping(WHITESPACE_OR_NEWLINE);
		}
		return next;
	}

	/**
	 * Reads the next non-space character. Doesn't skip comments.
	 */
	static char readNonSpaceChar(CharacterInput input, boolean skipNewlines) {
		return skipNewlines ? input.readCharSkipping(WHITESPACE_OR_NEWLINE)
							: input.readCharSkipping(WHITESPACE);
	}

	/**
	 * Reads the next non-space character. Doesn't skip comments.
	 */
	static int readNonSpace(CharacterInput input, boolean skipNewlines) {
		return skipNewlines ? input.readSkipping(WHITESPACE_OR_NEWLINE)
							: input.readSkipping(WHITESPACE);
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

	static boolean isValidInBareKey(char c, boolean lenient) {
		if (lenient) { return c > ' ' && !Utils.arrayContains(FORBIDDEN_IN_ALL_BARE_KEYS, c); }
		return (c >= 'a' && c <= 'z')
			   || (c >= 'A' && c <= 'Z')
			   || (c >= '0' && c <= '9')
			   || c == '-'
			   || c == '_';
	}

	static boolean isValidBareKey(CharSequence csq, boolean lenient) {
		for (int i = 0; i < csq.length(); i++) {
			if (!isValidInBareKey(csq.charAt(i), lenient)) { return false; }
		}
		return true;
	}

	static boolean isKeyValueSeparator(char c, boolean lenient) {
		return c == '=' || (lenient && c == ':');
	}

	private Toml() {}
}