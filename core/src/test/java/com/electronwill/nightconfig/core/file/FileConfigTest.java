package com.electronwill.nightconfig.core.file;

import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingException;

public class FileConfigTest {
    @TempDir
    static Path tmp;

    @Test
    public void testWriteDelay() throws IOException, InterruptedException {
        Path file = tmp.resolve("fileNotifications.txt"); // does not exist yet
        TestFormat format = new TestFormat();
        var config = FileConfig.builder(file, format).autosave().build();

        // The config will be saved automatically after some delay. Check the delay
        for (int i = 0; i < 1000; i++) {
            config.set("a", "b"); // save() is called but the events are debounced
            assertEquals(-1, format.writeTime);
            assertTrue(Files.notExists(file) || Files.size(file) == 0);
        }

        Thread.sleep(AsyncFileConfig.DEFAULT_WRITE_DEBOUNCE_TIME.toMillis() / 2);
        assertEquals(-1, format.writeTime);
        assertTrue(Files.notExists(file) || Files.size(file) == 0);

        Thread.sleep(AsyncFileConfig.DEFAULT_WRITE_DEBOUNCE_TIME.toMillis() + 100);
        assertNotEquals(-1, format.writeTime);
        assertTrue(Files.exists(file));
        assertEquals("a = b", Files.readString(file).trim());

        config.close();
        assertNotEquals(-1, format.writeTime);
        assertTrue(Files.exists(file));
        assertEquals("a = b", Files.readString(file).trim());
    }

    private static final class TestFormat implements ConfigFormat<CommentedConfig> {
        volatile long writeTime = -1;

        @Override
        public CommentedConfig createConfig(Supplier<Map<String, Object>> mapCreator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ConfigParser<CommentedConfig> createParser() {
            return new TestParser();
        }

        @Override
        public ConfigWriter createWriter() {
            return new TestWriter(this);
        }

        @Override
        public boolean supportsComments() {
            return true;
        }
    }

    private static final class TestWriter implements ConfigWriter {
        private final TestFormat format;

        TestWriter(TestFormat format) {
            this.format = format;
        }

        @Override
        public void write(UnmodifiableConfig config, Writer writer) {
            format.writeTime = System.currentTimeMillis();
            for (UnmodifiableConfig.Entry e : config.entrySet()) {
                try {
                    writer.write(e.getKey() + " = " + e.getValue());
                    writer.flush();
                } catch (IOException ex) {
                    throw new WritingException(ex);
                }
            }
        }
    }

    private static final class TestParser implements ConfigParser<CommentedConfig> {

        @Override
        public ConfigFormat<CommentedConfig> getFormat() {
            return InMemoryCommentedFormat.defaultInstance();
        }

        @Override
        public CommentedConfig parse(Reader reader) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void parse(Reader reader, Config destination, ParsingMode parsingMode) {
            throw new UnsupportedOperationException();
        }

    }

}
