package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.CharsWrapper;
import com.electronwill.nightconfig.core.io.ParsingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml#user-content-table">TOML specification - Tables</a>
 */
final class TableParser {

	private static final char[] KEY_END = {'\t', ' ', '=', '.', '\n', '\r', ']'};

	static TomlConfig parseInline(CharacterInput input, TomlParser parser) {
		TomlConfig config = new TomlConfig();
		while (true) {
			char keyFirst = Toml.readNonSpaceChar(input);
			if (keyFirst == '}') {
				return config;// handles {} and {k1=v1,... ,}
			}
			String key = parseKey(input, keyFirst, parser);
			char sep = Toml.readNonSpaceChar(input);
			checkInvalidSeparator(sep, key);

			Object value = ValueParser.parseValue(input, parser);
			Object previous = config.asMap().putIfAbsent(key, value);/* bypasses path parsing (in
																		order to be faster) */
			checkDuplicateKey(key, previous);

			char after = Toml.readNonSpaceChar(input);
			if (after == '}') {
				return config;
			}
			if (after != ',') {
				throw new ParsingException(
						"Invalid entry separator '" + after + "' in inline table.");
			}
		}
	}

	static <T extends Config> T parseNormal(CharacterInput input, TomlParser parser, T config) {
		while (true) {
			int keyFirst = Toml.readUseful(input);
			if (keyFirst == -1 || keyFirst == '[') {
				return config;// No more data, or beginning of an other table
			}
			String key = parseKey(input, (char)keyFirst, parser);
			char sep = Toml.readNonSpaceChar(input);
			checkInvalidSeparator(sep, key);

			Object value = ValueParser.parseValue(input, parser);
			Object previous = config.asMap().putIfAbsent(key, value);/* bypasses path parsing (in
																		order to be faster) */
			checkDuplicateKey(key, previous);

			int after = Toml.readNonSpace(input);
			if (after == -1) {// End of the stream
				return config;
			}
			if (after == '#') {
				Toml.skipComment(input);
			} else if (after != '\n' && after != '\r') {
				throw new ParsingException("Invalid character '"
										   + (char)after
										   + "' after table entry \""
										   + key
										   + "\" = "
										   + value);
			}
		}
	}

	private static void checkDuplicateKey(String key, Object previousValue) {
		if (previousValue != null) {
			throw new ParsingException(
					"Invalid TOML data: entry \"" + key + "\" defined twice" + " in its table.");
		}
	}

	private static void checkInvalidSeparator(char sep, String key) {
		if (sep != '=') {
			throw new ParsingException(
					"Invalid separator '" + sep + "'after key \"" + key + "\" in some table.");
		}
	}

	static TomlConfig parseNormal(CharacterInput input, TomlParser parser) {
		return parseNormal(input, parser, new TomlConfig());
	}

	static List<String> parseTableName(CharacterInput input, TomlParser parser) {
		List<String> list = new ArrayList<>(parser.getInitialListCapacity());

		// Special case for the first part because we need to forbid [] and [ <spaces here> ]
		char first = input.readChar();
		if (first == ']') {
			throw new ParsingException("Table names must not be empty");
		}
		String firstKey = parseKey(input, first, parser);
		list.add(firstKey);

		while (true) {
			char before = Toml.readNonSpaceChar(input);
			if (before == ']') {
				return list;
			}
			if (before != '.') {
				throw new ParsingException("Found invalid table name: unexpected character '"
										   + before
										   + "' after a part of the name.");
			}
			char next = input.readChar();
			String key = parseKey(input, next, parser);
			list.add(key);
		}
	}

	static String parseKey(CharacterInput input, char firstChar, TomlParser parser) {
		// Note that a key can't be multiline
		// Empty keys are allowed if and only if they are quoted (with double or single quotes)
		if (firstChar == '\"') {
			return StringParser.parseBasic(input, parser);
		} else if (firstChar == '\'') {
			return StringParser.parseLiteral(input, parser);
		} else {
			CharsWrapper restOfKey = input.readCharsUntil(KEY_END);
			CharsWrapper bareKey = new CharsWrapper.Builder(restOfKey.length() + 1).append(
					firstChar).append(restOfKey).build();
			// Checks that the bare key is conform to the specification
			if (bareKey.isEmpty()) {
				throw new ParsingException("Empty bare keys aren't allowed.");
			}
			for (char c : bareKey) {
				if (!Toml.isValidInBareKey(c, parser.isLenientWithBareKeys())) {
					throw new ParsingException("Forbidden character in bare key: '" + c + "'");
				}
			}
			return bareKey.toString();
		}
	}

	private TableParser() {}
}