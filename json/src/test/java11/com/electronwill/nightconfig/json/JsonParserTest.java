package com.electronwill.nightconfig.json;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class JsonParserTest {
	@Test
	public void read() {
		Config config = new JsonParser().parse(new File("test.json"), FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
	}

	@Test
	public void readToSynchronizedConfig() {
		File f = new File("test.json");
		SynchronizedConfig config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		new JsonParser().parse(f, config, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
	}

	@Test
	public void readToStampedConfig() {
		File f = new File("test.json");
		StampedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		new JsonParser().parse(f, config, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void readSpaced() {
		Config config = new JsonParser().parse("    {}    ");
		assertTrue(config.isEmpty());

		config = (Config)new JsonParser().parseDocument("    {}    ");
		assertTrue(config.isEmpty());

		List<Object> array = (List<Object>)new JsonParser().parseDocument("    []    ");
		assertTrue(array.isEmpty());

		array = (List<Object>)new JsonParser().parseList("    []    ");
		assertTrue(array.isEmpty());
	}

	@Test
	public void parseInvalidDocument() {
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseDocument("{}abcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseDocument("abcdefg{}");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseDocument("{}    \nabcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseDocument("[]abcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseDocument("[]    \nabcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseDocument("a");
		});
	}

	@Test
	public void parseInvalidObject() {
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parse("{}abcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parse("abcdefg{}");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parse("{}    \nabcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parse("[]"); // not an object
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parse("[1,2,3,4]"); // not an object
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parse("[]abcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parse("[]    \nabcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parse("a");
		});
	}

	@Test
	public void parseInvalidList() {
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseList("[]abcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseList("abcdefg[]");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseList("{}"); // not a list
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseList("[]abcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseList("[]    \nabcdefg");
		});
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parseList("a");
		});
	}
}
