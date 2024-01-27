package com.electronwill.nightconfig.core.concurrent;

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
        var nThreads = 4;
        var executor = Executors.newFixedThreadPool(nThreads);
        var keys = Arrays.asList("a", "b", "c", "e", "sub.a", "sub.b", "sub.nested.a",
                "sub.nested.b");
        var oldValues = keys.stream().map(k -> "old value of " + k).collect(Collectors.toList());
        var newValues = keys.stream().map(k -> "new value of " + k).collect(Collectors.toList());

        var config = newConfig();

        // fill config
        for (int i = 0; i < keys.size(); i++) {
            var key = keys.get(i);
            var val = oldValues.get(i);
            config.set(key, val);
        }

        // From multiple threads, check the integrity of the config: either full old version, or full new version.
        CommonTests.checkConcurrentConfigIntegrity(executor, nThreads, config, keys, oldValues,
                newValues);

        // fill accumulator
        var acc = StampedConfigTest.newAccumulator();
        for (int i = 0; i < keys.size(); i++) {
            var key = keys.get(i);
            var val = oldValues.get(i);
            acc.set(key, val);
        }

        // replace the config's content, the tasks submitted to the executor should stop when they see the new content
        config.replaceContentBy(acc);

        // wait for the tasks to finish
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    public void replaceContentByConfig() throws InterruptedException {
        var nThreads = 4;
        var executor = Executors.newFixedThreadPool(nThreads);
        var keys = Arrays.asList("a", "b", "c", "e", "sub.a", "sub.b", "sub.nested.a",
                "sub.nested.b");
        var oldValues = keys.stream().map(k -> "old value of " + k).collect(Collectors.toList());
        var newValues = keys.stream().map(k -> "new value of " + k).collect(Collectors.toList());

        var config = newConfig();

        // fill config
        for (int i = 0; i < keys.size(); i++) {
            var key = keys.get(i);
            var val = oldValues.get(i);
            config.set(key, val);
        }

        // From multiple threads, check the integrity of the config: either full old version, or full new version.
        CommonTests.checkConcurrentConfigIntegrity(executor, nThreads, config, keys, oldValues,
                newValues);

        // fill new config
        var newConfig = newConfig();
        for (int i = 0; i < keys.size(); i++) {
            var key = keys.get(i);
            var val = oldValues.get(i);
            newConfig.set(key, val);
        }

        // replace the config's content, the tasks submitted to the executor should stop when they see the new content
        config.replaceContentBy(newConfig);

        // wait for the tasks to finish
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }
}
