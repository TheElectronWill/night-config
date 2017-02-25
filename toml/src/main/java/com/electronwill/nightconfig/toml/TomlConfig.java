package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.List;

/**
 * @author TheElectronWill
 */
public class TomlConfig extends MapConfig {

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
	public TomlConfig createEmptyConfig() {
		return new TomlConfig();
	}

	public LocalTime getLocalTime(String path) {
		return (LocalTime)getValue(path);
	}

	public void setLocalTime(String path, LocalTime value) {
		setValue(path, value);
	}

	public LocalDate getLocalDate(String path) {
		return (LocalDate)getValue(path);
	}

	public void setLocalDate(String path, LocalDate value) {
		setValue(path, value);
	}

	public LocalDateTime getLocalDateTime(String path) {
		return (LocalDateTime)getValue(path);
	}

	public void setLocalDateTime(String path, LocalDateTime value) {
		setValue(path, value);
	}

	public OffsetDateTime getOffsetDateTime(String path) {
		return (OffsetDateTime)getValue(path);
	}

	public void setOffsetDateTime(String path, OffsetDateTime value) {
		setValue(path, value);
	}

}
