package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serialization.ConfigParser;
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
 * @author TheElectronWill
 */
public final class HoconParser implements ConfigParser<HoconConfig> {
	private static final ConfigParseOptions PARSE_OPTIONS = ConfigParseOptions.defaults()
																			  .setAllowMissing(false)
																			  .setSyntax(ConfigSyntax.CONF);

	private static HoconConfig convert(com.typesafe.config.Config typesafeConfig) {
		HoconConfig config = new HoconConfig();
		put(typesafeConfig, config);
		return config;
	}

	private static void put(com.typesafe.config.Config typesafeConfig, HoconConfig destination) {
		for (Map.Entry<String, ConfigValue> entry : typesafeConfig.entrySet()) {
			List<String> path = ConfigUtil.splitPath(entry.getKey());
			destination.setValue(path, unwrap(entry.getValue().unwrapped()));
		}
	}

	@Override
	public HoconConfig parseConfig(Reader reader) {
		return convert(ConfigFactory.parseReader(reader, PARSE_OPTIONS).resolve());
	}

	@Override
	public void parseConfig(Reader reader, HoconConfig destination) {
		put(ConfigFactory.parseReader(reader, PARSE_OPTIONS).resolve(), destination);
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
