package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

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
	 * @return an instance of JsonFormat with a parser that accepts empty inputs and a fancy writer
	 */
	public static JsonFormat<FancyJsonWriter> emptyTolerantInstance() {
		return new JsonFormat<FancyJsonWriter>() {
			@Override
			public FancyJsonWriter createWriter() {
				return new FancyJsonWriter();
			}

			@Override
			public ConfigParser<Config> createParser() {
				return new JsonParser(this).setEmptyDataAccepted(true);
			}
		};
	}

	/**
	 * @return an instance of JsonFormat with a parser that accepts empty inputs and a minimal writer
	 */
	public static JsonFormat<MinimalJsonWriter> minimalEmptyTolerantInstance() {
		return new JsonFormat<MinimalJsonWriter>() {
			@Override
			public MinimalJsonWriter createWriter() {
				return new MinimalJsonWriter();
			}

			@Override
			public ConfigParser<Config> createParser() {
				return new JsonParser(this).setEmptyDataAccepted(true);
			}
		};
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
	 * Initializes an JSON nioPath with an empty JSON object.
	 *
	 * @param f the existing nioPath to initialize
	 */
	@Override
	public void initEmptyFile(Path f) throws IOException {
		try (Writer writer = Files.newBufferedWriter(f)) {
			writer.write("{}");
		}
	}
}