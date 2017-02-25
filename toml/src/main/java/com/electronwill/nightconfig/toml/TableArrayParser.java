package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.serialization.CharacterInput;
import com.electronwill.nightconfig.core.serialization.ParsingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml#user-content-array-of-tables">TOML specification -
 * Arrays of tables</a>
 */
final class TableArrayParser {
	static List<String> parseElementName(CharacterInput input, TomlParser parser) {
		List<String> list = new ArrayList<>(parser.getInitialListCapacity());

		// Special case for the first part because we need to forbid [[]] and [[ <spaces here> ]]
		char first = input.readChar();
		if (first == ']') {
			throw new ParsingException("The name of an element in an array of tables must not be empty.");
		}
		String firstKey = TableParser.parseKey(input, first, parser);
		list.add(firstKey);

		while (true) {
			char before = Toml.readNonSpaceChar(input);
			if (before == ']') {
				if (input.readChar() != ']') {
					throw new ParsingException("Invalid declaration of an element of an array of tables: " +
						"missing closing bracket at the end of the name.");
				}
				return list;
			}
			if (before != '.') throw new ParsingException("Found invalid table name: unexpected character '"
				+ before + "' after a part of the name.");

			char next = input.readChar();
			String key = TableParser.parseKey(input, next, parser);
			list.add(key);
		}
	}

	private TableArrayParser() {}
}
