package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.concurrent.ConcurrentConfig;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.StampedConfig.Accumulator;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.utils.TransformingList;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * A YAML parser that uses the snakeYaml library.
 *
 * @author TheElectronWill
 */
public final class YamlParser implements ConfigParser<Config> {
	private final Yaml yaml;
	private final ConfigFormat<Config> configFormat;

	public YamlParser() {
		this(YamlFormat.defaultInstance());
	}

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
		if (destination instanceof ConcurrentConfig) {
			((ConcurrentConfig)destination).bulkUpdate(view -> {
				parse(reader, view, parsingMode);
			});
			return;
		}

		try {
			Map<String, Object> map = yaml.loadAs(reader, Map.class);
			parsingMode.prepareParsing(destination);
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				parsingMode.put(destination, Collections.singletonList(entry.getKey()), convertValue(entry.getValue(), destination));
			}
		} catch (Exception e) {
			throw new ParsingException(null, "YAML parsing failed", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Object convertValue(Object v, Config parentConfig) {
		if (v instanceof Map) {
			Map<String, Object> map = (Map<String, Object>)v;
			Config sub = parentConfig.createSubConfig();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				sub.set(Collections.singletonList(entry.getKey()), convertValue(entry.getValue(), sub));
			}
			return sub;
		} else if (v instanceof List) {
			List<Object> list = (List<Object>)v;
			list.replaceAll(e -> convertValue(e, parentConfig));
			return list;
		} else {
			return v;
		}
	}
}