package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import com.electronwill.nightconfig.core.serialization.FileConfig;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author TheElectronWill
 */
public final class YamlConfig extends MapConfig implements FileConfig {
	private static final ThreadLocal<YamlWriter> LOCAL_WRITER = ThreadLocal.withInitial(YamlWriter::new);
	private static final ThreadLocal<YamlParser> LOCAL_PARSER = ThreadLocal.withInitial(YamlParser::new);

	public YamlConfig() {}

	public YamlConfig(Map<String, Object> map) {
		super(map);
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
	public void writeTo(File file, boolean append) throws IOException {
		LOCAL_WRITER.get().writeConfig(this, file, append);
	}

	@Override
	public void readFrom(File file, boolean merge) throws IOException {
		if (!merge) { asMap().clear(); }
		LOCAL_PARSER.get().parseConfig(file, this);
	}
}
