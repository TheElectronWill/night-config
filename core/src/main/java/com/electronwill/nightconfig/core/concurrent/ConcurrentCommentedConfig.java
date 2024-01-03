package com.electronwill.nightconfig.core.concurrent;

import java.util.function.Consumer;
import java.util.function.Function;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/** Interface for thread-safe configurations with comments. */
public interface ConcurrentCommentedConfig extends CommentedConfig, ConcurrentConfig {
    /** Performs multiple read operations in a thread-safe way.
     * No modification of the config is allowed during the reads.
     */
    <R> R bulkCommentedRead(Function<? super UnmodifiableCommentedConfig, R> action);

    /**
     * Performs multiple read/write operations in a thread-safe way.
     * No concurrent modification of the config is allowed.
     * <p>
     * If you only have to read the configuration, prefer to use {@link #bulkCommentedRead(Consumer)},
     * since it may provide a better performance.
     */
    <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action);

    @Override
    default <R> R bulkRead(Function<? super UnmodifiableConfig, R> action) {
        return bulkCommentedRead(action);
    }

    @Override
    default <R> R bulkUpdate(Function<? super Config, R> action) {
        return bulkCommentedUpdate(action);
    }
}
