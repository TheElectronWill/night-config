package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.io.ConfigParser;
import java.io.Reader;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * @author TheElectronWill
 */
public final class YamlParser implements ConfigParser<YamlConfig> {
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
	public void parseConfig(Reader reader, YamlConfig destination) {
		Map<String, Object> map = yaml.loadAs(reader, Map.class);
		destination.asMap().putAll(map);
	}
}