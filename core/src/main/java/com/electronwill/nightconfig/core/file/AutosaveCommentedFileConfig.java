package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;
import com.electronwill.nightconfig.core.utils.ObservedMap;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
final class AutosaveCommentedFileConfig extends CommentedConfigWrapper<CommentedConfig> implements CommentedFileConfig {
	private final FileConfig fileConfig;

	AutosaveCommentedFileConfig(CommentedConfig config, FileConfig fileConfig) {
		super(config);
		this.fileConfig = fileConfig;
	}

	@Override
	public <T> T set(List<String> path, Object value) {
		T result = super.set(path, value);
		save();
		return result;
	}

	@Override
	public boolean add(List<String> path, Object value) {
		boolean result = super.add(path, value);
		save();
		return result;
	}

	@Override
	public <T> T remove(List<String> path) {
		T result = super.remove(path);
		save();
		return result;
	}

	@Override
	public String setComment(List<String> path, String comment) {
		String result = super.setComment(path, comment);
		save();
		return result;
	}

	@Override
	public String removeComment(List<String> path) {
		String result = super.removeComment(path);
		save();
		return result;
	}

	@Override
	public Map<String, Object> valueMap() {
		return new ObservedMap<>(super.valueMap(), this::save);
	}

	@Override
	public Map<String, String> commentMap() {
		return new ObservedMap<>(super.commentMap(), this::save);
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