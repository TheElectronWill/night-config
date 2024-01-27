package com.electronwill.nightconfig.toml;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringWriter;
import java.sql.Array;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.TestEnum;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.utils.StringUtils;

/**
 * @author TheElectronWill
 */
public class TomlWriterTest {

	@Test
	public void writeToString() {
		Config subConfig = TomlFormat.instance().createConfig();
		subConfig.set("string", "test");
		subConfig.set("dateTime", ZonedDateTime.now());
		subConfig.set("sub", TomlFormat.instance().createConfig());

		List<Config> tableArray = new ArrayList<>();
		tableArray.add(subConfig);
		tableArray.add(subConfig);
		tableArray.add(subConfig);

		Config config = TomlFormat.instance().createConfig();
		config.set("string", "\"value\"");
		config.set("integer", 2);
		config.set("long", 123456789L);
		config.set("double", 3.1415926535);
		config.set("bool_array", Arrays.asList(true, false, true, false));
		config.set("config", subConfig);
		config.set("table_array", tableArray);
		config.set("table_array2", tableArray);
		config.set("enum", TestEnum.A);

		StringWriter stringWriter = new StringWriter();
		TomlWriter writer = new TomlWriter();
		writer.setIndentArrayElementsPredicate(array -> array.size() > 3);
		writer.setWriteTableInlinePredicate(table -> table.size() <= 2);
		writer.write(config, stringWriter);

		System.out.println("Written:");
		System.out.println(stringWriter);
	}

	@Test
	public void correctNewlinesSub() { // Test fails, because of additional empty line at the end
		Config conf = TomlFormat.instance().createConfig();
		Config sub = conf.createSubConfig();
		conf.set("table", sub);
		sub.set("key", "value");

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		// Fixable by adding "" at the end, because it only appears at the end of the file
		assertLinesMatch(Arrays.asList("[table]", "\tkey = \"value\"", ""), StringUtils.splitLines(written));
	}

	@Test
	public void correctNewlinesArrayOfTables() {  // Test fails, because of additional empty line at the end
		Config conf = TomlFormat.instance().createConfig();

		Config sub = conf.createSubConfig();
		sub.set("key", "value");

		List<Config> arrayOfTables = Arrays.asList(sub);
		conf.set("aot", arrayOfTables);

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		// Fixable by adding "" at the end, because it only appears at the end of the file
		assertLinesMatch(Arrays.asList("[[aot]]", "\tkey = \"value\"", ""), StringUtils.splitLines(written));
	}

	@Test
	public void correctNewlinesSimple() {
		Config conf = TomlFormat.instance().createConfig();
		conf.set("simple", 123);

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		assertLinesMatch(Arrays.asList("simple = 123", ""), StringUtils.splitLines(written));
	}

	@Test
	public void correctNewlinesMixed() {  // Test fails, because of additional empty line at the end
		Config conf = TomlFormat.instance().createConfig();
		Config sub = conf.createSubConfig();
		conf.set("simple", 123);
		conf.set("table", sub);
		sub.set("key", "value");

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		// Fixable by adding "" at the end, because it only appears at the end of the file
		assertLinesMatch(Arrays.asList("simple = 123", "", "[table]", "\tkey = \"value\"", ""),
				StringUtils.splitLines(written));
	}

	@Test
	public void correctTableSeparation() {
		Config config = TomlFormat.instance().createConfig();
		Config subConfig = config.createSubConfig();

		config.set("first", subConfig);
		config.set("second", subConfig);
		subConfig.set("key", "value");

		TomlWriter writer = new TomlWriter();
		String written = writer.writeToString(config);
		System.out.println(written);
		assertLinesMatch(Arrays.asList("[first]", "\tkey = \"value\"", "", "[second]", "\tkey = \"value\"", "", ""),
			StringUtils.splitLines(written));
	}

	@Test
	public void correctTableArraySeparation() {
		Config subConfig = TomlFormat.instance().createConfig();
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

		assertLinesMatch(Arrays.asList("[[firstArray]]", "\tkey = \"value\"", "[[firstArray]]", "\tkey = \"value\"", "",
			"[[secondArray]]", "\tkey = \"value\"", "[[secondArray]]", "\tkey = \"value\"", "", ""),
			StringUtils.splitLines(written));
	}

	@Test
	public void noNulls() {
		Config config = TomlFormat.newConfig();
		Executable tryToWrite = () -> TomlFormat.instance().createWriter().writeToString(config);

		config.set("null", null);
		assertThrows(WritingException.class, tryToWrite);

		config.set("null", NullObject.NULL_OBJECT);
		assertThrows(WritingException.class, tryToWrite);
	}
}