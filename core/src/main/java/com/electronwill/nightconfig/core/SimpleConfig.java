package com.electronwill.nightconfig.core;


/**
 * A simple configuration that allows the user to specify which types of value it supports.
 */
public final class SimpleConfig extends AbstractConfig {
	private final ConfigFormat<?, ?, ?> configFormat;

	/**
	 * Creates a SimpleConfig with the format {@link InMemoryFormat#defaultInstance()}
	 */
	public SimpleConfig() {
		this(InMemoryFormat.defaultInstance());
	}

	/**
	 * Creates a SimpleConfig with the specified format.
	 *
	 * @param configFormat the config's format
	 */
	public SimpleConfig(ConfigFormat<?, ?, ?> configFormat) {
		this.configFormat = configFormat;
	}

	/**
	 * Creates a SimpleConfig by copying a config. The format will be the one of the copied config.
	 *
	 * @param toCopy the config to copy
	 */
	public SimpleConfig(UnmodifiableConfig toCopy) {
		this(toCopy, toCopy.configFormat());
	}

	/**
	 * Creates a SimpleConfig by copying a config.
	 *
	 * @param toCopy       the config to copy
	 * @param configFormat the config's format
	 */
	public SimpleConfig(UnmodifiableConfig toCopy, ConfigFormat<?, ?, ?> configFormat) {
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
		return new SimpleConfig(this);
	}
}