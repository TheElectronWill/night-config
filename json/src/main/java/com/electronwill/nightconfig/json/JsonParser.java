package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.serialization.*;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * A JSON parser.
 *
 * @author TheElectronWill
 */
public final class JsonParser implements ConfigParser<JsonConfig> {
	private static final char[] SPACES = {' ', '\t', '\n', '\r'};
	private static final char[] TRUE_LAST = {'r', 'u', 'e'}, FALSE_LAST = {'a', 'l', 's', 'e'};
	private static final char[] NULL_LAST = {'u', 'l', 'l'};
	private static final char[] NUMBER_END = {',', '}', ']', ' ', '\t', '\n', '\r'};

	@Override
	public JsonConfig parseConfig(Reader reader) {
		return parseJsonObject(new ReaderInput(reader));
	}

	@Override
	public void parseConfig(Reader reader, JsonConfig destination) {
		parseJsonObject(new ReaderInput(reader), destination);
	}

	/**
	 * Parses a JSON document, either a JSON object or a JSON array.
	 *
	 * @param input the input to read the data from
	 * @return a JsonConfig or a List, depending of the document's type
	 */
	public Object parseJsonDocument(CharacterInput input) {
		char firstChar = input.readCharAndSkip(SPACES);
		if (firstChar == '{') return parseObject(input, new JsonConfig());
		if (firstChar == '[') return parseArray(input, new ArrayList<>());
		throw new ParsingException("Invalid first character for a json document: " + firstChar);
	}

	/**
	 * Parses the next JSON object and puts it in a new JsonConfig.
	 *
	 * @return the next JSON object.
	 */
	public JsonConfig parseJsonObject(CharacterInput input) {
		JsonConfig config = new JsonConfig();
		parseJsonObject(input, config);
		return config;
	}

	/**
	 * Parses the next JSON object and puts it in the specified configuration. The object's entries are added
	 * to the configuration. Any previous entry with a conflicting name is replaced.
	 *
	 * @param destination the config where the JSON object will be stored
	 */
	public void parseJsonObject(CharacterInput input, JsonConfig destination) {
		char firstChar = input.readCharAndSkip(SPACES);
		if (firstChar != '{')
			throw new ParsingException("Invalid first character for a json object: " + firstChar);
		parseObject(input, destination);
	}

	public List<Object> parseJsonArray(CharacterInput input) {
		List<Object> list = new ArrayList<>();
		parseJsonArray(input, list);
		return list;
	}

	public void parseJsonArray(CharacterInput input, List<Object> destination) {
		char firstChar = input.readCharAndSkip(SPACES);
		if (firstChar != '[')
			throw new ParsingException("Invalid first character for a json array: " + firstChar);
		parseArray(input, destination);
	}

	private JsonConfig parseObject(CharacterInput input, JsonConfig config) {
		while (true) {
			char keyFirst = input.readCharAndSkip(SPACES);//the first character of the key
			if (keyFirst != '"')
				throw new ParsingException("Invalid beginning of a key: " + keyFirst);

			String key = parseString(input);
			char separator = input.readCharAndSkip(SPACES);//the separator between the key and the value
			if (separator != ':')
				throw new ParsingException("Invalid key/value separator: " + separator);

			char valueFirst = input.readCharAndSkip(SPACES);//the first character of the value
			Object value = parseValue(input, valueFirst);
			config.setValue(key, value);

			char next = input.readCharAndSkip(SPACES);//the next non-space character, should be '}' or ','
			if (next == '}')//end of the object
				return config;
			else if (next != ',')
				throw new ParsingException("Invalid value separator: " + next);
		}
	}

	private List<Object> parseArray(CharacterInput input, List<Object> list) {
		while (true) {
			char valueFirst = input.readCharAndSkip(SPACES);//the first character of the value
			Object value = parseValue(input, valueFirst);
			list.add(value);

			char next = input.readCharAndSkip(SPACES);//the next character, should be ']' or ','
			if (next == ']')//end of the array
				return list;
			else if (next != ',')//invalid separator
				throw new ParsingException("Invalid value separator: " + valueFirst);
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
		if (chars.contains('.') || chars.contains('e') || chars.contains('E')) {//must be a double
			return Utils.parseDouble(chars);
		}
		return Utils.parseLong(chars, 10);
	}

	private boolean parseTrue(CharacterInput input) {
		CharsWrapper chars = input.readChars(3);
		if (!chars.contentEquals(TRUE_LAST))
			throw new ParsingException("Invalid value: t" + new CharsWrapper(chars) + " - expected boolean true");
		return true;
	}

	private boolean parseFalse(CharacterInput input) {
		CharsWrapper chars = input.readChars(4);
		if (!chars.contentEquals(FALSE_LAST))
			throw new ParsingException("Invalid value: f" + new CharsWrapper(chars) + " - expected boolean false");
		return false;
	}

	private Object parseNull(CharacterInput input) {
		CharsWrapper chars = input.readChars(3);
		if (!chars.contentEquals(NULL_LAST))
			throw new ParsingException("Invaid value: n" + new CharsWrapper(chars) + " - expected null");
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
