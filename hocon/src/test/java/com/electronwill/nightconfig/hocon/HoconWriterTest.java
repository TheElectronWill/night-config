package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.Config;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
public class HoconWriterTest {
	@Test
	public void testWrite() throws IOException {
		Config subConfig = new HoconConfig();
		subConfig.setValue("string", "test");
		subConfig.setValue("sub", new HoconConfig());

		List<Config> configList = new ArrayList<>();
		configList.add(subConfig);
		configList.add(subConfig);
		configList.add(subConfig);

		HoconConfig config = new HoconConfig();
		config.setValue("string", "\"value\"");
		config.setValue("integer", 2);
		config.setValue("long", 123456789L);
		config.setValue("double", 3.1415926535);
		config.setValue("bool_array", Arrays.asList(true, false, true, false));
		config.setValue("config", subConfig);
		config.setValue("config_list", configList);
		config.setComment("string", " Comment 1\n Comment 2\n Comment 3");

		StringWriter sw = new StringWriter();
		HoconWriter writer = new HoconWriter();
		writer.write(config, sw);
		System.out.println("Written:");
		System.out.println(sw);
	}
}