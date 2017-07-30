package com.electronwill.nightconfig.core;

import java.util.Map;

/**
 * A basic commented configuration.
 *
 * @author TheElectronWill
 */
public final class SimpleCommentedConfig extends AbstractCommentedConfig {
	private final ConfigFormat<?, ?, ?> configFormat;

	/**
	 * Creates a SimpleCommentedConfig with the format {@link InMemoryFormat#defaultInstance()}
	 */
	public SimpleCommentedConfig() {
		this(InMemoryFormat.defaultInstance());
	}

	/**
	 * Creates a SimpleCommentedConfig with the specified format.
	 *
	 * @param configFormat the config's format
	 */
	public SimpleCommentedConfig(ConfigFormat<?, ?, ?> configFormat) {
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleConfig with the specified data and the format {@link
	 * InMemoryFormat#defaultInstance()}. The map is used as it is and isn't copied.
	 */
	public SimpleCommentedConfig(Map<String, Object> valueMap) {
		this(valueMap, InMemoryFormat.defaultInstance());
	}

	/**
	 * Creates a SimpleConfig with the specified data and format. The map is used as it is and
	 * isn't copied.
	 */
	public SimpleCommentedConfig(Map<String, Object> valueMap, ConfigFormat<?, ?, ?> configFormat) {
		super(valueMap);
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleCommentedConfig by copying a config. The format will be the one of the copied
	 * config.
	 *
	 * @param toCopy the config to copy
	 */
	public SimpleCommentedConfig(UnmodifiableConfig toCopy) {
		this(toCopy, toCopy.configFormat());
	}

	/**
	 * Creates a SimpleCommentedConfig by copying a config and with the specified format.
	 *
	 * @param toCopy       the config to copy
	 * @param configFormat the config's format
	 */
	public SimpleCommentedConfig(UnmodifiableConfig toCopy, ConfigFormat<?, ?, ?> configFormat) {
		super(toCopy);
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleCommentedConfig by copying a config. The format will be the one of the copied
	 * config.
	 *
	 * @param toCopy the config to copy
	 */
	public SimpleCommentedConfig(UnmodifiableCommentedConfig toCopy) {
		this(toCopy, toCopy.configFormat());
	}

	/**
	 * Creates a SimpleCommentedConfig by copying a config and with the specified format.
	 *
	 * @param toCopy       the config to copy
	 * @param configFormat the config's format
	 */
	public SimpleCommentedConfig(UnmodifiableCommentedConfig toCopy,
								 ConfigFormat<?, ?, ?> configFormat) {
		super(toCopy);
		this.configFormat = configFormat;
	}

	@Override
	public ConfigFormat<?, ?, ?> configFormat() {
		return configFormat;
	}

	@Override
	protected SimpleCommentedConfig createSubConfig() {
		return new SimpleCommentedConfig(configFormat);
	}

	@Override
	public AbstractCommentedConfig clone() {
		return new SimpleCommentedConfig(this);
	}
}