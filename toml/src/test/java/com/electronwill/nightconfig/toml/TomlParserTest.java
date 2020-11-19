package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.TestEnum;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author TheElectronWill
 */
public class TomlParserTest {

	private static void parseAndPrint(String tomlString) {
		TomlParser parser = new TomlParser();
		CommentedConfig parsed = parser.parse(new StringReader(tomlString));
		System.out.println("parsed: " + parsed);
	}

	@Test
	public void readWriteReadAgain() {
		File file = new File("test.toml");
		CommentedConfig parsed = new TomlParser().parse(file, FileNotFoundAction.THROW_ERROR);

		System.out.println("--- parsed --- \n" + parsed);
		System.out.println("--------------------------------------------");
		assertNull(parsed.getComment("without_comment"));
		assertNotNull(parsed.getComment("with_comments"));
		assertTrue(parsed.getComment("with_comments").contains("\n"));
		assertEquals(TestEnum.A, parsed.getEnum("enum", TestEnum.class));

		java.io.StringWriter sw = new StringWriter();
		TomlWriter writer = new TomlWriter();
		writer.write(parsed, sw);
		System.out.println("--- written --- \n" + sw);
		System.out.println("--------------------------------------------");

		CommentedConfig reparsed = new TomlParser().parse(new StringReader(sw.toString()));
		System.out.println("--- reparsed --- \n" + reparsed);
		assertEquals(parsed, reparsed);
	}

	@Test
	public void testIterationOrder() {
		File file = new File("test.toml");
		CommentedConfig parsed = TomlFormat.newConfig(Config.getDefaultMapCreator(false, true));
		new TomlParser().parse(file, parsed, ParsingMode.MERGE, FileNotFoundAction.THROW_ERROR);
		assertTrue(parsed.valueMap() instanceof LinkedHashMap);
		assertTrue(parsed.<CommentedConfig>get("inline_table").valueMap() instanceof LinkedHashMap);
		assertTrue(parsed.<CommentedConfig>get("table").valueMap() instanceof LinkedHashMap);
		assertTrue(parsed.<CommentedConfig>get("table").<CommentedConfig>get("subTable").valueMap() instanceof LinkedHashMap);
		assertTrue(parsed.<CommentedConfig>get("array").<List<CommentedConfig>>get("ofTables").get(0).valueMap() instanceof LinkedHashMap);
	}

	@Test
	public void readInvalidString() {
		assertThrows(ParsingException.class, this::testAlreadyDefinedTable);
		assertThrows(ParsingException.class, this::testAlreadyDefinedTable2);
		assertThrows(ParsingException.class, this::testAlreadyDefinedKey);
		assertThrows(ParsingException.class, this::testAlreadyDefinedKeyInline);
		assertThrows(ParsingException.class, this::testInvalidKeyValueSeparator);
		assertThrows(ParsingException.class, this::testInvalidArrayValueSeparator);
		assertThrows(ParsingException.class, this::testInvalidInlineEntrySeparator);
		assertThrows(ParsingException.class, this::testInvalidNotAComment);
		assertThrows(ParsingException.class, this::testInvalidNotAComment2);
		assertThrows(ParsingException.class, this::testInvalidUnquotedString);
		assertThrows(ParsingException.class, this::testInvalidTableDeclaration);
		assertThrows(ParsingException.class, this::testInvalidTableDeclaration2);
		assertThrows(ParsingException.class, this::testInvalidTableDeclaration3);
		assertThrows(ParsingException.class, this::testInvalidTableDeclaration4);
		assertThrows(ParsingException.class, this::testInvalidTableDeclaration5);
		assertThrows(ParsingException.class, this::testInvalidTableDeclaration6);
		assertThrows(ParsingException.class, this::testInvalidTableDeclaration7);
		assertThrows(ParsingException.class, this::testInvalidTableArrayDeclaration);
		assertThrows(ParsingException.class, this::testInvalidTableArrayDeclaration2);
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
		String toml = "string = \"value\"\n" + "test = 'success'\n" + "test = 'already defined!'";
		parseAndPrint(toml);
	}

	private void testAlreadyDefinedKeyInline() {
		String toml = "string = \"value\"\n"
					  + "inline = {test = 'success', test = 'already defined!'}";
		parseAndPrint(toml);
	}

	private void testInvalidKeyValueSeparator() {
		String toml = "string : \"value\"\n";
		parseAndPrint(toml);
	}

	private void testInvalidArrayValueSeparator() {
		String toml = "array = [0,1,2,3;4]\n";
		parseAndPrint(toml);
	}

	private void testInvalidNotAComment() {
		String toml = "value = 'literal string' this is not a real comment";
		parseAndPrint(toml);
	}

	private void testInvalidNotAComment2() {
		String toml = "value = 2.7 this is not a real comment";
		parseAndPrint(toml);
	}

	private void testInvalidUnquotedString() {
		String toml = "string = this is invalid";
		parseAndPrint(toml);
	}

	private void testInvalidInlineEntrySeparator() {
		String toml = "inlineTable = {a = 1; b = 2}\n";
		parseAndPrint(toml);
	}

	private void testInvalidTableDeclaration() {
		String toml = "[missing.closing.bracket \n";
		parseAndPrint(toml);
	}

	private void testInvalidTableDeclaration2() {
		String toml = "[]\n";
		parseAndPrint(toml);
	}

	private void testInvalidTableDeclaration3() {
		String toml = "[.]\n";
		parseAndPrint(toml);
	}

	private void testInvalidTableDeclaration4() {
		String toml = "[a.]\n";
		parseAndPrint(toml);
	}

	private void testInvalidTableDeclaration5() {
		String toml = "[.a]\n";
		parseAndPrint(toml);
	}

	private void testInvalidTableDeclaration6() {
		String toml = "[a.b..'']\n";
		parseAndPrint(toml);
	}

	private void testInvalidTableDeclaration7() {
		String toml = "[ 	]\n";
		parseAndPrint(toml);
	}

	private void testInvalidTableArrayDeclaration() {
		String toml = "[[missing.closing.brackets \n";
		parseAndPrint(toml);
	}

	private void testInvalidTableArrayDeclaration2() {
		String toml = "[[missing.closing.bracket] \n";
		parseAndPrint(toml);
	}

}