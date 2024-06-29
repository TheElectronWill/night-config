package com.electronwill.nightconfig.core.file;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.electronwill.nightconfig.core.io.ParsingMode;

public class SyncFileConfigTest {
    @TempDir
    static Path tmp;

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
