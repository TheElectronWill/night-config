package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.impl.CharacterInput;
import com.electronwill.nightconfig.core.impl.Charray;
import com.electronwill.nightconfig.core.impl.Tokenizer;
import com.electronwill.nightconfig.core.impl.Utils;
import com.electronwill.nightconfig.core.io.ParsingException;

import static com.electronwill.nightconfig.json.JsonToken.*;

/**
 * Produces JSON tokens ({@link com.electronwill.nightconfig.json.JsonToken}) from characters.
 */
public final class JsonTokenizer implements Tokenizer<JsonToken> {
	private static final char[] TRUE_TAIL = {'r', 'u', 'e'},
								FALSE_TAIL = {'a', 'l', 's', 'e'},
								NULL_TAIL = {'u', 'l', 'l'};
	private static final Charray VALID_NUMBER_END = new Charray(" \t\n,:");

	private final CharacterInput input;
	private long integerValue;
	private double floatingValue;
	private CharSequence stringValue;

	public JsonTokenizer(CharacterInput input) {
		this.input = input;
	}

	@Override
	public JsonToken next() {
		int c = input.skipWhitespaces();
		switch (c) {
			case -1:
				return END_OF_DATA;
			case '{':
				return OBJECT_START;
			case '}':
				return OBJECT_END;
			case '[':
				return ARRAY_START;
			case ']':
				return ARRAY_END;
			case ':':
				return KV_SEPARATOR;
			case ',':
				return ELEMENT_SEPARATOR;
			case 't':
				checkNextChars(TRUE_TAIL, 't');
				return VALUE_TRUE;
			case 'f':
				checkNextChars(FALSE_TAIL, 'f');
				return VALUE_FALSE;
			case 'n':
				checkNextChars(NULL_TAIL, 'n');
				return VALUE_NULL;
			case '"':
				stringValue = readString();
				return VALUE_STRING;
			default:
				input.pushBack((char)c);
				return detectNumberType(input);

		}
	}

	@Override
	public CharSequence textValue() {
		return stringValue;
	}

	@Override
	public int intValue() {
		return (int)integerValue;
	}

	@Override
	public long longValue() {
		return integerValue;
	}

	@Override
	public double doubleValue() {
		return floatingValue;
	}

	@Override
	public int line() {
		return 0;
	}

	@Override
	public int column() {
		return 0;
	}

	private JsonToken detectNumberType(CharacterInput input) {
		Charray chars = input.readUntilAny(VALID_NUMBER_END);
		try {
			integerValue = Utils.parseLong(chars, 10);
			floatingValue = (double)integerValue;
			return VALUE_INTEGER;
		} catch (Exception ex) {
			floatingValue = Utils.parseDouble(chars);
			integerValue = (long)floatingValue;
			return VALUE_FLOATING;
		}
	}

	private void checkNextChars(char[] expected, char c) {
		Charray next = input.readExactly(expected.length);
		if (!next.contentEquals(expected)) {
			throw new ParsingException("Invalid value: " + c + next);
		}
	}

	private CharSequence readString() {
		Charray cha = new Charray();
		boolean escape = false;
		int c;
		while ((c = input.read()) != '"' || escape) {
			if (c == -1) {
				throw ParsingException.notEnoughData();
			}
			if (escape) {
				cha.append(escape(c, input));
				escape = false;
			} else if (c == '\\') {
				escape = true;
			} else {
				cha.append(c);
			}
		}
		return cha;
	}

	private char escape(int c, CharacterInput more) {
		switch (c) {
			case '"':
			case '\\':
			case '/':
				return (char)c;
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
				Charray chars = more.readExactly(4);
				return (char)Utils.parseLong(chars, 16);
			default:
				throw new ParsingException("Invalid escapement: \\" + c);
		}
	}
}
