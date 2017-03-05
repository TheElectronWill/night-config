package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import com.electronwill.nightconfig.core.serialization.FileConfig;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;

/**
 * @author TheElectronWill
 */
public final class YamlConfig extends MapConfig implements FileConfig {
	private static final ThreadLocal<Yaml> localYaml = ThreadLocal.withInitial(Yaml::new);

	public YamlConfig() {}

	public YamlConfig(Map<String, Object> map) {
		super(map);
	}

	public void writeTo(File file) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
			StandardCharsets.UTF_8));
		Map<String, Object> dataMap = ConfigUnwrapper.unwrap(this);
		localYaml.get().dump(dataMap, writer);
	}

	public void readFrom(File file) throws IOException {
		InputStream inputStream = new FileInputStream(file);
		Map<String, Object> loadedMap = localYaml.get().loadAs(inputStream, Map.class);
		Map<String, Object> currentMap = asMap();
		currentMap.clear();
		currentMap.putAll(loadedMap);
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
}
