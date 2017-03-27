package com.electronwill.nightconfig.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.electronwill.nightconfig.core.utils.StringUtils.split;

/**
 * A (modifiable) configuration that contains key/value mappings. Configurations are generally
 * <b>not</b> thread-safe.
 *
 * @author TheElectronWill
 */
public interface Config extends UnmodifiableConfig {

	/**
	 * Sets a config value.
	 *
	 * @param path  the value's path, each part separated by a dot. Example "a.b.c"
	 * @param value the value to set
	 */
	default void setValue(String path, Object value) {
		setValue(split(path, '.'), value);
	}

	/**
	 * Sets a config value.
	 *
	 * @param path  the value's path, each element of the list is a different part of the path.
	 * @param value the value to set
	 */
	void setValue(List<String> path, Object value);

	/**
	 * Removes a value from the config.
	 *
	 * @param path the value's path, each part separated by a dot. Example "a.b.c"
	 */
	default void removeValue(String path) {
		removeValue(split(path, '.'));
	}

	/**
	 * Removes a value from the config.
	 *
	 * @param path the value's path, each element of the list is a different part of the path.
	 */
	void removeValue(List<String> path);

	/**
	 * Removes all values from the config.
	 */
	void clear();

	/**
	 * Returns an Unmodifiable view of the config. Any change to the original (modifiable) config
	 * is still reflected to the returned UnmodifiableConfig, so it's unmodifiable but not
	 * immutable.
	 *
	 * @return an Unmodifiable view of the config.
	 */
	default UnmodifiableConfig asUnmodifiable() {
		return new UnmodifiableConfig() {
			@Override
			public <T> T getValue(List<String> path) {
				return Config.this.getValue(path);
			}

			@Override
			public boolean containsValue(List<String> path) {
				return Config.this.containsValue(path);
			}

			@Override
			public int size() {
				return Config.this.size();
			}

			@Override
			public Map<String, Object> asMap() {
				return Collections.unmodifiableMap(Config.this.asMap());
			}
		};
	}

	/**
	 * Checks if the given type is supported by this config. If the type is null, it checks if the
	 * config supports null values.
	 * <p>
	 * Please note that an implementation of the Config interface is <b>not</b> required to check
	 * the type of the values that you add to it.
	 *
	 * @param type the type's class, or {@code null} to check if the config supports null values
	 * @return {@code true} if it is supported, {@code false} if it isn't.
	 */
	boolean supportsType(Class<?> type);

	/**
	 * Returns a Map view of the config. Any change to the map is reflected in the config and
	 * vice-versa.
	 * <p>
	 * The returned map is not required to (and likely doesn't) check if the values that you put
	 * into it are supported by this configuration. It is also not required to perform any kind of
	 * synchronization or anything to ensure thread-safety. The caller of this method is
	 * responsible for taking care of these things.
	 *
	 * @return a Map view of the config.
	 */
	Map<String, Object> asMap();
}