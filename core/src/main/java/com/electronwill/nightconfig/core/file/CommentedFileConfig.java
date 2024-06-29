package com.electronwill.nightconfig.core.file;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;

import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.concurrent.ConcurrentCommentedConfig;
import com.electronwill.nightconfig.core.concurrent.ConcurrentConfig;

/**
 * A commented configuration that is tied to a particular file.
 * <p>
 * A {@code CommentedFileConfig} is simply a {@link FileConfig} that implements {@link CommentedConfig}
 * (and provides some methods of {@link ConcurrentCommentedConfig}).
 * Please refer to the documentation of {@link FileConfig} for more information.
 *
 * @author TheElectronWill
 */
public interface CommentedFileConfig extends ConcurrentCommentedConfig, FileConfig {
	@Override
	default CommentedFileConfig checked() {
		return new CheckedCommentedFileConfig(this);
	}

	/**
	 * Performs multiple read/write operations, and do not save the configuration until the end
	 * (unless {@code save()} is called by {@code action}).
	 *
	 * This is a way of manually grouping config modifications together.
	 * <p>
	 * If this configuration automatically saves, it will not do so before the end of the bulkUpdate.
	 */
	<R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action);

	/**
	 * Performs multiple read/write operations, and do not save the configuration until the end
	 * (unless {@code save()} is called by {@code action}).
	 *
	 * This is a way of manually grouping config modifications together.
	 * <p>
	 * If this configuration automatically saves, it will not do so before the end of the bulkUpdate.
	 */
	default void bulkCommentedUpdate(Consumer<? super CommentedConfig> action) {
		bulkCommentedUpdate(config -> {
			action.accept(config);
			return null;
		});
	}

	@Override
	default <R> R bulkUpdate(Function<? super Config, R> action) {
		return bulkCommentedUpdate(action);
	}

	// ----- static --
	// #region static

	/**
	 * Creates a new FileConfig based on the specified file and format. The format is detected
	 * automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new FileConfig associated to the specified file
	 * @see #builder(File)
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedFileConfig of(File file) {
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
	static CommentedFileConfig of(File file, ConfigFormat<? extends CommentedConfig> format) {
		return of(file.toPath(), format);
	}

	/**
	 * Creates a new FileConfig based on the specified file and format. The format is detected
	 * automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new FileConfig associated to the specified file
	 * @see #builder(Path)
	 * @throws NoFormatFoundException if the format detection fails
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static CommentedFileConfig of(Path file) {
		ConfigFormat format = FormatDetector.detect(file);
		if (format == null || !format.supportsComments()) {
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
	static CommentedFileConfig of(Path file, ConfigFormat<? extends CommentedConfig> format) {
		return builder(file, format).build();
	}

	/**
	 * Creates a new FileConfig based on the specified file and format. The format is detected
	 * automatically.
	 *
	 * @param filePath the file's path
	 * @return a new FileConfig associated to the specified file
	 * @see #builder(String)
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedFileConfig of(String filePath) {
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
	static CommentedFileConfig of(String filePath, ConfigFormat<? extends CommentedConfig> format) {
		return of(Paths.get(filePath), format);
	}

	/**
	 * Creates a new thread-safe CommentedFileConfig based on the specified file and format. The
	 * format is detected automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new thread-safe CommentedFileConfig associated to the specified file
	 * @throws NoFormatFoundException if the format detection fails
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static CommentedFileConfig ofConcurrent(File file) {
		return ofConcurrent(file.toPath());
	}

	/**
	 * Creates a new trhead-safe CommentedFileConfig based on the specified file and format.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new thread-safe CommentedFileConfig associated to the specified file
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static CommentedFileConfig ofConcurrent(File file,
			ConfigFormat<? extends CommentedConfig> format) {
		return ofConcurrent(file.toPath(), format);
	}

	/**
	 * Creates a new thread-safe CommentedFileConfig based on the specified file and format. The
	 * format is detected automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new thread-safe CommentedFileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static CommentedFileConfig ofConcurrent(Path file) {
		return builder(file).concurrent().build();
	}

	/**
	 * Creates a new trhead-safe CommentedFileConfig based on the specified file and format.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new thread-safe CommentedFileConfig associated to the specified file
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static CommentedFileConfig ofConcurrent(Path file,
			ConfigFormat<? extends CommentedConfig> format) {
		return builder(file, format).concurrent().build();
	}

	/**
	 * Creates a new trhead-safe CommentedFileConfig based on the specified file and format.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new thread-safe CommentedFileConfig associated to the specified file
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static CommentedFileConfig ofConcurrent(String filePath,
			ConfigFormat<? extends CommentedConfig> format) {
		return ofConcurrent(Paths.get(filePath), format);
	}

	/**
	 * Creates a new trhead-safe CommentedFileConfig based on the specified file and format. The
	 * format is detected automatically.
	 *
	 * @param filePath the file's path
	 * @return a new thread-safe CommentedFileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 * @deprecated All FileConfig are now thread-safe by default, backed by a {@link ConcurrentConfig}
	 */
	@Deprecated
	static CommentedFileConfig ofConcurrent(String filePath) {
		return ofConcurrent(Paths.get(filePath));
	}

	/**
	 * Returns a CommentedFileConfigBuilder to create a CommentedFileConfig with many options.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new FileConfigBuilder that will build a CommentedFileConfig associated to the
	 *         specified file
	 */
	static CommentedFileConfigBuilder builder(File file,
			ConfigFormat<? extends CommentedConfig> format) {
		return builder(file.toPath(), format);
	}

	/**
	 * Returns a CommentedFileConfigBuilder to create a CommentedFileConfig with many options. The
	 * format is detected automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new FileConfigBuilder that will build a CommentedFileConfig associated to the
	 *         specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedFileConfigBuilder builder(File file) {
		return builder(file.toPath());
	}

	/**
	 * Returns a CommentedFileConfigBuilder to create a CommentedFileConfig with many options.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new FileConfigBuilder that will build a CommentedFileConfig associated to the
	 *         specified file
	 */
	static CommentedFileConfigBuilder builder(Path file,
			ConfigFormat<? extends CommentedConfig> format) {
		return new CommentedFileConfigBuilder(file, format);
	}

	/**
	 * Returns a CommentedFileConfigBuilder to create a CommentedFileConfig with many options. The
	 * format is detected automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new FileConfigBuilder that will build a CommentedFileConfig associated to the
	 *         specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static CommentedFileConfigBuilder builder(Path file) {
		ConfigFormat format = FormatDetector.detect(file);
		if (format == null) {
			throw new NoFormatFoundException("No suitable format for " + file.getFileName());
		} else if (!format.supportsComments()) {
			throw new NoFormatFoundException(
					"The available format doesn't support comments for " + file.getFileName());
		}
		return builder(file, format);
	}

	/**
	 * Returns a CommentedFileConfigBuilder to create a CommentedFileConfig with many options. The
	 * format is detected automatically.
	 *
	 * @param filePath the file's path
	 * @return a new FileConfigBuilder that will build a CommentedFileConfig associated to the
	 *         specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedFileConfigBuilder builder(String filePath) {
		return builder(Paths.get(filePath));
	}

	/**
	 * Returns a CommentedFileConfigBuilder to create a CommentedFileConfig with many options.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new FileConfigBuilder that will build a CommentedFileConfig associated to the
	 *         specified file
	 */
	static CommentedFileConfigBuilder builder(String filePath,
			ConfigFormat<? extends CommentedConfig> format) {
		return builder(Paths.get(filePath), format);
	}

	// #endregion static
}
