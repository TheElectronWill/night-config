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
public class AbstractConfigTest {

	@Test
	public void basicTest() {
		AbstractConfig config = new SimpleConfig();
		config.set("true", true);
		config.set("false", false);
		assert config.<Boolean>get("true");
		assert !config.<Boolean>get("false");

		config.set("int", 1234567890);
		assert config.<Integer>get("int") == 1234567890;

		config.set("long", 123456789876543210L);
		assert config.<Long>get("long") == 123456789876543210L;

		config.set("float", 1.23456789F);
		assert config.<Float>get("float") == 1.23456789F;

		config.set("double", Math.PI);
		assert config.<Double>get("double") == Math.PI;

		String string = "!!!???";
		config.set("string", string);
		assert config.<String>get("string") == string;

		List<String> stringList = Arrays.asList("a", "b", "c", "d");
		config.set("stringList", stringList);
		assert config.<List<String>>get("stringList") == stringList;

		Config subConfig = new SimpleConfig(type -> true);
		subConfig.set("string", "test!");
		subConfig.set("subSubConfig.string", "another test!");
		config.set("subConfig", subConfig);
		assert config.<Config>get("subConfig") == subConfig;
		assert config.<String>get("subConfig.string").equals("test!");
		assert config.<String>get("subConfig.subSubConfig.string").equals("another test!");

		Map<String, Object> map = new HashMap<>();
		map.put("key in the map", "isn't in a config path");
		config.set("map", map);
		assert config.get("map") == map;
		assert !config.contains("map.key in the map");
	}

	@Test
	public void specialCase() {
		List<String> split = StringUtils.split(".a...a.", '.');
		System.out.println("StringUtils:  " + split);

		String[] jsplit = ".a...a.".split("\\.");
		System.out.println("String#split: " + Arrays.toString(jsplit));

		AbstractConfig config = new SimpleConfig(type -> true);
		config.set(".a...a.", "value");
		assert config.contains(".a...a.");
		assert config.<String>get(".a...a.").equals("value");

		Map<String, Object> map = config.valueMap();
		assert map.get("") instanceof Config;
		Config c1 = (Config)map.get("");
		Config c2 = c1.get("a");
		Config c3 = c2.get("");
		Config c4 = c3.get("");
		Config c5 = c4.get("a");
		String value = c5.get("");
		assert value.equals("value");
	}

	@Test
	public void size() {
		AbstractConfig config = new SimpleConfig(InMemoryFormat.withUniversalSupport());
		config.set("a.b.c", "value");
		config.set("pi", Math.PI);

		Config subConfig = new SimpleConfig(InMemoryFormat.withUniversalSupport());
		subConfig.set("string", "test!");
		config.set("subConfig", subConfig);

		assert subConfig.size() == 1 : "Invalid subConfig size: " + subConfig.size();
		assert config.size() == 3 : "Invalid config size: " + config.size();
	}

	@Test
	public void asMap() {
		AbstractConfig config = new SimpleConfig(InMemoryFormat.withUniversalSupport());
		config.set("a.b.c", "value");
		config.set("pi", Math.PI);

		Map<String, Object> map = config.valueMap();
		assert map.size() == config.size();

		assert map.get("pi") instanceof Double;
		assert ((double)map.get("pi")) == Math.PI;

		assert !map.containsKey("a.b.c");
		assert map.get("a") instanceof Config;

		Config a = (Config)map.get("a");
		assert a.size() == 1;
		assert a.<String>get("b.c").equals("value");
	}

	@Test
	public void containsValue() {
		AbstractConfig config = new SimpleConfig(InMemoryFormat.withUniversalSupport());
		config.set("a.b.c", "value");
		assert config.contains("a");

		assert !config.contains("b");
		assert config.contains("a.b");

		assert !config.contains("c");
		assert config.contains("a.b.c");

		config.set("int", 12);
		assert config.contains("int");
	}
}