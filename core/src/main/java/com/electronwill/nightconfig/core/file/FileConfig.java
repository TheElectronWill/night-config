package com.electronwill.nightconfig.core.file;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.concurrent.ConcurrentConfig;

/**
 * A configuration that is tied to a particular file.
 *
 * <h2>IO operations</h2>
 * When created, a {@code FileConfig} is empty. It is linked to a file, but that file has not been parsed yet.
 * To parse the file and fill the {@code FileConfig}, call the {@link #load()} method.
 * <b>You probably want to call {@link load()} before using the configuration.</b>
 * <p>
 * Depending on the options that you have chosen when building the {@code FileConfig} (see {@link FileConfigBuilder}),
 * it may be saved automatically after each writing operation. To manually save the content of the configuration to
 * its file, call the {@link #save()} method.
 * <p>
 * <h3>Optimizing writes</h3>
 * If your {@code FileConfig} has been configured to automatically save its content, it can suffer from
 * performance issues when applying many modifications. A solution is to use {@link #bulkUpdate(Consumer)}
 * in order to apply multiple modifications as one writing operation, which will trigger only one save operation
 * (at the end of {@code bulkUpdate}).
 * <p>
 * Example:
 * <pre>
 * {@code
 * FileConfig config = FileConfig.builder(file).autosave().build();
 * config.bulkUpdate(c -> {
 *     c.set("key1", "value1");
 *     c.set("key2", "value2");
 *     // ... other operations
 *     // NOTE: you must NOT use the config variable here, only c
 * });
 * }
 * </pre>
 * <p>
 * Even if your {@code FileConfig} is not auto-saved, bulk methods like {@link #bulkUpdate(Consumer)}
 * are useful to ensure the consistency of the configuration in a multi-thread environment.
 * See the next section for more details.
 *
 * <h2>Thread safety</h2>
 * Since NightConfig 3.7.0, every {@code FileConfig} is thread-safe (see <a href="https://github.com/TheElectronWill/night-config/pull/152">PR #152</a>).
 * In other words, you can read and modify the same {@code FileConfig} from different threads at the same time,
 * without any external synchronization (no need to guard the configuration with a lock).
 * <p>
 * However, some rules need to be followed in order to ensure the consistency of the configuration's content.
 * In particular, when you write multiple values to a {@code FileConfig}, or read multiple values from
 * a {@code FileConfig}, and expect all the values to be consistent (i.e. you are performing multiple
 * operations as if they were all applied together, with no other thread modifying the configuration in
 * between), you should use {@link #bulkUpdate(Consumer)} and {@link #bulkRead(Consumer)}.
 * <p>
 * For more details, please see the documentation of the {@code concurrent} package, which is internally
 * used by {@code FileConfig}s: {@link com.electronwill.nightconfig.core.concurrent}.
 *
 * @author TheElectronWill
 */
public interface FileConfig extends ConcurrentConfig, AutoCloseable {
	/**
	 * @return the config's file, as a classic File object
	 */
	File getFile();

	/**
	 * @return the config's file, as a NIO Path object
	 */
	Path getNioPath();

	/**
	 * Saves this config as soon as possible. This method may return quickly and perform the IO
	 * operations in background, or it may block until the operations are done.
	 */
	void save();

	/**
	 * (Re)loads this config from the file. This method blocks until the read operation completes.
	 */
	void load();

	/**
	 * Closes this FileConfig, releases its associated resources (if any), and ensure that the
	 * ongoing saving operations complete.
	 * <p>
	 * A closed FileConfig can still be used via the Config's methods, but {@link #save()} and
	 * {@link #load()} will throw an IllegalStateException. Closing an aleady closed FileConfig has
	 * no effect.
	 */
	@Override
	void close();

	@Override
	default FileConfig checked() {
		return new CheckedFileConfig(this);
	}

	/**
	 * Performs multiple read/write operations, and do not save the configuration until the end
	 * (unless {@code save()} is called by {@code action}).
	 *
	 * This is a way of manually grouping config modifications together.
	 * <p>
	 * If this configuration automatically saves, it will not do so before the end of the bulkUpdate.
	 * <p>
	 * This has the same guarantees of consistency as {@link ConcurrentConfig#bulkUpdate(Function)}.
	 *
	 */
	<R> R bulkUpdate(Function<? super Config, R> action);

	/**
	 * Performs multiple read/write operations, and do not save the configuration until the end
	 * (unless {@code save()} is called by {@code action}).
	 *
	 * This is a way of manually grouping config modifications together.
	 * <p>
	 * If this configuration automatically saves, it will not do so before the end of the bulkUpdate.
	 * <p>
	 * This has the same guarantees of consistency as {@link ConcurrentConfig#bulkUpdate(Consumer)}.
	 */
	default void bulkUpdate(Consumer<? super Config> action) {
		bulkUpdate(config -> {
			action.accept(config);
			return null;
		});
	}

