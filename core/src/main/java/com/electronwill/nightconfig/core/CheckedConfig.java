package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.TransformingMap;
import com.electronwill.nightconfig.core.utils.TransformingSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A checker wrapped around a configuration. It checks that all the values put into the config are
 * supported (as per the {@link Config#supportsType(Class)} method. Trying to insert an unsupported
 * value throws an IllegalArgumentException.
 *
 * @author TheElectronWill
 */
public final class CheckedConfig implements Config {
	private final Config config;

	/**
	 * Creates a new CheckedConfig around a given configuration.
	 *
	 * @param config the configuration to wrap
	 */
	public CheckedConfig(Config config) {
		this.config = Objects.requireNonNull(config, "The config to wrap must not be null!");
		config.valueMap().forEach((k, v) -> checkValue(v));
		//The config might already contain some elements and we must be sure that they are all supported
	}

	@Override
	public <T> T getValue(List<String> path) {
		return config.getValue(path);
	}

	@Override
	public <T> T setValue(List<String> path, Object value) {
		return config.setValue(path, checkedValue(value));
	}

	@Override
	public <T> T removeValue(List<String> path) {
		return config.removeValue(path);
	}

	@Override
	public boolean containsValue(List<String> path) {
		return config.containsValue(path);
	}

	@Override
	public void clear() {
		config.clear();
	}

	@Override
	public int size() {
		return config.size();
	}

	@Override
	public Map<String, Object> valueMap() {
		return new TransformingMap<>(config.valueMap(), v -> v, this::checkedValue, v -> v);
	}

	@Override
	public Set<? extends Entry> entrySet() {
		return new TransformingSet<>(config.entrySet(), e -> e, o -> null, e -> e);
		/* the writeTransformation is not important because we can't write to the set anyway,
		   since it's a generic Set<? extends Entry> */
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return config.supportsType(type);
	}

	@Override
	public boolean equals(Object obj) {
		return config.equals(obj);
	}

	@Override
	public int hashCode() {
		return config.hashCode();
	}

	@Override
	public String toString() {
		return "CheckedConfig of " + super.toString();
	}

	/**
	 * Checks that a value is supported by the config. Throws an unchecked exception if the value
	 * isn't supported.
	 *
	 * @param value the value to check
	 * @throws IllegalArgumentException if the value isn't supported
	 */
	private void checkValue(Object value) {
		if (value != null && !supportsType(value.getClass())) {
			throw new IllegalArgumentException(
					"Unsupported value type: " + value.getClass().getTypeName());
		} else if (value == null && !supportsType(null)) {
			throw new IllegalArgumentException(
					"Null values aren't supported by this configuration.");
		}
		if (value instanceof Config) {
			((Config)value).valueMap().forEach((k, v) -> checkValue(v));
		}
	}

	/**
	 * Checks that a value is supported by the config, and returns it if it's supported. Throws an
	 * unchecked exception if the value isn't supported.
	 *
	 * @param value the value to check
	 * @param <T>   the value's type
	 * @return the value, if it's supported
	 *
	 * @throws IllegalArgumentException if the value isn't supported
	 */
	private <T> T checkedValue(T value) {
		checkValue(value);
		return value;
	}
}