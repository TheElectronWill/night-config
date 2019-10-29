package com.electronwill.nightconfig.core.file;

import java.io.IOException;

/**
 * @author TheElectronWill
 */
final class AutoreloadFileConfig extends FileConfigWrapper {
	private final FileWatcher watcher = FileWatcher.defaultInstance();

	AutoreloadFileConfig(FileConfig config) {
		super(config);
		try {
			watcher.addWatch(config.getFile(), config::load);
		} catch (IOException e) {
			throw new RuntimeException("Unable to create the autoreloaded config", e);
		}
	}

	@Override
	public void close() {
		watcher.removeWatch(config.getFile());
		config.close();
	}
}