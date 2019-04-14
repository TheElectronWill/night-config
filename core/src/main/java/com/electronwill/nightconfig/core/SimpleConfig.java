package com.electronwill.nightconfig.core;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Default concrete implementation of Config. The values are stored in a map, generally a HashMap,
 * or a ConcurrentHashMap if the config is concurrent.
 */
final class SimpleConfig extends AbstractConfig {
	private final ConfigFormat<?> configFormat;

	/**
	 * Creates a SimpleConfig with the specified format.
	 *
	 * @param configFormat the config's format
	 */
	SimpleConfig(ConfigFormat<?> configFormat, boolean concurrent) {
		super(concurrent);
		this.configFormat = configFormat;
	}
	
	/**
	 * Creates a SimpleConfig with the specified data and format. The map is used as it is and
	 * isn't copied.
	 * 
	 * @param map the data to use in the config
	 * @param configFormat the config's format
	 */
	SimpleConfig(Map<String, Object> map, ConfigFormat<?> configFormat) {
		super(map);
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleConfig with the specified backing map supplier and format.
	 * 
	 * @param mapCreator the supplier for backing maps
	 * @param configFormat the config's format
	 */
	SimpleConfig(Supplier<Map<String, Object>> mapCreator, ConfigFormat<?> configFormat) {
		super(mapCreator);
		this.configFormat = configFormat;
	}
	
	/**
	 * Creates a SimpleConfig by copying a config.
	 *
	 * @param toCopy       the config to copy
	 * @param configFormat the config's format
	 * @param concurrent
	 */
	SimpleConfig(UnmodifiableConfig toCopy, ConfigFormat<?> configFormat, boolean concurrent) {
		super(toCopy, concurrent);
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleConfig by copying a config.
	 *
	 * @param toCopy       the config to copy
	 * @param mapCreator   the supplier for backing maps
	 * @param configFormat the config's format
	 */
	SimpleConfig(UnmodifiableConfig toCopy, Supplier<Map<String, Object>> mapCreator, ConfigFormat<?> configFormat) {
		super(toCopy, mapCreator);
		this.configFormat = configFormat;
	}

	@Override
	public ConfigFormat<?> configFormat() {
		return configFormat;
	}

	@Override
	public SimpleConfig createSubConfig() {
		return new SimpleConfig(mapCreator, configFormat);
	}

	@Override
	public SimpleConfig clone() {
		return new SimpleConfig(this, mapCreator, configFormat);
	}
}