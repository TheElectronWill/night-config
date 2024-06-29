package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
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

	private static final char[] KEY_END = {'\t', ' ', '=', '.', '\n', '\r', ']', ':'};

	static <T extends CommentedConfig> T parseInline(CharacterInput input, TomlParser parser, T config) {
		parser.registerInlineTable(config);
		boolean configWasInitiallyEmpty = config.isEmpty();
		boolean expectNextElement = false;
		while (true) {
			char keyFirst = Toml.readNonSpaceChar(input, false);
			if (keyFirst == '}') {
				if (expectNextElement) {
					throw new ParsingException("Invalid trailing comma in inline table");
				}
				return config;// handles {} and {k1=v1,... ,}
			}
			List<String> key = parseDottedKey(input, keyFirst, parser);
			// Forbid the insertion if a parent already exists
			checkDuplicateKeyBecauseOfParents(key, config, configWasInitiallyEmpty);

			Object value = ValueParser.parse(input, parser, config);
			Object previous = parser.getParsingMode().put(config, key, value);
			checkDuplicateKey(key, previous, true);

			char after = Toml.readNonSpaceChar(input, false);
			if (after == '}') {
				return config;
			}
			if (after != ',') {
				throw new ParsingException(
						"Invalid entry separator '" + after + "' in inline table.");
			}
			expectNextElement = true;
		}
	}

	static <T extends CommentedConfig> T parseNormal(CharacterInput input, TomlParser parser,
													 T config) {
		boolean configWasInitiallyEmpty = config.isEmpty();
		while (true) {
			List<CharsWrapper> commentsList = new ArrayList<>(2);
			int keyFirst = Toml.readUseful(input, commentsList);
			if (keyFirst == -1 || keyFirst == '[') {
				parser.setComment(commentsList);// Saves the comments that are above the next table
				return config;// No more data, or beginning of an other table
			}
			List<String> key = parseDottedKey(input, (char)keyFirst, parser);
			checkDuplicateKeyBecauseOfParents(key, config, configWasInitiallyEmpty);

			Object value = ValueParser.parse(input, parser, config);
			Object previous = parser.getParsingMode().put(config, key, value);
			checkDuplicateKey(key, previous, parser.configWasEmpty());

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
			config.setComment(key, parser.consumeComment());
		}
	}

	private static void checkDuplicateKeyBecauseOfParents(List<String> key, Config config, boolean emptyConfig) {
		if (!emptyConfig || key.size() <= 1) {
			return;
		}
		Config current = config;
		int i = 0;
		for (String k : key.subList(0, key.size()-1)) {
			List<String> singleKey = Collections.singletonList(k);
			Object sub = current.get(singleKey);
			if (sub == null) {
				return;
			}
			if (!(sub instanceof Config)) {
				throw new ParsingException(String.format("Wrong duplicate key %s: intermediary level %s already exists", key, key.subList(0, i+1)));
			}
			current = (Config)sub;
			i += 1;
		}
	}

	private static void checkDuplicateKey(Object key, Object previousValue, boolean emptyConfig) {
		/*
		If the config wasn't empty at the beginning of the parsing, there is no way to know if
		previousValue isn't null because we parsed the data twice or because it was in the config
		from the beginning.
		 */
		if (previousValue != null && emptyConfig) {
			throw new ParsingException(
					"Invalid TOML data: entry \"" + key + "\" defined twice in its table.");
		}
	}

	static CommentedConfig parseNormal(CommentedConfig parentConfig, CharacterInput input, TomlParser parser) {
		return parseNormal(input, parser, parentConfig.createSubConfig());
	}

	static List<String> parseTableName(CharacterInput input, TomlParser parser, boolean array) {
		List<String> list = parser.createList();
		while (true) {
			char firstChar = Toml.readNonSpaceChar(input, false);
			if (firstChar == ']') {
				throw new ParsingException("Tables names must not be empty.");
			}
			String key = parseNonDottedKey(input, firstChar, parser);
			list.add(key);

			char separator = Toml.readNonSpaceChar(input, false);
			if (separator == ']') {// End of the declaration
				if (array) {
					char after = input.readChar();
					if (after != ']') {
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
					throw new ParsingException(
							"Invalid character '" + after + "' after a table " + "declaration.");
				}
				return list;
			} else if (separator != '.') {
				throw new ParsingException("Invalid separator '" + separator + "' in table name.");
			}
		}
	}

	/**
	 * Parses a key that can be dotted (e.g. {@code a.b.c}, quoted {@code "abc"} or bare {@code abc}).
	 * The next non-space character is also read, and an exception is thrown if it's not a key-value separator.
	 *
	 * @return the components of the key
	 */
	static List<String> parseDottedKey(CharacterInput input, char firstChar, TomlParser parser) {
		List<String> list = parser.createList();
		char first = firstChar;
		while (true) {
			String part = parseNonDottedKey(input, first, parser);
			list.add(part);

			char sep = Toml.readNonSpaceChar(input, false);
			if (Toml.isKeyValueSeparator(sep, parser.isLenientWithSeparators())) {
				return list;
			} else if (sep != '.') {
				throw new ParsingException("Invalid character '" + sep + "' after key " + list);
			}
			first = Toml.readNonSpaceChar(input, false);
		}
	}

	static String parseNonDottedKey(CharacterInput input, char firstChar, TomlParser parser) {
		// Note that a key can't be multiline
		// Empty keys are allowed if and only if they are quoted (with double or single quotes)
		if (firstChar == '\"') {
			return StringParser.parseBasic(input, parser);
		} else if (firstChar == '\'') {
			return StringParser.parseLiteral(input, parser);
		} else {
			CharsWrapper restOfKey = input.readCharsUntil(KEY_END);
			@SuppressWarnings("resource")
			String bareKey = new CharsWrapper.Builder(restOfKey.length() + 1).append(firstChar)
																			 .append(restOfKey)
																			 .toString();
			// Checks that the bare key is conform to the specification
			if (bareKey.isEmpty()) {
				throw new ParsingException("Empty bare keys aren't allowed.");
			}
			if (!Toml.isValidBareKey(bareKey, parser.isLenientWithBareKeys())) {
				throw new ParsingException("Invalid bare key: \'" + bareKey + "\'");
			}
			return bareKey;
		}
	}

	private TableParser() {}
}