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
			if (writer.writesLiteral(string)) {
				StringWriter.writeLiteral(string, output);
			} else {
				StringWriter.writeBasic(string, output);
			}
		} else if (value instanceof Temporal) {
			TemporalWriter.write((Temporal)value, output);
		} else if (value instanceof Float || value instanceof Double) {
			double d = (double)value;
			if (Double.isNaN(d)) {
				output.write("nan");
			} else if (d == Double.POSITIVE_INFINITY) {
				output.write("+inf");
			} else if (d == Double.NEGATIVE_INFINITY) {
				output.write("-inf");
			} else {
				output.write(value.toString());
			}
		} else {
			// Note: TOML doesn't support null values
			output.write(value.toString());
		}
	}

	private ValueWriter() {}
}