	// ----- static methods -----
	// #region static
	/**
	 * Creates a new FileConfig based on the specified file. The format is detected automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new FileConfig associated to the specified file
	 * @see #builder(File)
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfig of(File file) {
		return of(file.toPath());
	}

	/**
	 * Creates a new FileConfig based on the specified file and format.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new FileConfig associated to the specified file
	 * @see #builder(File, ConfigFormat)
	 */
	static FileConfig of(File file, ConfigFormat<? extends Config> format) {
		return of(file.toPath(), format);
	}

	/**
	 * Creates a new FileConfig based on the specified file. The format is detected automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new FileConfig associated to the specified file
	 * @see #builder(Path)
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfig of(Path file) {
		ConfigFormat<?> format = FormatDetector.detect(file);
		if (format == null) {
			throw new NoFormatFoundException("No suitable format for " + file.getFileName());
		}
		return of(file, format);
	}

	/**
	 * Creates a new FileConfig based on the specified file and format.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new FileConfig associated to the specified file
	 * @see #builder(Path, ConfigFormat)
	 */
	static FileConfig of(Path file, ConfigFormat<? extends Config> format) {
		return builder(file, format).build();
	}

	/**
	 * Creates a new FileConfig based on the specified file. The format is detected automatically.
	 *
	 * @param filePath the file's path
	 * @return a new FileConfig associated to the specified file
	 * @see #builder(String)
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfig of(String filePath) {
		return of(Paths.get(filePath));
	}

	/**
	 * Creates a new FileConfig based on the specified file and format.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new FileConfig associated to the specified file
	 * @see #builder(String, ConfigFormat)
	 */
	static FileConfig of(String filePath, ConfigFormat<?> format) {
		return of(Paths.get(filePath), format);
	}

	/**
	 * Creates a new thread-safe FileConfig based on the specified file. The format is detected
	 * automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new thread-safe FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static FileConfig ofConcurrent(File file) {
		return ofConcurrent(file.toPath());
	}

	/**
	 * Creates a new thread-safe FileConfig based on the specified file and format.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new thread-safe FileConfig associated to the specified file
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static FileConfig ofConcurrent(File file, ConfigFormat<?> format) {
		return ofConcurrent(file.toPath(), format);
	}

	/**
	 * Creates a new thread-safe FileConfig based on the specified file. The format is detected
	 * automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new thread-safe FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static FileConfig ofConcurrent(Path file) {
		return builder(file).concurrent().build();
	}

	/**
	 * Creates a new thread-safe FileConfig based on the specified file and format.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new thread-safe FileConfig associated to the specified file
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static FileConfig ofConcurrent(Path file, ConfigFormat<?> format) {
		return builder(file, format).concurrent().build();
	}

	/**
	 * Creates a new thread-safe FileConfig based on the specified file. The format is detected
	 * automatically.
	 *
	 * @param filePath the file's path
	 * @return a new thread-safe FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static FileConfig ofConcurrent(String filePath) {
		return ofConcurrent(Paths.get(filePath));
	}

	/**
	 * Creates a new thread-safe FileConfig based on the specified file and format.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new thread-safe FileConfig associated to the specified file
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static FileConfig ofConcurrent(String filePath, ConfigFormat<?> format) {
		return ofConcurrent(Paths.get(filePath), format);
	}

	/**
	 * Returns a FileConfigBuilder to create a FileConfig with many options. The format is detected
	 * automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new FileConfigBuilder that will build a FileConfig associated to the specified file
	 * @see FileConfigBuilder
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfigBuilder builder(File file) {
		return builder(file.toPath());
	}

	/**
	 * Returns a FileConfigBuilder to create a FileConfig with many options.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new FileConfigBuilder that will build a FileConfig associated to the specified file
	 * @see FileConfigBuilder
	 */
	static FileConfigBuilder builder(File file, ConfigFormat<?> format) {
		return builder(file.toPath(), format);
	}

	/**
	 * Returns a FileConfigBuilder to create a FileConfig with many options. The format is detected
	 * automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new FileConfigBuilder that will build a FileConfig associated to the specified file
	 * @see FileConfigBuilder
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfigBuilder builder(Path file) {
		ConfigFormat<?> format = FormatDetector.detect(file);
		if (format == null) {
			throw new NoFormatFoundException("No suitable format for " + file.getFileName());
		}
		return builder(file, format);
	}

	/**
	 * Returns a FileConfigBuilder to create a FileConfig with many options.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new FileConfigBuilder that will build a FileConfig associated to the specified file
	 * @see FileConfigBuilder
	 */
	static FileConfigBuilder builder(Path file, ConfigFormat<?> format) {
		return new FileConfigBuilder(file, format);
	}

	/**
	 * Returns a FileConfigBuilder to create a FileConfig with many options. The format is detected
	 * automatically.
	 *
	 * @param filePath the file's path
	 * @return a new FileConfigBuilder that will build a FileConfig associated to the specified file
	 * @see FileConfigBuilder
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfigBuilder builder(String filePath) {
		return builder(Paths.get(filePath));
	}

	/**
	 * Returns a FileConfigBuilder to create a FileConfig with many options.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new FileConfigBuilder that will build a FileConfig associated to the specified file
	 * @see FileConfigBuilder
	 */
	static FileConfigBuilder builder(String filePath, ConfigFormat<?> format) {
		return builder(Paths.get(filePath), format);
	}

	// #endregion static
}