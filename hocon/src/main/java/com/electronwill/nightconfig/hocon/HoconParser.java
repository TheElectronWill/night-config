package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.io.ConfigParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import com.typesafe.config.ConfigValue;
import java.io.Reader;
import java.util.Map;

/**
 * HoconParser that uses the typesafe config library.
 *
 * @author TheElectronWill
 */
public final class HoconParser implements ConfigParser<HoconConfig> {
	private static final ConfigParseOptions OPTIONS = ConfigParseOptions.defaults()
																		.setAllowMissing(false)
																		.setSyntax(
																				ConfigSyntax.CONF);

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
		return convert(ConfigFactory.parseReader(reader, OPTIONS).resolve());
	}

	@Override
	public void parseConfig(Reader reader, HoconConfig destination) {
		put(ConfigFactory.parseReader(reader, OPTIONS).resolve(), destination);
	}
}