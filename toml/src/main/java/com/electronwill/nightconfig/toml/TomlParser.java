package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ReaderInput;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A configurable parser of TOML configurations.
 *
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml">TOML specification</a>
 */
public final class TomlParser implements ConfigParser<TomlConfig, Config> {
	// --- Parser's settings ---
	private int initialStringBuilderCapacity = 16, initialListCapacity = 10;
	private boolean lenientBareKeys = false;

	// --- Parser's methods ---
	@Override
	public TomlConfig parse(Reader reader) {
		return parse(new ReaderInput(reader), new TomlConfig());
	}

	@Override
	public void parse(Reader reader, Config destination) {
		parse(new ReaderInput(reader), destination);
	}

	private <T extends Config> T parse(CharacterInput input, T destination) {
		T rootTable = TableParser.parseNormal(input, this, destination);
		int next;
		while ((next = input.peek()) != -1) {
			if (next == '[') {//[[ element of an array of tables
				input.skipPeeks();
				List<String> key = TableArrayParser.parseElementName(input, this);
				TomlConfig table = TableParser.parseNormal(input, this);
				List<TomlConfig> arrayOfTables = rootTable.getValue(key);
				if (arrayOfTables == null) {
					arrayOfTables = new ArrayList<>(initialListCapacity);
					rootTable.setValue(key, arrayOfTables);
				}
				arrayOfTables.add(table);
			} else {//[ a table
				List<String> key = TableParser.parseTableName(input, this);
				TomlConfig table = TableParser.parseNormal(input, this);
				Object alreadyDefined = rootTable.getValue(key);
				if (alreadyDefined instanceof TomlConfig) {
					TomlConfig adTable = (TomlConfig)alreadyDefined;
					for (Map.Entry<String, Object> entry : adTable.asMap().entrySet()) {
						if (!(entry.getValue() instanceof TomlConfig)) {
							throw new ParsingException("Invalid TOML data: table with path "
													   + key
													   + " has been declared twice.");
						}
					}
					for (Map.Entry<String, Object> entry : table.asMap().entrySet()) {
						if (adTable.asMap().containsKey(entry.getKey())) {
							throw new ParsingException("Invalid TOML data: entry \""
													   + entry.getKey()
													   + "\" has been defined twice in path "
													   + key);
						} else {
							adTable.asMap().put(entry.getKey(), entry.getValue());
						}
					}
					adTable.asMap().putAll(table.asMap());// Merge the tables
				} else if (alreadyDefined != null) {
					throw new ParsingException(
							"Invalid TOML data: entry " + key + " has been defined twice.");
				} else {
					rootTable.setValue(key, table);
				}
			}
		}
		return rootTable;
	}

	// --- Getters/setters for the settings ---
	public boolean isLenientWithBareKeys() {
		return lenientBareKeys;
	}

	public void setLenientWithBareKeys(boolean lenientBareKeys) {
		this.lenientBareKeys = lenientBareKeys;
	}

	public int getInitialStringBuilderCapacity() {
		return initialStringBuilderCapacity;
	}

	public void setInitialStringBuilderCapacity(int initialStringBuilderCapacity) {
		this.initialStringBuilderCapacity = initialStringBuilderCapacity;
	}

	public int getInitialListCapacity() {
		return initialListCapacity;
	}

	public void setInitialListCapacity(int initialListCapacity) {
		this.initialListCapacity = initialListCapacity;
	}
}