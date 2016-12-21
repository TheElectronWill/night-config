package com.electronwill.nightconfig.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * @author TheElectronWill
 */
public class MapConfigTest {

	@Test
	public void basicTest() throws Exception {
		MapConfig config = new SimpleConfig();
		config.setBoolean("true", true);
		config.setBoolean("false", false);
		assert config.getBoolean("true");
		assert !config.getBoolean("false");

		config.setInt("int", 1234567890);
		assert config.getInt("int") == 1234567890;

		config.setLong("long", 123456789876543210L);
		assert config.getLong("long") == 123456789876543210L;

		config.setFloat("float", 1.23456789F);
		assert config.getFloat("float") == 1.23456789F;

		config.setDouble("double", Math.PI);
		assert config.getDouble("double") == Math.PI;

		String string = "!!!???";
		config.setString("string", string);
		assert config.getString("string") == string;

		List<String> stringList = Arrays.asList("a", "b", "c", "d");
		config.setList("stringList", stringList);
		assert config.<String>getList("stringList") == stringList;

		Config subConfig = new SimpleConfig(new SimpleConfig.SupportEverythingStrategy());
		subConfig.setString("string", "test!");
		subConfig.setString("subSubConfig.string", "another test!");
		config.setConfig("subConfig", subConfig);
		assert config.getConfig("subConfig") == subConfig;
		assert config.getString("subConfig.string").equals("test!");
		assert config.getString("subConfig.subSubConfig.string").equals("another test!");

		Map map = new HashMap<>();
		map.put("key in the map", "isn't in a config path");
		config.setValue("map", map);
		assert config.getValue("map") == map;
		assert !config.containsValue("map.key in the map");
	}

	@Test
	public void specialCase() throws Exception {
		List<String> split = StringUtils.split(".a...a.", '.');
		System.out.println("StringUtils:  " + split);

		String[] jsplit = ".a...a.".split("\\.");
		System.out.println("String#split: " + Arrays.toString(jsplit));

		MapConfig config = new SimpleConfig(new SimpleConfig.SupportEverythingStrategy());
		config.setString(".a...a.", "value");
		assert config.containsValue(".a...a.");
		assert config.getString(".a...a.").equals("value");

		Map<String, Object> map = config.asMap();
		assert map.get("") instanceof Config;
		Config c1 = (Config)map.get("");
		Config c2 = c1.getConfig("a");
		Config c3 = c2.getConfig("");
		Config c4 = c3.getConfig("");
		Config c5 = c4.getConfig("a");
		String value = c5.getString("");
		assert value.equals("value");
	}

	@Test
	public void size() throws Exception {
		MapConfig config = new SimpleConfig(new SimpleConfig.SupportEverythingStrategy());
		config.setString("a.b.c", "value");
		config.setDouble("pi", Math.PI);

		Config subConfig = new SimpleConfig(new SimpleConfig.SupportEverythingStrategy());
		subConfig.setString("string", "test!");
		config.setConfig("subConfig", subConfig);

		assert subConfig.size() == 1 : "Invalid subConfig size: " + subConfig.size();
		assert config.size() == 3 : "Invalid config size: " + config.size();
	}

	@Test
	public void asMap() throws Exception {
		MapConfig config = new SimpleConfig(new SimpleConfig.SupportEverythingStrategy());
		config.setString("a.b.c", "value");
		config.setDouble("pi", Math.PI);

		Map<String, Object> map = config.asMap();
		assert map.size() == config.size();

		assert map.get("pi") instanceof Double;
		assert ((double)map.get("pi")) == Math.PI;

		assert !map.containsKey("a.b.c");
		assert map.get("a") instanceof Config;

		Config a = (Config)map.get("a");
		assert a.size() == 1;
		assert a.getString("b.c").equals("value");
	}

	@Test
	public void containsValue() throws Exception {
		MapConfig config = new SimpleConfig(new SimpleConfig.SupportEverythingStrategy());
		config.setString("a.b.c", "value");
		assert config.containsValue("a");

		assert !config.containsValue("b");
		assert config.containsValue("a.b");

		assert !config.containsValue("c");
		assert config.containsValue("a.b.c");

		config.setInt("int", 12);
		assert config.containsValue("int");
	}

}