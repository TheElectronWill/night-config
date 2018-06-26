package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;

import java.io.File;
import java.nio.file.Path;

/**
 * @author TheElectronWill
 */
class SimpleCommentedFileConfig extends CommentedConfigWrapper<CommentedConfig>
		implements CommentedFileConfig {
	private final FileConfig fileConfig;

	SimpleCommentedFileConfig(CommentedConfig config, FileConfig fileConfig) {
		super(config);
		this.fileConfig = fileConfig;
	}

	@Override
	public File getFile() {
		return fileConfig.getFile();
	}

	@Override
	public Path getNioPath() {
		return fileConfig.getNioPath();
	}

	@Override
	public void save() {
		fileConfig.save();
	}

	@Override
	public void load() {
		fileConfig.load();
	}

	@Override
	public void close() {
		fileConfig.close();
	}
}