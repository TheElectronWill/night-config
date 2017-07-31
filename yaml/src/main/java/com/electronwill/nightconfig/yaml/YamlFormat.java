package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import java.util.List;
import java.util.Set;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * @author TheElectronWill
 */
public final class YamlFormat implements ConfigFormat<Config, Config, UnmodifiableConfig> {
	private static final ThreadLocal<YamlFormat> LOCAL_DEFAULT_FORMAT = ThreadLocal.withInitial(
			() -> new YamlFormat(new Yaml()));

	public static YamlFormat defaultInstance() {
		return LOCAL_DEFAULT_FORMAT.get();
	}

	public static YamlFormat configuredInstance(Yaml yaml) {
		return new YamlFormat(yaml);
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