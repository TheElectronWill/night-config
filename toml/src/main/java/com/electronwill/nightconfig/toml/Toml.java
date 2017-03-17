package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.Utils;

/**
 * @author TheElectronWill
 */
final class Toml {

	static final char[] WHITESPACE_OR_NEWLINE = {'\t', ' ', '\n', '\r'};
	static final char[] WHITESPACE = {'\t', ' '};
	static final char[] NEWLINE = {'\n'};
	static final char[] FORBIDDEN_IN_ALL_BARE_KEYS = {'.', '[', ']', '#', '='};

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

	static char readNonSpaceChar(CharacterInput input) {
		return input.readCharAndSkip(WHITESPACE);
	}

	static void skipComment(CharacterInput input) {
		input.readCharsUntil(NEWLINE);
	}

	static int readUseful(CharacterInput input) {
		int next = input.readAndSkip(WHITESPACE_OR_NEWLINE);
		while (next == '#') {
			input.readUntil(NEWLINE);
			next = input.readAndSkip(WHITESPACE_OR_NEWLINE);
		}
		return next;
	}

	static int readNonSpace(CharacterInput input) {
		return input.readAndSkip(WHITESPACE);
	}

	static boolean isValidInBareKey(char c, boolean lenient) {
		if (lenient) return c > ' ' && !Utils.arrayContains(FORBIDDEN_IN_ALL_BARE_KEYS, c);
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == '_';
	}

	static boolean isValidBareKey(CharSequence csq, boolean lenient) {
		for (int i = 0; i < csq.length(); i++) {
			if (!isValidInBareKey(csq.charAt(i), lenient))
				return false;
		}
		return true;
	}

	private Toml() {}
}
