package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.CharsWrapper;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.Utils;

/**
 * @author TheElectronWill
 * @see <a href="https://toml.io/en/v1.0.0#string">TOML specification - Strings</a>
 */
final class StringParser {
	private static final char[] SINGLE_QUOTE = {'\''};
	private static final char[] SINGLE_QUOTE_OR_NEWLINE = {'\'', '\n', '\r'};

	/**
	 * Parses a basic string (surrounded by "). The opening quote must be parse before calling this
	 * method.
	 */
	static String parseBasic(CharacterInput input, TomlParser parser) {
		CharsWrapper.Builder builder = parser.createBuilder();
		boolean escape = false;
		char c;
		while ((c = input.readChar()) != '\"' || escape) {
			if (escape) {
				builder.write(unescape(c, input));
				escape = false;
			} else if (c == '\\') {
				escape = true;
			} else if (c == '\n' || c == '\r') {
				throw new ParsingException("Invalid newline in basic string, you should use a multiline string or escape the newline by writing \\n. The string begins with: \"" + builder + "\"");
			} else if (c != '\t' && Toml.isControlChar(c)) {
				String properEscape = "\\u" + Integer.toHexString((int)c).toUpperCase();
				throw new ParsingException("Invalid control character '" + c + "' in string, you should escape it by writing " + properEscape);
			} else {
				builder.write(c);
			}
		}
		return builder.toString();
	}

	/**
	 * Parses a literal string (surrounded by '). The opening quote must be read before calling
	 * this method.
	 */
	static String parseLiteral(CharacterInput input, TomlParser parser) {
		String str = input.readCharsUntil(SINGLE_QUOTE_OR_NEWLINE).toString();
		char end = input.readChar();// consume the closing quote
		// check for invalid charcters
		if (end != '\'') {
			throw new ParsingException("Invalid newline in literal string, you should use a multiline string. The string is '" + str + "'");
		}
		str.codePoints().forEach(codePoint -> {
			if (codePoint != '\t' && Toml.isControlChar(codePoint)) {
				String properEscape = "\\u" + Integer.toHexString(codePoint).toUpperCase();
				CharsWrapper display = new CharsWrapper(Character.toChars(codePoint));
				throw new ParsingException("Invalid control character '" + display + "' in literal string '" + str + "', you should escape it by writing " + properEscape);
			}
		});
		return str;
	}

	/**
	 * Parses a multiline basic string (surrounded by """). The 3 opening quotes must be read
	 * before calling this method.
	 */
	static String parseMultiBasic(CharacterInput input, TomlParser parser) {
		CharsWrapper.Builder builder = parser.createBuilder();
		char c;
		while ((c = input.readChar()) != '\"' || input.peek() != '\"' || input.peek(1) != '\"') {
			if (c == '\\') {
				final char next = input.readChar();
				if (next == '\n'
					|| (next == '\r' && input.peekChar() == '\n')
					|| (next == '\t' || next == ' ') && isWhitespace(Toml.readLine(input))) {
					// Goes to the next non-space char (skips newlines too)
					char nextNonSpace = Toml.readNonSpaceChar(input, true);
					input.pushBack(nextNonSpace);
					continue;
				} else if (next == '\t' || next == ' ') {
					throw new ParsingException("Invalid escapement: \\" + next);
				}
				builder.write(unescape(next, input));
			} else if (c != '\n' && c != '\r' && c != '\t' && Toml.isControlChar(c)) {
				String properEscape = "\\u" + Integer.toHexString((int)c).toUpperCase();
				throw new ParsingException("Invalid control character '" + c + "' in multiline string, you should escape it by writing " + properEscape);
			} else {
				builder.write(c);
			}
		}
		input.skipPeeks();// Don't include the closing quotes in the String

		// TOML allows quotes and double quotes anywhere inside of multiline basic string.
		// This means that, here, there can be 1 or 2 additional quotes!
		if (input.peek() == '\"') {
			input.skipPeeks();
			builder.write('\"');
		}
		if (input.peek() == '\"') {
			input.skipPeeks();
			builder.write('\"');
		}

		return buildMultilineString(builder);
	}

	/**
	 * Parses a multiline literal string (surrounded by '''). The 3 opening quotes must be parse
	 * before calling this method.
	 */
	static String parseMultiLiteral(CharacterInput input, TomlParser parser) {
		CharsWrapper.Builder builder = parser.createBuilder();
		char c;
		while ((c = input.readChar()) != '\'' || input.peek() != '\'' || input.peek(1) != '\'') {
			if (c != '\n' && c != '\r' && c != '\t' && Toml.isControlChar(c)) {
				String properEscape = "\\u" + Integer.toHexString((int)c).toUpperCase();
				throw new ParsingException("Invalid control character '" + c + "' in multiline literal string, you should escape it by writing " + properEscape);
			}
			builder.append(c);
		}
		input.skipPeeks();// Don't include the closing quotes in the String

		// Here, as in multiline basic strings, there can be 1 or 2 additional quotes, and it's valid.
		if (input.peek() == '\'') {
			input.skipPeeks();
			builder.write('\'');
		}
		if (input.peek() == '\'') {
			input.skipPeeks();
			builder.write('\'');
		}

		return buildMultilineString(builder);
	}

	/**
	 * Builds a multiline string with the content of a Builder. Trims the first line break if it's
	 * at the beginning of the string.
	 */
	private static String buildMultilineString(CharsWrapper.Builder builder) {
		if (builder.get(0) == '\n') {
			return builder.toString(1);
		}
		if (builder.get(0) == '\r' && builder.get(1) == '\n') {
			return builder.toString(2);
		}
		return builder.toString();
	}

	/**
	 * Parses an escape sequence.
	 *
	 * @param c the first character, ie the one just after the backslash.
	 */
	private static String unescape(char c, CharacterInput input) {
		switch (c) {
			case '"':
			case '\\':
				return String.valueOf(c);
			case 'b':
				return "\b";
			case 'f':
				return "\f";
			case 'n':
				return "\n";
			case 'r':
				return "\r";
			case 't':
				return "\t";
			case 'u': {
				CharsWrapper chars = input.readChars(4);
				return parseUnicodeCodepoint(chars);
			}
			case 'U': {
				CharsWrapper chars = input.readChars(8);
				return parseUnicodeCodepoint(chars);
			}
			default:
				throw new ParsingException("Invalid escapement: \\" + c);
		}
	}

	private static String parseUnicodeCodepoint(CharsWrapper chars) {
		try {
			int codePoint = Utils.parseInt(chars, 16);
			if (!Toml.isValidCodePoint(codePoint)) {
				throw new ParsingException("Invalid unicode codepoint: " + chars);
			}
			return new String(new int[] { codePoint }, 0, 1);
		} catch (IllegalArgumentException ex) {
			throw new ParsingException("Invalid unicode codepoint: " + chars, ex);
		}
	}

	/**
	 * Checks if a sequence contains only whitespace characters.
	 *
	 * @return true iff it contains only whitespace characters or it is empty, false otherwise
	 */
	private static boolean isWhitespace(CharSequence csq) {
		for (int i = 0; i < csq.length(); i++) {
			char c = csq.charAt(i);
			if (!(c == '\t' || c == ' ')) {
				return false;
			}
		}
		return true;
	}

	private StringParser() {}
}