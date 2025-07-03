package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.WritingMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AtomicWriteTest {

	@TempDir
	static Path tmp;

	/**
	 * Regression test for <a href="https://github.com/TheElectronWill/night-config/issues/195">issue 195</a>,
	 * that occurred when saving to a relative path with multiple components.
	 */
	@Test
	public void testAtomicWrite() throws IOException {
		// Do a bit of work to create a relative path with multiple components
		Path tmpRelative = Paths.get(".").toAbsolutePath().relativize(tmp);
		Path configPath = tmpRelative.resolve("a/b/config.txt");
		Files.createDirectories(configPath.getParent());

		// Trivial config
		var format = new Util.TestFormat(true);
		var config = Config.inMemory();
		config.set("test", true);

		// Try to save it
		format.createWriter().write(config, configPath, WritingMode.REPLACE_ATOMIC);

		assertTrue(Files.exists(configPath));
	}
}
