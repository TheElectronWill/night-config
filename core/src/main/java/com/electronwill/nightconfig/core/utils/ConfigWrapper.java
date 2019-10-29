package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.AttributeType;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.EntryData;

import java.util.Map;
import java.util.Set;

/**
 * @author TheElectronWill
 */
public abstract class ConfigWrapper<C extends Config> extends UnmodifiableConfigWrapper<C>
		implements Config {

	protected ConfigWrapper(C config) {
		super(config);
	}

	@Override
	public EntryData getData(String[] path) {
		return config.getData(path);
	}

	@Override
	public <T> T set(AttributeType<T> attribute, String[] path, T value) {
		return config.set(attribute, path, value);
	}

	@Override
	public <T> T add(AttributeType<T> attribute, String[] path, T value) {
		return config.add(attribute, path, value);
	}

	@Override
	public <T> T remove(AttributeType<T> attribute, String[] path) {
		return config.remove(attribute, path);
	}

	@Override
	public Map<String, EntryData> dataMap() {
		return config.dataMap();
	}

	@Override
	public Set<Config.Entry> entries() {
		return config.entries();
	}

	@Override
	public void clearAttributes() {
		config.clearAttributes();
	}

	@Override
	public void clearComments() {
		config.clearComments();
	}

	@Override
	public void clear() {
		config.clear();
	}

	@Override
	public Config createSubConfig() {
		return config.createSubConfig();
	}
}