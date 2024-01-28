package com.electronwill.nightconfig.core.concurrent;

import java.util.function.Consumer;
import java.util.function.Function;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/** Interface for thread-safe configurations with comments. */
public interface ConcurrentCommentedConfig extends CommentedConfig, ConcurrentConfig {
    /**
     * Performs multiple read operations in a thread-safe way.
     * No modification of the config is allowed during the reads.
     * <p>
     * If you don't need access to the comments, prefer to use {@link #bulkRead(Function)},
     * sine it may provide a better performance.
     */
    <R> R bulkCommentedRead(Function<? super UnmodifiableCommentedConfig, R> action);

    /**
     * Performs multiple read operations in a thread-safe way.
     * No modification of the config is allowed during the reads.
     * <p>
     * If you don't need access to the comments, prefer to use {@link #bulkRead(Consumer)},
     * sine it may provide a better performance.
     */
    default void bulkCommentedRead(Consumer<? super UnmodifiableCommentedConfig> action) {
        bulkCommentedRead(config -> {
            action.accept(config);
            return null;
        });
    }

    /**
     * Performs multiple read/write operations in a thread-safe way.
     * No concurrent modification of the config is allowed.
     * <p>
     * If you don't need access to the comments, prefer to use {@link #bulkUpdate(Function)},
     * since it may provide a better performance. Also, if you only have to read the configuration,
     * prefer to use {@link #bulkCommentedRead(Function)}.
     */
    <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action);

    /**
     * Performs multiple read/write operations in a thread-safe way.
     * No concurrent modification of the config is allowed.
     * <p>
     * If you don't need access to the comments, prefer to use {@link #bulkUpdate(Consumer)},
     * since it may provide a better performance. Also, if you only have to read the configuration,
     * prefer to use {@link #bulkCommentedRead(Consumer)}.
     */
    default void bulkCommentedUpdate(Consumer<? super CommentedConfig> action) {
        bulkCommentedUpdate(config -> {
            action.accept(config);
            return null;
        });
    }

    @Override
    default <R> R bulkRead(Function<? super UnmodifiableConfig, R> action) {
        return bulkCommentedRead(action);
    }

    @Override
    default <R> R bulkUpdate(Function<? super Config, R> action) {
        return bulkCommentedUpdate(action);
    }

    @Override
    ConcurrentCommentedConfig createSubConfig();
}
