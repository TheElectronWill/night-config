package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.CharsWrapper;
import com.electronwill.nightconfig.core.io.ParsingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml#user-content-table">TOML specification -
 * Tables</a>
 */
final class TableParser {

	private static final char[] KEY_END = {'\t', ' ', '=', '.', '\n', '\r', ']'};

	static TomlConfig parseInline(CharacterInput input, TomlParser parser) {
		TomlConfig config = new TomlConfig();
		while (true) {
			char keyFirst = Toml.readNonSpaceChar(input, false);
			if (keyFirst == '}') {
				return config;// handles {} and {k1=v1,... ,}
			}
			String key = parseKey(input, keyFirst, parser);
			char sep = Toml.readNonSpaceChar(input, false);
			checkInvalidSeparator(sep, key);

			Object value = ValueParser.parse(input, parser);
			Object previous = config.valueMap().putIfAbsent(key, value);/* bypasses path parsing (in
																		order to be faster) */
			checkDuplicateKey(key, previous);

			char after = Toml.readNonSpaceChar(input, false);
			if (after == '}') {
				return config;
			}
			if (after != ',') {
				throw new ParsingException(
						"Invalid entry separator '" + after + "' in inline table.");
			}
		}
	}

	static <T extends CommentedConfig> T parseNormal(CharacterInput input, TomlParser parser, T config) {
		while (true) {
			List<CharsWrapper> commentsList = new ArrayList<>(2);
			int keyFirst = Toml.readUseful(input, commentsList);
			if (keyFirst == -1 || keyFirst == '[') {
				parser.setComment(commentsList);// Save the comments for later
				return config;// No more data, or beginning of an other table
			}
			String key = parseKey(input, (char)keyFirst, parser);
			char sep = Toml.readNonSpaceChar(input, false);
			checkInvalidSeparator(sep, key);

			Object value = ValueParser.parse(input, parser);
			Object previous = config.valueMap().putIfAbsent(key, value);/* bypasses path parsing (in
																		order to be faster) */
			checkDuplicateKey(key, previous);

			int after = Toml.readNonSpace(input, false);
			if (after == -1) {// End of the stream
				return config;
			}
			if (after == '#') {
				CharsWrapper comment = Toml.readLine(input);
				commentsList.add(comment);
			} else if (after != '\n' && after != '\r') {
				throw new ParsingException("Invalid character '"
										   + (char)after
										   + "' after table entry \""
										   + key
										   + "\" = "
										   + value);
			}
			parser.setComment(commentsList);
			config.setComment(Collections.singletonList(key), parser.consumeComment());
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

	static List<String> parseTableName(CharacterInput input, TomlParser parser, boolean array) {
		List<String> list = parser.createList();
		while (true) {
			char firstChar = Toml.readNonSpaceChar(input, false);
			if (firstChar == ']') {
				throw new ParsingException("Tables names must not be empty.");
			}
			String key = parseKey(input, firstChar, parser);
			list.add(key);

			char separator = Toml.readNonSpaceChar(input, false);
			if (separator == ']') {// End of the declaration
				if (array) {
					char after = input.readChar();
					if(after != ']') {
						throw new ParsingException("Invalid declaration of an element of an array"
												   + " of tables: it ends by ]"
												   + after
												   + " but should end by ]]");

					}
				}
				char after = Toml.readNonSpaceChar(input, false);
				if (after == '#') {// Comment
					CharsWrapper comment = Toml.readLine(input);
					parser.setComment(comment);
				} else if (after != '\n' && after != '\r') {
					throw new ParsingException("Invalid character '" + after + "' after a table "
											   + "declaration.");
				}
				return list;
			} else if (separator != '.') {
				throw new ParsingException("Invalid separator '" + separator + "' in table name.");
			}
		}
	}

	static List<String> parseTableName(CharacterInput input, TomlParser parser) {
		return parseTableName(input, parser, false);
	}

	static List<String> parseTableArrayName(CharacterInput input, TomlParser parser) {
		return parseTableName(input, parser, true);
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
			String bareKey = new CharsWrapper.Builder(restOfKey.length() + 1).append(firstChar)
																			 .append(restOfKey)
																			 .toString();
			// Checks that the bare key is conform to the specification
			if (bareKey.isEmpty()) {
				throw new ParsingException("Empty bare keys aren't allowed.");
			}
			if(!Toml.isValidBareKey(bareKey, parser.isLenientWithBareKeys())) {
				throw new ParsingException("Invalid bare key: " + bareKey);
			}
			return bareKey;
		}
	}

	private TableParser() {}
}