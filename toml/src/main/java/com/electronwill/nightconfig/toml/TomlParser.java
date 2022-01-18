package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.*;

import java.io.Reader;
import java.util.*;

/**
 * A configurable parser of TOML configurations. It is not thread-safe.
 *
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml">TOML specification</a>
 */
public final class TomlParser implements ConfigParser<CommentedConfig> {
	// --- Parser's settings ---
	private int initialStringBuilderCapacity = 16, initialListCapacity = 10;
	private boolean lenientBareKeys = false;
	private boolean lenientSeparators = false;
	private boolean configWasEmpty = false;
	private ParsingMode parsingMode;

	// --- Parser's state for TOML compliance ---
	private final Set<Config> inlineTables = Collections.newSetFromMap(new IdentityHashMap<>());

	void registerInlineTable(Config table) {
		inlineTables.add(table);
	}

	boolean isInlineTable(Config table) {
		return inlineTables.contains(table);
	}

	private void clearParsingState() {
		inlineTables.clear();
	}

	// --- Parser's methods ---
	@Override
	public CommentedConfig parse(Reader reader) {
		configWasEmpty = true;
		return parse(new ReaderInput(reader), TomlFormat.instance().createConfig(), ParsingMode.MERGE);
	}

	@Override
	public void parse(Reader reader, Config destination, ParsingMode parsingMode) {
		if(parsingMode == ParsingMode.REPLACE) {
			configWasEmpty = true;
		}
		parse(new ReaderInput(reader), destination, parsingMode);
	}

	private <T extends Config> T parse(CharacterInput input, T destination, ParsingMode parsingMode) {
		this.parsingMode = parsingMode;
		parsingMode.prepareParsing(destination);
		CommentedConfig commentedConfig = CommentedConfig.fake(destination);
		CommentedConfig rootTable = TableParser.parseNormal(input, this, commentedConfig);
		int next;
		while ((next = input.peek()) != -1) {
			final boolean isArray = (next == '[');
			if (isArray) {
				input.skipPeeks();
			}
			final List<String> path = TableParser.parseTableName(input, this, isArray);
			final int lastIndex = path.size() - 1;
			final String lastKey = path.get(lastIndex);
			final List<String> parentPath = path.subList(0, lastIndex);
			final Config parentConfig = getSubTable(rootTable, parentPath);
			final Map<String, Object> parentMap = (parentConfig != null) ? parentConfig.valueMap()
																		 : null;
			if (hasPendingComment()) {// Handles comments that are before the table declaration
				String comment = consumeComment();
				if (parentConfig instanceof CommentedConfig) {
					List<String> lastPath = Collections.singletonList(lastKey);
					((CommentedConfig)parentConfig).setComment(lastPath, comment);
				}
			}
			if (isArray) {// It's an element of an array of tables
				if (parentMap == null) {
					throw new ParsingException("Cannot create entry "
											   + path
											   + " because of an invalid "
											   + "parent that isn't a table.");
				}
				CommentedConfig table = TableParser.parseNormal(input, this);
				List<CommentedConfig> arrayOfTables = (List)parentMap.get(lastKey);
				if (arrayOfTables == null) {
					arrayOfTables = createList();
					parentMap.put(lastKey, arrayOfTables);
				}
				arrayOfTables.add(table);
			} else {// It's a table
				if (parentMap == null) {
					throw new ParsingException("Cannot create entry "
											   + path
											   + " because of an invalid "
											   + "parent that isn't a table.");
				}
				Object alreadyDeclared = parentMap.get(lastKey);
				if (alreadyDeclared == null) {
					CommentedConfig table = TableParser.parseNormal(input, this);
					parentMap.put(lastKey, table);
				} else {
					if (alreadyDeclared instanceof Config) {
						Config table = (Config)alreadyDeclared;
						checkContainsOnlySubtables(table, path);
						CommentedConfig commentedTable = CommentedConfig.fake(table);
						TableParser.parseNormal(input, this, commentedTable);
					} else if (configWasEmpty) {
						throw new ParsingException("Entry " + path + " has been defined twice.");
					}
				}
			}
		}
		clearParsingState();
		return destination;
	}

