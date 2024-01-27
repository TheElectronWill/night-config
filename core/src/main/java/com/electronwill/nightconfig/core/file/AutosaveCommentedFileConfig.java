package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;
import com.electronwill.nightconfig.core.utils.ObservedMap;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author TheElectronWill
 */
final class AutosaveCommentedFileConfig extends CommentedConfigWrapper<CommentedFileConfig> implements CommentedFileConfig {
	AutosaveCommentedFileConfig(CommentedFileConfig fileConfig) {
		super(fileConfig);
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
		R result = config.bulkCommentedUpdate(action);
		save();
		return result;
	}

	@Override
	public <R> R bulkUpdate(Function<? super Config, R> action) {
		R result = CommentedFileConfig.super.bulkUpdate(action);
		save();
		return result;
	}
}
