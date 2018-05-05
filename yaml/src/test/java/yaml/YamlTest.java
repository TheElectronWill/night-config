package yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.yaml.YamlFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

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
		yamlFormat.createParser().parse(file, parsed, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		System.out.println("\nParsed: " + parsed);
		System.out.println("classOf[sub] = " + parsed.get("sub").getClass());
		assert parsed.get("sub.null") == null;
		assert parsed.get("sub.nullObject") == null;
		assert parsed.valueMap().get("null") == NULL_OBJECT;
		assert parsed.valueMap().get("nullObject") == NULL_OBJECT;

		Assertions.assertEquals(config, parsed, "Error: written != parsed");
	}
}
