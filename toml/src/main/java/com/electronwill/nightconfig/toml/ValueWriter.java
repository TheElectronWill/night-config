package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.impl.CharacterOutput;
import com.electronwill.nightconfig.core.io.WritingException;

import java.time.temporal.Temporal;
import java.util.List;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * @author TheElectronWill
 */
final class ValueWriter {
	private static void writeString(String string, CharacterOutput output, TomlWriter writer) {
		if (writer.writesLiteral(string)) {
			StringWriter.writeLiteral(string, output);
		} else {
			StringWriter.writeBasic(string, output);
		}
	}
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
		} else if (value instanceof CharSequence) {// String
			writeString(value.toString(), output, writer);
		} else if (value instanceof Enum) {// Enum value
			writeString(((Enum<?>)value).name(), output, writer);
		} else if (value instanceof Temporal) {// Date or DateTime
			TemporalWriter.write((Temporal)value, output);
		} else if (value instanceof Float || value instanceof Double) {// Floating-point number
			double d = ((Number)value).doubleValue();
			if (Double.isNaN(d)) {
				output.write("nan");
			} else if (d == Double.POSITIVE_INFINITY) {
				output.write("+inf");
			} else if (d == Double.NEGATIVE_INFINITY) {
				output.write("-inf");
			} else {
				output.write(value.toString());
			}
		} else if (value instanceof Number || value instanceof Boolean) {
			output.write(value.toString());
		} else if (value == null || value == NULL_OBJECT) {
			throw new WritingException("TOML doesn't support null values");
		} else {
			throw new WritingException("Unsupported value type: " + value.getClass());
		}
	}

	private ValueWriter() {}
}