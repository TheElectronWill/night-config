package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.AttributeType;
import com.electronwill.nightconfig.core.Config;

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
	public Config.Entry getEntry(String[] path) {
		return config.getEntry(path);
	}

	@Override
	public Config.Entry getEntry(Iterable<String> path) {
		return config.getEntry(path);
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
	public Iterable<? extends Config.Entry> entries() {
		return config.entries();
	}

	@Override
	public Iterable<String> keys() {
		return config.keys();
	}

	@Override
	public Iterable<Object> values() {
		return config.values();
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