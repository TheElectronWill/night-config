package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serialization.ConfigWriter;
import java.io.IOException;
import java.io.Writer;
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
		yaml.dump(ConfigUnwrapper.unwrap(config), writer);
	}
}
