package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.serialization.CharacterInput;
import com.electronwill.nightconfig.core.serialization.ParsingException;
import java.util.ArrayList;
import java.util.List;

/**
 * A configurable parser of TOML configurations.
 *
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml">TOML specification</a>
 */
public final class TomlParser {
	private int initialStringBuilderCapacity = 16, initialListCapacity = 10;
	private boolean lenientBareKeys = false;

	/**
	 * Parses a TOML configuration.
	 *
	 * @param input the input containing the data
	 * @return a new TomlConfig
	 * @throws ParsingException if an error occur
	 */
	public TomlConfig parseConfiguration(CharacterInput input) {
		TomlConfig rootTable = TableParser.parseNormal(input, this);
		int next;
		while ((next = input.peek()) != -1) {
			if (next == '[') {//[[ element of an array of tables
				input.skipPeeks();
				List<String> key = TableArrayParser.parseElementName(input, this);
				TomlConfig table = TableParser.parseNormal(input, this);
				List<TomlConfig> arrayOfTables = rootTable.getList(key);
				if (arrayOfTables == null) {
					arrayOfTables = new ArrayList<>(initialListCapacity);
					rootTable.setList(key, arrayOfTables);
				}
				arrayOfTables.add(table);
			} else {//[ a table
				List<String> key = TableParser.parseTableName(input, this);
				TomlConfig table = TableParser.parseNormal(input, this);
				rootTable.setConfig(key, table);
			}
		}
		return rootTable;
	}

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
