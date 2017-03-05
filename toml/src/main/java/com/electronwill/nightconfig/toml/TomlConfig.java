package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
public class TomlConfig extends MapConfig {
	public TomlConfig() {}

	public TomlConfig(Map<String, Object> map) {
		super(map);
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return type == Integer.class
			|| type == Long.class
			|| type == Float.class
			|| type == Double.class
			|| type == Boolean.class
			|| type == String.class
			|| Temporal.class.isAssignableFrom(type)
			|| List.class.isAssignableFrom(type)
			|| Config.class.isAssignableFrom(type);
	}

	@Override
	public TomlConfig createSubConfig() {
		return new TomlConfig();
	}

	//TODO writeTo(File) and readFrom(File)
}