	private Config getSubTable(Config parentTable, List<String> path) {
		if (path.isEmpty()) {
			return parentTable;
		}
		Config currentConfig = parentTable;
		for (String key : path) {
			Object value = currentConfig.valueMap().get(key);
			if (value == null) {
				Config sub = TomlFormat.instance().createConfig();
				currentConfig.valueMap().put(key, sub);
				currentConfig = sub;
			} else if (value instanceof Config) {
				currentConfig = (Config)value;
			} else if (value instanceof List) {
				List<?> list = (List<?>)value;
				if (!list.isEmpty() && list.stream().allMatch(Config.class::isInstance)) {// Arrays of tables
					int lastIndex = list.size() - 1;
					currentConfig = (Config)list.get(lastIndex);
				} else {
					return null;
				}
			} else {
				return null;
			}
			if (this.isInlineTable(currentConfig)) {
				// reject modification of inline tables
				throw new ParsingException("Cannot modify an inline table after its creation. Key path: " + path);
			}
		}
		return currentConfig;
	}

	private void checkContainsOnlySubtables(Config table, List<String> path) {
		for (Object value : table.valueMap().values()) {
			if (!(value instanceof Config)) {
				throw new ParsingException("Table with path " + path + " has been declared twice.");
			}
		}
	}

	// --- Getters/setters for the settings ---
	public boolean isLenientWithSeparators() {
		return lenientSeparators;
	}

	/**
	 * Makes this parser lenient (if true) or strict (if false - this is the default) with
	 * key/values separators. In lenient mode, the parser accepts both '=' and ':' between
	 * keys and values. In strict mode, only the standard '=' is accepted.
	 *
	 * @param lenientSeparators true for lenient, false for strict
	 * @return this parser
	 */
	public TomlParser setLenientWithSeparators(boolean lenientSeparators) {
		this.lenientSeparators = lenientSeparators;
		return this;
	}

	public boolean isLenientWithBareKeys() {
		return lenientBareKeys;
	}

	/**
	 * Makes this parser lenient (if true) or strict (if false - this is the default) with bar keys.
	 * In lenient mode, almost all characters are allowed in bare keys. In struct mode, only the
	 * standard A-Za-z0-9_- range is allowed.
	 *
	 * @param lenientBareKeys true for lenient, false for strict
	 * @return this parser
	 */
	public TomlParser setLenientWithBareKeys(boolean lenientBareKeys) {
		this.lenientBareKeys = lenientBareKeys;
		return this;
	}

	public TomlParser setInitialStringBuilderCapacity(int initialStringBuilderCapacity) {
		this.initialStringBuilderCapacity = initialStringBuilderCapacity;
		return this;
	}

	public TomlParser setInitialListCapacity(int initialListCapacity) {
		this.initialListCapacity = initialListCapacity;
		return this;
	}

	@Override
	public ConfigFormat<CommentedConfig> getFormat() {
		return TomlFormat.instance();
	}

	boolean configWasEmpty() {
		return configWasEmpty;
	}

	ParsingMode getParsingMode() {
		return parsingMode;
	}

	// --- Configured objects creation ---
	<T> List<T> createList() {
		return new ArrayList<>(initialListCapacity);
	}

	CharsWrapper.Builder createBuilder() {
		return new CharsWrapper.Builder(initialStringBuilderCapacity);
	}

	// --- Comment management ---
	private String currentComment;

	boolean hasPendingComment() {
		return currentComment != null;
	}

	String consumeComment() {
		String comment = currentComment;
		currentComment = null;
		return comment;
	}

	void setComment(CharsWrapper comment) {
		if (comment != null) {
			if (currentComment == null) {
				currentComment = comment.toString();
			} else {
				currentComment = currentComment + '\n' + comment.toString();
			}
		}
	}

	void setComment(List<CharsWrapper> commentsList) {
		CharsWrapper.Builder builder = new CharsWrapper.Builder(32);
		if (!commentsList.isEmpty()) {
			Iterator<CharsWrapper> it = commentsList.iterator();
			builder.append(it.next());
			while (it.hasNext()) {
				builder.append('\n');
				builder.append(it.next());
			}
			setComment(builder.build());// Appends the builder to the current comment if any
		}
	}
}