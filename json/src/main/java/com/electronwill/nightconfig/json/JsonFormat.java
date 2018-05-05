package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;

/**
 * @author TheElectronWill
 */
public abstract class JsonFormat<W extends ConfigWriter<UnmodifiableConfig>>
		implements ConfigFormat<Config, Config, UnmodifiableConfig> {

	private static final JsonFormat<FancyJsonWriter> FANCY = new JsonFormat<FancyJsonWriter>() {
		@Override
		public FancyJsonWriter createWriter() {
			return new FancyJsonWriter();
		}

		@Override
		public ConfigParser<Config, Config> createParser() {
			return new JsonParser(this);
		}
	};
	private static final JsonFormat<MinimalJsonWriter> MINIMAL = new JsonFormat<MinimalJsonWriter>() {
		@Override
		public MinimalJsonWriter createWriter() {
			return new MinimalJsonWriter();
		}

		@Override
		public ConfigParser<Config, Config> createParser() {
			return new JsonParser(this);
		}
	};

	/**
	 * @return the unique instance of JsonFormat that creates FancyJsonWriters.
	 */
	public static JsonFormat<FancyJsonWriter> fancyInstance() {
		return FANCY;
	}

	/**
	 * @return the unique instance of JsonFormat that creates MinimalJsonWriters.
	 */
	public static JsonFormat<MinimalJsonWriter> minimalInstance() {
		return MINIMAL;
	}

	/**
	 * @return a new config with the format {@link JsonFormat#fancyInstance()}.
	 */
	public static Config newConfig() {
		return FANCY.createConfig();
	}

	/**
	 * @return a new thread-safe config with the format {@link JsonFormat#fancyInstance()}.
	 */
	public static Config newConcurrentConfig() {
		return FANCY.createConcurrentConfig();
	}

	static {
		FormatDetector.registerExtension("json", FANCY);
	}

	private JsonFormat() {}

	@Override
	public abstract W createWriter();

	@Override
	public abstract ConfigParser<Config, Config> createParser();

	@Override
	public Config createConfig() {
		return Config.of(this);
	}

	@Override
	public Config createConcurrentConfig() {
		return Config.ofConcurrent(this);
	}

	@Override
	public boolean supportsComments() {
		return false;
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return ConfigFormat.super.supportsType(type) || type == NullObject.class;
	}
}