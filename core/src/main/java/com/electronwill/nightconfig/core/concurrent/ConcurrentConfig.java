package com.electronwill.nightconfig.core.concurrent;

import java.util.function.Consumer;
import java.util.function.Function;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/** Interface for thread-safe configurations. */
public interface ConcurrentConfig extends Config {
    /** Performs multiple read operations in a thread-safe way.
     * No modification of the config is allowed during the reads.
     */
    <R> R bulkRead(Function<? super UnmodifiableConfig, R> action);

    /**
     * Performs multiple read/write operations in a thread-safe way.
     * No concurrent modification of the config is allowed.
     * <p>
     * If you only have to read the configuration, prefer to use {@link #bulkRead(Consumer)},
     * since it may provide a better performance.
     */
    <R> R bulkUpdate(Function<? super Config, R> action);
}
