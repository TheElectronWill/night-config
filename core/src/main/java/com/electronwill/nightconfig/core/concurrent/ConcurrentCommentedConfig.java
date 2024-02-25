package com.electronwill.nightconfig.core.concurrent;

import java.util.function.Consumer;
import java.util.function.Function;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * Interface for thread-safe configurations with comments.
 *
 * See the package documentation for more information: {@link com.electronwill.nightconfig.core.concurrent}.
 */
public interface ConcurrentCommentedConfig extends CommentedConfig, ConcurrentConfig {
    /**
     * Performs multiple reads as a single atomic operation, and returns a value.
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
     * config.bulkCommentedRead(conf -> {
     *     Object value = conf.get("a.b.c");
     *     String comment = conf.getComment("a.b.c");
     *     // note that we use `conf` and not `config`!
     * });
     * }
     * </pre>
     * <p>
     * If you don't need access to the comments, prefer to use {@link #bulkRead(Function)},
     * sine it may provide a better performance.
     * 
     * @param action a function to execute on the configuration view
     * @param <R>    the type of the function's result
     * @return the result of the function
     */
    <R> R bulkCommentedRead(Function<? super UnmodifiableCommentedConfig, R> action);

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
     * Object value = config.bulkCommentedRead(conf -> {
     *     Object value = conf.get("a.b.c");
     *     String comment = conf.getComment("a.b.c");
     *     // note that we use `conf` and not `config`!
     *     return value;
     * });
     * }
     * </pre>
     * <p>
     * If you don't need access to the comments, prefer to use {@link #bulkRead(Consumer)},
     * sine it may provide a better performance.
     * 
     * @param action a function to execute on the configuration view
     */
    default void bulkCommentedRead(Consumer<? super UnmodifiableCommentedConfig> action) {
        bulkCommentedRead(config -> {
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
     * Object value = config.bulkCommentedUpdate(conf -> {
     *     Object value = conf.get("a.b.c");
     *     String comment = conf.getComment("a.b.c");
     *     conf.setComment("a.b.c", comment + "\nAdditional comment here.");
     *     // note that we use `conf` and not `config`!
     *     return value;
     * });
     * }
     * </pre>
     * 
     * <p>
     * If you don't need access to the comments, prefer to use {@link #bulkUpdate(Function)},
     * since it may provide a better performance. Also, if you only have to read the configuration,
     * prefer to use {@link #bulkCommentedRead(Function)}.
     * 
     * @param action a function to execute on the configuration view
     * @param <R>    the type of the function's result
     * @return the result of the function
     */
    <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action);

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
     * config.bulkCommentedUpdate(conf -> {
     *     Object value = conf.get("a.b.c");
     *     String comment = conf.getComment("a.b.c");
     *     conf.setComment("a.b.c", comment + "\nAdditional comment here.");
     *     // note that we use `conf` and not `config`!
     * });
     * }
     * </pre>
     * 
     * <p>
     * If you don't need access to the comments, prefer to use {@link #bulkUpdate(Consumer)},
     * since it may provide a better performance. Also, if you only have to read the configuration,
     * prefer to use {@link #bulkCommentedRead(Consumer)}.
     * 
     * @param action a function to execute on the configuration view
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
