package com.electronwill.nightconfig.yaml;

import com.electronwill.sharedtests.BasicTestEnum;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;
import static com.electronwill.nightconfig.core.file.FileNotFoundAction.THROW_ERROR;
import static org.junit.jupiter.api.Assertions.*;

public class YamlTest {

	private final File file = new File("test.yml");

	@Test
	public void testReadWrite() {
		Config config = Config.inMemory();
		Config config1 = Config.inMemory();
		Config config2 = Config.inMemory();
		config1.set("foo", "bar");
		config2.set("baz", true);
		config.set("null", null);
		config.set("nullObject", NULL_OBJECT);
		config.set("string", "this is a string");
		config.set("sub.null", null);
		config.set("sub.nullObject", NULL_OBJECT);
		config.set("enum", BasicTestEnum.A); // complex enums doesn't appear to work with SnakeYAML
		config.set("list", Arrays.asList(10, 12));
		config.set("objectList", Arrays.asList(config1, config2));
		config.set(Arrays.asList("not.a.subconfig"), "works");

		System.out.println("Config: " + config);
		System.out.println("classOf[sub] = " + config.get("sub").getClass());
		System.out.println("sub.null = " + config.get("sub.null"));
		System.out.println("sub.nullObject = " + config.get("sub.nullObject"));
		YamlFormat yamlFormat = YamlFormat.defaultInstance();
		yamlFormat.createWriter().write(config, file, WritingMode.REPLACE);

		Config parsed = yamlFormat.createConfig();
		yamlFormat.createParser().parse(file, parsed, ParsingMode.REPLACE, THROW_ERROR);
		System.out.println("\nParsed: " + parsed);
		System.out.println("classOf[sub] = " + parsed.get("sub").getClass());
		assertNull(parsed.get("sub.null"));
		assertNull(parsed.get("sub.nullObject"));
		assertSame(NULL_OBJECT, parsed.valueMap().get("null"));
		assertSame(NULL_OBJECT,	parsed.valueMap().get("nullObject"));
		assertEquals(BasicTestEnum.A, parsed.getEnum("enum", BasicTestEnum.class));
		assertEquals(12, parsed.<List<Integer>>get("list").get(1));
		assertEquals(Boolean.TRUE, parsed.<List<UnmodifiableConfig>>get("objectList").get(1).get("baz"));
		assertEquals("works", parsed.<String>get(Arrays.asList("not.a.subconfig")));
		assertEquals(config, parsed, "written != parsed");
	}

	@Test
	public void testYamlFormat() {
		YamlFormat f = YamlFormat.defaultInstance();
		assertTrue(f.supportsType(null));
		assertTrue(f.supportsType(String.class));
		assertTrue(f.supportsType(Boolean.class));
		assertTrue(f.supportsType(Integer.class));
		assertTrue(f.supportsType(Long.class));
		assertTrue(f.supportsType(Float.class));
		assertTrue(f.supportsType(Double.class));
		assertTrue(f.supportsType(List.class));
	}
}
