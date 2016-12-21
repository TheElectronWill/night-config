package com.electronwill.nightconfig.core.reflection;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.SimpleConfig;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * @author TheElectronWill
 */
public class ConfigToObjectMapperTest {

	private final Config config = new SimpleConfig();
	private final List<String> list1 = Arrays.asList("a", "b", "c"), list2 = Arrays.asList("element");
	private final Config config1 = new SimpleConfig(SimpleConfig.STRATEGY_SUPPORT_ALL), config2 = new SimpleConfig();

	{
		config.setInt("integer", 1234568790);
		config.setDouble("decimal", Math.PI);
		config.setString("string", "value");
		config.setList("stringList", list1);
		config.setConfig("config", config1);
		config.setInt("subObject.integer", -1);
		config.setDouble("subObject.decimal", 0.5);
		config.setString("subObject.string", "Hey!");
		config.setList("subObject.stringList", list2);
		config.setConfig("subObject.config", config2);
		config.setConfig("subObject.subObject", null);
	}

	@Test
	public void configToObjectToConfig() throws Exception {
		ConfigToObjectMapper ctom = new ConfigToObjectMapper();
		MyObject object = ctom.map(config, MyObject.class);

		ObjectToConfigMapper otcm = new ObjectToConfigMapper();
		Config myConfig = new SimpleConfig();
		otcm.map(object, myConfig);

		System.out.println("Original config: " + config);
		System.out.println("New config: " + myConfig);

		assert myConfig.equals(config) : "Invalid conversion!";
	}

	@Test
	public void test() throws Exception {
		System.out.println("====== Test with non-final fields ======");
		{
			MyObject object = new MyObject();
			testConfigToObject(config, object);// does the mapping
			assert object.integer == config.getInt("integer");
			assert object.decimal == config.getDouble("decimal");
			assert object.string == config.getString("string");
			assert object.stringList == list1;
			assert object.config == config1;
			assert object.subObject != null;
			assert object.subObject.integer == config.getInt("subObject.integer");
			assert object.subObject.decimal == config.getDouble("subObject.decimal");
			assert object.subObject.string == config.getString("subObject.string");
			assert object.subObject.stringList == list2;
			assert object.subObject.config == config2;
			assert object.subObject.subObject == null;
		}

		System.out.println();
		System.out.println("====== Test with final fields ======");
		{
			MyObjectFinal object = new MyObjectFinal();
			testConfigToObject(config, object);//does the mapping
			assert object.integer == config.getInt("integer");
			assert object.decimal == config.getDouble("decimal");
			assert object.string == config.getString("string");
			assert object.stringList == list1;
			assert object.config == config1;
			assert object.subObject != null;
			assert object.subObject.integer == config.getInt("subObject.integer");
			assert object.subObject.decimal == config.getDouble("subObject.decimal");
			assert object.subObject.string == config.getString("subObject.string");
			assert object.subObject.stringList == list2;
			assert object.subObject.config == config2;
			assert object.subObject.subObject == null;
		}
	}

	private void testConfigToObject(Config config, Object object) throws Exception {
		System.out.println("Before: " + object);

		ConfigToObjectMapper mapper = new ConfigToObjectMapper();
		mapper.map(config, object);

		System.out.println("After: " + object);
	}

	private static class MyObject {
		int integer;
		double decimal;
		String string;
		List<String> stringList;
		Config config;
		MyObject subObject;

		@Override
		public String toString() {
			return "MyObject{" + "integer=" + integer + ", decimal=" + decimal + ", string='" + string + '\'' + ", stringList=" + stringList + ", config=" + config + ", subObject=" + subObject + '}';
		}
	}

	private static class MyObjectFinal {
		final int integer;
		final double decimal;
		final String string;
		final List<String> stringList;
		final Config config;
		final MyObjectFinal subObject;

		public MyObjectFinal() {
			this(123, 1.23, "v", null, null, null);
		}

		/*
		Not necessary for the mapper to work, but necessary to prevent the compiler from inlining the use
		of the primitive fields. This allows us to print the changes correctly.
		 */
		public MyObjectFinal(int integer, double decimal, String string, List<String> stringList, Config config, MyObjectFinal subObject) {
			this.integer = integer;
			this.decimal = decimal;
			this.string = string;
			this.stringList = stringList;
			this.config = config;
			this.subObject = subObject;
		}

		@Override
		public String toString() {
			return "MyObjectFinal{" + "integer=" + integer + ", decimal=" + decimal + ", string='" + string + '\'' + ", stringList=" + stringList + ", config=" + config + ", subObject=" + subObject + '}';
		}
	}

}