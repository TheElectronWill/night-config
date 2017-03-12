package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.serialization.ConfigParser;
import com.typesafe.config.*;
import java.io.Reader;
import java.util.Map;

/**
 * @author TheElectronWill
 */
public final class HoconParser implements ConfigParser<HoconConfig> {
	private static final ConfigParseOptions PARSE_OPTIONS = ConfigParseOptions.defaults()
		.setAllowMissing(false).setSyntax(ConfigSyntax.CONF);

	private static HoconConfig convert(Config typesafeConfig) {
		HoconConfig config = new HoconConfig();
		put(typesafeConfig, config);
		return config;
	}

	private static void put(Config typesafeConfig, HoconConfig config) {
		Map<String, Object> map = config.asMap();
		for (Map.Entry<String, ConfigValue> entry : typesafeConfig.entrySet()) {
			Object value = entry.getValue().unwrapped();
			if (value instanceof Map) {
				value = new HoconConfig((Map)value);
			}
			map.put(entry.getKey(), value);
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
}
