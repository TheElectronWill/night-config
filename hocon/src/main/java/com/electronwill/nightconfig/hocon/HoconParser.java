package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.serialization.CharacterInput;
import com.electronwill.nightconfig.core.serialization.CharsWrapper;
import com.electronwill.nightconfig.core.serialization.ParsingException;
import com.electronwill.nightconfig.core.serialization.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TheElectronWill
 */
public final class HoconParser {
	private static final char[] ADDITIONAL_SPACES =
		{'\t', '\r', '\uFEFF', '\u000B', '\u000C', '\u001C', '\u001D', '\u001D', '\u001E', '\u001F'};

	private static final char[] NEWLINE = {('\n')};
	private static final char[] TRUE_LAST = {'r', 'u', 'e'}, FALSE_LAST = {'a', 'l', 's', 'e'};
	private static final char[] NULL_LAST = {'u', 'l', 'l'};
	private static final char[] NUMBER_END = {',', '}', ']', ' ', '\t', '\n', '\r'};
	private static final char[] UNQUOTED_FORBIDDEN = {'$', '"', '{', '}', '[', ']', ':', '=', ',', '+',
		'#', '`', '^', '?', '!', '@', '*', '&', '\\'};//whitespaces are forbidden too

	private final CharacterInput input;
	private boolean rootBracesOmitted;

	public HoconParser(CharacterInput input) {
		this.input = input;
	}

	private boolean isWhiteSpace(char c) {
		return c == '\n' || isWhiteSpaceExceptLF(c);
	}

	private boolean isWhiteSpaceExceptLF(char c) {
		return Character.isSpaceChar(c) || Utils.arrayContains(ADDITIONAL_SPACES, c);
	}

	private char readCharAndSkipSpaces() {
		char c;
		do {
			c = input.readChar();
			if ((c == '/' && input.peek() == '/') || c == '#') {//comment
				input.readCharsUntil(NEWLINE);
			}
		} while (isWhiteSpace(c));
		return c;
	}

	private char readCharAndSkipSpacesExceptLF() {
		int read = readAndSkipSpacesExceptLF();
		if (read == -1) throw ParsingException.notEnoughData();
		return (char)read;
	}

	private int readAndSkipSpacesExceptLF() {
		int c;
		do {
			c = input.readChar();
			if (c == -1) {
				return -1;
			} else if ((c == '/' && input.peek() == '/') || c == '#') {//comment
				input.readCharsUntil(NEWLINE);
			}
		} while (isWhiteSpaceExceptLF((char)c));
		return c;
	}

	public HoconConfig parseRootObject() {
		char firstChar = readCharAndSkipSpaces();
		if (firstChar != '{') {
			rootBracesOmitted = true;
		}
		HoconConfig config = new HoconConfig();
		parseObject(config, firstChar);
		return config;
	}

	private HoconConfig parseObject(HoconConfig config) {
		return parseObject(config, -1);
	}

	private HoconConfig parseObject(HoconConfig config, int firstNonSpaceChar) {
		while (true) {
			final char keyFirst;
			if (firstNonSpaceChar > 0 && firstNonSpaceChar != '{') {
				keyFirst = (char)firstNonSpaceChar;
			} else {
				keyFirst = readCharAndSkipSpaces();
			}
			final String key;
			if(keyFirst == '"')
				key = parseString();
			else
				key = parseUnquotedString();
			if (keyFirst != '"')
				throw new ParsingException("Invalid beginning of a key: " + keyFirst);

			final String key = parseString();
			final char separator = readCharAndSkipSpaces();
			if (separator == '{') {//A a key may be directly followed by { to declare an object
				HoconConfig valueObject = parseObject(new HoconConfig());
				config.setValue(key, valueObject);
				continue;
			}
			if (separator != ':' && separator != '=')
				throw new ParsingException("Invalid key/value separator: " + separator);

			final char valueFirst = readCharAndSkipSpaces();
			Object value = parseValue(valueFirst);
			config.setValue(key, value);

			final int next = readAndSkipSpacesExceptLF();
			if (next == -1) {
				if (rootBracesOmitted && firstNonSpaceChar > 0)//end of the root object
					return config;
				throw ParsingException.notEnoughData();
			}
			if (next == '}')//end of the object
				return config;
			else if (next != ',')
				throw new ParsingException("Invalid value separator: " + next);
		}
	}

	private List<Object> parseArray() {
		final List<Object> list = new ArrayList<>();
		while (true) {
			char valueFirst = readCharAndSkipSpaces();
			Object value = parseValue(valueFirst);
			list.add(value);

			char next = readCharAndSkipSpaces();
			if (next == ']')//end of the array
				return list;
			else if (next != ',')//invalid separator
				throw new ParsingException("Invalid value separator: " + valueFirst);
		}
	}

	private Object parseValue(char firstChar) {
		switch (firstChar) {
			case '"':
				return parseString();
			case '{':
				return parseObject(new HoconConfig());
			case '[':
				return parseArray();
			case 't':
				return parseTrue();
			case 'f':
				return parseFalse();
			case 'n':
				return parseNull();
			default:
				return parseNumber();
		}
	}

	private Number parseNumber() {
		CharsWrapper chars = input.readCharsUntil(NUMBER_END);
		if (chars.contains('.') || chars.contains('e') || chars.contains('E')) {//must be a double
			return Utils.parseDouble(chars);
		}
		return Utils.parseLong(chars, 10);
	}

	private boolean parseTrue() {
		CharsWrapper chars = input.readChars(3);
		if (!chars.contentEquals(TRUE_LAST))
			throw new ParsingException("Invalid value: t" + new CharsWrapper(chars) + " - expected boolean true");
		return true;
	}

	private boolean parseFalse() {
		CharsWrapper chars = input.readChars(4);
		if (!chars.contentEquals(FALSE_LAST))
			throw new ParsingException("Invalid value: f" + new CharsWrapper(chars) + " - expected boolean false");
		return false;
	}

	private Object parseNull() {
		CharsWrapper chars = input.readChars(3);
		if (!chars.contentEquals(NULL_LAST))
			throw new ParsingException("Invaid value: n" + new CharsWrapper(chars) + " - expected null");
		return null;
	}

	private String parseString() {
		StringBuilder builder = new StringBuilder();
		boolean escape = false;
		char c;
		while((c = input.readChar()) != '"' || escape) {
			if (escape) {
				builder.append(escape(c));
				escape = false;
			} else if (c == '\\') {
				escape = true;
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}

	private String parseUnquotedString()

	private char escape(char c) {
		switch (c) {
			case '"':
			case '\\':
			case '/':
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
			default:
				throw new ParsingException("Invalid escapement: \\" + c);
		}
	}
}
