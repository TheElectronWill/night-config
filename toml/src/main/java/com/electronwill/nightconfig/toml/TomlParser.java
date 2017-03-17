package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharacterInput;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ReaderInput;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * A configurable parser of TOML configurations.
 *
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml">TOML specification</a>
 */
public final class TomlParser implements ConfigParser<TomlConfig> {
	private int initialStringBuilderCapacity = 16, initialListCapacity = 10;
	private boolean lenientBareKeys = false;

	@Override
	public TomlConfig parseConfig(Reader reader) {
		return parseConfig(new ReaderInput(reader), new TomlConfig());
	}

	@Override
	public void parseConfig(Reader reader, TomlConfig destination) {
		parseConfig(new ReaderInput(reader), destination);
	}

	private TomlConfig parseConfig(CharacterInput input, TomlConfig destination) {
		TomlConfig rootTable = TableParser.parseNormal(input, this, destination);
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
				rootTable.setValue(key, table);
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
