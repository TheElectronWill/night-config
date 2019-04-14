package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static java.lang.Math.PI;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author TheElectronWill
 */
public class AbstractConfigTest {

	@Test
	public void basicTest() {
		Config config = Config.inMemory();
		config.set("true", true);
		config.set("false", false);
		assertTrue(config.<Boolean>get("true"));
		assertFalse(config.<Boolean>get("false"));

		config.set("int", 1234567890);
		assertEquals(1234567890, (int)config.get("int"));

		config.set("long", 123456789876543210L);
		assertEquals(123456789876543210L, (long)config.get("long"));

		config.set("float", 1.23456789F);
		assertEquals(1.23456789F, (float)config.get("float"));

		config.set("double", PI);
		assertEquals(PI, (double)config.get("double"));

		String string = "!!!???";
		config.set("string", string);
		assertSame(string, config.get("string"));

		List<String> stringList = Arrays.asList("a", "b", "c", "d");
		config.set("stringList", stringList);
		assertSame(stringList, config.get("stringList"));

		Config subConfig = InMemoryFormat.withUniversalSupport().createConfig();
		subConfig.set("string", "test!");
		subConfig.set("subSubConfig.string", "another test!");
		config.set("subConfig", subConfig);
		assertSame(subConfig, config.get("subConfig"));
		assertEquals("test!", config.get("subConfig.string"));
		assertEquals("another test!", config.get("subConfig.subSubConfig.string"));

		Map<String, Object> map = new HashMap<>();
		map.put("key in the map", "isn't in a config path");
		config.set("map", map);
		assertSame(map, config.get("map"));
		assertFalse(config.contains("map.key in the map"));

		config.set("enum1", TestEnum.A);
		config.set("enum2", "a");
		config.set("enum3", "A");
		config.set("enum4", 0);
		assertEquals(TestEnum.A, config.getEnum("enum1", TestEnum.class));
		assertEquals(TestEnum.A, config.getEnum("enum2", TestEnum.class));
		assertEquals(TestEnum.A, config.getEnum("enum3", TestEnum.class));
		assertEquals(TestEnum.A, config.getEnum("enum4", TestEnum.class, EnumGetMethod.ORDINAL_OR_NAME));
		assertThrows(ClassCastException.class, ()->config.getEnum("enum4", TestEnum.class));
		assertThrows(ClassCastException.class, ()->config.getEnum("enum4", TestEnum.class, EnumGetMethod.NAME));
		assertThrows(IllegalArgumentException.class, ()->config.getEnum("enum2", TestEnum.class, EnumGetMethod.NAME));
	}

	@Test
	public void specialCase() {
		List<String> split = StringUtils.split(".a...a.", '.');
		System.out.println("StringUtils:  " + split);

		String[] jsplit = ".a...a.".split("\\.");
		System.out.println("String#split: " + Arrays.toString(jsplit));

		Config config = InMemoryFormat.withUniversalSupport().createConfig();
		config.set(".a...a.", "value");
		assertTrue(config.contains(".a...a."));
		assertEquals("value", config.get(".a...a."));

		Map<String, Object> map = config.valueMap();
		assertTrue(map.get("") instanceof Config);
		Config c1 = (Config)map.get("");
		Config c2 = c1.get("a");
		Config c3 = c2.get("");
		Config c4 = c3.get("");
		Config c5 = c4.get("a");
		String value = c5.get("");
		assertEquals("value", value);
	}

	@Test
	public void size() {
		Config config = InMemoryFormat.withUniversalSupport().createConfig();
		config.set("a.b.c", "value");
		config.set("pi", PI);

		Config subConfig = InMemoryFormat.withUniversalSupport().createConfig();
		subConfig.set("string", "test!");
		config.set("subConfig", subConfig);

		assertEquals(1, subConfig.size());
		assertEquals(3, config.size());
	}

