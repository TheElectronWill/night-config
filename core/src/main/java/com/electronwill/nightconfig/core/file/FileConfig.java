package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import java.io.File;

/**
 * @author TheElectronWill
 */
public interface FileConfig extends Config, AutoCloseable {
	/**
	 * @return the config's file
	 */
	File getFile();

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
	 * Closes this FileConfig and release its associated resources, if any. A closed FileConfig
	 * can still be used via the Config's methods, but {@link #save()} and {@link #load()} will
	 * throw an IllegalStateException. Closing an aleady closed FileConfig has no effect.
	 */
	@Override
	void close();

	@Override
	default FileConfig checked() {
		return new CheckedFileConfig(this);
	}

	/**
	 * Creates a new FileConfig based on the specified file and format.
	 *
	 * @param file   the file to use to save & load the config
	 * @param format the config's format
	 * @return a new FileConfig associated to the specified file
	 */
	static FileConfig of(File file,
						 ConfigFormat<? extends Config, ? super Config, ? super Config> format) {
		return builder(file, format).build();
	}

	/**
	 * Creates a new FileConfig based on the specified file. The format is detected automatically.
	 *
	 * @param file the file to use to save & load the config
	 * @return a new FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfig of(File file) {
		ConfigFormat format = FormatDetector.detect(file);
		if (format == null) {
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
	static FileConfig of(String filePath,
						 ConfigFormat<? extends Config, ? super Config, ? super Config> format) {
		return of(new File(filePath), format);
	}

	/**
	 * Creates a new FileConfig based on the specified file. The format is detected automatically.
	 *
	 * @param filePath the file's path
	 * @return a new FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfig of(String filePath) {
		return of(new File(filePath));
	}

	/**
	 * Creates a new thread-safe FileConfig based on the specified file and format.
	 *
	 * @param file   the file to use to save & load the config
	 * @param format the config's format
	 * @return a new thread-safe FileConfig associated to the specified file
	 */
	static FileConfig ofConcurrent(File file,
								   ConfigFormat<? extends Config, ? super Config, ? super Config> format) {
		return builder(file, format).concurrent().build();
	}

	/**
	 * Creates a new thread-safe FileConfig based on the specified file. The format is detected
	 * automatically.
	 *
	 * @param file the file to use to save & load the config
	 * @return a new thread-safe FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfig ofConcurrent(File file) {
		return builder(file).concurrent().build();
	}

	/**
	 * Creates a new thread-safe FileConfig based on the specified file and format.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new thread-safe FileConfig associated to the specified file
	 */
	static FileConfig ofConcurrent(String filePath,
								   ConfigFormat<? extends Config, ? super Config, ? super Config> format) {
		return ofConcurrent(new File(filePath), format);
	}

	/**
	 * Creates a new thread-safe FileConfig based on the specified file. The format is detected
	 * automatically.
	 *
	 * @param filePath the file's path
	 * @return a new thread-safe FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfig ofConcurrent(String filePath) {
		return ofConcurrent(new File(filePath));
	}

	/**
	 * Returns a FileConfigBuilder to create a FileConfig with many options.
	 *
	 * @param file   the file to use to save & load the config
	 * @param format the config's format
	 * @return a new FileConfigBuilder that will build a FileConfig associated to the specified file
	 */
	static FileConfigBuilder<Config> builder(File file,
											 ConfigFormat<? extends Config, ? super Config, ? super Config> format) {
		return new FileConfigBuilder<>(file, format);
	}

	/**
	 * Returns a FileConfigBuilder to create a FileConfig with many options. The format is detected
	 * automatically.
	 *
	 * @param file the file to use to save & load the config
	 * @return a new FileConfigBuilder that will build a FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfigBuilder<Config> builder(File file) {
		ConfigFormat format = FormatDetector.detect(file);
		if (format == null) {
			throw new NoFormatFoundException("No suitable format for " + file.getName());
		}
		return builder(file, format);
	}

	/**
	 * Returns a FileConfigBuilder to create a FileConfig with many options.
	 *
	 * @param filePath the file's path
	 * @param format   the config's format
	 * @return a new FileConfigBuilder that will build a FileConfig associated to the specified file
	 */
	static FileConfigBuilder<Config> builder(String filePath,
											 ConfigFormat<? extends Config, ? super Config, ? super Config> format) {
		return builder(new File(filePath), format);
	}

	/**
	 * Returns a FileConfigBuilder to create a FileConfig with many options. The format is detected
	 * automatically.
	 *
	 * @param filePath the file's path
	 * @return a new FileConfigBuilder that will build a FileConfig associated to the specified file
	 *
	 * @throws NoFormatFoundException if the format detection fails
	 */
	static FileConfigBuilder<Config> builder(String filePath) {
		return builder(new File(filePath));
	}
}