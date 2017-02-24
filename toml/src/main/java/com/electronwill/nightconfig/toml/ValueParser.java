package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.serialization.CharacterInput;
import com.electronwill.nightconfig.core.serialization.CharsWrapper;
import com.electronwill.nightconfig.core.serialization.ParsingException;
import com.electronwill.nightconfig.core.serialization.Utils;

/**
 * @author TheElectronWill
 */
public final class ValueParser {

	private static final char[] END_OF_VALUE = {'\t', ' ', '\n', '\r', ','};
	private static final char[] TRUE_END = {'r', 'u', 'e'}, FALSE_END = {'a', 'l', 's', 'e'};
	private static final char[] ONLY_IN_FP_NUMBER = {'.', 'e', 'E'};

	static Object parseValue(CharacterInput input, char firstChar) {
		switch (firstChar) {
			case '{':
				return TableParser.parseInline(input);
			case '[':
				return ArrayParser.parseArray(input);
			case '\'':
				if (input.peek() == '\'' && input.peek(1) == '\'') {
					return StringParser.parseMultiLiteral(input);
				}
				return StringParser.parseLiteral(input);
			case '\"':
				if (input.peek() == '\"' && input.peek(1) == '\"') {
					return StringParser.parseMultiBasic(input);
				}
				return StringParser.parseMultiBasic(input);
			case 't':
				return parseTrue(input);
			case 'f':
				return parseFalse(input);
			case '+':
			case '-':
				input.pushBack(firstChar);
				return parseNumber(input);
			default:
				input.pushBack(firstChar);
				return parseNumberOrDateTime(input);
		}
	}

	private static Object parseNumberOrDateTime(CharacterInput input) {
		CharsWrapper valueChars = input.readCharsUntil(END_OF_VALUE);
		if (TemporalParser.shouldBeTemporal(valueChars))
			return TemporalParser.parseTemporal(valueChars);
		return parseNumber(valueChars);
	}

	private static Number parseNumber(CharacterInput input) {
		CharsWrapper valueChars = input.readCharsUntil(END_OF_VALUE);
		return parseNumber(valueChars);
	}

	private static Number parseNumber(CharsWrapper valueChars) {
		if (valueChars.indexOfFirst(ONLY_IN_FP_NUMBER) != -1) {
			return Utils.parseDouble(valueChars);
		}
		long longValue = Utils.parseLong(valueChars, 10);
		int intValue = (int)longValue;
		if (intValue == longValue) return intValue;//returns an int if it is enough
		return longValue;
	}

	private static Boolean parseFalse(CharacterInput input) {
		CharsWrapper remaining = input.readCharsUntil(END_OF_VALUE);
		if (!remaining.contentEquals(FALSE_END)) {
			throw new ParsingException("Invalid value f" + remaining + " - Expected the boolean value false.");
		}
		return false;
	}

	private static Boolean parseTrue(CharacterInput input) {
		CharsWrapper remaining = input.readCharsUntil(END_OF_VALUE);
		if (!remaining.contentEquals(TRUE_END)) {
			throw new ParsingException("Invalid value t" + remaining + " - Expected the boolean value true.");
		}
		return true;
	}

	static Object parseValue(CharacterInput input) {
		return parseValue(input, input.readChar());
	}

	private ValueParser() {}
}
