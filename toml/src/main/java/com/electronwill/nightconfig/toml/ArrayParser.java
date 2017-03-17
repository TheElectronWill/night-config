package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.ParsingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml#user-content-array">TOML specification - Arrays</a>
 */
final class ArrayParser {
	static List<?> parseArray(CharacterInput input, TomlParser parser) {
		List<Object> list = new ArrayList<>(parser.getInitialListCapacity());
		while (true) {
			char firstChar = Toml.readUsefulChar(input);
			if (firstChar == ']') return list;//handle [] and [v1,v2,... ,]

			Object value = ValueParser.parseValue(input, firstChar, parser);
			list.add(value);

			char after = Toml.readUsefulChar(input);
			if (after == ']') return list;
			if (after != ',') throw new ParsingException("Invalid separator '" + after + "' in array.");
		}
	}

	private ArrayParser() {}
}
