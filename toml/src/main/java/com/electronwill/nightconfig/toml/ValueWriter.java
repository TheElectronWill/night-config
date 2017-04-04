package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.CharacterOutput;
import java.time.temporal.Temporal;
import java.util.List;

/**
 * @author TheElectronWill
 */
final class ValueWriter {
	/**
	 * Writes a value. This method calls the correct writing method based on the value's type.
	 */
	static void write(Object value, CharacterOutput output, TomlWriter writer) {
		if (value instanceof Config) {
			TableWriter.writeInline((Config)value, output, writer);
		} else if (value instanceof List) {
			List<?> list = (List<?>)value;
			if (!list.isEmpty() && list.get(0) instanceof Config) {// Array of tables
				for (Object table : list) {
					TableWriter.writeInline((Config)table, output, writer);
				}
			} else {// Normal array
				ArrayWriter.write((List<?>)value, output, writer);
			}
		} else if (value instanceof String) {
			String string = (String)value;
			if (writer.getWriteStringLiteralPredicate().test(string)) {
				StringWriter.writeLiteral(string, output);
			} else {
				StringWriter.writeBasic(string, output);
			}
		} else if (value instanceof Temporal) {
			TemporalWriter.write((Temporal)value, output);
		} else {
			// Note: TOML doesn't support null values
			output.write(value.toString());
		}
	}

	private ValueWriter() {}
}