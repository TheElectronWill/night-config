package com.electronwill.nightconfig.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;

/**
 * @author TheElectronWill
 */
public class JsonWriterTest {
    @Test
    public void write() throws IOException {
        CommentedConfig config = CommentedConfig.inMemory();
        Util.populateTest(config);

        var fancy = new FancyJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_FANCY, fancy);

        var minimal = new MinimalJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_MINIMAL, minimal);
    }

    @Test
    public void writeSynchronizedConfig() {
        CommentedConfig config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(),
                HashMap::new);
        Util.populateTest(config);

        var fancy = new FancyJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_FANCY, fancy);

        var minimal = new MinimalJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_MINIMAL, minimal);
    }

    @Test
    public void writeStampedConfig() {
        CommentedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(),
                HashMap::new);
        Util.populateTest(config);

        var fancy = new FancyJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_FANCY, fancy);

        var minimal = new MinimalJsonWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED_MINIMAL, minimal);
    }

}