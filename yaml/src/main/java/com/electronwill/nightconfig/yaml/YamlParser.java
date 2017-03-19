package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import java.io.Reader;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * A YAML parser that uses the snakeYaml library.
 *
 * @author TheElectronWill
 */
public final class YamlParser implements ConfigParser<YamlConfig, Config> {
	private final Yaml yaml;

	public YamlParser() {
		this(new Yaml());
	}

	public YamlParser(Yaml yaml) {
		this.yaml = yaml;
	}

	public YamlParser(LoaderOptions options) {
		this(new Yaml(options));
	}

	@Override
	public YamlConfig parseConfig(Reader reader) {
		YamlConfig config = new YamlConfig();
		parseConfig(reader, config);
		return config;
	}

	@Override
	public void parseConfig(Reader reader, Config destination) {
		Map<String, Object> wrappedMap = wrap(yaml.loadAs(reader, Map.class));
		destination.asMap().putAll(wrappedMap);
	}

	private static Map<String, Object> wrap(Map<String, Object> map) {
		return new TransformingMap<>(map, YamlParser::wrap, v -> v, v -> v);
	}

	private static Object wrap(Object value) {
		if (value instanceof Map) {
			return wrap((Map<String, Object>)value);
		}
		return value;
	}
}