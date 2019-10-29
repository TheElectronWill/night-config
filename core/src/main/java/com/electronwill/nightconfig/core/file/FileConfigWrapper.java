package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.utils.ConfigWrapper;

import java.io.File;
import java.nio.file.Path;

public abstract class FileConfigWrapper extends ConfigWrapper<FileConfig> implements FileConfig {

	protected FileConfigWrapper(FileConfig config) {
		super(config);
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
		config.close();
	}
}
