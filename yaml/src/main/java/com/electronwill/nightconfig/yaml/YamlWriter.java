package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * @author TheElectronWill
 */
public final class YamlWriter implements ConfigWriter<Config> {
	private final Yaml yaml;

	public YamlWriter() {
		this(new Yaml());
	}

	public YamlWriter(Yaml yaml) {
		this.yaml = yaml;
	}

	public YamlWriter(DumperOptions options) {
		this(new Yaml(options));
	}

	@Override
	public void writeConfig(Config config, Writer writer) throws IOException {
		Map<String, Object> unwrappedMap = unwrap(config);
		yaml.dump(unwrappedMap, writer);
	}

	private static Map<String, Object> unwrap(Config config) {
		return new TransformingMap<>(config.asMap(), YamlWriter::unwrap, v -> v, v -> v);
	}

	private static Object unwrap(Object value) {
		if (value instanceof Config) {
			return unwrap((Config)value);
		}
		return value;
	}
}