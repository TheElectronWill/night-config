package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import com.typesafe.config.ConfigUtil;
import com.typesafe.config.ConfigValue;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A HOCON parser that uses the typesafehub config library.
 *
 * @author TheElectronWill
 * @see <a href="https://github.com/typesafehub/config/blob/master/HOCON.md">HOCON spec by
 * typesafehub</a>
 */
public final class HoconParser implements ConfigParser<HoconConfig, Config> {
	private static final ConfigParseOptions OPTIONS = ConfigParseOptions.defaults()
																		.setAllowMissing(false)
																		.setSyntax(ConfigSyntax.CONF);

	@Override
	public HoconConfig parse(Reader reader) {
		HoconConfig config = new HoconConfig();
		parse(reader, config);
		return config;
	}

	@Override
	public void parse(Reader reader, Config destination) {
		try {
			com.typesafe.config.Config parsed = ConfigFactory.parseReader(reader, OPTIONS).resolve();
			if (destination instanceof CommentedConfig) {
				put(parsed, (CommentedConfig)destination);
			} else {
				put(parsed, destination);
			}
		} catch (Exception e) {
			throw new ParsingException("HOCON parsing failed", e);
		}
	}

	private static void put(com.typesafe.config.Config typesafeConfig, Config destination) {
		for (Map.Entry<String, ConfigValue> entry : typesafeConfig.entrySet()) {
			List<String> path = ConfigUtil.splitPath(entry.getKey());
			destination.set(path, unwrap(entry.getValue().unwrapped()));
		}
	}

	private static void put(com.typesafe.config.Config typesafeConfig, CommentedConfig destination) {
		for (Map.Entry<String, ConfigValue> entry : typesafeConfig.entrySet()) {
			List<String> path = ConfigUtil.splitPath(entry.getKey());
			ConfigValue value = entry.getValue();
			destination.set(path, unwrap(value.unwrapped()));
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
			return new HoconConfig(unwrappedMap);
		} else if (o instanceof List) {
			List<?> list = (List<?>)o;
			if (!list.isEmpty() && list.get(0) instanceof Map) {
				List<Config> configList = new ArrayList<>();
				for (Object element : list) {
					configList.add((Config)unwrap(element));
				}
				return configList;
			}
		}
		return o;
	}
}