	@Test
	public void asMap() {
		Config config = InMemoryFormat.withUniversalSupport().createConfig();
		config.set("a.b.c", "value");
		config.set("pi", PI);

		Map<String, Object> map = config.valueMap();
		assertEquals(map.size(), config.size());

		assertTrue(map.get("pi") instanceof Double);
		assertEquals(PI, (double)map.get("pi"));

		assertFalse(map.containsKey("a.b.c"));
		assertTrue(map.get("a") instanceof Config);

		Config a = (Config)map.get("a");
		assertEquals(1, a.size());
		assertEquals("value", a.get("b.c"));
	}

	@Test
	public void containsValue() {
		Config config = InMemoryFormat.withUniversalSupport().createConfig();
		config.set("a.b.c", "value");
		assertTrue(config.contains("a"));
		assertFalse(config.contains("b"));
		assertTrue(config.contains("a.b"));
		assertTrue(config.<Config>get("a").contains("b"));
		assertFalse(config.contains("c"));
		assertTrue(config.contains("a.b.c"));
		assertEquals("value", config.<Config>get("a").<Config>get("b").get("c"));

		assertFalse(config.contains("int"));
		config.set("int", 12);
		assertTrue(config.contains("int"));
		assertEquals(12, (int)config.get("int"));
		config.remove("int");
		assertFalse(config.contains("int"));
	}
	
	@Test
	public void backingMap() {
		Config config = Config.of(LinkedHashMap::new, InMemoryFormat.withUniversalSupport());
		testValuesOrder(config);
	}
	
	@Test
	public void nestedBackingMap() {
		Config config = Config.of(LinkedHashMap::new, InMemoryFormat.withUniversalSupport());
		testNestedValuesOrder(config);
	}

	@Test
	public void orderedSetting() {
		Config.setInsertionOrderPreserved(true);
		assertTrue(Config.isInsertionOrderPreserved());
		testValuesOrder(Config.inMemory());
		Config.setInsertionOrderPreserved(false);
		assertFalse(Config.isInsertionOrderPreserved());
		assertThrows(AssertionFailedError.class, ()->testValuesOrder(Config.inMemory()));
	}

	@Test
	public void concurrentOrderedSetting() {
		Config.setInsertionOrderPreserved(true);
		testValuesOrder(Config.inMemoryConcurrent());
		Config.setInsertionOrderPreserved(false);
		assertThrows(AssertionFailedError.class, ()->testValuesOrder(Config.inMemoryConcurrent()));
	}

	@Test
	public void nestedOrderedSetting() {
		Config.setInsertionOrderPreserved(true);
		testNestedValuesOrder(Config.inMemory());
		Config.setInsertionOrderPreserved(false);
		assertThrows(AssertionFailedError.class, ()->testNestedValuesOrder(Config.inMemory()));
	}

	private void testValuesOrder(Config config) {
		LinkedHashMap<String, String> mappings = new LinkedHashMap<>();
		for (int i = 25; i >= 0; i--) {
			//reverse order so that it's different from the HashMap's default one
			mappings.put(Character.toString((char) ('a' + i)), Character.toString((char) ('z' - i)));
		}

		for (Entry<String, String> e : mappings.entrySet()) {
			config.set(e.getKey(), e.getValue());
		}

		List<Entry<String, String>> input = new ArrayList<>(mappings.entrySet());
		List<Entry<String, Object>> output = new ArrayList<>(config.valueMap().entrySet());

		assertEquals(input.size(), output.size());

		for (int i = 0; i < input.size(); i++) {
			assertEquals(input.get(i), output.get(i), "Map values mismatched at index: " + i);
		}
	}

	private void testNestedValuesOrder(Config config) {
		LinkedHashMap<String, String> mappings = new LinkedHashMap<>();
		for (int i = 25; i >= 0; i--) {
			mappings.put(Character.toString((char) ('a' + i)), Character.toString((char) ('z' - i)));
		}

		for (Entry<String, String> e : mappings.entrySet()) {
			config.set("foo." + e.getKey(), e.getValue());
		}

		List<Entry<String, String>> input = new ArrayList<>(mappings.entrySet());
		List<Entry<String, Object>> output = new ArrayList<>(config.<Config>get("foo").valueMap().entrySet());

		assertEquals(input.size(), output.size());

		for (int i = 0; i < input.size(); i++) {
			assertEquals(input.get(i), output.get(i), "Map values mismatched at index: " + i);
		}
	}
}