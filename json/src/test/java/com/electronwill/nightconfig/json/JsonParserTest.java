package com.electronwill.nightconfig.json;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingMode;

import java.io.File;
import java.util.HashMap;

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
}
