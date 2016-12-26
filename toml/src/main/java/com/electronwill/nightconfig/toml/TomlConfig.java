package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;

/**
 * @author TheElectronWill
 */
public class TomlConfig extends MapConfig {

	private static final HashSet<Class<?>> SUPPORTED_TYPES = new HashSet<>();

	static {
		SUPPORTED_TYPES.add(int.class);
		SUPPORTED_TYPES.add(Integer.class);
		SUPPORTED_TYPES.add(long.class);
		SUPPORTED_TYPES.add(Long.class);
		SUPPORTED_TYPES.add(float.class);
		SUPPORTED_TYPES.add(Float.class);
		SUPPORTED_TYPES.add(double.class);
		SUPPORTED_TYPES.add(Double.class);
		SUPPORTED_TYPES.add(boolean.class);
		SUPPORTED_TYPES.add(Boolean.class);
		SUPPORTED_TYPES.add(String.class);
		SUPPORTED_TYPES.add(List.class);
		SUPPORTED_TYPES.add(Config.class);
		SUPPORTED_TYPES.add(LocalTime.class);
		SUPPORTED_TYPES.add(LocalDate.class);
		SUPPORTED_TYPES.add(LocalDateTime.class);
		SUPPORTED_TYPES.add(OffsetDateTime.class);
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type)
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
