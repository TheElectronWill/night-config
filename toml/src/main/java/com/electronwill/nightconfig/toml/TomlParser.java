package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.serialization.CharacterInput;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TheElectronWill
 */
public final class TomlParser {
	private final CharacterInput input;

	public TomlParser(CharacterInput input) {
		this.input = input;
	}

	public TomlConfig parseConfiguration() {
		TomlConfig rootTable = TableParser.parseNormal(input);
		int next;
		while ((next = input.peek()) != -1) {
			if (next == '[') {//[[ element of an array of tables
				input.skipPeeks();
				List<String> key = TableArrayParser.parseElementName(input);
				TomlConfig table = TableParser.parseNormal(input);
				List<TomlConfig> arrayOfTables = rootTable.getList(key);
				if (arrayOfTables == null) {
					arrayOfTables = new ArrayList<>();
					rootTable.setList(key, arrayOfTables);
				}
				arrayOfTables.add(table);
			} else {//[ a table
				List<String> key = TableParser.parseTableName(input);
				TomlConfig table = TableParser.parseNormal(input);
				rootTable.setConfig(key, table);
			}
		}
		return rootTable;
	}

}
