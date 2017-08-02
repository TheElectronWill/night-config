package com.electronwill.nightconfig.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default concrete implementation of Config. The values are stored in a map, generally a HashMap,
 * or a ConcurrentHashMap if the config is concurrent.
 */
final class SimpleConfig extends AbstractConfig {
	private final ConfigFormat<?, ?, ?> configFormat;

	/**
	 * Creates a SimpleConfig with the specified format.
	 *
	 * @param configFormat the config's format
	 */
	SimpleConfig(ConfigFormat<?, ?, ?> configFormat, boolean concurrent) {
		super(concurrent ? new ConcurrentHashMap<>() : new HashMap<>());
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
	SimpleConfig(UnmodifiableConfig toCopy, ConfigFormat<?, ?, ?> configFormat,
				 boolean concurrent) {
		super(toCopy, concurrent);
		this.configFormat = configFormat;
	}

	@Override
	public ConfigFormat<?, ?, ?> configFormat() {
		return configFormat;
	}

	@Override
	public SimpleConfig createSubConfig() {
		return new SimpleConfig(configFormat, map instanceof ConcurrentMap);
	}

	@Override
	public SimpleConfig clone() {
		return new SimpleConfig(this, configFormat, map instanceof ConcurrentMap);
	}
}