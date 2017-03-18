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
		subConfig.setValue("string", "test");
		subConfig.setValue("dateTime", ZonedDateTime.now());
		subConfig.setValue("sub", new TomlConfig());

		List<Config> tableArray = new ArrayList<>();
		tableArray.add(subConfig);
		tableArray.add(subConfig);
		tableArray.add(subConfig);

		Config config = new TomlConfig();
		config.setValue("string", "\"value\"");
		config.setValue("integer", 2);
		config.setValue("long", 123456789L);
		config.setValue("double", 3.1415926535);
		config.setValue("bool_array", Arrays.asList(true, false, true, false));
		config.setValue("config", subConfig);
		config.setValue("table_array", tableArray);
		StringWriter writer = new StringWriter();

		new TomlWriter().writeConfig(config, writer);
		System.out.println("Written:");
		System.out.println(writer);
	}
}