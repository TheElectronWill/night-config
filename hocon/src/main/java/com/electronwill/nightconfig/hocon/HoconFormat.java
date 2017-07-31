package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;

/**
 * @author TheElectronWill
 */
public final class HoconFormat implements ConfigFormat<CommentedConfig, Config, UnmodifiableConfig> {
	private static final HoconFormat INSTANCE = new HoconFormat();

	public static HoconFormat instance() {
		return INSTANCE;
	}

	private HoconFormat() {}

	@Override
	public ConfigWriter<UnmodifiableConfig> createWriter() {
		return new HoconWriter();
	}

	@Override
	public ConfigParser<CommentedConfig, Config> createParser() {
		return new HoconParser();
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