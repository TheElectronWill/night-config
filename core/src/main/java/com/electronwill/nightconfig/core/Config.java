package com.electronwill.nightconfig.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	 * @param <T>   the type of the old value
	 * @return the old value if any, or {@code null}
	 */
	default <T> T set(String path, Object value) {
		return set(split(path, '.'), value);
	}

	/**
	 * Sets a config value.
	 *
	 * @param path  the value's path, each element of the list is a different part of the path.
	 * @param value the value to set
	 * @param <T>   the type of the old value
	 * @return the old value if any, or {@code null}
	 */
	<T> T set(List<String> path, Object value);

	/**
	 * Adds a config value. The value is set iff there is no value associated with the given path.
	 *
	 * @param path  the value's path, each element of the list is a different part of the path.
	 * @param value the value to set
	 */
	void add(List<String> path, Object value);

	/**
	 * Adds a config value. The value is set iff there is no value associated with the given path.
	 *
	 * @param path  the value's path, each part separated by a dot. Example "a.b.c"
	 * @param value the value to set
	 */
	default void add(String path, Object value) {
		add(split(path, '.'), value);
	}

	/**
	 * Removes a value from the config.
	 *
	 * @param path the value's path, each part separated by a dot. Example "a.b.c"
	 * @param <T>  the type of the old value
	 * @return the old value if any, or {@code null}
	 */
	default <T> T remove(String path) {
		return remove(split(path, '.'));
	}

	/**
	 * Removes a value from the config.
	 *
	 * @param path the value's path, each element of the list is a different part of the path.
	 * @param <T>  the type of the old value
	 * @return the old value if any, or {@code null}
	 */
	<T> T remove(List<String> path);

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
	default UnmodifiableConfig unmodifiable() {
		return new UnmodifiableConfig() {
			@Override
			public <T> T get(List<String> path) {
				return Config.this.get(path);
			}

			@Override
			public boolean contains(List<String> path) {
				return Config.this.contains(path);
			}

			@Override
			public int size() {
				return Config.this.size();
			}

			@Override
			public Map<String, Object> valueMap() {
				return Collections.unmodifiableMap(Config.this.valueMap());
			}

			@Override
			public Set<? extends Entry> entrySet() {
				return Config.this.entrySet();
			}

			@Override
			public ConfigFormat<?, ?, ?> configFormat() {
				return Config.this.configFormat();
			}
		};
	}

	/**
	 * Returns a checked view of the config. It checks that all the values put into the config are
	 * supported by the config's format (as per the {@link ConfigFormat#supportsType(Class)}
	 * method. Trying to insert an unsupported value throws an IllegalArgumentException.
	 * <p>
	 * The values that are in the config when this method is called are also checked.
	 *
	 * @return a checked view of the config.
	 */
	default Config checked() {
		return new CheckedConfig(this);
	}

	/**
	 * Returns a Map view of the config's values. Any change to the map is reflected in the config
	 * and vice-versa.
	 */
	Map<String, Object> valueMap();

	/**
	 * Returns a Set view of the config's entries. Any change to the set or to the entries is
	 * reflected in the config, and vice-versa.
	 */
	@Override
	Set<? extends Entry> entrySet();

	/**
	 * A modifiable config entry.
	 */
	interface Entry extends UnmodifiableConfig.Entry {
		/**
		 * Sets the entry's value.
		 *
		 * @param value the value to set
		 * @param <T>   the type of the old value
		 * @return the previous value
		 */
		<T> T setValue(Object value);
	}

	static Config of(ConfigFormat<? extends Config, ? super Config, ? super Config> format) {
		return new SimpleConfig(format);
	}

	static Config inMemory() {
		return new SimpleConfig(InMemoryFormat.defaultInstance());
	}
}