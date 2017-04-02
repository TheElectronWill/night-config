package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.SimpleConfig;
import com.electronwill.nightconfig.core.io.FileConfig;
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
	private final FileConfig config = new JsonConfig();

	{
		Config config2 = new SimpleConfig();
		config2.setValue("boolean", true);
		config2.setValue("false", false);

		config.setValue("string",
						"This is a string with a lot of characters to escape \n\r\t \\ \" ");
		config.setValue("int", 123456);
		config.setValue("long", 1234567890l);
		config.setValue("float", 0.123456f);
		config.setValue("double", 0.123456d);
		config.setValue("config", config2);
		config.setValue("list", Arrays.asList("a", "b", 3, null, true, false, 17.5));
		config.setValue("null", null);
	}

	private final File file = new File("test.json");

	@Test
	public void testWrite() throws IOException {
		config.write(file);
	}

	@Test
	public void testRead() throws IOException {
		config.parse(file);
		System.out.println(config);
	}

	@Test
	public void testFancyWriter() throws IOException {
		try (Writer fileWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			FancyJsonWriter jsonWriter = new FancyJsonWriter();
			jsonWriter.setNewline("\r");
			jsonWriter.setIndent("    ");
			jsonWriter.writeConfig(config, fileWriter);
		}// finally closes the writer
	}

	@Test
	public void testMinimalWriter() {
		StringWriter sw = new StringWriter();
		MinimalJsonWriter writer = new MinimalJsonWriter();
		writer.writeConfig(config, sw);
		System.out.println("Written:\n" + sw);
	}
}