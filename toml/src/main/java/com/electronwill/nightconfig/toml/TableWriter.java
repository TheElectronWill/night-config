package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.CharacterOutput;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.utils.FakeUnmodifiableCommentedConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
final class TableWriter {

	private static final char[] KEY_VALUE_SEPARATOR = {' ', '=', ' '},
								INLINE_ENTRY_SEPARATOR = ArrayWriter.ELEMENT_SEPARATOR,
								ARRAY_OF_TABLES_NAME_BEGIN = {'[', '['},
								ARRAY_OF_TABLES_NAME_END = {']', ']'},
								TABLE_NAME_BEGIN = {'['},
								TABLE_NAME_END = {']'};

	static void writeInline(UnmodifiableConfig config, CharacterOutput output, TomlWriter writer) {
		output.write('{');
		Iterator<Map.Entry<String, Object>> iterator = config.valueMap().entrySet().iterator();
		while (iterator.hasNext()) {
			final Map.Entry<String, Object> entry = iterator.next();
			final String key = entry.getKey();
			final Object value = entry.getValue();
			// Comments aren't written in an inline table
			writer.writeKey(key, output);
			output.write(KEY_VALUE_SEPARATOR);
			ValueWriter.write(value, output, writer);
			if (iterator.hasNext()) {
				output.write(INLINE_ENTRY_SEPARATOR);
			}
		}
		output.write('}');
	}

	static void writeNormal(UnmodifiableConfig config, List<String> configPath,
							CharacterOutput output, TomlWriter writer) {
		UnmodifiableCommentedConfig commentedConfig = UnmodifiableCommentedConfig.fake(config);
		writeNormal(commentedConfig, configPath, output, writer);
	}

	private static void writeNormal(UnmodifiableCommentedConfig config, List<String> configPath,
									CharacterOutput output, TomlWriter writer) {
		List<UnmodifiableCommentedConfig.Entry> tablesEntries = new ArrayList<>();
		List<UnmodifiableCommentedConfig.Entry> tableArraysEntries = new ArrayList<>();

		// Writes the "simple" values:
		writer.increaseIndentLevel();// Indent++
		for (UnmodifiableCommentedConfig.Entry entry : config.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
			final String comment = entry.getComment();
			if (value instanceof UnmodifiableConfig &&
				!writer.writesInline((UnmodifiableConfig)value)) {
				tablesEntries.add(entry);
				continue;
			} else if (value instanceof List) {
				List<?> list = (List<?>)value;
				if (!list.isEmpty() && list.stream().allMatch(UnmodifiableConfig.class::isInstance)) {
					tableArraysEntries.add(entry);
					continue;
				}
			}
			writer.writeComment(comment, output);// Writes the comment above the key
			writer.writeIndent(output);// Indents the line.
			writer.writeKey(key, output);
			output.write(KEY_VALUE_SEPARATOR);
			ValueWriter.write(value, output, writer);
			writer.writeNewline(output);
		}
		writer.writeNewline(output);

		// Writes the tables:
		for (UnmodifiableCommentedConfig.Entry entry : tablesEntries) {
			// Writes the comment, if there is one
			writer.writeComment(entry.getComment(), output);

			// Writes the table declaration
			configPath.add(entry.getKey());// path level ++
			writeTableName(configPath, output, writer);
			writer.writeNewline(output);

			// Writes the table's content
			writeNormal(entry.<UnmodifiableConfig>getValue(), configPath, output, writer);
			configPath.remove(configPath.size() - 1);// path level --
		}

		// Writes the arrays of tables:
		for (UnmodifiableCommentedConfig.Entry entry : tableArraysEntries) {
			// Writes the comment, if there is one
			writer.writeComment(entry.getComment(), output);

			// Writes the tables
			configPath.add(entry.getKey());// path level ++
			List<Config> tableArray = entry.getValue();
			for (UnmodifiableConfig table : tableArray) {
				writeTableArrayName(configPath, output, writer);
				writer.writeNewline(output);
				writeNormal(table, configPath, output, writer);
			}
			configPath.remove(configPath.size() - 1);// path level --
		}
		writer.decreaseIndentLevel();// Indent--
	}

	private static void writeTableArrayName(List<String> name, CharacterOutput output,
											TomlWriter writer) {
		writeTableName(name, output, writer, ARRAY_OF_TABLES_NAME_BEGIN, ARRAY_OF_TABLES_NAME_END);
	}

	private static void writeTableName(List<String> name, CharacterOutput output,
									   TomlWriter writer) {
		writeTableName(name, output, writer, TABLE_NAME_BEGIN, TABLE_NAME_END);
	}

	private static void writeTableName(List<String> name, CharacterOutput output, TomlWriter writer,
									   char[] begin, char[] end) {
		if (name.isEmpty()) {
			throw new WritingException("Invalid empty table name.");
		}
		writer.writeIndent(output);// Indents the line.
		output.write(begin);
		Iterator<String> it = name.iterator();
		writer.writeKey(it.next(), output);// Writes the first part
		while (it.hasNext()) {
			output.write('.');// part separator
			writer.writeKey(it.next(), output);
		}
		output.write(end);
	}

	private TableWriter() {}
}
