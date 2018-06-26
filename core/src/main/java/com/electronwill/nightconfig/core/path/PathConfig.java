package com.electronwill.nightconfig.core.path;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.FormatDetector;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author TheElectronWill
 */
public interface PathConfig extends Config, AutoCloseable {
	/**
	 * @return the config's file
	 */
	Path getPath();

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
	 * Closes this PathConfig, releases its associated resources (if any), and ensure that the
	 * ongoing saving operations complete.
	 * <p>
	 * A closed PathConfig can still be used via the Config's methods, but {@link #save()} and
	 * {@link #load()} will throw an IllegalStateException. Closing an aleady closed PathConfig has
	 * no effect.
	 */
	@Override
	void close();

	@Override
	default PathConfig checked() {
		return new CheckedPathConfig(this);
	}

	/**
	 * Creates a new PathConfig based on the specified path and format.
	 *
	 * @param path   the path to use to save and load the config
	 * @param format the config's format
	 * @return a new PathConfig associated to the specified path
	 */
	static PathConfig of(Path path, ConfigFormat<? extends Config> format) {
		return builder(path, format).build();
	}

	/**
	 * Creates a new PathConfig based on the specified path. The format is detected automatically.
	 *
	 * @param path the path to use to save and load the config
	 * @return a new PathConfig associated to the specified path
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static PathConfig of(Path path) {
		ConfigFormat format = FormatDetector.detect(path);
		if (format == null) {
			throw new NoFormatFoundException("No suitable format for " + path.getFileName());
		}
		return of(path, format);
	}

	/**
	 * Creates a new PathConfig based on the specified file and format.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new PathConfig associated to the specified file
	 */
	static PathConfig of(String filePath, ConfigFormat<?> format) {
		return of(Paths.get(filePath), format);
	}

	/**
	 * Creates a new PathConfig based on the specified file. The format is detected automatically.
	 *
	 * @param filePath the file's path
	 * @return a new PathConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static PathConfig of(String filePath) {
		return of(Paths.get(filePath));
	}

	/**
	 * Creates a new thread-safe PathConfig based on the specified path and format.
	 *
	 * @param path   the path to use to save and load the config
	 * @param format the config's format
	 * @return a new thread-safe PathConfig associated to the specified path
	 */
	static PathConfig ofConcurrent(Path path, ConfigFormat<?> format) {
		return builder(path, format).concurrent().build();
	}

	/**
	 * Creates a new thread-safe PathConfig based on the specified path. The format is detected
	 * automatically.
	 *
	 * @param path the path to use to save and load the config
	 * @return a new thread-safe PathConfig associated to the specified path
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static PathConfig ofConcurrent(Path path) {
		return builder(path).concurrent().build();
	}

	/**
	 * Creates a new thread-safe PathConfig based on the specified file and format.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new thread-safe PathConfig associated to the specified file
	 */
	static PathConfig ofConcurrent(String filePath, ConfigFormat<?> format) {
		return ofConcurrent(Paths.get(filePath), format);
	}

	/**
	 * Creates a new thread-safe PathConfig based on the specified file. The format is detected
	 * automatically.
	 *
	 * @param filePath the file's path
	 * @return a new thread-safe PathConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static PathConfig ofConcurrent(String filePath) {
		return ofConcurrent(Paths.get(filePath));
	}

	/**
	 * Returns a PathConfigBuilder to create a PathConfig with many options.
	 *
	 * @param path   the path to use to save and load the config
	 * @param format the config's format
	 * @return a new PathConfigBuilder that will build a PathConfig associated to the specified path
	 */
	static PathConfigBuilder<Config> builder(Path path, ConfigFormat<?> format) {
		return new PathConfigBuilder<>(path, format);
	}

	/**
	 * Returns a PathConfigBuilder to create a PathConfig with many options. The format is detected
	 * automatically.
	 *
	 * @param path the path to use to save and load the config
	 * @return a new PathConfigBuilder that will build a PathConfig associated to the specified path
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static PathConfigBuilder<Config> builder(Path path) {
		ConfigFormat format = FormatDetector.detect(path);
		if (format == null) {
			throw new NoFormatFoundException("No suitable format for " + path.getFileName());
		}
		return builder(path, format);
	}

	/**
	 * Returns a PathConfigBuilder to create a PathConfig with many options.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new PathConfigBuilder that will build a PathConfig associated to the specified file
	 */
	static PathConfigBuilder<Config> builder(String filePath, ConfigFormat<?> format) {
		return builder(Paths.get(filePath), format);
	}

	/**
	 * Returns a PathConfigBuilder to create a PathConfig with many options. The format is detected
	 * automatically.
	 *
	 * @param filePath the file's path
	 * @return a new PathConfigBuilder that will build a PathConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static PathConfigBuilder<Config> builder(String filePath) {
		return builder(Paths.get(filePath));
	}
}