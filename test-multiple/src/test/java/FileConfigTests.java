import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class FileConfigTests {
	@Test
	public void asyncJson() throws URISyntaxException {
		Path jsonResource = Path.of(getClass().getResource("/simple_config.json").toURI());
		FileConfig fileConfig = FileConfig.builder(jsonResource).async().build();
		fileConfig.load();
		fileConfig.close();

		assertEquals(2024, fileConfig.getInt(List.of("year")));
		assertInstanceOf(List.class, fileConfig.get(List.of("dependencies", "testproject")));
		assertEquals("dep-a", (fileConfig.<List<Config>>get(List.of("dependencies", "testproject"))).get(0).get("a"));
		assertNull((fileConfig.<List<Config>>get(List.of("dependencies", "testproject"))).get(0).get("null"));
		assertTrue((fileConfig.<List<Config>>get(List.of("dependencies", "testproject"))).get(0).contains("null"));
	}

	@Test
	public void asyncToml() throws URISyntaxException {
		Path tomlResource = Path.of(getClass().getResource("/simple_config.toml").toURI());
		FileConfig fileConfig = FileConfig.builder(tomlResource).async().build();
		fileConfig.load();
		fileConfig.close();

		assertEquals(2024, fileConfig.getInt(List.of("year")));
		assertInstanceOf(List.class, fileConfig.get(List.of("dependencies", "testproject")));
		assertEquals("dep-a", (fileConfig.<List<Config>>get(List.of("dependencies", "testproject"))).get(0).get("a"));
	}

	@Test
	public void asyncForgeTest() throws URISyntaxException {
		Path forgeTestResource = Path.of(getClass().getResource("/forge_test.toml").toURI());
		FileConfig fileConfig = FileConfig.builder(forgeTestResource).async().build();
		fileConfig.load(); // initial load
		System.out.println("loaded: " + fileConfig);

		checkForgeTestContent(fileConfig);
		fileConfig.load(); // reload (should have the same content)
		checkForgeTestContent(fileConfig);
		fileConfig.close(); // close (should not modify the config)
		checkForgeTestContent(fileConfig);
	}

	@Test
	public void syncForgeTest() throws URISyntaxException {
		Path forgeTestResource = Path.of(getClass().getResource("/forge_test.toml").toURI());
		FileConfig fileConfig = FileConfig.builder(forgeTestResource).sync().build();
		fileConfig.load(); // initial load
		System.out.println("loaded: " + fileConfig);

		checkForgeTestContent(fileConfig);
		fileConfig.load(); // reload (should have the same content)
		checkForgeTestContent(fileConfig);
		fileConfig.close(); // close (should not modify the config)
		checkForgeTestContent(fileConfig);
	}

	@Test
	public void asyncMiniTest() throws Exception {
		Path forgeTestResource = Path.of(getClass().getResource("/mini_test.toml").toURI());
		FileConfig fileConfig = FileConfig.builder(forgeTestResource).async().build();
		fileConfig.load();
		assertEquals(1, fileConfig.getInt("v"));

		List<Config> arrayOfTables = fileConfig.get(List.of("a.b", "b.c"));
		assertEquals("value", arrayOfTables.get(0).get("key"));
	}

	@Test
	public void syncMiniTest() throws Exception {
		Path forgeTestResource = Path.of(getClass().getResource("/mini_test.toml").toURI());
		FileConfig fileConfig = FileConfig.builder(forgeTestResource).sync().build();
		fileConfig.load();
		assertEquals(1, fileConfig.getInt("v"));

		List<Config> arrayOfTables = fileConfig.get(List.of("a.b", "b.c"));
		assertEquals("value", arrayOfTables.get(0).get("key"));
	}

	void checkForgeTestContent(FileConfig fileConfig) {
		assertEquals("javafml", fileConfig.get("modLoader"));
		assertEquals("[3,)", fileConfig.get("loaderVersion"));
		assertEquals("CC0", fileConfig.get("license"));

		List<Config> mods = fileConfig.get("mods");
		Config mod0 = mods.get(0);
		assertEquals("testproject", mod0.get("modId"));
		assertEquals("A test project.", mod0.get("description"));

		List<Config> dependencies = fileConfig.get("dependencies.testproject");
		Config dep0 = dependencies.get(0);
		assertEquals("neoforge", dep0.get("modId"));
		assertEquals("required", dep0.get("type"));
		assertEquals("NONE", dep0.get("ordering"));
		assertEquals("BOTH", dep0.get("side"));
	}
}
