package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import java.util.List;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;

/**
 * @author TheElectronWill
 */
public final class YamlFormat implements ConfigFormat<Config, Config, UnmodifiableConfig> {
	private static final ThreadLocal<YamlFormat> LOCAL_DEFAULT_FORMAT = ThreadLocal.withInitial(
			() -> new YamlFormat(new Yaml()));

	/**
	 * @return the default instance of HoconFormat
	 */
	public static YamlFormat defaultInstance() {
		return LOCAL_DEFAULT_FORMAT.get();
	}

	/**
	 * Creates an instance of YamlFormat, set with the specified Yaml object.
	 *
	 * @param yaml the Yaml object to use
	 * @return a new instance of YamlFormat
	 */
	public static YamlFormat configuredInstance(Yaml yaml) {
		return new YamlFormat(yaml);
	}

	/**
	 * @return a new config with the format {@link YamlFormat#defaultInstance()}.
	 */
	public static Config newConfig() {
		return defaultInstance().createConfig();
	}

	/**
	 * @return a new concurrent config with the format {@link YamlFormat#defaultInstance()}.
	 */
	public static Config newConcurrentConfig() {
		return defaultInstance().createConcurrentConfig();
	}

	static {
		FormatDetector.registerExtension("yaml", YamlFormat::defaultInstance);
		FormatDetector.registerExtension("yml", YamlFormat::defaultInstance);
	}

	final Yaml yaml;

	private YamlFormat(Yaml yaml) {
		this.yaml = yaml;
	}

	@Override
	public ConfigWriter<UnmodifiableConfig> createWriter() {
		return new YamlWriter(yaml);
	}

	@Override
	public ConfigParser<Config, Config> createParser() {
		return new YamlParser(this);
	}

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
		return type == null
			   || type == Boolean.class
			   || type == String.class
			   || type == java.util.Date.class
			   || type == java.sql.Date.class
			   || type == java.sql.Timestamp.class
			   || type == byte[].class
			   || type == Object[].class
			   || Number.class.isAssignableFrom(type)
			   || Set.class.isAssignableFrom(type)
			   || List.class.isAssignableFrom(type)
			   || Config.class.isAssignableFrom(type);
	}
}