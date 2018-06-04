package com.electronwill.nightconfig.core.path;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.FormatDetector;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author TheElectronWill
 */
public interface CommentedPathConfig extends CommentedConfig, PathConfig {
	@Override
	default CommentedPathConfig checked() {
		return new CheckedCommentedPathConfig(this);
	}

	/**
	 * Creates a new PathConfig based on the specified path and format.
	 *
	 * @param path   the path to use to save and load the config
	 * @param format the config's format
	 * @return a new PathConfig associated to the specified path
	 */
	static CommentedPathConfig of(Path path, ConfigFormat<? extends CommentedConfig> format) {
		return builder(path, format).build();
	}

	/**
	 * Creates a new PathConfig based on the specified path and format. The format is detected
	 * automatically.
	 *
	 * @param path the path to use to save and load the config
	 * @return a new PathConfig associated to the specified path
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedPathConfig of(Path path) {
		ConfigFormat format = FormatDetector.detect(path);
		if (format == null || !format.supportsComments()) {
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
	static CommentedPathConfig of(String filePath, ConfigFormat<? extends CommentedConfig> format) {
		return of(Paths.get(filePath), format);
	}

	/**
	 * Creates a new PathConfig based on the specified file and format. The format is detected
	 * automatically.
	 *
	 * @param filePath the file's path
	 * @return a new PathConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedPathConfig of(String filePath) {
		return of(Paths.get(filePath));
	}

	/**
	 * Creates a new trhead-safe CommentedPathConfig based on the specified path and format.
	 *
	 * @param path   the path to use to save and load the config
	 * @param format the config's format
	 * @return a new thread-safe CommentedPathConfig associated to the specified path
	 */
	static CommentedPathConfig ofConcurrent(Path path, ConfigFormat<? extends CommentedConfig> format) {
		return builder(path, format).concurrent().build();
	}

	/**
	 * Creates a new thread-safe CommentedPathConfig based on the specified path and format. The
	 * format is detected automatically.
	 *
	 * @param path the path to use to save and load the config
	 * @return a new thread-safe CommentedPathConfig associated to the specified path
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedPathConfig ofConcurrent(Path path) {
		return builder(path).concurrent().build();
	}

	/**
	 * Creates a new trhead-safe CommentedPathConfig based on the specified file and format.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new thread-safe CommentedPathConfig associated to the specified file
	 */
	static CommentedPathConfig ofConcurrent(String filePath, ConfigFormat<? extends CommentedConfig> format) {
		return ofConcurrent(Paths.get(filePath), format);
	}

	/**
	 * Creates a new trhead-safe CommentedPathConfig based on the specified file and format. The
	 * format is detected automatically.
	 *
	 * @param filePath the file's path
	 * @return a new thread-safe CommentedPathConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedPathConfig ofConcurrent(String filePath) {
		return ofConcurrent(Paths.get(filePath));
	}

	/**
	 * Returns a CommentedPathConfigBuilder to create a CommentedPathConfig with many options.
	 *
	 * @param path   the path to use to save and load the config
	 * @param format the config's format
	 * @return a new PathConfigBuilder that will build a CommentedPathConfig associated to the
	 * specified path
	 */
	static CommentedPathConfigBuilder builder(Path path, ConfigFormat<? extends CommentedConfig> format) {
		return new CommentedPathConfigBuilder(path, format);
	}

	/**
	 * Returns a CommentedPathConfigBuilder to create a CommentedPathConfig with many options. The
	 * format is detected automatically.
	 *
	 * @param path the path to use to save and load the config
	 * @return a new PathConfigBuilder that will build a CommentedPathConfig associated to the
	 * specified path
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedPathConfigBuilder builder(Path path) {
		ConfigFormat format = FormatDetector.detect(path);
		if (format == null || !format.supportsComments()) {
			throw new NoFormatFoundException("No suitable format for " + path.getFileName());
		}
		return builder(path, format);
	}

	/**
	 * Returns a CommentedPathConfigBuilder to create a CommentedPathConfig with many options.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new PathConfigBuilder that will build a CommentedPathConfig associated to the
	 * specified file
	 */
	static CommentedPathConfigBuilder builder(String filePath, ConfigFormat<? extends CommentedConfig> format) {
		return builder(Paths.get(filePath), format);
	}

	/**
	 * Returns a CommentedPathConfigBuilder to create a CommentedPathConfig with many options. The
	 * format is detected automatically.
	 *
	 * @param filePath the file's path
	 * @return a new PathConfigBuilder that will build a CommentedPathConfig associated to the
	 * specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedPathConfigBuilder builder(String filePath) {
		return builder(Paths.get(filePath));
	}
}