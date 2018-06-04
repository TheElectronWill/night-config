package com.electronwill.nightconfig.core.path;

import com.electronwill.nightconfig.core.utils.ConfigWrapper;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author TheElectronWill
 */
final class AutoreloadPathConfig<C extends PathConfig> extends ConfigWrapper<C> implements PathConfig {
	private final PathWatcher watcher = PathWatcher.defaultInstance();

	AutoreloadPathConfig(C config) {
		super(config);
		try {
			watcher.addWatch(config.getPath(), config::load);
		} catch (IOException e) {
			throw new RuntimeException("Unable to create the autoreloaded config", e);
		}
	}

	@Override
	public Path getPath() {
		return config.getPath();
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
		watcher.removeWatch(config.getPath());
		config.close();
	}
}