package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.CharacterOutput;
import com.electronwill.nightconfig.core.io.WritingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
final class TableWriter {

	private static final char[] KEY_VALUE_SEPARATOR = {' ', '=', ' '},
								AFTER_INLINE_ENTRY = {',', ' '},
								ARRAY_OF_TABLES_NAME_BEGIN = {'[', '['},
								ARRAY_OF_TABLES_NAME_END = {']', ']'},
								TABLE_NAME_BEGIN = {'['},
								TABLE_NAME_END = {']'};

	static void writeInline(Config config, CharacterOutput output, TomlWriter writer) {
		output.write('{');
		for (Map.Entry<String, Object> entry : config.asMap().entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
			if (Toml.isValidBareKey(key, writer.isLenientWithBareKeys())) {
				output.write(key);
			} else {
				StringWriter.writeBasic(key, output);
			}
			output.write(KEY_VALUE_SEPARATOR);
			ValueWriter.writeValue(value, output, writer);
			output.write(AFTER_INLINE_ENTRY);
		}
		output.write('}');
	}

	private static void writeNormal(Config config, List<String> configKey, CharacterOutput output,
									TomlWriter writer) {
		List<Map.Entry<String, Object>> tablesEntries = new ArrayList<>();
		List<Map.Entry<String, Object>> tableArraysEntries = new ArrayList<>();

		// Writes the "simple" values:
		writer.increaseIndentLevel();// Indent++
		for (Map.Entry<String, Object> entry : config.asMap().entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
			if (value instanceof Config && !writer.getWriteTableInlinePredicate()
												  .test((Config)value)) {
				tablesEntries.add(entry);
				continue;
			} else if (value instanceof List) {
				List<?> list = (List<?>)value;
				if (!list.isEmpty() && list.get(0) instanceof Config) {
					tableArraysEntries.add(entry);
					continue;
				}
			}
			writer.writeIndent(output);// Indents the line.
			if (Toml.isValidBareKey(key, writer.isLenientWithBareKeys())) {
				output.write(key);
			} else {
				StringWriter.writeBasic(key, output);
			}
			output.write(KEY_VALUE_SEPARATOR);
			ValueWriter.writeValue(value, output, writer);
			output.write(writer.getNewline());

		}
		output.write(writer.getNewline());

		// Writes the tables:
		for (Map.Entry<String, Object> entry : tablesEntries) {
			configKey.add(entry.getKey());
			writeTableName(configKey, output, writer);
			output.write(writer.getNewline());
			writeNormal((Config)entry.getValue(), configKey, output, writer);
			configKey.remove(configKey.size() - 1);
		}

		// Writes the arrays of tables:
		for (Map.Entry<String, Object> entry : tableArraysEntries) {
			configKey.add(entry.getKey());
			List<Config> tableArray = (List<Config>)entry.getValue();
			for (Config table : tableArray) {
				writeTableArrayName(configKey, output, writer);
				output.write(writer.getNewline());
				writeNormal(table, configKey, output, writer);
			}
			configKey.remove(configKey.size() - 1);
		}
		writer.decreaseIndentLevel();// Indent--
	}

	private static void writeTableArrayName(List<String> name, CharacterOutput output,
											TomlWriter writer) {
		writeTableName(name, output, writer, ARRAY_OF_TABLES_NAME_BEGIN, ARRAY_OF_TABLES_NAME_END);
	}

	private static void writeTableName(List<String> name, CharacterOutput output, TomlWriter writer) {
		writeTableName(name, output, writer, TABLE_NAME_BEGIN, TABLE_NAME_END);
	}

	private static void writeTableName(List<String> name, CharacterOutput output, TomlWriter writer,
									   char[] begin, char[] end) {
		if (name.isEmpty()) {
			throw new WritingException("Invalid empty table name.");
		}
		writer.writeIndent(output);//Indents the line.
		output.write(begin);
		Iterator<String> it = name.iterator();
		while (true) {
			String part = it.next();
			if (Toml.isValidBareKey(part, writer.isLenientWithBareKeys())) {
				output.write(part);
			} else if (writer.getWriteStringLiteralPredicate().test(part)) {
				StringWriter.writeLiteral(part, output);
			} else {
				StringWriter.writeBasic(part, output);
			}
			if (it.hasNext()) {
				output.write('.');
			} else {
				break;
			}
		}
		output.write(end);
	}

	static void writeSmartly(Config config, List<String> key, CharacterOutput output,
							 TomlWriter writer) {
		if (writer.getWriteTableInlinePredicate().test(config)) {
			writeInline(config, output, writer);
		} else {
			writeNormal(config, key, output, writer);
		}
	}

	private TableWriter() {}
}