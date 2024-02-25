package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharacterOutput;
import java.util.Iterator;
import java.util.List;

/**
 * @author TheElectronWill
 */
final class ArrayWriter {
	private static final char[] EMPTY_ARRAY = {'[', ']'};
	static final char[] ELEMENT_SEPARATOR = {',', ' '};

	/**
	 * Writes a plain array, not an array of tables.
	 */
	static void write(List<?> values, CharacterOutput output, TomlWriter writer) {
		if (values.isEmpty()) {
			output.write(EMPTY_ARRAY);
			return;
		}
		output.write('[');
		boolean indent = writer.writesIndented(values);
		if (indent) {
			writer.increaseIndentLevel();
		}
		Iterator<?> iterator = values.iterator();
		for (boolean hasNext = iterator.hasNext(); hasNext; ) {
			if (indent) {
				writer.writeNewline(output);
				writer.writeIndent(output);
			}
			Object value = iterator.next();
			ValueWriter.write(value, output, writer);
			if ((hasNext = iterator.hasNext())) {
				if (indent) {
					output.write(',');
				} else {
					output.write(ELEMENT_SEPARATOR);
				}
			}
		}
		if (indent) {
			writer.decreaseIndentLevel();
			writer.writeNewline(output);
			writer.writeIndent(output);
		}
		output.write(']');
	}

	private ArrayWriter() {}
}