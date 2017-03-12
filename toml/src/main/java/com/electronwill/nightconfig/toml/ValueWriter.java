package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serialization.CharacterOutput;
import java.time.temporal.Temporal;
import java.util.List;

/**
 * @author TheElectronWill
 */
final class ValueWriter {
	static void writeValue(Object value, CharacterOutput output, TomlWriter writer) {
		//System.out.println("writeValue: (" + value.getClass() + ") " + value);//TODO debug
		if (value instanceof Config) {
			TableWriter.writeInline((Config)value, output, writer);
		} else if (value instanceof List) {
			List<?> list = (List<?>)value;
			if (!list.isEmpty() && list.get(0) instanceof Config) {
				for (Object table : list) {
					TableWriter.writeInline((Config)table, output, writer);
				}
			} else {
				ArrayWriter.writeArray((List<?>)value, output);
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
			//Note: TOML doesn't support null values
			output.write(value.toString());
		}
	}

	private ValueWriter() {}
}
