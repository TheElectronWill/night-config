package com.electronwill.nightconfig.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;

public class YamlWriterTest {
	@Test
	public void write() {
		Config config = YamlFormat.defaultInstance().createConfig();
		Util.populateTest(config);
		var result = new YamlWriter().writeToString(config);
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}

	@Test
	public void writeSynchronizedConfig() {
		Config config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(),
				HashMap::new);
		Util.populateTest(config);
		var result = new YamlWriter().writeToString(config);
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}

	@Test
	public void writeStampedConfig() {
		StampedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(),
				HashMap::new);
		Util.populateTest(config);
		var result = new YamlWriter().writeToString(config);
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}
}
