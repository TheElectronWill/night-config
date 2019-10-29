package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.FormatDetector;

import java.time.temporal.Temporal;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author TheElectronWill
 */
public final class TomlFormat implements ConfigFormat<CommentedConfig> {
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
	 * @return a new config with the given map creator
	 */
	public static CommentedConfig newConfig(Supplier<Map<String, Object>> s) {
		return INSTANCE.createConfig(s);
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
	public TomlWriter writer() {
		return new TomlWriter();
	}

	@Override
	public TomlParser parser() {
		return new TomlParser();
	}

	@Override
	public CommentedConfig createConfig(Supplier<Map<String, Object>> mapCreator) {
		return CommentedConfig.of(mapCreator, this);
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