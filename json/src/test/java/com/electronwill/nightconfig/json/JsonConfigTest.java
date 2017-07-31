package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
public class JsonConfigTest {
	private final Config config = JsonFormat.minimalInstance().createConfig();

	{
		Config config2 = Config.inMemory();
		config2.set("boolean", true);
		config2.set("false", false);

		config.set("string",
						"This is a string with a lot of characters to escape \n\r\t \\ \" ");
		config.set("int", 123456);
		config.set("long", 1234567890l);
		config.set("float", 0.123456f);
		config.set("double", 0.123456d);
		config.set("config", config2);
		config.set("list", Arrays.asList("a", "b", 3, null, true, false, 17.5));
		config.set("null", null);
	}

	private final File file = new File("test.json");

	@Test
	public void testWriteThenRead() throws IOException {
		FancyJsonWriter writer = new FancyJsonWriter();
		writer.write(config, file, WritingMode.REPLACE);

		Config read = new JsonParser().parse(file, FileNotFoundAction.THROW_ERROR);

		System.out.println("config: " + config);
		System.out.println("read: " + read);
	}

	@Test
	public void testWrite() throws IOException {
		new FancyJsonWriter().write(config, file, WritingMode.REPLACE);
	}

	@Test
	public void testRead() throws IOException {
		new JsonParser().parse(file, FileNotFoundAction.READ_NOTHING);
		System.out.println(config);
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