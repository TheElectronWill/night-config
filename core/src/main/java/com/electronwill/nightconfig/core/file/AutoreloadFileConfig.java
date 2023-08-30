package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.utils.ConfigWrapper;

import java.io.File;
import java.nio.file.Path;

/**
 * @author TheElectronWill
 */
final class AutoreloadFileConfig<C extends FileConfig> extends ConfigWrapper<C> implements FileConfig {
	private final FileWatcher watcher = FileWatcher.defaultInstance();

	AutoreloadFileConfig(C config) {
		super(config);
		watcher.addWatch(config.getNioPath(), config::load);
	}

	@Override
	public File getFile() {
		return config.getFile();
	}

	@Override
	public Path getNioPath() {
		return config.getNioPath();
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
		watcher.removeWatch(config.getNioPath());
		config.close();
	}
}
