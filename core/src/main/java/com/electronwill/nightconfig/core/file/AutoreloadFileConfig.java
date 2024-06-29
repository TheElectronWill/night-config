package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.utils.ConcurrentCommentedConfigWrapper;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * @author TheElectronWill
 */
final class AutoreloadFileConfig<C extends CommentedFileConfig> extends ConcurrentCommentedConfigWrapper<C>
		implements CommentedFileConfig {

	private final FileWatcher watcher;
	private final Runnable autoListener; // called on automatic reload

	AutoreloadFileConfig(C config, FileWatcher watcher, Runnable autoreloadListener) {
		super(config);
		this.watcher = watcher;
		this.autoListener = autoreloadListener;
		watcher.addWatch(config.getNioPath(), this::autoReload);
	}

	private void autoReload() {
		load();
		autoListener.run();
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
		try {
			watcher.removeWatchFuture(config.getNioPath()).get(5, TimeUnit.SECONDS);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			config.close();
		}
	}
}
