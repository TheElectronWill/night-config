package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A <a href="http://yaml.org/spec/current.html">YAML 1.1</a> configuration.
 *
 * @author TheElectronWill
 */
public final class YamlConfig extends AbstractConfig implements FileConfig {
	private static final ThreadLocal<YamlWriter> LOCAL_WRITER = ThreadLocal.withInitial(YamlWriter::new);
	private static final ThreadLocal<YamlParser> LOCAL_PARSER = ThreadLocal.withInitial(YamlParser::new);

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

	public boolean supportsType(Class<?> type) {
		return type == null
			   || type == Boolean.class
			   || type == String.class
			   || type == java.util.Date.class
			   || type == java.sql.Date.class
			   || type == java.sql.Timestamp.class
			   || type == byte[].class
			   || type == Object[].class
			   || Number.class.isAssignableFrom(type)
			   || Set.class.isAssignableFrom(type)
			   || List.class.isAssignableFrom(type)
			   || Config.class.isAssignableFrom(type);
	}

	@Override
	public void write(File file, boolean append) {
		LOCAL_WRITER.get().write(this, file, append);
	}

	@Override
	public void parse(File file, boolean merge) {
		if (!merge) {
			clear();
		}
		LOCAL_PARSER.get().parse(file, this);
	}
}