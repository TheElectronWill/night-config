package yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.yaml.YamlFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;
import static com.electronwill.nightconfig.core.file.FileNotFoundAction.THROW_ERROR;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class YamlTest {

	private final File file = new File("test.yml");

	@Test
	public void testReadWrite() {
		Config config = Config.inMemory();
		config.set("null", null);
		config.set("nullObject", NULL_OBJECT);
		config.set("string", "this is a string");
		config.set("sub.null", null);
		config.set("sub.nullObject", NULL_OBJECT);

		System.out.println("Config: " + config);
		System.out.println("classOf[sub] = " + config.get("sub").getClass());
		System.out.println("sub.null = " + config.get("sub.null"));
		System.out.println("sub.nullObject = " + config.get("sub.nullObject"));
		YamlFormat yamlFormat = YamlFormat.defaultInstance();
		yamlFormat.createWriter().write(config, file, WritingMode.REPLACE);

		Config parsed = yamlFormat.createConcurrentConfig();
		yamlFormat.createParser().parse(file, parsed, ParsingMode.REPLACE, THROW_ERROR);
		System.out.println("\nParsed: " + parsed);
		System.out.println("classOf[sub] = " + parsed.get("sub").getClass());
		assertNull(parsed.get("sub.null"));
		assertNull(parsed.get("sub.nullObject"));
		assertSame(NULL_OBJECT, parsed.valueMap().get("null"));
		assertSame(NULL_OBJECT,parsed.valueMap().get("nullObject"));

		Assertions.assertEquals(config, parsed, "Error: written != parsed");
	}
}
