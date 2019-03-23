package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.TestEnum;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author TheElectronWill
 */
public class JsonConfigTest {
	private final Config config = JsonFormat.minimalInstance().createConfig();

	{
		Config config2 = Config.inMemory();
		config2.set("boolean", true);
		config2.set("false", false);

		config.set("string", "This is a string with a lot of characters to escape \n\r\t \\ \" ");
		config.set("int", 123456);
		config.set("long", 1234567890l);
		config.set("float", 0.123456f);
		config.set("double", 0.123456d);
		config.set("config", config2);
		config.set("list", Arrays.asList("a", "b", 3, null, true, false, 17.5));
		config.set("null", null);
		config.set("enum", TestEnum.A);
	}

	private final File file = new File("test.json");

	@Test
	public void testAuto() throws InterruptedException {
		FileConfig config = FileConfig.builder(file).autoreload().autosave().build();
		config.load();
		System.out.println(config);
		double d;
		for (int i = 0; i < 20; i++) {
			d = Math.random();
			config.set("double", d);
			Thread.sleep(20);
			//System.out.println(i + ":" + d);
			assertEquals(d, config.<Double>get("double").doubleValue());
		}
		for (int i = 0; i < 1000; i++) {
			config.set("double", 0.123456);
		}
		config.close();
	}

	@Test
	public void testWriteThenRead() throws IOException {
		FancyJsonWriter writer = new FancyJsonWriter();
		writer.write(config, file, WritingMode.REPLACE);

		Config read = new JsonParser().parse(file, FileNotFoundAction.THROW_ERROR);
		assertEquals(TestEnum.A, read.getEnum("enum", TestEnum.class));

		System.out.println("config: " + config);
		System.out.println("read: " + read);

		assertEquals(read.toString(), config.toString());
	}

	@Test
	public void testWrite() throws IOException {
		new FancyJsonWriter().setIndent(IndentStyle.SPACES_4).write(config, file, WritingMode.REPLACE);
	}

	@Test
	public void testRead() throws IOException {
		new JsonParser().parse(file, FileNotFoundAction.READ_NOTHING);
		System.out.println(config);
	}

	@Test
	public void testReadEmptyObject() throws IOException {
		Config conf = new JsonParser().parse("{}");
		System.out.println(conf);
		assertTrue(conf.isEmpty());
		assertThrows(ParsingException.class, () -> {
			new JsonParser().parse("{\"this\":12, }");
		});
	}

	@Test
	public void testReadEmptyFile() throws IOException {
		File f = new File("tmp.json");
		FileConfig config = FileConfig.of(f);
		config.load();
		config.close();
		Config conf = new JsonParser().parse(f, FileNotFoundAction.THROW_ERROR);
		System.out.println(conf);
		assertTrue(conf.isEmpty());
		System.out.println("tmp.json:\n" + Files.readAllLines(f.toPath()).get(0));
		f.delete();
	}

	@Test
	public void testEmptyDataTolerance() throws IOException {
		File f = new File("empty.json");
		assertEquals(0, f.length());

		FileConfig cDefault = FileConfig.of(f);
		assertThrows(ParsingException.class, cDefault::load);

		JsonFormat<?> f1 = JsonFormat.fancyInstance();
		FileConfig c1 = FileConfig.of(f, f1);
		assertThrows(ParsingException.class, c1::load);

		JsonFormat<?> f2 = JsonFormat.emptyTolerantInstance();
		FileConfig c2 = FileConfig.of(f, f2);
		c2.load();
		assertTrue(c2.isEmpty());

		assertEquals(0, f.length());

		assertThrows(ParsingException.class, ()->new JsonParser().parse(""));
		new JsonParser().setEmptyDataAccepted(true).parse("");
	}

	@Test
	public void testEmptyArray() throws IOException {
		List<?> list = new JsonParser().parseList("[]");
		assertEquals(0, list.size());

		Object obj = new JsonParser().parseDocument("[]");
		assertTrue(obj instanceof List);
		assertEquals(0, ((List<?>)obj).size());
	}

	@Test
	public void testIntegers() throws IOException {
		List<Object> l = new JsonParser().parseList("[-17,0,17,123456789000]");
		for (Object o : l) {
			System.out.println(o + ":" + o.getClass());
		}
		assertSame(l.get(0).getClass(), Integer.class);
		assertSame(l.get(1).getClass(), Integer.class);
		assertSame(l.get(2).getClass(), Integer.class);
		assertSame(l.get(3).getClass(), Long.class);
	}

	@Test
	public void testNestedArrays() throws IOException {
		List<?> a1 = new JsonParser().parseList("[[true, true], [false, false]]");
		assertEquals("[[true, true], [false, false]]", a1.toString());

		List<?> a2 = new JsonParser().parseList("[[[ [[],[    ]] ]]]");
		assertEquals("[[[[[], []]]]]", a2.toString());
	}

	@Test
	public void testNestedObjects() throws IOException {
		Config a1 = new JsonParser().parse("{\"1\":{\"a\":\"va\"}, \"2\":{\"b\":\"vb\", \"a\":17}}");
		assertEquals("va", a1.get("1.a"));
		assertEquals("vb", a1.get("2.b"));
		assertEquals(Integer.valueOf(17), a1.get("2.a"));

		Config a2 = new JsonParser().parse("{\"a\":{\"b\":{ \"c\": {\"d\":{},\"e\":{    }} }}}");
		assertTrue(a2.<Config>get("a.b.c.d").isEmpty());
		assertTrue(a2.<Config>get("a.b.c.e").isEmpty());
	}

	@Test
	public void testGenericList() throws IOException {
		List<Number> numberList = new JsonParser().parseList("[-17,0,17,123456789000]");
		for (Number o : numberList) {
			System.out.println(o.doubleValue());
		}

		List<String> stringList = new JsonParser().parseList("[\"a\", \"b\", \"c\"]");
		for (String o : stringList) {
			System.out.println(o.charAt(0));
		}

		List<List<Boolean>> nestedBoolList = new JsonParser().parseList("[[true, true], [false, false]]");
		for (List<Boolean> booleanList : nestedBoolList) {
			for (boolean b : booleanList) {
				System.out.println(!b);
			}
		}
	}

	@Test
	public void testFancyWriter() throws IOException {
		try (Writer fileWriter = new BufferedWriter(
			new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			FancyJsonWriter jsonWriter = new FancyJsonWriter().setIndent(IndentStyle.SPACES_4);
			jsonWriter.write(config, fileWriter);
		}// finally closes the writer
	}

	@Test
	public void testMinimalWriter() {
		StringWriter sw = new StringWriter();
		MinimalJsonWriter writer = new MinimalJsonWriter();
		writer.write(config, sw);
		System.out.println("Written:\n" + sw);
	}
}