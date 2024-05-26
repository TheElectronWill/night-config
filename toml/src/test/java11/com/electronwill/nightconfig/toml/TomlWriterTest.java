package com.electronwill.nightconfig.toml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.utils.StringUtils;

/**
 * @author TheElectronWill
 */
public class TomlWriterTest {

	@Test
	public void multilineStrings1() {
		var config = TomlFormat.newConfig(LinkedHashMap::new);
		config.set("basic", "normal string");
		config.set("multiline", "first line\nsecond line\n\tthird line (indented)\nfourth line!");

		var result = writerWithoutIndentation().writeToString(config);
		var expected = "basic = \"normal string\"\n" +
			"multiline = \"\"\"\n" +
			"first line\n" +
			"second line\n" +
			"\tthird line (indented)\n" +
			"fourth line!\"\"\"\n" +
			"";
		assertEquals(expected, result);

		var reparsed = TomlFormat.instance().createParser().parse(expected);
		assertEquals(config, reparsed);
	}

	@Test
	public void multilineStrings2() {
		var config = TomlFormat.newConfig(LinkedHashMap::new);
		config.set("basic", "normal string");
		config.set("multiline", "a\nb\t\f\n\\_\\_\\ \"\"in double quotes\"\"\nbad triplet\"\"\"");
		var result = writerWithoutIndentation().writeToString(config);
		var expected = join("basic = \"normal string\"",
			"multiline = \"\"\"",
			"a",
			"b\t\\f",
			"\\\\_\\\\_\\\\ \"\"in double quotes\"\"",
			"bad triplet\"\"\\\"" + "\"\"\"",
			""
		);
		assertEquals(expected, result);

		var reparsed = TomlFormat.instance().createParser().parse(expected);
		assertEquals(config, reparsed);
	}

	@Test
	public void writeToString() {
		CommentedConfig config = TomlFormat.newConfig(LinkedHashMap::new);
		Util.populateTest(config);

		var writer = writerWithIndentation();
		var result = writer.writeToString(config);
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}

