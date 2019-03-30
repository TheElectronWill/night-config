package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.typesafe.config.*;

import java.io.Reader;
import java.util.*;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * A HOCON parser that uses the typesafehub config library.
 *
 * @author TheElectronWill
 * @see <a href="https://github.com/typesafehub/config/blob/master/HOCON.md">HOCON spec by
 * typesafehub</a>
 */
public final class HoconParser implements ConfigParser<CommentedConfig> {
	private static final ConfigParseOptions OPTIONS = ConfigParseOptions.defaults()
																		.setAllowMissing(false)
																		.setSyntax(ConfigSyntax.CONF);

	@Override
	public ConfigFormat<CommentedConfig> getFormat() {
		return HoconFormat.instance();
	}

	@Override
	public CommentedConfig parse(Reader reader) {
		CommentedConfig config = HoconFormat.instance().createConfig();
		parse(reader, config, ParsingMode.MERGE);
		return config;
	}

	@Override
	public void parse(Reader reader, Config destination, ParsingMode parsingMode) {
		try {
			ConfigObject parsed = ConfigFactory.parseReader(reader, OPTIONS).resolve().root();
			parsingMode.prepareParsing(destination);
			if (destination instanceof CommentedConfig) {
				put(parsed, (CommentedConfig)destination, parsingMode);
			} else {
				put(parsed, destination, parsingMode);
			}
		} catch (Exception e) {
			throw new ParsingException("HOCON parsing failed", e);
		}
	}

	private static void put(ConfigObject typesafeConfig, Config destination,
							ParsingMode parsingMode) {
		for (Map.Entry<String, ConfigValue> entry : typesafeConfig.entrySet()) {
			List<String> path = ConfigUtil.splitPath(entry.getKey());
			parsingMode.put(destination, path, unwrap(entry.getValue().unwrapped()));
		}
	}

	private static void put(ConfigObject typesafeConfig, CommentedConfig destination,
							ParsingMode parsingMode) {
		for (Map.Entry<String, ConfigValue> entry : typesafeConfig.entrySet()) {
			List<String> path = Collections.singletonList(entry.getKey());
			ConfigValue value = entry.getValue();
			if (value instanceof ConfigObject) {
				CommentedConfig subConfig = destination.createSubConfig();
				put((ConfigObject)value, subConfig, parsingMode);
				parsingMode.put(destination, path, subConfig);
			} else {
				parsingMode.put(destination, path, unwrap(value.unwrapped()));
			}
			List<String> comments = value.origin().comments();
			if (!comments.isEmpty()) {
				destination.setComment(path, String.join("\n", value.origin().comments()));
			}
		}
	}

	private static Object unwrap(Object o) {
		if (o instanceof Map) {
			Map<String, ?> map = (Map)o;
			Map<String, Object> unwrappedMap = new HashMap<>(map.size());
			for (Map.Entry<String, ?> entry : map.entrySet()) {
				unwrappedMap.put(entry.getKey(), unwrap(entry.getValue()));
			}
			return Config.wrap(unwrappedMap, HoconFormat.instance());
		} else if (o instanceof List) {
			List<?> list = (List<?>)o;
			if (!list.isEmpty() && list.get(0) instanceof Map) {
				List<Config> configList = new ArrayList<>();
				for (Object element : list) {
					configList.add((Config)unwrap(element));
				}
				return configList;
			}
		} else if (o == null) {
			return NULL_OBJECT;
		}
		return o;
	}
}