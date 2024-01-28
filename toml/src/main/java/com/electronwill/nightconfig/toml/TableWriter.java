package com.electronwill.nightconfig.toml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig.Entry;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.CharacterOutput;
import com.electronwill.nightconfig.core.io.WritingException;

/**
 * @author TheElectronWill
 */
final class TableWriter {

	private static final char[] KEY_VALUE_SEPARATOR = { ' ', '=', ' ' },
			INLINE_ENTRY_SEPARATOR = ArrayWriter.ELEMENT_SEPARATOR,
			ARRAY_OF_TABLES_NAME_BEGIN = { '[', '[' },
			ARRAY_OF_TABLES_NAME_END = { ']', ']' },
			TABLE_NAME_BEGIN = { '[' },
			TABLE_NAME_END = { ']' };

	static void writeInline(UnmodifiableConfig config, CharacterOutput output, TomlWriter writer) {
		output.write('{');
		Iterator<? extends UnmodifiableConfig.Entry> iterator = config.entrySet().iterator();
		while (iterator.hasNext()) {
			UnmodifiableConfig.Entry entry = iterator.next();
			String key = entry.getKey();
			Object value = entry.getValue();
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

	static void writeTopLevel(UnmodifiableConfig config, List<String> configPath,
			CharacterOutput output, TomlWriter writer) {

		UnmodifiableCommentedConfig commentedConfig = UnmodifiableCommentedConfig.fake(config);

		writeWithHeader(commentedConfig, null, false, false, configPath, output, writer);
	}

	static class OrganizedTable {
		List<UnmodifiableCommentedConfig.Entry> simples, subTables, arraysOfTables;
		String comment; // comment on the table itself

		OrganizedTable(String comment, List<Entry> simpleEntries, List<Entry> tablesEntries,
				List<Entry> tableArraysEntries) {
			this.comment = (comment == null) ? "" : comment;
			this.simples = simpleEntries;
			this.subTables = tablesEntries;
			this.arraysOfTables = tableArraysEntries;
		}

		boolean canBeSkipped() {
			return (comment.isEmpty() || !arraysOfTables.isEmpty()) // we can write the comment before the first array of tables
					&& simples.isEmpty()
					&& (!subTables.isEmpty() || !arraysOfTables.isEmpty());
		}

	}

	/** Separate the table in three groups: simple values, sub-configurations, and arrays of tables. */
	static OrganizedTable prepareTable(UnmodifiableCommentedConfig config, String comment,
			TomlWriter writer) {
		List<Entry> simpleEntries = new ArrayList<>();
		List<Entry> tablesEntries = new ArrayList<>();
		List<Entry> tableArraysEntries = new ArrayList<>();

		for (Entry entry : config.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof UnmodifiableCommentedConfig) {
				UnmodifiableConfig sub = (UnmodifiableConfig) value;
				if (writer.writesInline(sub)) {
					simpleEntries.add(entry);
				} else {
					tablesEntries.add(entry);
				}
			} else if (value instanceof List) {
				List<?> list = (List<?>) value;
				if (!list.isEmpty()
						&& list.stream().allMatch(UnmodifiableConfig.class::isInstance)) {
					tableArraysEntries.add(entry);
				} else {
					simpleEntries.add(entry);
				}
			} else {
				simpleEntries.add(entry);
			}
		}

		return new OrganizedTable(comment, simpleEntries, tablesEntries, tableArraysEntries);
	}

	private static void writeWithHeader(
			UnmodifiableCommentedConfig config,
			String tableComment,
			boolean inArrayOfTables,
			boolean tableHeader,
			List<String> configPath,
			CharacterOutput output, TomlWriter writer) {

		// First, analyze the table so that we can skip useless intermediate levels and create a nice result
		OrganizedTable table = prepareTable(config, tableComment, writer);
		boolean hasSubTables = !table.subTables.isEmpty();

		if (table.canBeSkipped() && writer.isHidingRedundantLevels()) {
			writer.increaseIndentLevel();

			// subtables
			writeSubTables(table, configPath, output, writer);

			// sub arrays of tables, if there is a comment we write it before them
			if (!table.comment.isEmpty()) {
				writer.writeIndentedComment(tableComment, output);
			}
			writeArraysOfTables(table, configPath, output, writer);
			writer.decreaseIndentLevel();
		} else {
			// header
			if (inArrayOfTables) {
				writeTableArrayName(configPath, output, writer);
				writer.writeNewline(output);
			} else if (tableHeader) {
				writeTableName(configPath, output, writer);
				writer.writeNewline(output);
			}

			// body
			writer.increaseIndentLevel();
			for (Entry entry : table.simples) {
				writer.writeIndentedComment(entry.getComment(), output);
				writer.writeIndentedKey(entry.getKey(), output);
				output.write(KEY_VALUE_SEPARATOR);
				ValueWriter.write(entry.getValue(), output, writer);
				writer.writeNewline(output);
			}

			if (hasSubTables) {
				writer.writeNewline(output);
			}
			writeSubTables(table, configPath, output, writer);
			writeArraysOfTables(table, configPath, output, writer);
			writer.decreaseIndentLevel();
			// end of body
		}
	}

	private static void writeSubTables(OrganizedTable table, List<String> configPath, CharacterOutput output, TomlWriter writer) {
		boolean hasArraysOfTables = !table.arraysOfTables.isEmpty();
		for (Iterator<Entry> it = table.subTables.iterator(); it.hasNext();) {
			Entry entry = it.next();
			UnmodifiableCommentedConfig sub = UnmodifiableCommentedConfig
					.fake((UnmodifiableConfig) entry.getRawValue());
			configPath.add(entry.getKey());
			writeWithHeader(sub, entry.getComment(), false, true, configPath, output, writer);
			configPath.remove(configPath.size() - 1);

			// separate the tables
			if (hasArraysOfTables || it.hasNext()) {
				writer.writeNewline(output);
			}
		}
	}

	private static void writeArraysOfTables(OrganizedTable table, List<String> configPath, CharacterOutput output, TomlWriter writer) {
		for (Iterator<Entry> it = table.arraysOfTables.iterator(); it.hasNext();) {
			Entry entry = it.next();
			configPath.add(entry.getKey());
			List<? extends UnmodifiableConfig> array = (List) entry.getRawValue();
			for (UnmodifiableConfig sub : array) {
				writeWithHeader(UnmodifiableCommentedConfig.fake(sub), entry.getComment(), true,
						true, configPath, output, writer);
			}
			configPath.remove(configPath.size() - 1);

			// separate the arrays of tables
			if (it.hasNext()) {
				writer.writeNewline(output);
			}
		}
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

	private TableWriter() {
	}
}
