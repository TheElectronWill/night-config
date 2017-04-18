package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.ParsingException;
import java.util.List;

/**
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml#user-content-array">TOML specification - Arrays</a>
 */
final class ArrayParser {
	/**
	 * Parses a plain array, not an array of tables.
	 */
	static List<?> parse(CharacterInput input, TomlParser parser) {
		List<Object> list = parser.createList();
		while (true) {
			char firstChar = Toml.readUsefulChar(input);
			if (firstChar == ']') {// End of the array
				return list;// handle [] and [v1,v2,... ,]
			} else if (firstChar == ',') {// Handles [,] which is an empty array too
				char nextChar = Toml.readUsefulChar(input);
				if (nextChar == ']') {
					return list;
				}
				throw new ParsingException("Unexpected character in array: '"
										   + nextChar
										   + "' - "
										   + "Expected end of array because of the leading comma.");
			}
			Object value = ValueParser.parse(input, firstChar, parser);
			list.add(value);
			char after = Toml.readUsefulChar(input);
			if (after == ']') {// End of the array
				return list;
			}
			if (after != ',') {// Invalid character between two elements of the array
				throw new ParsingException("Invalid separator '" + after + "' in array.");
			}
		}
	}

	private ArrayParser() {}
}