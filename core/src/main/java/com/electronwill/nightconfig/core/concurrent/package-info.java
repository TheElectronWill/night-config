/**
 * Provides configurations that can be used from multiple threads. This package exists in order to solve the
 * following problem.
 *
 * <h2>The problem</h2>
 * "Standard" configurations, created using methods of the {@code core} package such as
 * {@link com.electronwill.nightconfig.core.Config#inMemory() },
 * are not thread-safe. It is wrong to use them from multiple threads.
 * <p>
 * Even when a thread-safe {@code Map} is used to store the config's values, as that is the case with
 * {@link com.electronwill.nightconfig.core.Config#inMemoryConcurrent()},
 * there is no way to perform multiple operations in a consistent way, because their order is not guaranteed
 * and they can overlap each other.
 * On top of that, using sub-configurations in a single call, such as {@code config.set("a.b.c", x)} is
 * problematic, because each subconfig has its own {@code Map},
 * the comments and the values are stored separately, and there is no mechanism that ensures the consistency
 * of the whole configuration.
 * <p>
 * For instance, if a thread A executes
 * 
 * <pre>
 * {@code
 * config.set("a.b", "value");
 * String a = config.get("a.b")
 * }
 * </pre>
 * 
 * and another thread B executes
 * 
 * <pre>
 * {@code
 * String b = config.remove("a.b");
 * }
 * </pre>
 * 
 * it is possible that thread A gets {@code a = null}, or that thread B gets {@code b = null}, or that they
 * both get a value!
 * 
 * This problem is even worse with complex operations and can lead to incorrect results or corrupted
 * configurations.
 * 
 * <h2>The solution: using concurrent configurations</h2>
 * This package provides a new interface:
 * {@link com.electronwill.nightconfig.core.concurrent.ConcurrentConfig} (and its commented version
 * {@link com.electronwill.nightconfig.core.concurrent.ConcurrentCommentedConfig}).
 * Classes that implement {@link com.electronwill.nightconfig.core.concurrent.ConcurrentConfig} offer the
 * following features and guarantees:
 * <ul>
 * <li>They are thread-safe: every method can be safely called simultaneously from multiple threads, including
 * every {@code get} and {@code set} methods.</li>
 * <li>They provide thread-safe atomic bulk operations that allow to apply a function to the config as a
 * whole, coherent, indivisible operation:
 * {@link ConcurrentConfig#bulkRead(java.util.function.Function)},
 * {@link ConcurrentConfig#bulkUpdate(java.util.function.Function)},
 * {@link ConcurrentCommentedConfig#bulkCommentedRead(java.util.function.Function)}
 * and
 * {@link ConcurrentCommentedConfig#bulkCommentedUpdate(java.util.function.Function)}.
 * </li>
 * </ul>
 * 
 * Every time that you have multiple operations to perform on the config, you should use the bulk methods.
 * Here is an example:
 * 
 * <pre>
 * {@code
 * ConcurrentConfig config = new SynchronizedConfig();
 * List<String> newPlayerList = config.bulkUpdate(conf -> {
 *     List<String> playerNames = conf.get("players");
 *     playerNames.add("NewPlayer");
 *     playerNames.remove("BadPlayer");
 *     conf.set("players", playerNames);
 *     return playerNames;
 * });
 * }
 * </pre>
 * 
 * Some configurations like {@link StampedConfig} and {@link SynchronizedConfig} also provide a way to atomically
 * replace their entire content. At the moment, this feature is not part of the {@link ConcurrentConfig} interface.
 * 
 * <h3>Important caveats</h3>
 * In order to guarantee the safety of the operations and prevent any deadlock:
 * <ol>
 * <li><b>In bulk operations, only the "view" provided to your bulk action can be used to read and modify the
 * config</b> (like in the example above). The reference to the original {@code config}
 * object must <b>not</b> be used in the function given to {@code bulkRead} and {@code bulkUpdate}.
 * Conversely, the view must <b>not</b> be used outside of the bulk operation.
 * </li>
 * <li>After replacement operations, the old configuration must not be used anymore, because its internal structure may have been
 * totally or partially "moved" to the new configuration. See the documentation of
 * {@link SynchronizedConfig#replaceContentBy(com.electronwill.nightconfig.core.Config)}
 * for more information.</li>
 * </ol>
 * 
 * @since 3.7.0
 */
package com.electronwill.nightconfig.core.concurrent;
