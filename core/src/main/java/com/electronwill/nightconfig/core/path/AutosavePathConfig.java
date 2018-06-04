package com.electronwill.nightconfig.core.path;

import com.electronwill.nightconfig.core.utils.ConfigWrapper;
import com.electronwill.nightconfig.core.utils.ObservedMap;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
final class AutosavePathConfig<C extends PathConfig> extends ConfigWrapper<C> implements PathConfig {
	AutosavePathConfig(C config) {
		super(config);
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
	public Map<String, Object> valueMap() {
		return new ObservedMap<>(super.valueMap(), this::save);
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
		config.close();
	}
}