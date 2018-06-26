package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.FormatDetector;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author TheElectronWill
 */
public abstract class JsonFormat<W extends ConfigWriter> implements ConfigFormat<Config> {

	private static final JsonFormat<FancyJsonWriter> FANCY = new JsonFormat<FancyJsonWriter>() {
		@Override
		public FancyJsonWriter createWriter() {
			return new FancyJsonWriter();
		}

		@Override
		public ConfigParser<Config> createParser() {
			return new JsonParser(this);
		}
	};
	private static final JsonFormat<MinimalJsonWriter> MINIMAL = new JsonFormat<MinimalJsonWriter>() {
		@Override
		public MinimalJsonWriter createWriter() {
			return new MinimalJsonWriter();
		}

		@Override
		public ConfigParser<Config> createParser() {
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
	public abstract ConfigParser<Config> createParser();

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

	/**
	 * Initializes an JSON file with an empty JSON object.
	 *
	 * @param f the existing file to initialize
	 */
	@Override
	public void initEmptyFile(File f) throws IOException {
		try (FileWriter writer = new FileWriter(f)) {
			writer.write("{}");
		}
	}
}