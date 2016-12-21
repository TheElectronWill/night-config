package com.electronwill.nightconfig.core;

import java.util.List;
import java.util.Map;

/**
 * A checker wrapper around a configuration. It checks that the values you are trying to set are supported
 * by the underlying configuration. Trying to insert an unsupported value will throw an
 * IllegalArgumentException.
 *
 * @author TheElectronWill
 */
public final class CheckedConfig implements Config {
	private final Config config;

	/**
	 * Creates a new CheckedConfig around a given configuration.
	 *
	 * @param config the configuration to wrap
	 */
	public CheckedConfig(Config config) {
		this.config = config;
	}

	@Override
	public int size() {
		return config.size();
	}

	@Override
	public Map<String, Object> asMap() {
		return config.asMap();
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return config.supportsType(type);
	}

	@Override
	public boolean containsValue(List<String> path) {
		return config.containsValue(path);
	}

	@Override
	public Object getValue(List<String> path) {
		return config.getValue(path);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the value's type is not supported by this configuration
	 */
	@Override
	public void setValue(List<String> path, Object value) {
		if (!supportsType(value.getClass())) {
			throw new IllegalArgumentException("Unsupported type: " + value.getClass());
		}
		config.setValue(path, value);
	}

	@Override
	public CheckedConfig createEmptyConfig() {
		return new CheckedConfig(config.createEmptyConfig());
	}

	@Override
	public boolean equals(Object obj) {
		return config.equals(obj);
	}

	@Override
	public int hashCode() {
		return config.hashCode();
	}
}
