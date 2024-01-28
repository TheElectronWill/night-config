package com.electronwill.nightconfig.core.concurrent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;

public class SynchronizedConfigTest {
    static SynchronizedConfig newConfig() {
        return new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(),
                Config.getDefaultMapCreator(false));
    }

    @Test
    public void basicSanity() {
        CommonTests.testBasicSanity(newConfig());
    }

    @Test
    public void comments() {
        CommonTests.testComments(newConfig());
    }

    @Test
    public void errors() {
        CommonTests.testErrors(newConfig());
    }

    @Test
    public void putAll() {
        CommonTests.testPutAll(newConfig(), newConfig());
        CommonTests.testPutAll(newConfig(), StampedConfigTest.newAccumulator());
    }

    @Test
    public void removeAll() {
        CommonTests.testRemoveAll(newConfig(), newConfig());
        CommonTests.testRemoveAll(newConfig(), StampedConfigTest.newAccumulator());
    }

    @Test
    public void putAllComments() {
        CommonTests.testPutAllComments(newConfig(), newConfig());
        CommonTests.testPutAllComments(newConfig(), StampedConfigTest.newAccumulator());
    }

    @Test
    public void iterators() {
        CommonTests.testIterators(newConfig());
    }

    @Test
    public void concurrentCounters() throws InterruptedException {
        CommonTests.testConcurrentCounters(newConfig());
    }

    @Test
    public void bulk() {
        CommonTests.testBulkOperations(newConfig());
    }

    @Test
    public void replaceContentByAccumulator() throws InterruptedException {
        CommonTests.testReplaceContent(4, newConfig(), StampedConfigTest.newAccumulator(), (a,b) -> a.replaceContentBy(b));
    }

    @Test
    public void replaceContentByConfig() throws InterruptedException {
        CommonTests.testReplaceContent(4, newConfig(), newConfig(), (a,b) -> a.replaceContentBy(b));
    }
}
