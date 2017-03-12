package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serialization.CharacterOutput;
import com.electronwill.nightconfig.core.serialization.WriterOutput;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
public class HoconWriterTest {
	@Test
	public void testWrite() {
		Config subConfig = new HoconConfig();
		subConfig.setValue("string", "test");
		subConfig.setValue("sub", new HoconConfig());

		List<Config> configList = new ArrayList<>();
		configList.add(subConfig);
		configList.add(subConfig);
		configList.add(subConfig);

		Config config = new HoconConfig();
		config.setValue("string", "\"value\"");
		config.setValue("integer", 2);
		config.setValue("long", 123456789L);
		config.setValue("double", 3.1415926535);
		config.setValue("bool_array", Arrays.asList(true, false, true, false));
		config.setValue("config", subConfig);
		config.setValue("config_list", configList);

		StringWriter sw = new StringWriter();
		CharacterOutput output = new WriterOutput(sw);
		HoconWriter writer = new HoconWriter.Builder().build(output);
		writer.writeHoconObject(config);
		System.out.println("Written:");
		System.out.println(sw.toString());
	}
}