	@Test
	public void writeSynchronizedConfig() {
		CommentedConfig config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(),
				HashMap::new);
		Util.populateTest(config);
		var result = writerWithIndentation().writeToString(config);
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}

	@Test
	public void writeStampedConfig() {
		CommentedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(),
				HashMap::new);
		Util.populateTest(config);
		var result = writerWithIndentation().writeToString(config);
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}

	@Test
	public void correctNewlinesSub() {
		Config conf = TomlFormat.newConfig(LinkedHashMap::new);
		Config sub = conf.createSubConfig();
		conf.set("table", sub);
		sub.set("key", "value");

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		assertLinesMatch(Arrays.asList("[table]", "\tkey = \"value\"", ""),
				StringUtils.splitLines(written));
	}

	@Test
	public void correctNewlinesArrayOfTables() {
		Config conf = TomlFormat.newConfig(LinkedHashMap::new);

		Config sub = conf.createSubConfig();
		sub.set("key", "value");

		List<Config> arrayOfTables = Arrays.asList(sub);
		conf.set("aot", arrayOfTables);

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		assertLinesMatch(Arrays.asList("[[aot]]", "\tkey = \"value\"", ""),
				StringUtils.splitLines(written));
	}

	@Test
	public void correctNewlinesSimple() {
		Config conf = TomlFormat.newConfig(LinkedHashMap::new);
		conf.set("simple", 123);

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		assertLinesMatch(Arrays.asList("simple = 123", ""), StringUtils.splitLines(written));
	}

	@Test
	public void correctNewlinesMixed() {
		Config conf = TomlFormat.newConfig(LinkedHashMap::new);
		Config sub = conf.createSubConfig();
		conf.set("simple", 123);
		conf.set("table", sub);
		sub.set("key", "value");

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		assertLinesMatch(Arrays.asList("simple = 123", "", "[table]", "\tkey = \"value\"", ""),
				StringUtils.splitLines(written));
	}

	@Test
	public void correctTableSeparation() {
		Config config = TomlFormat.newConfig(LinkedHashMap::new);
		Config subConfig = config.createSubConfig();

		config.set("first", subConfig);
		config.set("second", subConfig);
		subConfig.set("key", "value");

		TomlWriter writer = new TomlWriter();
		String written = writer.writeToString(config);
		// System.err.println(written);
		assertEquals(
				join("[first]", "\tkey = \"value\"", "", "[second]", "\tkey = \"value\"",
						""),
				written);
	}

	@Test
	public void correctTableArraySeparation() {
		Config subConfig = TomlFormat.newConfig(LinkedHashMap::new);
		subConfig.set("key", "value");

		List<Config> tableArray = new ArrayList<>();
		tableArray.add(subConfig);
		tableArray.add(subConfig);

		Config config = TomlFormat.instance().createConfig();
		config.set("firstArray", tableArray);
		config.set("secondArray", tableArray);

		TomlWriter writer = new TomlWriter();
		String written = writer.writeToString(config);
		System.out.println(written);

		assertEquals(join(
				"[[firstArray]]",
				"\tkey = \"value\"",
				"[[firstArray]]",
				"\tkey = \"value\"",
				"",
				"[[secondArray]]",
				"\tkey = \"value\"",
				"[[secondArray]]",
				"\tkey = \"value\"",
				""), written);
	}

	@Test
	public void noNulls() {
		Config config = TomlFormat.newConfig(LinkedHashMap::new);
		Executable tryToWrite = () -> TomlFormat.instance().createWriter().writeToString(config);

		config.set("null", null);
		assertThrows(WritingException.class, tryToWrite);

		config.set("null", NullObject.NULL_OBJECT);
		assertThrows(WritingException.class, tryToWrite);
	}

	@Test
	public void foldUselessIntermediateLevels() {
		var config = TomlFormat.newConfig(LinkedHashMap::new);
		config.set("top.sub.a", 1);
		config.set("top.sub.b", 2);
		assertEquals(join(
				"[top.sub]",
				"a = 1",
				"b = 2",
				""), writerWithoutIndentation().writeToString(config));

		config.clear();
		config.set("top.a", 1);
		config.set("top.intermediate.c.d", 2);

		assertEquals(join(
				"[top]",
				"a = 1",
				"",
				"[top.intermediate.c]",
				"d = 2",
				""), writerWithoutIndentation().writeToString(config));

		config.clear();
		var sub = config.createSubConfig();
		sub.set("a", 1);
		sub.set("b", 2);
		config.set("top.sub", Arrays.asList(sub, sub));
		assertEquals(join(
				"[[top.sub]]",
				"a = 1",
				"b = 2",
				"[[top.sub]]",
				"a = 1",
				"b = 2",
				""), writerWithoutIndentation().writeToString(config));

		config.set("simple", true);
		assertEquals(join(
				"simple = true",
				"",
				"[[top.sub]]",
				"a = 1",
				"b = 2",
				"[[top.sub]]",
				"a = 1",
				"b = 2",
				""), writerWithoutIndentation().writeToString(config));
	}

	@Test
	public void writeAlignedList() {
		CommentedConfig config = CommentedConfig.inMemory();
		List<String> list = Arrays.asList("value1", "value2", "value3");

		config.set("Test.List", list);

		TomlWriter writer = new TomlWriter();
		writer.setIndentArrayElementsPredicate(objects -> true);
		String written = writer.writeToString(config);

		System.out.println(written);
		assertEquals(join("[Test]",
							"\tList = [",
							"\t\t\"value1\",",
							"\t\t\"value2\",",
							"\t\t\"value3\"",
							"\t]",
							""), written);
	}

	@Test
	public void writeCategoryAndValueComments() {
		CommentedConfig config = CommentedConfig.inMemory();

		config.setComment("Header", "Header Comment");
		config.setComment("Header.key", "Value Comment");

		config.set("Header.key", "value");

		TomlWriter writer = new TomlWriter();
		String written = writer.writeToString(config);

		System.out.println(written);
		assertEquals(join("#Header Comment",
			"[Header]",
			"\t#Value Comment",
			"\tkey = \"value\"",
			""), written);
	}

	@Test
	public void writeNestedArraysAndTables() {
		TomlWriter writer = new TomlWriter();

		CommentedConfig config = CommentedConfig.inMemory();
		CommentedConfig table1 = CommentedConfig.inMemory();
		CommentedConfig table2 = CommentedConfig.inMemory();
		table1.set("one", 1);
		table2.set("two", 2);
		config.set("arr_arr_tbls", List.of(List.of(table1, table2)));

		String written = writer.writeToString(config);
		System.out.println(written);
		assertEquals(join("arr_arr_tbls = [[{one = 1}, {two = 2}]]\n"), written);
	}

	private String join(String... lines) {
		return String.join(System.lineSeparator(), lines);
	}

	private TomlWriter writerWithoutIndentation() {
		var w = new TomlWriter();
		w.setIndent(IndentStyle.NONE);
		return w;
	}

	private TomlWriter writerWithIndentation() {
		var w = new TomlWriter();
		w.setIndentArrayElementsPredicate(array -> array.size() > 3);
		w.setWriteTableInlinePredicate(table -> table.size() <= 2);
		return w;
	}
}