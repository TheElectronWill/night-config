package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import java.io.File;

/**
 * @author TheElectronWill
 */
public interface FileConfig extends Config {
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

	@Override
	default FileConfig checked() {
		return new CheckedFileConfig(this);
	}

	static FileConfig of(File file,
						 ConfigFormat<? extends Config, ? super Config, ? super Config> format) {
		return builder(file, format).build();
	}

	static FileConfigBuilder<Config> builder(File file,
											 ConfigFormat<? extends Config, ? super Config, ? super Config> format) {
		return new FileConfigBuilder<>(file, format);
	}
}