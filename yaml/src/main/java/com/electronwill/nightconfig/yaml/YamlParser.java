package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.Map;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * A YAML parser that uses the snakeYaml library.
 *
 * @author TheElectronWill
 */
public final class YamlParser implements ConfigParser<Config> {
	private final Yaml yaml;
	private final ConfigFormat<Config> configFormat;

	public YamlParser(YamlFormat configFormat) {
		this.yaml = configFormat.yaml;
		this.configFormat = configFormat;
	}

	public YamlParser(Yaml yaml) {
		this.yaml = yaml;
		this.configFormat = YamlFormat.configuredInstance(yaml);
	}

	public YamlParser(LoaderOptions options) {
		this(new Yaml(options));
	}

	@Override
	public ConfigFormat<Config> getFormat() {
		return configFormat;
	}

	@Override
	public Config parse(Reader reader) {
		Config config = configFormat.createConfig();
		parse(reader, config, ParsingMode.MERGE);
		return config;
	}

	@Override
	public void parse(Reader reader, Config destination, ParsingMode parsingMode) {
		try {
			Map<String, Object> wrappedMap = wrap(yaml.loadAs(reader, Map.class));
			parsingMode.prepareParsing(destination);
			if (parsingMode == ParsingMode.ADD) {
				for (Map.Entry<String, Object> entry : wrappedMap.entrySet()) {
					destination.valueMap().putIfAbsent(entry.getKey(), entry.getValue());
				}
			} else {
				destination.valueMap().putAll(wrappedMap);
			}
		} catch (Exception e) {
			throw new ParsingException("YAML parsing failed", e);
		}
	}

	private Map<String, Object> wrap(Map<String, Object> map) {
		return new TransformingMap<>(map, this::wrap, v -> v, v -> v);
	}

	private Object wrap(Object value) {
		if (value instanceof Map) {
			Map<String, Object> map = wrap((Map)value);
			return Config.wrap(map, configFormat);
		}
		if (value == null) {
			return NULL_OBJECT;
		}
		return value;
	}
}