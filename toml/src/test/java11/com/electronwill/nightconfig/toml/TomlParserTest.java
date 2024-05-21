package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.sharedtests.TestEnum;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.AdditionalCharsets;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Arrays;

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
	public void utf8WithByteOrderMark() {
		var parser = new TomlParser();
		byte[] utf8WithBom = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'k', '=', '1' };
		byte[] utf8WithoutBom = { 'k', '=', '1' };
		var config = parseBytes(parser, utf8WithBom, AdditionalCharsets.UTF_8_BOM);
		assertEquals(1, config.getInt("k"));
		assertEquals(1, config.size());

		config = parseBytes(parser, utf8WithoutBom, AdditionalCharsets.UTF_8_BOM);
		assertEquals(1, config.getInt("k"));
		assertEquals(1, config.size());
	}

	@Test
	public void utf8Or16MaybeBOM() {
		var parser = new TomlParser();
		byte[] utf16WithBomBe = { (byte) 0xFE, (byte) 0xFF, 0, 'k', 0, '=', 0, '1' };
		byte[] utf16WithBomLe = { (byte) 0xFF, (byte) 0xFE, 'k', 0, '=', 0, '1', 0 };
		byte[] utf8WithoutBom = { 'k', '=', '1' }; // UTF-16 without bom is not supported here
		var config = parseBytes(parser, utf16WithBomBe, AdditionalCharsets.UTF_8_OR_16);
		assertEquals(1, config.getInt("k"));
		assertEquals(1, config.size());

		config = parseBytes(parser, utf16WithBomLe, AdditionalCharsets.UTF_8_OR_16);
		assertEquals(1, config.getInt("k"));
		assertEquals(1, config.size());

		config = parseBytes(parser, utf8WithoutBom, AdditionalCharsets.UTF_8_OR_16);
		assertEquals(1, config.getInt("k"));
		assertEquals(1, config.size());
	}

	@Test
	public void utf16WithByteOrderMark() {
		var parser = new TomlParser();
		byte[] withBomBe = { (byte) 0xFE, (byte) 0xFF, 0, 'k', 0, '=', 0, '1' };
		byte[] withBomLe = { (byte) 0xFF, (byte) 0xFE, 'k', 0, '=', 0, '1', 0 };
		byte[] withoutBom = { 0, 'k', 0, '=', 0, '1' };
		var config = parseBytes(parser, withBomBe, StandardCharsets.UTF_16);
		assertEquals(1, config.getInt("k"));
		assertEquals(1, config.size());

		config = parseBytes(parser, withBomLe, StandardCharsets.UTF_16);
		assertEquals(1, config.getInt("k"));
		assertEquals(1, config.size());

		config = parseBytes(parser, withoutBom, StandardCharsets.UTF_16);
		assertEquals(1, config.getInt("k"));
		assertEquals(1, config.size());
	}

	CommentedConfig parseBytes(TomlParser parser, byte[] bytes, Charset charset) {
		return parser.parse(new ByteArrayInputStream(bytes), charset);
	}

	@Test
	public void readOfficialExample() {
		File f = new File("example.toml");
		CommentedConfig parsed = new TomlParser().parse(f, FileNotFoundAction.THROW_ERROR, StandardCharsets.UTF_8);
		Util.checkExample(parsed);
	}

	@Test
	public void utf8Or16ReadOfficialExample() {
		File f = new File("example.toml");
		var parsed = new TomlParser().parse(f, FileNotFoundAction.THROW_ERROR, AdditionalCharsets.UTF_8_OR_16);
		Util.checkExample(parsed);

		parsed = new TomlParser().parse(f, FileNotFoundAction.THROW_ERROR, AdditionalCharsets.UTF_8_BOM);
		Util.checkExample(parsed);
	}

	@Test
	public void readToSynchronizedConfig() {
		File f = new File("example.toml");
		SynchronizedConfig config = new SynchronizedConfig(
				InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		new TomlParser().parse(f, config, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
	}

	@Test
	public void readToStampedConfig() {
		File f = new File("example.toml");
		StampedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(),
				HashMap::new);
		new TomlParser().parse(f, config, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
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
		assertThrows(ParsingException.class, this::testMixedArraySubtableTable);
		assertThrows(ParsingException.class, this::testMixedArraySubtablePrimitive);
		assertThrows(ParsingException.class, this::testInlineTableArraySubtable);
		assertThrows(ParsingException.class, this::testInlineTableArraySubtable2);
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

	private void testMixedArraySubtableTable() {
		String toml = "array = [{}, 42, {}]\n"
				+ "[array.subtable]\n"
				+ "   test = 'success'\n";
		parseAndPrint(toml);
	}

	private void testMixedArraySubtablePrimitive() {
		String toml = "array = [{}, 42]\n"
				+ "[array.subtable]\n"
				+ "   test = 'success'\n";
		parseAndPrint(toml);
	}

	private void testInlineTableArraySubtable() {
		String toml = "array_full_inline = [{}, {}]\n"
				+ "[array_full_inline.subtable]\n"
				+ "   test = 'success'\n";
		parseAndPrint(toml);
	}

	private void testInlineTableArraySubtable2() {
		String toml = "array_full_inline = [{}, {}]\n"
				+ "[array_full_inline.sub2.subtable]\n"
				+ "   test = 'success'\n";
		parseAndPrint(toml);
	}

	@Test
	public void testInlineTables() {
		String toml = "dotted_key_in_inline_table = { version.number = \"1.2.3\", \"a.b.c\" = \"normal key\" }";
		CommentedConfig config = new TomlParser().parse(toml);
		CommentedConfig sub = config.get("dotted_key_in_inline_table");
		assertEquals(2, sub.size());
		assertEquals("1.2.3", sub.get(Arrays.asList("version", "number")));
		assertEquals("normal key", sub.get(Arrays.asList("a.b.c")));
	}
}
