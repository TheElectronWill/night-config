package com.electronwill.nightconfig.core.concurrent;

import java.util.function.Consumer;
import java.util.function.Function;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * Interface for thread-safe configurations.
 * 
 * See the package documentation for more information: {@link com.electronwill.nightconfig.core.concurrent}.
 */
public interface ConcurrentConfig extends Config {
    /**
     * Performs multiple reads as a single atomic operation and returns a value.
     * <p>
     * It is guaranteed that the content of the config, accessed through the provided "view", will not change
     * during the entire operation. The configuration must be used exclusively through the object passed to the
     * consumer (the "view"), <b>not</b> with the original reference.
     * After the bulk operation, the view cannot be used anymore.
     * <p>
     * Here is an example:
     * 
     * <pre>
     * {@code
     * String combined = config.bulkUpdate(conf -> {
     *     String a = conf.get("a");
     *     String b = conf.get("b");
     *     return a + b;
     *     // note that we use `conf` and not `config`!
     * });
     * }
     * </pre>
     * 
     * @param action a function to execute on the configuration view
     * @param <R>    the type of the function's result
     * @return the result of the function
     */
    <R> R bulkRead(Function<? super UnmodifiableConfig, R> action);

    /**
     * Performs multiple reads as a single atomic operation.
     * <p>
     * It is guaranteed that the content of the config, accessed through the provided "view", will not change
     * during the entire operation. The configuration must be used exclusively through the object passed to the
     * consumer (the "view"), <b>not</b> with the original reference.
     * After the bulk operation, the view cannot be used anymore.
     * <p>
     * Here is an example:
     * 
     * <pre>
     * {@code
     * config.bulkRead(conf -> {
     *     String a = conf.get("a");
     *     String b = conf.get("b");
     *     // note that we use `conf` and not `config`!
     * });
     * }
     * </pre>
     * 
     * @param action a function to execute on the configuration view
     */
    default void bulkRead(Consumer<? super UnmodifiableConfig> action) {
        bulkRead(config -> {
            action.accept(config);
            return null;
        });
    }

    /**
     * Performs multiple reads and writes as a single atomic operation and returns a value.
     * <p>
     * It is guaranteed that the content of the config, accessed through the provided "view", will not be
     * concurrently modified by other threads during the entire bulk operation.
     * The configuration must be used exclusively through the object passed to the consumer
     * (the "view"), <b>not</b> with the original reference.
     * After the bulk operation, the view cannot be used anymore.
     * <p>
     * Here is an example:
     * 
     * <pre>
     * {@code
     * String combined = config.bulkUpdate(conf -> {
     *     String a = conf.get("a");
     *     String b = conf.get("b");
     *     String combined = a + b;
     *     conf.set("combined", combined);
     *     return combined;
     *     // note that we use `conf` and not `config`!
     * });
     * }
     * </pre>
     * 
     * <p>
     * If you only have to read the configuration, prefer to use {@link #bulkRead(Function)},
     * since it may provide a better performance.
     * 
     * @param action a function to execute on the configuration view
     * @param <R>    the type of the function's result
     * @return the result of the function
     */
    <R> R bulkUpdate(Function<? super Config, R> action);

    /**
     * Performs multiple reads and writes as a single atomic operation.
     * <p>
     * It is guaranteed that the content of the config, accessed through the provided "view", will not be
     * concurrently modified by other threads during the entire bulk operation.
     * The configuration must be used exclusively through the object passed to the consumer
     * (the "view"), <b>not</b> with the original reference.
     * After the bulk operation, the view cannot be used anymore.
     * <p>
     * Here is an example:
     * 
     * <pre>
     * {@code
     * config.bulkUpdate(conf -> {
     *     String a = conf.get("a");
     *     String b = conf.get("b");
     *     String combined = a + b;
     *     conf.set("combined", combined);
     *     // note that we use `conf` and not `config`!
     * });
     * }
     * </pre>
     * <p>
     * If you only have to read the configuration, prefer to use {@link #bulkRead(Consumer)},
     * since it may provide a better performance.
     * 
     * @param action a function to execute on the configuration view
     */
    default void bulkUpdate(Consumer<? super Config> action) {
        bulkUpdate(config -> {
            action.accept(config);
            return null;
        });
    }

    /**
     * Creates a new configuration that is meant to be inserted into this config.
     * <p>
     * Every sub-config <b>must</b> be created with this method, mixing
     * different types of sub-configs in {@code ConcurrentConfig}s is invalid.
     * 
     * @return a new sub-config
     */
    @Override
    ConcurrentConfig createSubConfig();
}
