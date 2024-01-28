package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author TheElectronWill
 */
class SimpleCommentedFileConfig extends CommentedConfigWrapper<CommentedFileConfig>
		implements CommentedFileConfig {

	SimpleCommentedFileConfig(CommentedFileConfig fileConfig) {
		super(fileConfig);
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

	@Override
	public <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action) {
		return config.bulkCommentedUpdate(action);
	}

	@Override
	public <R> R bulkUpdate(Function<? super Config, R> action) {
		return config.bulkUpdate(action);
	}

}