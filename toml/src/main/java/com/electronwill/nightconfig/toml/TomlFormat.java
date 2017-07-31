package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.ConfigFormat;

/**
 * @author TheElectronWill
 */
public final class TomlFormat implements ConfigFormat<CommentedConfig, Config, UnmodifiableConfig> {
	private static final TomlFormat INSTANCE = new TomlFormat();

	public static TomlFormat instance() {
		return INSTANCE;
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
	public boolean supportsComments() {
		return true;
	}
}