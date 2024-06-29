package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
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
final class AutosaveCommentedFileConfig extends CommentedConfigWrapper<CommentedFileConfig>
		implements CommentedFileConfig {
	private final Runnable autoSaveListener; // called on automatic save

	AutosaveCommentedFileConfig(CommentedFileConfig fileConfig, Runnable autosaveListener) {
		super(fileConfig);
		this.autoSaveListener = autosaveListener;
	}

	private void autoSave() {
		save();
		autoSaveListener.run();
	}

	@Override
	public void save() {
		config.save();
	}

	@Override
	public <T> T set(List<String> path, Object value) {
		T result = super.set(path, value);
		autoSave();
		return result;
	}

	@Override
	public boolean add(List<String> path, Object value) {
		boolean result = super.add(path, value);
		autoSave();
		return result;
	}

	@Override
	public <T> T remove(List<String> path) {
		T result = super.remove(path);
		autoSave();
		return result;
	}

	@Override
	public String setComment(List<String> path, String comment) {
		String result = super.setComment(path, comment);
		autoSave();
		return result;
	}

	@Override
	public String removeComment(List<String> path) {
		String result = super.removeComment(path);
		autoSave();
		return result;
	}

	@Override
	public void removeAll(UnmodifiableConfig config) {
		super.removeAll(config);
		autoSave();
	}

	@Override
	public void putAll(UnmodifiableConfig config) {
		super.putAll(config);
		autoSave();
	}

	@Override
	public void clear() {
		super.clear();
		autoSave();
	}

	@Override
	public void clearComments() {
		super.clearComments();
		autoSave();
	}

	@Override
	public void putAllComments(UnmodifiableCommentedConfig commentedConfig) {
		super.putAllComments(commentedConfig);
		autoSave();
	}

	@Override
	public void putAllComments(Map<String, CommentNode> comments) {
		super.putAllComments(comments);
		autoSave();
	}

	@Override
	public Map<String, Object> valueMap() {
		return new ObservedMap<>(super.valueMap(), this::autoSave);
	}

	@Override
	public Map<String, String> commentMap() {
		return new ObservedMap<>(super.commentMap(), this::autoSave);
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
	public void load() {
		config.load();
	}

	@Override
	public void close() {
		config.close();
	}

	@Override
	public <R> R bulkRead(Function<? super UnmodifiableConfig, R> action) {
		return config.bulkRead(action);
	}

	@Override
	public <R> R bulkCommentedRead(Function<? super UnmodifiableCommentedConfig, R> action) {
		return config.bulkCommentedRead(action);
	}

	@Override
	public <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action) {
		R result = config.bulkCommentedUpdate(action);
		autoSave();
		return result;
	}

	@Override
	public <R> R bulkUpdate(Function<? super Config, R> action) {
		R result = CommentedFileConfig.super.bulkUpdate(action);
		autoSave();
		return result;
	}
}
