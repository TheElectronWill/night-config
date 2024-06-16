package com.electronwill.nightconfig.core.file;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;

public class AsyncFileConfigTest {
    @TempDir
    static Path tmp;

    @Test
    public void testWriteDelay() throws IOException, InterruptedException {
        Path file = tmp.resolve("fileNotifications.txt"); // does not exist yet
        var format = new Util.TestFormat(true);
        var autosaveCounter = new AtomicInteger(0);
        var saveCounter = new AtomicInteger(0);
        var config = FileConfig.builder(file, format)
            .autosave()
            .writingMode(WritingMode.REPLACE)
            .onAutoSave(autosaveCounter::incrementAndGet)
            .onSave(saveCounter::incrementAndGet)
            .build();

        // The config will be saved automatically after some delay. Check the delay
        for (int i = 0; i < 500; i++) {
            config.set("a", "b"); // save() is called but the events are debounced
            assertEquals(-1, format.writeTime);
            assertEquals(i+1, autosaveCounter.get());
            assertEquals(0, saveCounter.get());
            assertTrue(Files.notExists(file) || Files.size(file) == 0);
        }

        Thread.sleep(AsyncFileConfig.DEFAULT_WRITE_DEBOUNCE_TIME.toMillis() / 2);
        assertEquals(-1, format.writeTime);
        assertEquals(500, autosaveCounter.get());
        assertEquals(0, saveCounter.get());
        assertTrue(Files.notExists(file) || Files.size(file) == 0);

        Thread.sleep(AsyncFileConfig.DEFAULT_WRITE_DEBOUNCE_TIME.toMillis() + 100);
        assertNotEquals(-1, format.writeTime);
        assertEquals(500, autosaveCounter.get());
        assertEquals(1, saveCounter.get());
        assertTrue(Files.exists(file));
        assertEquals("a = b", Files.readString(file).trim());

        config.close();
        assertNotEquals(-1, format.writeTime);
        assertTrue(Files.exists(file));
        assertEquals("a = b", Files.readString(file).trim());
    }

    @Test
    public void testCustomWriteDelay() throws IOException, InterruptedException {
        Path file = tmp.resolve("fileNotificationsCustom.txt"); // does not exist yet
        var format = new Util.TestFormat(true);
        var config = FileConfig.builder(file, format)
            .autosave()
            .writingMode(WritingMode.REPLACE)
            .asyncWithDebouncing(Duration.ofMillis(500))
            .build();

        // The config will be saved automatically after some delay. Check the delay
        for (int i = 0; i < 100; i++) {
            config.set("a", "b"); // save() is called but the events are debounced
            assertEquals(-1, format.writeTime);
            assertTrue(Files.notExists(file) || Files.size(file) == 0);
        }

        Thread.sleep(500 / 2);
        assertEquals(-1, format.writeTime);
        assertTrue(Files.notExists(file) || Files.size(file) == 0);

        Thread.sleep(500 + 100);
        assertNotEquals(-1, format.writeTime);
        assertTrue(Files.exists(file));
        assertEquals("a = b", Files.readString(file).trim());

        config.close();
        assertNotEquals(-1, format.writeTime);
        assertTrue(Files.exists(file));
        assertEquals("a = b", Files.readString(file).trim());
    }

    @Test
    public void testReplace() throws IOException {
        Path file = tmp.resolve("syncFileConfig.txt");
        Util.testLoadReplace(newConfig(file, ParsingMode.REPLACE, true));
        Util.testLoadReplace(newConfig(file, ParsingMode.REPLACE, false));
    }

    @Test
    public void testAdd() throws IOException {
        Path file = tmp.resolve("syncFileConfig.txt");
        Util.testLoadAdd(newConfig(file, ParsingMode.ADD, true));
        Util.testLoadAdd(newConfig(file, ParsingMode.ADD, false));
    }

    @Test
    public void testMerge() throws IOException {
        Path file = tmp.resolve("syncFileConfig.txt");
        Util.testLoadMerge(newConfig(file, ParsingMode.MERGE, true));
        Util.testLoadMerge(newConfig(file, ParsingMode.MERGE, false));
    }

    private FileConfig newConfig(Path file, ParsingMode parsingMode, boolean useProperSubConfigType) {
        var format = new Util.TestFormat(useProperSubConfigType);
        return FileConfig.builder(file, format).sync().parsingMode(parsingMode).build();
    }

}
