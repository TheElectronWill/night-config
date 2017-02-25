package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.serialization.CharacterOutput;
import java.util.Iterator;
import java.util.List;

/**
 * @author TheElectronWill
 */
class ArrayWriter {
	private static final char[] EMPTY_ARRAY = {'[', ']'}, ELEMENT_SEPARATOR = {',', ' '};

	static void writeArray(List<?> values, CharacterOutput output) {
		if (values.isEmpty()) {
			output.write(EMPTY_ARRAY);
			return;
		}
		output.write('[');
		Iterator<?> it = values.iterator();
		while (true) {
			Object next = it.next();
			output.write(next.toString());
			if (it.hasNext()) {
				output.write(ELEMENT_SEPARATOR);
			} else {
				output.write(']');
				return;
			}
		}
	}
}
