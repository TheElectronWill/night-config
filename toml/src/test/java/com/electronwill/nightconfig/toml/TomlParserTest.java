package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.ParsingException;
import java.io.StringReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * @author TheElectronWill
 */
public class TomlParserTest {

	private static void assertThrows(Class<? extends Exception> exceptionClass,
									 Executable executable) {
		Exception exception = Assertions.assertThrows(exceptionClass, executable);
		System.out.println("Got expected exception: " + exception);
	}

	private static void parseAndPrint(String tomlString) {
		TomlParser parser = new TomlParser();
		TomlConfig parsed = parser.parseConfig(new StringReader(tomlString));
		System.out.println("parsed: " + parsed);
	}

	@Test
	public void readValidString() {
		String toml = "string = \"value\"\n"
					  + "integer = 2\n"
					  + "long = 1234567890000\n"
					  + "double = 1.2345678901\n"
					  + "bool_array=[true, false, true, false,] # comment\n"
					  + "[table] #comment	\n"
					  + "	'key' = '\"literal string\"\\n\\t'\n"
					  + "[table.subTable.subDefinedFirst]\n"
					  + "   test = 'this is valid TOML'\n"
					  + "[table.subTable]\n"
					  + "    \"subkey\"=2017-02-25T12:00:01.123456789   \n";
		parseAndPrint(toml);
	}

	@Test
	public void readInvalidString() {
		assertThrows(ParsingException.class, this::testAlreadyDefinedTable);
		assertThrows(ParsingException.class, this::testAlreadyDefinedTable2);
		assertThrows(ParsingException.class, this::testAlreadyDefinedKey);
	}

	private void testAlreadyDefinedTable() {
		String toml = "string = \"value\"\n"
					  + "[table.subTable.subDefinedFirst]\n"
					  + "   test = 'success'\n"
					  + "[table.subTable]\n"
					  + "    subDefinedFirst = {}"
					  + "\r\n# 'key' = 0.2";
		parseAndPrint(toml);
	}

	private void testAlreadyDefinedTable2() {
		String toml = "string = \"value\"\n"
					  + "[table.subTable]\n"
					  + "   test = 'success'\n"
					  + "[table.subTable]\n"
					  + "   subDefinedFirst = {}";
		parseAndPrint(toml);
	}

	private void testAlreadyDefinedKey() {
		String toml = "string = \"value\"\n"
					  + "test = 'success'\n"
					  + "test = 'already defined!'";
		parseAndPrint(toml);
	}

}