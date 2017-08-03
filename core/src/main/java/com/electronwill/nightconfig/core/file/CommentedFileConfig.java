package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import java.io.File;

/**
 * @author TheElectronWill
 */
public interface CommentedFileConfig extends CommentedConfig, FileConfig {
	@Override
	default CommentedFileConfig checked() {
		return new CheckedCommentedFileConfig(this);
	}

	/**
	 * Creates a new FileConfig based on the specified file and format.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new FileConfig associated to the specified file
	 */
	static CommentedFileConfig of(File file,
								  ConfigFormat<? extends CommentedConfig, ? super CommentedConfig, ? super CommentedConfig> format) {
		return builder(file, format).build();
	}

	/**
	 * Creates a new FileConfig based on the specified file and format. The format is detected
	 * automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedFileConfig of(File file) {
		ConfigFormat format = FormatDetector.detect(file);
		if (format == null || !format.supportsComments()) {
			throw new NoFormatFoundException("No suitable format for " + file.getName());
		}
		return of(file, format);
	}

	/**
	 * Creates a new FileConfig based on the specified file and format.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new FileConfig associated to the specified file
	 */
	static CommentedFileConfig of(String filePath,
								  ConfigFormat<? extends CommentedConfig, ? super CommentedConfig, ? super CommentedConfig> format) {
		return of(new File(filePath), format);
	}

	/**
	 * Creates a new FileConfig based on the specified file and format. The format is detected
	 * automatically.
	 *
	 * @param filePath the file's path
	 * @return a new FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedFileConfig of(String filePath) {
		return of(new File(filePath));
	}

	/**
	 * Creates a new trhead-safe CommentedFileConfig based on the specified file and format.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new thread-safe CommentedFileConfig associated to the specified file
	 */
	static CommentedFileConfig ofConcurrent(File file,
											ConfigFormat<? extends CommentedConfig, ? super CommentedConfig, ? super CommentedConfig> format) {
		return builder(file, format).concurrent().build();
	}

	/**
	 * Creates a new thread-safe CommentedFileConfig based on the specified file and format. The
	 * format is detected automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new thread-safe CommentedFileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedFileConfig ofConcurrent(File file) {
		return builder(file).concurrent().build();
	}

	/**
	 * Creates a new trhead-safe CommentedFileConfig based on the specified file and format.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new thread-safe CommentedFileConfig associated to the specified file
	 */
	static CommentedFileConfig ofConcurrent(String filePath,
											ConfigFormat<? extends CommentedConfig, ? super CommentedConfig, ? super CommentedConfig> format) {
		return ofConcurrent(new File(filePath), format);
	}

	/**
	 * Creates a new trhead-safe CommentedFileConfig based on the specified file and format. The
	 * format is detected automatically.
	 *
	 * @param filePath the file's path
	 * @return a new thread-safe CommentedFileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedFileConfig ofConcurrent(String filePath) {
		return ofConcurrent(new File(filePath));
	}

	/**
	 * Returns a CommentedFileConfigBuilder to create a CommentedFileConfig with many options.
	 *
	 * @param file   the file to use to save and load the config
	 * @param format the config's format
	 * @return a new FileConfigBuilder that will build a CommentedFileConfig associated to the
	 * specified file
	 */
	static CommentedFileConfigBuilder builder(File file,
											  ConfigFormat<? extends CommentedConfig, ? super CommentedConfig, ? super CommentedConfig> format) {
		return new CommentedFileConfigBuilder(file, format);
	}

	/**
	 * Returns a CommentedFileConfigBuilder to create a CommentedFileConfig with many options. The
	 * format is detected automatically.
	 *
	 * @param file the file to use to save and load the config
	 * @return a new FileConfigBuilder that will build a CommentedFileConfig associated to the
	 * specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedFileConfigBuilder builder(File file) {
		ConfigFormat format = FormatDetector.detect(file);
		if (format == null || !format.supportsComments()) {
			throw new NoFormatFoundException("No suitable format for " + file.getName());
		}
		return builder(file, format);
	}

	/**
	 * Returns a CommentedFileConfigBuilder to create a CommentedFileConfig with many options.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new FileConfigBuilder that will build a CommentedFileConfig associated to the
	 * specified file
	 */
	static CommentedFileConfigBuilder builder(String filePath,
											  ConfigFormat<? extends CommentedConfig, ? super CommentedConfig, ? super CommentedConfig> format) {
		return builder(new File(filePath), format);
	}

	/**
	 * Returns a CommentedFileConfigBuilder to create a CommentedFileConfig with many options. The
	 * format is detected automatically.
	 *
	 * @param filePath the file's path
	 * @return a new FileConfigBuilder that will build a CommentedFileConfig associated to the
	 * specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static CommentedFileConfigBuilder builder(String filePath) {
		return builder(new File(filePath));
	}
}