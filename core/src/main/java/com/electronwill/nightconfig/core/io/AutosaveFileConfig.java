package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.utils.ConfigWrapper;
import com.electronwill.nightconfig.core.utils.ObservedMap;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
final class AutosaveFileConfig<C extends FileConfig> extends ConfigWrapper<C> implements FileConfig {
	AutosaveFileConfig(C config) {
		super(config);
	}

	@Override
	public <T> T set(List<String> path, Object value) {
		T result = super.set(path, value);
		save();
		return result;
	}

	@Override
	public void add(List<String> path, Object value) {
		super.add(path, value);
		save();
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
	public File getFile() {
		return config.getFile();
	}

	@Override
	public void save() {
		config.save();
	}

	@Override
	public void load() {
		config.load();
	}
}