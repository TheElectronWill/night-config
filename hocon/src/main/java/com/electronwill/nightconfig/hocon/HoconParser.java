package com.electronwill.nightconfig.hocon;

import com.typesafe.config.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author TheElectronWill
 */
public final class HoconParser {
	private HoconParser() {}

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
			map.put(entry.getKey(), entry.getValue().unwrapped());
		}
	}

	public static HoconConfig parseConfiguration(File file) {
		return convert(ConfigFactory.parseFile(file, PARSE_OPTIONS).resolve());
	}

	public static void parseConfiguration(File file, HoconConfig config) {
		put(ConfigFactory.parseFile(file, PARSE_OPTIONS).resolve(), config);
	}

	public static HoconConfig parseConfiguration(Reader reader) {
		return convert(ConfigFactory.parseReader(reader, PARSE_OPTIONS).resolve());
	}

	public static void parseConfiguration(Reader reader, HoconConfig config) {
		put(ConfigFactory.parseReader(reader, PARSE_OPTIONS).resolve(), config);
	}

	public static HoconConfig parseConfiguration(InputStream input) {
		return parseConfiguration(new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)));
	}

	public static void parseConfiguration(InputStream input, HoconConfig config) {
		parseConfiguration(new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)), config);
	}
}
