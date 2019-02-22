package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.io.WritingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

		StringWriter stringWriter = new StringWriter();
		TomlWriter writer = new TomlWriter();
		writer.setIndentArrayElementsPredicate(array -> array.size() > 3);
		writer.setWriteTableInlinePredicate(table -> table.size() <= 2);
		writer.write(config, stringWriter);

		System.out.println("Written:");
		System.out.println(stringWriter);
	}

	@Test
	public void noNulls() {
		Config config = TomlFormat.newConfig();
		config.set("null", null);
		Assertions.assertThrows(WritingException.class,
			() -> TomlFormat.instance().createWriter().writeToString(config));

		config.set("null", NullObject.NULL_OBJECT);
		Assertions.assertThrows(WritingException.class,
			() -> TomlFormat.instance().createWriter().writeToString(config));
	}
}