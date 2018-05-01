package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FormatDetector;

import java.time.temporal.Temporal;

/**
 * @author TheElectronWill
 */
public final class TomlFormat implements ConfigFormat<CommentedConfig, Config, UnmodifiableConfig> {
	private static final TomlFormat INSTANCE = new TomlFormat();

	/**
	 * @return the unique instance of TomlFormat
	 */
	public static TomlFormat instance() {
		return INSTANCE;
	}

	/**
	 * @return a new config with the toml format
	 */
	public static CommentedConfig newConfig() {
		return INSTANCE.createConfig();
	}

	/**
	 * @return a new thread-safe config with the toml format
	 */
	public static CommentedConfig newConcurrentConfig() {
		return INSTANCE.createConcurrentConfig();
	}

	static {
		FormatDetector.registerExtension("toml", INSTANCE);
	}

	private TomlFormat() {}

	@Override
	public TomlWriter createWriter() {
		return new TomlWriter();
	}

	@Override
	public TomlParser createParser() {
		return new TomlParser();
	}

	@Override
	public CommentedConfig createConfig() {
		return CommentedConfig.of(this);
	}

	@Override
	public CommentedConfig createConcurrentConfig() {
		return CommentedConfig.ofConcurrent(this);
	}

	@Override
	public boolean supportsComments() {
		return true;
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return ConfigFormat.super.supportsType(type) || Temporal.class.isAssignableFrom(type);
	}
}