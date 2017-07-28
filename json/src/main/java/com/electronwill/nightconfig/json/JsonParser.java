package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.CharsWrapper;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ReaderInput;
import com.electronwill.nightconfig.core.io.Utils;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * A JSON parser.
 *
 * @author TheElectronWill
 */
public final class JsonParser implements ConfigParser<JsonConfig, Config> {
	private static final char[] SPACES = {' ', '\t', '\n', '\r'};
	private static final char[] TRUE_LAST = {'r', 'u', 'e'}, FALSE_LAST = {'a', 'l', 's', 'e'};
	private static final char[] NULL_LAST = {'u', 'l', 'l'};
	private static final char[] NUMBER_END = {',', '}', ']', ' ', '\t', '\n', '\r'};

	/**
	 * Parses a JSON document, either a JSON object (parsed to a JsonConfig) or a JSON array
	 * (parsed to a List).
	 *
	 * @param reader the Reader to parse
	 * @return either a JsonConfig or a List, depending on the document's type
	 */
	public Object parseDocument(Reader reader) {
		CharacterInput input = new ReaderInput(reader);
		char firstChar = input.readCharAndSkip(SPACES);
		if (firstChar == '{') {
			return parseObject(input, new JsonConfig());
		}
		if (firstChar == '[') {
			return parseArray(input, new ArrayList<>());
		}
		throw new ParsingException("Invalid first character for a json document: " + firstChar);
	}

	/**
	 * Parses a JSON object to a Config.
	 */
	@Override
	public JsonConfig parse(Reader reader) {
		JsonConfig config = new JsonConfig();
		parse(reader, config);
		return config;
	}

	/**
	 * Parses a JSON object to a Config.
	 */
	@Override
	public void parse(Reader reader, Config destination) {
		CharacterInput input = new ReaderInput(reader);
		char firstChar = input.readCharAndSkip(SPACES);
		if (firstChar != '{') {
			throw new ParsingException("Invalid first character for a json object: " + firstChar);
		}
		parseObject(input, destination);
	}

	/**
	 * Parses a JSON array to a List.
	 *
	 * @param reader the Reader to parse
	 * @return a List with the content of the parse array
	 */
	public List<Object> parseList(Reader reader) {
		List<Object> list = new ArrayList<>();
		parseList(reader, list);
		return list;
	}

	/**
	 * Parses a JSON array to a List.
	 *
	 * @param reader      the Reader to parse
	 * @param destination the List where to put the data
	 */
	public void parseList(Reader reader, List<?> destination) {
		CharacterInput input = new ReaderInput(reader);
		char firstChar = input.readCharAndSkip(SPACES);
		if (firstChar != '[') {
			throw new ParsingException("Invalid first character for a json array: " + firstChar);
		}
		parseArray(input, destination);
	}

	private <T extends Config> T parseObject(CharacterInput input, T config) {
		while (true) {
			char keyFirst = input.readCharAndSkip(SPACES);// the first character of the key
			if (keyFirst != '"') {
				throw new ParsingException("Invalid beginning of a key: " + keyFirst);
			}

			String key = parseString(input);
			char separator = input.readCharAndSkip(SPACES);// the char between the key and the value
			if (separator != ':') {
				throw new ParsingException("Invalid key/value separator: " + separator);
			}

			char valueFirst = input.readCharAndSkip(SPACES);// the first character of the value
			Object value = parseValue(input, valueFirst);
			config.set(key, value);

			char next = input.readCharAndSkip(SPACES);// should be'}' or ','
			if (next == '}') {// end of the object
				return config;
			} else if (next != ',') {
				throw new ParsingException("Invalid value separator: " + next);
			}
		}
	}

	private <T> List<T> parseArray(CharacterInput input, List<T> list) {
		while (true) {
			char valueFirst = input.readCharAndSkip(SPACES);// the first character of the value
			T value = (T)parseValue(input, valueFirst);
			list.add(value);
			char next = input.readCharAndSkip(SPACES);// the next character, should be ']' or ','
			if (next == ']') {// end of the array
				return list;
			} else if (next != ',') {// invalid separator
				throw new ParsingException("Invalid value separator: " + valueFirst);
			}
		}
	}

	private Object parseValue(CharacterInput input, char firstChar) {
		switch (firstChar) {
			case '"':
				return parseString(input);
			case '{':
				return parseObject(input, new JsonConfig());
			case '[':
				return parseArray(input, new ArrayList<>());
			case 't':
				return parseTrue(input);
			case 'f':
				return parseFalse(input);
			case 'n':
				return parseNull(input);
			default:
				input.pushBack(firstChar);
				return parseNumber(input);
		}
	}

	private Number parseNumber(CharacterInput input) {
		CharsWrapper chars = input.readCharsUntil(NUMBER_END);
		if (chars.contains('.') || chars.contains('e') || chars.contains('E')) {// must be a double
			return Utils.parseDouble(chars);
		}
		return Utils.parseLong(chars, 10);
	}

	private boolean parseTrue(CharacterInput input) {
		CharsWrapper chars = input.readChars(3);
		if (!chars.contentEquals(TRUE_LAST)) {
			throw new ParsingException("Invalid value: t" + chars + " - expected boolean true");
		}
		return true;
	}

	private boolean parseFalse(CharacterInput input) {
		CharsWrapper chars = input.readChars(4);
		if (!chars.contentEquals(FALSE_LAST)) {
			throw new ParsingException("Invalid value: f" + chars + " - expected boolean false");
		}
		return false;
	}

	private Object parseNull(CharacterInput input) {
		CharsWrapper chars = input.readChars(3);
		if (!chars.contentEquals(NULL_LAST)) {
			throw new ParsingException("Invaid value: n" + chars + " - expected null");
		}
		return null;
	}

	private String parseString(CharacterInput input) {
		StringBuilder builder = new StringBuilder();
		boolean escape = false;
		char c;
		while ((c = input.readChar()) != '"' || escape) {
			if (escape) {
				builder.append(escape(c, input));
				escape = false;
			} else if (c == '\\') {
				escape = true;
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}

	private char escape(char c, CharacterInput input) {
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