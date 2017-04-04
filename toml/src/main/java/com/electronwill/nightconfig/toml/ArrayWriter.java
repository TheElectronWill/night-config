package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharacterOutput;
import java.util.Iterator;
import java.util.List;

/**
 * @author TheElectronWill
 */
final class ArrayWriter {
	private static final char[] EMPTY_ARRAY = {'[', ']'}, ELEMENT_SEPARATOR = {',', ' '};

	static void writeArray(List<?> values, CharacterOutput output, TomlWriter writer) {
		if (values.isEmpty()) {
			output.write(EMPTY_ARRAY);
			return;
		}
		output.write('[');
		boolean indent = writer.getIndentArrayElementsPredicate().test(values);
		if (indent) {
			writer.increaseIndentLevel();
		}
		for (Object value : values) {
			if (indent) {// Indents the first element
				output.write(writer.getNewline());
				writer.writeIndent(output);
			}
			output.write(value.toString());
			output.write(ELEMENT_SEPARATOR);
		}
		if (indent) {
			writer.decreaseIndentLevel();
			output.write(writer.getNewline());
		}
		output.write(']');
	}

	private ArrayWriter() {}
}