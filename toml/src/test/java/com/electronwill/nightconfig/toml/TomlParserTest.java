package com.electronwill.nightconfig.toml;

import java.io.StringReader;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
public class TomlParserTest {

	@Test
	public void readFromString() {
		String toml = "string = \"value\"\n"
					  + "integer = 2\n"
					  + "long = 1234567890000\n"
					  + "double = 1.2345678901\n"
					  + "bool_array=[true, false, true, false,] # comment\n"
					  + "[table] #comment	\n"
					  + "	'key' = '\"literal string\"\\n\\t'\n"
					  + "[table.subTable]\n"
					  + "    \"subkey\"=2017-02-25T12:00:01.123456789   \n"
					  + "\r\n# 'key' = 0.2";
		TomlParser parser = new TomlParser();
		TomlConfig parsed = parser.parseConfig(new StringReader(toml));
		System.out.println("parsed: " + parsed);
	}
}