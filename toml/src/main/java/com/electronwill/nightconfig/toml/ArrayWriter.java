package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharacterOutput;
import java.util.List;

/**
 * @author TheElectronWill
 */
final class ArrayWriter {
	private static final char[] EMPTY_ARRAY = {'[', ']'}, ELEMENT_SEPARATOR = {',', ' '};

	/**
	 * Writes a plain array, not an array of tables.
	 */
	static void write(List<?> values, CharacterOutput output, TomlWriter writer) {
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
				writer.writeNewline(output);
				writer.writeIndent(output);
			}
			ValueWriter.write(value, output, writer);
			output.write(ELEMENT_SEPARATOR);
		}
		if (indent) {
			writer.decreaseIndentLevel();
			writer.writeNewline(output);
		}
		output.write(']');
	}

	private ArrayWriter() {}
}