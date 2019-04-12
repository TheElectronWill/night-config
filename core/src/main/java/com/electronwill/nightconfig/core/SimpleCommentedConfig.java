package com.electronwill.nightconfig.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Default concrete implementation of CommentedConfig. The values are stored in a map, generally a
 * HashMap, or a ConcurrentHashMap if the config is concurrent.
 *
 * @author TheElectronWill
 */
final class SimpleCommentedConfig extends AbstractCommentedConfig {
	private final ConfigFormat<?> configFormat;

	/**
	 * Creates a SimpleCommentedConfig with the specified format.
	 *
	 * @param configFormat the config's format
	 */
	SimpleCommentedConfig(ConfigFormat<?> configFormat, boolean concurrent) {
		super(concurrent ? new ConcurrentHashMap<>() : new HashMap<>());
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleCommentedConfig with the specified data and format. The map is used as it is and
	 * isn't copied.
	 */
	SimpleCommentedConfig(Map<String, Object> valueMap, ConfigFormat<?> configFormat) {
		super(valueMap);
		this.configFormat = configFormat;
	}
	
	/**
	 * Creates a SimpleCommentedConfig with the specified backing map supplier and format.
	 * 
	 * @param mapCreator the supplier for backing maps
	 * @param configFormat the config's format
	 */
	SimpleCommentedConfig(Supplier<Map<String, Object>> mapCreator, ConfigFormat<?> configFormat) {
		super(mapCreator);
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleCommentedConfig by copying a config and with the specified format.
	 *
	 * @param toCopy       the config to copy
	 * @param configFormat the config's format
	 */
	SimpleCommentedConfig(UnmodifiableConfig toCopy, ConfigFormat<?> configFormat,
						  boolean concurrent) {
		super(toCopy, concurrent);
		this.configFormat = configFormat;
	}
	
	/**
	 * Creates a SimpleCommentedConfig by copying a config, with the specified backing map creator and format.
	 *
	 * @param toCopy       the config to copy
	 * @param mapCreator   the supplier for backing maps
	 * @param configFormat the config's format
	 */
	public SimpleCommentedConfig(UnmodifiableConfig toCopy, Supplier<Map<String, Object>> mapCreator,
			ConfigFormat<?> configFormat) {
		super(toCopy, mapCreator);
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleCommentedConfig by copying a config and with the specified format.
	 *
	 * @param toCopy       the config to copy
	 * @param configFormat the config's format
	 */
	SimpleCommentedConfig(UnmodifiableCommentedConfig toCopy, ConfigFormat<?> configFormat,
						  boolean concurrent) {
		super(toCopy, concurrent);
		this.configFormat = configFormat;
	}
	
	/**
	 * Creates a SimpleCommentedConfig by copying a config, with the specified backing map creator and format.
	 *
	 * @param toCopy       the config to copy
	 * @param mapCreator   the supplier for backing maps
	 * @param configFormat the config's format
	 */
	public SimpleCommentedConfig(UnmodifiableCommentedConfig toCopy, Supplier<Map<String, Object>> mapCreator,
			ConfigFormat<?> configFormat) {
		super(toCopy, mapCreator);
		this.configFormat = configFormat;
	}

	@Override
	public ConfigFormat<?> configFormat() {
		return configFormat;
	}

	@Override
	public SimpleCommentedConfig createSubConfig() {
		return new SimpleCommentedConfig(configFormat, map instanceof ConcurrentMap);
	}

	@Override
	public AbstractCommentedConfig clone() {
		return new SimpleCommentedConfig(this, configFormat, map instanceof ConcurrentMap);
	}
}