package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.serialization.CharacterInput;
import com.electronwill.nightconfig.core.serialization.ParsingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml#user-content-array">TOML specification -
 * Arrays</a>
 */
public final class ArrayParser {
	static List<?> parseArray(CharacterInput input) {
		List<Object> list = new ArrayList<>();
		while (true) {
			char firstChar = Toml.readUsefulChar(input);
			if (firstChar == ']') return list;//handle [] and [v1,v2,... ,]

			Object value = ValueParser.parseValue(input, firstChar);
			list.add(value);

			char after = Toml.readUsefulChar(input);
			if (after == ']') return list;
			if (after != ',') throw new ParsingException("Invalid separator '" + after + "' in array.");
		}
	}

	private ArrayParser() {}
}
