package com.electronwill.nightconfig.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

/**
 * @author TheElectronWill
 */
public class JsonWriterTest {
    @TempDir
    Path tmp;

    @Test
    public void write() throws IOException {
        Config config = Config.inMemory();
        Util.populateTest(config);

        var fancy = new FancyJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_FANCY, fancy);

        var minimal = new MinimalJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_MINIMAL, minimal);
    }

    @Test
    public void writeSynchronizedConfig() {
        Config config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(),
                HashMap::new);
        Util.populateTest(config);

        var fancy = new FancyJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_FANCY, fancy);

        var minimal = new MinimalJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_MINIMAL, minimal);
    }

    @Test
    public void writeStampedConfig() {
        Config config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(),
                HashMap::new);
        Util.populateTest(config);

        var fancy = new FancyJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_FANCY, fancy);

        var minimal = new MinimalJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_MINIMAL, minimal);
    }

    @Test
    public void writeAppend() throws IOException {
        var config = Config.inMemory();
        Util.populateTest(config);

        Path configFile = tmp.resolve("appended.json");
        new MinimalJsonWriter().write(config, configFile, WritingMode.APPEND);
        assertEquals(Util.EXPECTED_SERIALIZED_MINIMAL, Files.readString(configFile));

        new MinimalJsonWriter().write(config, configFile, WritingMode.APPEND);
        assertEquals(Util.EXPECTED_SERIALIZED_MINIMAL + Util.EXPECTED_SERIALIZED_MINIMAL, Files.readString(configFile));
    }

    @Test
    public void writeAtomic() throws IOException {
        var config = Config.inMemory();
        Util.populateTest(config);

        Path configFile = tmp.resolve("config.json");
        Path tmpFile = tmp.resolve("config.json.new.tmp");
        assertFalse(Files.exists(configFile));
        assertFalse(Files.exists(tmpFile));

        new FancyJsonWriter().write(config, configFile, WritingMode.REPLACE_ATOMIC);
        assertFalse(Files.exists(tmpFile));
        assertTrue(Files.exists(configFile));
        assertEquals(Util.EXPECTED_SERIALIZED_FANCY, Files.readString(configFile));

        new MinimalJsonWriter().write(config, configFile, WritingMode.REPLACE_ATOMIC);
        assertFalse(Files.exists(tmpFile));
        assertTrue(Files.exists(configFile));
        assertEquals(Util.EXPECTED_SERIALIZED_MINIMAL, Files.readString(configFile));
    }

}