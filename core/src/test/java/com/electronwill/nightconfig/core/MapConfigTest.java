package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.StringUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
public class MapConfigTest {

	@Test
	public void basicTest() {
		MapConfig config = new SimpleConfig();
		config.setValue("true", true);
		config.setValue("false", false);
		assert config.<Boolean>getValue("true");
		assert !config.<Boolean>getValue("false");

		config.setValue("int", 1234567890);
		assert config.<Integer>getValue("int") == 1234567890;

		config.setValue("long", 123456789876543210L);
		assert config.<Long>getValue("long") == 123456789876543210L;

		config.setValue("float", 1.23456789F);
		assert config.<Float>getValue("float") == 1.23456789F;

		config.setValue("double", Math.PI);
		assert config.<Double>getValue("double") == Math.PI;

		String string = "!!!???";
		config.setValue("string", string);
		assert config.<String>getValue("string") == string;

		List<String> stringList = Arrays.asList("a", "b", "c", "d");
		config.setValue("stringList", stringList);
		assert config.<List<String>>getValue("stringList") == stringList;

		Config subConfig = new SimpleConfig(type -> true);
		subConfig.setValue("string", "test!");
		subConfig.setValue("subSubConfig.string", "another test!");
		config.setValue("subConfig", subConfig);
		assert config.<Config>getValue("subConfig") == subConfig;
		assert config.<String>getValue("subConfig.string").equals("test!");
		assert config.<String>getValue("subConfig.subSubConfig.string").equals("another test!");

		Map<String, Object> map = new HashMap<>();
		map.put("key in the map", "isn't in a config path");
		config.setValue("map", map);
		assert config.getValue("map") == map;
		assert !config.containsValue("map.key in the map");
	}

	@Test
	public void specialCase() {
		List<String> split = StringUtils.split(".a...a.", '.');
		System.out.println("StringUtils:  " + split);

		String[] jsplit = ".a...a.".split("\\.");
		System.out.println("String#split: " + Arrays.toString(jsplit));

		MapConfig config = new SimpleConfig(type -> true);
		config.setValue(".a...a.", "value");
		assert config.containsValue(".a...a.");
		assert config.<String>getValue(".a...a.").equals("value");

		Map<String, Object> map = config.asMap();
		assert map.get("") instanceof Config;
		Config c1 = (Config)map.get("");
		Config c2 = c1.<Config>getValue("a");
		Config c3 = c2.<Config>getValue("");
		Config c4 = c3.<Config>getValue("");
		Config c5 = c4.<Config>getValue("a");
		String value = c5.<String>getValue("");
		assert value.equals("value");
	}

	@Test
	public void size() {
		MapConfig config = new SimpleConfig(type -> true);
		config.setValue("a.b.c", "value");
		config.setValue("pi", Math.PI);

		Config subConfig = new SimpleConfig(type -> true);
		subConfig.setValue("string", "test!");
		config.setValue("subConfig", subConfig);

		assert subConfig.size() == 1 : "Invalid subConfig size: " + subConfig.size();
		assert config.size() == 3 : "Invalid config size: " + config.size();
	}

	@Test
	public void asMap() {
		MapConfig config = new SimpleConfig(type -> true);
		config.setValue("a.b.c", "value");
		config.setValue("pi", Math.PI);

		Map<String, Object> map = config.asMap();
		assert map.size() == config.size();

		assert map.get("pi") instanceof Double;
		assert ((double)map.get("pi")) == Math.PI;

		assert !map.containsKey("a.b.c");
		assert map.get("a") instanceof Config;

		Config a = (Config)map.get("a");
		assert a.size() == 1;
		assert a.<String>getValue("b.c").equals("value");
	}

	@Test
	public void containsValue() {
		MapConfig config = new SimpleConfig(type -> true);
		config.setValue("a.b.c", "value");
		assert config.containsValue("a");

		assert !config.containsValue("b");
		assert config.containsValue("a.b");

		assert !config.containsValue("c");
		assert config.containsValue("a.b.c");

		config.setValue("int", 12);
		assert config.containsValue("int");
	}
}