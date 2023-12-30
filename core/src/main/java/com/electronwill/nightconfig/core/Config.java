package com.electronwill.nightconfig.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

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
	 * @return true if the value has been added, false if a value is already associated with the
	 * given path
	 */
	boolean add(List<String> path, Object value);

	/**
	 * Adds a config value. The value is set iff there is no value associated with the given path.
	 *
	 * @param path  the value's path, each part separated by a dot. Example "a.b.c"
	 * @param value the value to set
	 * @return true if the value has been added, false if a value is already associated with the
	 * given path
	 */
	default boolean add(String path, Object value) {
		return add(split(path, '.'), value);
	}

	/**
	 * Adds all the values of a config to this config, without replacing existing entries.
	 *
	 * @param config the source config
	 */
	default void addAll(UnmodifiableConfig config) {
		for (UnmodifiableConfig.Entry ue : config.entrySet()) {
			List<String> key = Collections.singletonList(ue.getKey());
			Object value = ue.getRawValue();
			boolean existed = !add(key, value);
			if (existed && value instanceof UnmodifiableConfig) {

			}
		}
	}

	/**
	 * Copies all the values of a config into this config. Existing entries are replaced, missing
	 * entries are created.
	 *
	 * @param config the source config
	 */
	default void putAll(UnmodifiableConfig config) {
		valueMap().putAll(config.valueMap());
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
	 * Removes all the values of the given config from this config.
	 *
	 * @param config the values to remove
	 */
	default void removeAll(UnmodifiableConfig config) {
		valueMap().keySet().removeAll(config.valueMap().keySet());
	}

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
			public <T> T getRaw(List<String> path) {
				return Config.this.getRaw(path);
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
			public ConfigFormat<?> configFormat() {
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

	/**
	 * Creates a new sub config of this config, as created when a subconfig's creation is
	 * implied by {@link #set(List, Object)} or {@link #add(List, Object)}.
	 *
	 * @return a new sub config
	 */
	Config createSubConfig();

	//--- Scala convenience methods ---

	/**
	 * For scala: sets a config value.
	 *
	 * @param path  the value's path, each part separated by a dot. Example "a.b.c"
	 * @param value the value to set
	 * @see #set(String, Object)
	 */
	default void update(String path, Object value) {
		set(path, value);
	}

	/**
	 * For scala: sets a config value.
	 *
	 * @param path  the value's path, each element of the list is a different part of the path.
	 * @param value the value to set
	 * @see #set(List, Object)
	 */
	default void update(List<String> path, Object value) {
		set(path, value);
	}

	//--- Static methods ---

	/**
	 * Creates a Config of the given format.
	 *
	 * @param format the config's format
	 * @return a new empty config
	 */
	static Config of(ConfigFormat<? extends Config> format) {
		return new SimpleConfig(format, false);
	}

	/**
	 * Creates a Config backed by a certain kind of map, given by a supplier.
	 *
	 * If you wish all your configs to preserve insertion order, please have a look at the more
	 * practical setting {@link #setInsertionOrderPreserved(boolean)}.
	 *
	 * @param mapCreator a supplier which will be called to create all backing maps for this config (including sub-configs)
	 * @param format the config's format
	 * @return a new config backed by the map
	 */
	static Config of(Supplier<Map<String, Object>> mapCreator, ConfigFormat<?> format) {
		return new SimpleConfig(mapCreator, format);
	}

	/**
	 * Creates a thread-safe Config of the given format.
	 *
	 * @param format the config's format
	 * @return a new empty, thread-safe config
	 */
	static Config ofConcurrent(ConfigFormat<? extends Config> format) {
		return new SimpleConfig(format, true);
	}

	/**
	 * Creates a Config with format {@link InMemoryFormat#defaultInstance()}.
	 *
	 * @return a new empty config
	 */
	static Config inMemory() {
		return InMemoryFormat.defaultInstance().createConfig();
	}

	/**
	 * Creates a Config with format {@link InMemoryFormat#withUniversalSupport()}.
	 *
	 * @return a new empty config
	 */
	static Config inMemoryUniversal() {
		return InMemoryFormat.withUniversalSupport().createConfig();
	}

	/**
	 * Creates a thread-safe Config with format {@link InMemoryFormat#defaultInstance()}.
	 *
	 * @return a new empty config
	 */
	static Config inMemoryConcurrent() {
		return InMemoryFormat.defaultInstance().createConcurrentConfig();
	}

	/**
	 * Creates a thread-safe Config with format {@link InMemoryFormat#withUniversalSupport()}.
	 *
	 * @return a new empty config
	 */
	static Config inMemoryUniversalConcurrent() {
		return InMemoryFormat.withUniversalSupport().createConcurrentConfig();
	}

	/**
	 * Creates a Config backed by a Map. Any change to the map is reflected in the config and
	 * vice-versa.
	 *
	 * If you wish all your configs to preserve insertion order, please have a look at the more
	 * practical setting {@link #setInsertionOrderPreserved(boolean)}.
	 *
	 * @param map    the Map to use
	 * @param format the config's format
	 * @return a new config backed by the map
	 */
	static Config wrap(Map<String, Object> map, ConfigFormat<?> format) {
		return new SimpleConfig(map, format);
	}

	/**
	 * Creates a new Config with the content of the given config. The returned config will have
	 * the same format as the copied config.
	 *
	 * @param config the config to copy
	 * @return a copy of the config
	 */
	static Config copy(UnmodifiableConfig config) {
		return new SimpleConfig(config, config.configFormat(), false);
	}

	/**
	 * Creates a new Config with the content of the given config. The returned config will have
	 * the same format as the copied config, and be backed by the given supplier.
	 *
	 * If you wish all your configs to preserve insertion order, please have a look at the more
	 * practical setting {@link #setInsertionOrderPreserved(boolean)}.
	 *
	 * @see #of(Supplier, ConfigFormat)
	 *
	 * @param config the config to copy
	 * @param mapCreator a supplier which will be called to create all backing maps for this config (including sub-configs)
	 * @return a copy of the config
	 */
	static Config copy(UnmodifiableConfig config, Supplier<Map<String, Object>> mapCreator) {
		return new SimpleConfig(config, mapCreator, config.configFormat());
	}

	/**
	 * Creates a new Config with the content of the given config.
	 *
	 * @param config the config to copy
	 * @param format the config's format
	 * @return a copy of the config
	 */
	static Config copy(UnmodifiableConfig config, ConfigFormat<?> format) {
		return new SimpleConfig(config, format, false);
	}

	/**
	 * Creates a new Config with the content of the given config.
	 * The returned config will be backed by the given map supplier.
	 *
	 * If you wish all your configs to preserve insertion order, please have a look at the more
	 * practical setting {@link #setInsertionOrderPreserved(boolean)}.
	 *
	 * @see #of(Supplier, ConfigFormat)
	 *
	 * @param config the config to copy
	 * @param mapCreator a supplier which will be called to create all backing maps for this config (including sub-configs)
	 * @param format the config's format
	 * @return a copy of the config
	 */
	static Config copy(UnmodifiableConfig config, Supplier<Map<String, Object>> mapCreator, ConfigFormat<?> format) {
		return new SimpleConfig(config, mapCreator, format);
	}

	/**
	 * Creates a new Config with the content of the given config. The returned config will have
	 * the same format as the copied config.
	 *
	 * @param config the config to copy
	 * @return a thread-safe copy of the config
	 */
	static Config concurrentCopy(UnmodifiableConfig config) {
		return new SimpleConfig(config, config.configFormat(), true);
	}

	/**
	 * Creates a new Config with the content of the given config.
	 *
	 * @param config the config to copy
	 * @param format the config's format
	 * @return a thread-safe copy of the config
	 */
	static Config concurrentCopy(UnmodifiableConfig config, ConfigFormat<?> format) {
		return new SimpleConfig(config, format, true);
	}

	/**
	 * Checks if the newly created configs keep the insertion order of their content.
	 * By default this is not the case. This can be controlled with the `nightconfig.ordered`
	 * system property or by calling {@link #setInsertionOrderPreserved(boolean)}.
	 * <p>
	 * This setting does not apply to configurations created from a Map, from another Config,
	 * or with a specific map supplier.
	 *
	 * @return true if the new configs preserve the insertion order of their values, false to
	 *         give no guarantee about the values ordering.
	 */
	static boolean isInsertionOrderPreserved() {
		String prop =  System.getProperty("nightconfig.preserveInsertionOrder");
		return (prop != null) && (prop.equals("true") || prop.equals("1"));
	}

	/**
	 * Modifies the behavior of the new configurations with regards to the preservation of the
	 * order of config values.
	 * <p>
	 * This setting does not apply to configurations created from a Map, from another Config,
	 * or with a specific map supplier.
	 *
	 * @param orderPreserved true to make the new configs preserve the insertion order of their
	 *                       values, false to give no guarantee about the values ordering.
	 * @see #isInsertionOrderPreserved()
	 */
	static void setInsertionOrderPreserved(boolean orderPreserved) {
		System.setProperty("nightconfig.preserveInsertionOrder", orderPreserved ? "true" : "false");
	}

	/**
	 * Returns a map supplier that fulfills the given requirements.
	 *
	 * @param concurrent true to make the maps thread-safe
	 * @param insertionOrderPreserved true to make the maps preserve the insertion order of values
	 * @return a map supplier corresponding to the given settings
	 */
	static <T> Supplier<Map<String, T>> getDefaultMapCreator(boolean concurrent, boolean insertionOrderPreserved) {
		if (insertionOrderPreserved) {
			return concurrent ? ()->Collections.synchronizedMap(new LinkedHashMap<>()) : LinkedHashMap::new;
			// TODO find or make a ConcurrentMap that preserves the insertion order
		}
		return concurrent ? ConcurrentHashMap::new : HashMap::new;
	}

	/**
	 * Returns a map supplier that fullfills the given requirements. It preserves
	 * (or not) the insertion order of its values according to
	 * {@link Config#isInsertionOrderPreserved()}.
	 *
	 * @param concurrent true to make the maps thread-safe
	 * @return a map supplier corresponding to the given settings
	 */
	static <T> Supplier<Map<String, T>> getDefaultMapCreator(boolean concurrent) {
		return getDefaultMapCreator(concurrent, Config.isInsertionOrderPreserved());
	}
}