package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.BasicTestEnum;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.electronwill.nightconfig.core.TestEnum;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
public class HoconWriterTest {
	@Test
	public void testWrite() throws IOException {
		Config subConfig = CommentedConfig.inMemory();
		subConfig.set("string", "test");
		subConfig.set("enum", BasicTestEnum.C);
		subConfig.set("sub", CommentedConfig.inMemory());

		List<Config> configList = new ArrayList<>();
		configList.add(subConfig);
		configList.add(subConfig);
		configList.add(subConfig);

		CommentedConfig config = CommentedConfig.inMemory();
		config.set("string", "\"value\"");
		config.set("integer", 2);
		config.set("long", 123456789L);
		config.set("double", 3.1415926535);
		config.set("bool_array", Arrays.asList(true, false, true, false));
		config.set("config", subConfig);
		config.set("config_list", configList);
		config.setComment("string", " Comment 1\n Comment 2\n Comment 3");
		config.set("enum", TestEnum.A);

		StringWriter sw = new StringWriter();
		HoconWriter writer = new HoconWriter();
		writer.write(config, sw);
		System.out.println("Written:");
		System.out.println(sw);
	}
}