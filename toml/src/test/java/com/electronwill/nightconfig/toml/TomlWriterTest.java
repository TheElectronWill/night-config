package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
public class TomlWriterTest {

	@Test
	public void writeToString() throws IOException {
		Config subConfig = new TomlConfig();
		subConfig.set("string", "test");
		subConfig.set("dateTime", ZonedDateTime.now());
		subConfig.set("sub", new TomlConfig());

		List<Config> tableArray = new ArrayList<>();
		tableArray.add(subConfig);
		tableArray.add(subConfig);
		tableArray.add(subConfig);

		Config config = new TomlConfig();
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
}