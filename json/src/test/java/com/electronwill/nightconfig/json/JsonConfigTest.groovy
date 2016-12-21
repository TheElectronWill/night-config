package com.electronwill.nightconfig.json

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.SimpleConfig
import com.electronwill.nightconfig.core.serialization.FileConfig
import org.junit.Test

/**
 * @author TheElectronWill
 */
class JsonConfigTest {
	private final FileConfig config = new JsonConfig();
	{
		Config config2 = new SimpleConfig(supportStrategy);
		config2.setBoolean("boolean", true);
		config2.setBoolean("false", false);

		config.setString("string", "This is a string with a lot of characters to escape \n\r\t \\ \" ");
		config.setInt("int", 123456);
		config.setLong("long", 1234567890l);
		config.setFloat("float", 0.123456f);
		config.setDouble("double", 0.123456d);
		config.setConfig("config", config2);
		config.setList("list", Arrays.asList("a", "b", 3, null, true, false, 17.5));
		config.setValue("null", null);
	}
	private final File file = "test.json";

	@Test
	void testWriteTo() {
		config.writeTo(file);
	}

	@Test
	void testReadFrom() {
		config.readFrom(file);
		println config;
	}
}
