package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.CharsWrapper;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.Utils;

/**
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml#user-content-string">TOML specification - Strings</a>
 */
final class StringParser {
	private static final char[] SINGLE_QUOTE = {'\''};

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
				builder.write(escape(c, input));
				escape = false;
			} else if (c == '\\') {
				escape = true;
			} else {
				if (c <= '\u001F') {
					if (c == '\n' || c == '\r') {
						throw new ParsingException("Invalid newline in string. Are you missing a \" quote? Use \\n to include a newline in the string.");
					}
					throw new ParsingException("Character \\u" + String.format("%04x", (int)c) + " must be escaped.");
				}
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
		String str = input.readCharsUntil(SINGLE_QUOTE).toString();
		input.readChar();// skips the last single quote
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
				builder.write(escape(next, input));
			} else {
				builder.write(c);
			}
		}
		input.skipPeeks();// Don't include the closing quotes in the String
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
			builder.append(c);
		}
		input.skipPeeks();// Don't include the closing quotes in the String
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
	private static char escape(char c, CharacterInput input) {
		switch (c) {
			case '"':
			case '\\':
				return c;
			case 'b':
				return '\b';
			case 'f':
				return '\f';
			case 'n':
				return '\n';
			case 'r':
				return '\r';
			case 't':
				return '\t';
			case 'u':
				CharsWrapper chars = input.readChars(4);
				return (char)Utils.parseInt(chars, 16);
			case 'U':
				chars = input.readChars(8);
				return (char)Utils.parseInt(chars, 16);
			default:
				throw new ParsingException("Invalid escapement: \\" + c);
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