package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.CharsWrapper;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.Utils;

/**
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml#user-content-integer">TOML specification - Integers</a>
 * @see <a href="https://github.com/toml-lang/toml#user-content-float">TOML specification - Floats</a>
 * @see <a href="https://github.com/toml-lang/toml#user-content-boolean">TOML specification - Booleans</a>
 */
final class ValueParser {

	private static final char[] END_OF_VALUE = {'\t', ' ', '\n', '\r', ',', ']', '}'};
	private static final char[] END_OF_VALUE_DATE = {'\t', '#', '\n', '\r', ',', ']', '}'};
	private static final char[] TRUE_END = {'r', 'u', 'e'}, FALSE_END = {'a', 'l', 's', 'e'};
	private static final char[] ONLY_IN_FP_NUMBER = {'.', 'e', 'E'};
	private static final char[] FP_INFINITY = {'i', 'n', 'f'};
	private static final char[] FP_NAN = {'n', 'a', 'n'};

	/**
	 * Parses a TOML value. The value's type is determinated with the first character, and with
	 * the next ones if necessary.
	 */
	static Object parse(CharacterInput input, char firstChar, TomlParser parser, CommentedConfig parentConfig) {
		switch (firstChar) {
			case '{':
				return TableParser.parseInline(input, parser, parentConfig);
			case '[':
				return ArrayParser.parse(input, parser, parentConfig);
			case '\'':
				if (input.peek() == '\'' && input.peek(1) == '\'') {
					input.skipPeeks();// Don't include the opening quotes in the String
					return StringParser.parseMultiLiteral(input, parser);
				}
				return StringParser.parseLiteral(input, parser);
			case '\"':
				if (input.peek() == '\"' && input.peek(1) == '\"') {
					input.skipPeeks();// Don't include the opening quotes in the String
					return StringParser.parseMultiBasic(input, parser);
				}
				return StringParser.parseBasic(input, parser);
			case 't':
				return parseTrue(input);
			case 'f':
				return parseFalse(input);
			case '+':
			case '-':
				input.pushBack(firstChar);
				return parseNumber(input.readUntil(END_OF_VALUE));
			default:
				input.pushBack(firstChar);
				CharsWrapper valueChars = input.readUntil(END_OF_VALUE_DATE);
				if (shouldBeTemporal(valueChars)) {
					return TemporalParser.parse(valueChars);
				}
				CharsWrapper trimmed = valueChars.trimmedView();
				if (trimmed.isEmpty()) {
					throw new ParsingException("Invalid value containing only whitespaces");
				}
				return parseNumber(trimmed);
		}
	}

	static Object parse(CharacterInput input, TomlParser parser, CommentedConfig parentConfig) {
		return parse(input, Toml.readNonSpaceChar(input, false), parser, parentConfig);
	}

	private static boolean shouldBeTemporal(CharsWrapper valueChars) {
		return (valueChars.length() >= 8)
			   && (valueChars.get(2) == ':' || (valueChars.get(4) == '-' && valueChars.get(7) == '-'));
	}

	private static Number parseNumber(CharsWrapper valueChars) {
		valueChars = simplifyNumber(valueChars);
		// Parse +-inf and +-nan
		char first = valueChars.get(0);
		CharsWrapper remaining;
		if (first == '-') {
			remaining = valueChars.subView(1);
			if (remaining.contentEquals(FP_INFINITY)) {
				return Double.NEGATIVE_INFINITY;
			}
		} else if (first == '+') {
			remaining = valueChars.subView(1);
		} else {
			remaining = valueChars;
		}
		if (remaining.contentEquals(FP_INFINITY)) {
			return Double.POSITIVE_INFINITY;
		} else if (remaining.contentEquals(FP_NAN)) {
			return Double.NaN;
		}
		// Parse other fp values
		if (valueChars.indexOfFirst(ONLY_IN_FP_NUMBER) != -1 && !valueChars.subView(0, 2).toString.equals("0x")) {
			try {
				return Utils.parseDouble(valueChars);
			} catch (NumberFormatException ex) {
				throw new ParsingException("Invalid value: " + valueChars);
			}
		}
		// Parse integers
		CharsWrapper numberChars = valueChars;
		int base = 10;
		if (valueChars.length() > 2) {
			switch (valueChars.subView(0, 2).toString()) {
				case "0x":
					base = 16;
					break;
				case "0b":
					base = 2;
					break;
				case "0o":
					base = 8;
					break;
			}
			if (base != 10) {
				numberChars = valueChars.subView(2);
			}
		}
		long longValue;
		try {
			longValue = Utils.parseLong(numberChars, base);
		} catch (NumberFormatException ex) {
			throw new ParsingException("Invalid value: " + valueChars);
		}
		int intValue = (int)longValue;
		if (intValue == longValue) {
			return intValue;// returns an int if it is enough to represent the value correctly
		}
		return longValue;
	}

	private static CharsWrapper simplifyNumber(CharsWrapper numberChars) {
		if (numberChars.charAt(0) == '_') {
			throw new ParsingException("Invalid leading underscore in number " + numberChars);
		}
		if (numberChars.charAt(numberChars.length() - 1) == '_') {
			throw new ParsingException("Invalid trailing underscore in number " + numberChars);
		}
		CharsWrapper.Builder builder = new CharsWrapper.Builder(16);
		boolean nextCannotBeUnderscore = false;
		for (char c : numberChars) {
			if (c == '_') {
				if (nextCannotBeUnderscore) {
					throw new ParsingException("Invalid underscore followed by another one in "
											   + "number "
											   + numberChars);
				}
				nextCannotBeUnderscore = true;
			} else {
				if (nextCannotBeUnderscore) {
					nextCannotBeUnderscore = false;
				}
				builder.append(c);
			}
		}
		return builder.build();
	}

	private static Boolean parseFalse(CharacterInput input) {
		CharsWrapper remaining = input.readUntil(END_OF_VALUE);
		if (!remaining.contentEquals(FALSE_END)) {
			throw new ParsingException(
					"Invalid value f" + remaining + " - Expected the boolean value false.");
		}
		return false;
	}

	private static Boolean parseTrue(CharacterInput input) {
		CharsWrapper remaining = input.readUntil(END_OF_VALUE);
		if (!remaining.contentEquals(TRUE_END)) {
			throw new ParsingException(
					"Invalid value t" + remaining + " - Expected the boolean value true.");
		}
		return true;
	}

	private ValueParser() {}
}