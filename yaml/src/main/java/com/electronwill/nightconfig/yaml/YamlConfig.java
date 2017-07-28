package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.UnmodifiableFileConfig;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A <a href="http://yaml.org/spec/current.html">YAML 1.1</a> configuration.
 *
 * @author TheElectronWill
 */
public final class YamlConfig extends AbstractConfig {
	/**
	 * Creates an empty YamlConfig.
	 */
	public YamlConfig() {}

	/**
	 * Creates a YamlConfig that is a copy of the specified config.
	 *
	 * @param toCopy the config to copy
	 */
	public YamlConfig(UnmodifiableConfig toCopy) {
		super(toCopy);
	}

	/**
	 * Creates a YamlConfig backed by the given Map.
	 *
	 * @param map the map containing the values
	 */
	public YamlConfig(Map<String, Object> map) {
		super(map);
	}

	@Override
	public YamlConfig clone() {
		return new YamlConfig(this);
	}

	protected YamlConfig createSubConfig() {
		return new YamlConfig();
	}
}