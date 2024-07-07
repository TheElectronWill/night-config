import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.electronwill.nightconfig.core.file.FileConfig;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Arrays;

public class ConfigSpecTests {
	private final ConfigSpec spec;
	{
		spec = new ConfigSpec();
		spec.define("a", "defaultA");
		spec.defineInRange("n", 1, 0, 10);
		spec.defineInList("sub.k", "A", Arrays.asList("A", "B", "C"));
	}

	@Test
	public void basicConfig() throws Exception {
		Config config = Config.of(InMemoryFormat.defaultInstance());
		testSpecOnConfig(config);
	}

	@Test
	public void syncFileConfig() throws Exception {
		FileConfig fileConfig = FileConfig.builder(Path.of("file.json")).sync().build();
		testSpecOnConfig(fileConfig);
	}

	@Test
	public void asyncFileConfig() throws Exception {
		FileConfig fileConfig = FileConfig.builder(Path.of("file.json")).async().build();
		testSpecOnConfig(fileConfig);
	}

	private void testSpecOnConfig(Config c) {
		assertFalse(spec.isCorrect(c));
		assertEquals(4, spec.correct(c, (action, path, incorrectValue, correctedValue) -> {
			System.out.println(action + "ed incorrect value " + path + " = " + incorrectValue + " => " + correctedValue);
		}));
		assertTrue(spec.isCorrect(c));

		c.set("a", 123);
		c.set("n", 20);
		assertFalse(spec.isCorrect(c));
		assertEquals(2, spec.correct(c));
		assertTrue(spec.isCorrect(c));

		c.remove("a");
		assertFalse(spec.isCorrect(c));
		assertEquals(1, spec.correct(c));
		assertTrue(spec.isCorrect(c));

		c.set("n", "bad type");
		assertFalse(spec.isCorrect(c));
		assertEquals(1, spec.correct(c));
		assertTrue(spec.isCorrect(c));

		c.set("wrong", "should not exist");
		assertFalse(spec.isCorrect(c));
		assertEquals(1, spec.correct(c));
		assertTrue(spec.isCorrect(c));

		c.set("sub", "bad type");
		assertFalse(spec.isCorrect(c));
		assertEquals(2, spec.correct(c)); // 1 for sub, 1 for sub.k
		assertTrue(spec.isCorrect(c));

		c.set("sub.k", "bad value");
		assertFalse(spec.isCorrect(c));
		assertEquals(1, spec.correct(c));
		assertTrue(spec.isCorrect(c));
		assertEquals("A", c.get("sub.k"));

		c.set("sub.k", "B");
		c.set("sub.wrong", "should not exist");
		assertFalse(spec.isCorrect(c));
		assertEquals(1, spec.correct(c));
		assertTrue(spec.isCorrect(c));
		assertEquals("B", c.get("sub.k"));

		c.remove("sub.k");
		assertFalse(spec.isCorrect(c));
		assertEquals(1, spec.correct(c));
		assertTrue(spec.isCorrect(c));
		assertEquals("A", c.get("sub.k"));
	}
}
