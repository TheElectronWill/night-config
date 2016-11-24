package com.electronwill.nightconfig.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/**
 * A TOML configuration, which supports the additional types LocalTime, LocalDate, LocalDateTime and
 * OffsetDateTime.
 *
 * @author TheElectronWill
 */
public interface TomlConfig extends Config {

	LocalTime getLocalTime(String path);

	void setLocalTime(String path, LocalTime value);

	LocalDate getLocalDate(String path);

	void setLocalDate(String path, LocalDate value);

	LocalDateTime getLocalDateTime(String path);

	void setLocalDateTime(String path, LocalDateTime value);

	OffsetDateTime getOffsetDateTime(String path);

	void setOffsetDateTime(String path, OffsetDateTime value);
}
