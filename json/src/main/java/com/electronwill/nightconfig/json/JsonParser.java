package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.serialization.CharacterInput;
import com.electronwill.nightconfig.core.serialization.CharsWrapper;
import com.electronwill.nightconfig.core.serialization.ParsingException;
import com.electronwill.nightconfig.core.serialization.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A JSON parser.
 *
 * @author TheElectronWill
 */
public final class JsonParser {
	private static final char[] SPACES = {' ', '\t', '\n', '\r'};
	private static final char[] TRUE_LAST = {'r', 'u', 'e'}, FALSE_LAST = {'a', 'l', 's', 'e'};
	private static final char[] NULL_LAST = {'u', 'l', 'l'};
	private static final char[] NUMBER_END = {',', '}', ']', ' ', '\t', '\n', '\r'};

	private final CharacterInput input;

	/**
	 * Creates a new JsonParser that will parse the specified CharacterInput.
	 *
	 * @param input the input to parse
	 */
	public JsonParser(CharacterInput input) {
		this.input = input;
	}

	/**
	 * Parses the next JSON object and puts it in a new JsonConfig.
	 *
	 * @return the next JSON object.
	 */
	public JsonConfig parseJsonObject() {
		JsonConfig config = new JsonConfig();
		parseJsonObject(config);
		return config;
	}

	/**
	 * Parses the next JSON object and puts it in the specified configuration. The object's entries are added
	 * to the configuration. Any previous entry with a conflicting name is replaced.
	 *
	 * @param config the config where the JSON object will be stored
	 */
	public void parseJsonObject(JsonConfig config) {
		char firstChar = input.readCharAndSkip(SPACES);
		if (firstChar != '{')
			throw new ParsingException("Invalid first character for a json object: " + firstChar);
		parseObject(config);
	}

	private JsonConfig parseObject(JsonConfig config) {
		while (true) {
			char keyFirst = input.readCharAndSkip(SPACES);//the first character of the key
			if (keyFirst != '"')
				throw new ParsingException("Invalid beginning of a key: " + keyFirst);

			String key = parseString();
			char separator = input.readCharAndSkip(SPACES);//the separator between the key and the value
			if (separator != ':')
				throw new ParsingException("Invalid key/value separator: " + separator);

			char valueFirst = input.readCharAndSkip(SPACES);//the first character of the value
			Object value = parseValue(valueFirst);
			config.setValue(key, value);

			char next = input.readCharAndSkip(SPACES);//the next non-space character, should be '}' or ','
			if (next == '}')//end of the object
				return config;
			else if (next != ',')
				throw new ParsingException("Invalid value separator: " + next);
		}
	}

	private List<Object> parseArray() {
		final List<Object> list = new ArrayList<>();
		while (true) {
			char valueFirst = input.readCharAndSkip(SPACES);//the first character of the value
			Object value = parseValue(valueFirst);
			list.add(value);

			char next = input.readCharAndSkip(SPACES);//the next character, should be ']' or ','
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
				return parseObject(new JsonConfig());
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
		char[] chars = input.readChars(3);
		if (!Arrays.equals(chars, TRUE_LAST))
			throw new ParsingException("Invalid value: t" + new CharsWrapper(chars) + " - expected boolean true");
		return true;
	}

	private boolean parseFalse() {
		char[] chars = input.readChars(4);
		if (!Arrays.equals(chars, FALSE_LAST))
			throw new ParsingException("Invalid value: f" + new CharsWrapper(chars) + " - expected boolean false");
		return false;
	}

	private Object parseNull() {
		char[] chars = input.readChars(3);
		if (!Arrays.equals(chars, NULL_LAST))
			throw new ParsingException("Invaid value: n" + new CharsWrapper(chars) + " - expected null");
		return null;
	}

	private String parseString() {
		StringBuilder builder = new StringBuilder();
		boolean escape = false;
		for (char c = input.readChar(); c != '"' || escape; c = input.readChar()) {
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
				char[] chars = input.readChars(4);
				return (char)Utils.parseInt(chars, 16);
			default:
				throw new ParsingException("Invalid escapement: \\" + c);
		}
	}
}
