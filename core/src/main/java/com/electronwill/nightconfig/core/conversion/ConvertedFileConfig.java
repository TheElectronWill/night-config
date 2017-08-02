package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.file.FileConfig;
import java.io.File;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
public class ConvertedFileConfig extends AbstractConvertedConfig<FileConfig> implements FileConfig {
	ConvertedFileConfig(FileConfig config, Function<Object, Object> readConversion,
						Function<Object, Object> writeConversion,
						Predicate<Class<?>> supportPredicate) {
		super(config, readConversion, writeConversion, supportPredicate);
	}

	@Override
	public File getFile() {
		return config.getFile();
	}

	@Override
	public void save() {
		config.save();
	}

	@Override
	public void load() {
		config.load();
	}

	@Override
	public void close() {
		config.close();
	}
}