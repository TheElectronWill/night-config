package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.serialization.CharacterInput;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * @author TheElectronWill
 */
public final class Toml {

	static final int BUILDER_INITIAL_CAPACITY = 16;//TODO make it configurable (later)
	static final char[] WHITESPACE_OR_NEWLINE = {'\t', ' ', '\n', '\r'};
	static final char[] WHITESPACE = {'\t', ' '};
	static final char[] NEWLINE = {'\n'};

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

	private Toml() {}

}
