package com.electronwill.nightconfig.core;

import java.util.Map;

/**
 * A simple configuration that allows the user to specify which types of value it supports.
 */
final class SimpleConfig extends AbstractConfig {
	private final ConfigFormat<?, ?, ?> configFormat;

	/**
	 * Creates a SimpleConfig with the specified format.
	 *
	 * @param configFormat the config's format
	 */
	SimpleConfig(ConfigFormat<?, ?, ?> configFormat) {
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleConfig with the specified data and format. The map is used as it is and
	 * isn't copied.
	 */
	SimpleConfig(Map<String, Object> valueMap, ConfigFormat<?, ?, ?> configFormat) {
		super(valueMap);
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleConfig by copying a config.
	 *
	 * @param toCopy       the config to copy
	 * @param configFormat the config's format
	 */
	SimpleConfig(UnmodifiableConfig toCopy, ConfigFormat<?, ?, ?> configFormat) {
		super(toCopy);
		this.configFormat = configFormat;
	}

	@Override
	public ConfigFormat<?, ?, ?> configFormat() {
		return configFormat;
	}

	@Override
	protected SimpleConfig createSubConfig() {
		return new SimpleConfig(configFormat);
	}

	@Override
	public SimpleConfig clone() {
		return new SimpleConfig(this, configFormat);
	}
}