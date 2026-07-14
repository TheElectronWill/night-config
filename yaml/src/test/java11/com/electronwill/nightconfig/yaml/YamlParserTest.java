package com.electronwill.nightconfig.yaml;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.HashMap;

public class YamlParserTest {
    @Test
    public void read() {
        Config config = new YamlParser().parse(new File("test_simple.yml"), FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
    }

    @Test
	public void readToSynchronizedConfig() {
		File f = new File("test_simple.yml");
		SynchronizedConfig config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		new YamlParser().parse(f, config, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
	}

	@Test
	public void readToStampedConfig() {
		File f = new File("test_simple.yml");
		StampedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		new YamlParser().parse(f, config, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
	}

	@Test
	public void rejectUntrustedInputByDefault() {
		String bad = "u: !!javax.script.ScriptEngineManager [ !!java.net.URLClassLoader [[ !!java.net.URL [\"file:///dev/null\"] ]] ]";
		assertThrows(ParsingException.class, () -> new YamlParser().parse(bad));
	}
}
