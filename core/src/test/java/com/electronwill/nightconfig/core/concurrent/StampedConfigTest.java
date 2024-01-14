package com.electronwill.nightconfig.core.concurrent;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;

public class StampedConfigTest {
    private StampedConfig newConfig() {
        return new StampedConfig(InMemoryCommentedFormat.defaultInstance(),
                Config.getDefaultMapCreator(false));
    }

    @Test
    public void basicSanity() {
        CommonTests.testBasicSanity(newConfig());
    }

    @Test
    public void errors() {
        CommonTests.testErrors(newConfig());
    }

    @Test
    public void comments() {
        CommonTests.testComments(newConfig());
    }

    @Test
    public void iterators() {
        CommonTests.testIterators(newConfig());
    }

    @Test
    public void concurrentCounters() throws InterruptedException {
        CommonTests.testConcurrentCounters(newConfig());
    }
}
