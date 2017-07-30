package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.io.ConfigFormat;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;

/**
 * A checker wrapped around a commented configuration. It checks that all the values put into the
 * config are supported by the config's format (as per the {@link ConfigFormat#supportsType(Class)}
 * method. Trying to insert an unsupported value throws an IllegalArgumentException.
 *
 * @author TheElectronWill
 */
class CheckedCommentedConfig extends CommentedConfigWrapper<CommentedConfig>
		implements CommentedConfig {
	/**
	 * Creates a new CheckedConfig around a commented configuration.
	 * <p>
	 * The values that are in the config when this method is called are also checked.
	 *
	 * @param config the configuration to wrap
	 */
	CheckedCommentedConfig(CommentedConfig config) {
		super(config);
		config.valueMap().forEach((k, v) -> checkValue(v));
		//The config might already contain some elements and we must be sure that they are all supported
	}

	@Override
	public CommentedConfig checked() {
		return this;
	}

	@Override
	public String toString() {
		return "CheckedCommentedConfig of " + config;
	}

	/**
	 * Checks that a value is supported by the config. Throws an unchecked exception if the value
	 * isn't supported.
	 */
	private void checkValue(Object value) {
		ConfigFormat<?, ?, ?> format = configFormat();
		if (value != null && !format.supportsType(value.getClass())) {
			throw new IllegalArgumentException(
					"Unsupported value type: " + value.getClass().getTypeName());
		} else if (value == null && !format.supportsType(null)) {
			throw new IllegalArgumentException(
					"Null values aren't supported by this configuration.");
		}
		if (value instanceof Config) {
			((Config)value).valueMap().forEach((k, v) -> checkValue(v));
		}
	}

	/**
	 * Checks that a value is supported by the config, and returns it if it's supported. Throws an
	 * unchecked exception if the value isn't supported.
	 */
	private <T> T checkedValue(T value) {
		checkValue(value);
		return value;
	}
}
