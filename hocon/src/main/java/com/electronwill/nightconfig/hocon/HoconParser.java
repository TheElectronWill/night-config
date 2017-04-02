package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import com.typesafe.config.ConfigValue;
import java.io.Reader;
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
																		.setSyntax(
																				ConfigSyntax.CONF);

	@Override
	public HoconConfig parse(Reader reader) {
		HoconConfig config = new HoconConfig();
		parse(reader, config);
		return config;
	}

	@Override
	public void parse(Reader reader, Config destination) {
		try {
			put(ConfigFactory.parseReader(reader, OPTIONS).resolve(), destination);
		} catch (Exception e) {
			throw new ParsingException("HOCON parsing failed", e);
		}
	}

	private static void put(com.typesafe.config.Config typesafeConfig, Config destination) {
		Map<String, Object> map = destination.asMap();
		for (Map.Entry<String, ConfigValue> entry : typesafeConfig.entrySet()) {
			Object value = entry.getValue().unwrapped();
			if (value instanceof Map) {
				value = new HoconConfig((Map)value);
			}
			map.put(entry.getKey(), value);
		}
	}
}