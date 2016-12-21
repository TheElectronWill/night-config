package com.electronwill.nightconfig.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract configuration that uses a {@link java.util.Map} to store its values.
 *
 * @author TheElectronWill
 */
public abstract class MapConfig implements Config {
	/**
	 * The internal map that stores the config values.
	 */
	protected final Map<String, Object> map;

	/**
	 * Creates a new MapConfig backed by a new {@link HashMap}.
	 *
	 * @see HashMap#HashMap()
	 */
	public MapConfig() {
		this(new HashMap<>());
	}

	/**
	 * Creates a new MapConfig backed by the specified {@link Map}.
	 *
	 * @param map the map to use to store the config values.
	 */
	public MapConfig(Map<String, Object> map) {
		this.map = map;
	}

	@Override
	public int size() {
		return map.size();
	}

	/**
	 * Returns the internal {@code Map} that stores the config's values.
	 *
	 * @return the internal map of the config.
	 */
	@Override
	public Map<String, Object> asMap() {
		return map;
	}

	@Override
	public String toString() {
		return map.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof MapConfig) {
			MapConfig config = (MapConfig)o;
			return config.map.equals(this.map);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean containsValue(List<String> path) {
		Map<String, Object> currentMap = map;
		for (String key : path) {
			final Object value = currentMap.get(key);
			if (!(value instanceof Config)) {//missing or incompatible intermediary level
				return false;
			}
			currentMap = ((Config)value).asMap();
		}
		return currentMap.containsKey(path.get(path.size() - 1));
	}

	@Override
	public Object getValue(List<String> path) {
		Map<String, Object> currentMap = map;
		for (String key : path) {
			final Object value = currentMap.get(key);
			if (!(value instanceof Config)) {//missing or incompatible intermediary level
				return null;
			}
			currentMap = ((Config)value).asMap();
		}
		return currentMap.get(path.get(path.size() - 1));
	}

	@Override
	public void setValue(List<String> path, Object value) {
		Map<String, Object> currentMap = map;
		for (String currentKey : path) {
			final Object currentValue = currentMap.get(currentKey);
			final Config config;
			if (currentValue == null) {//missing intermediary level
				config = createEmptyConfig();
				currentMap.put(currentKey, config);
			} else if (!(currentValue instanceof Config)) {//incompatible intermediary level
				throw new IllegalArgumentException("The specified path is already partially used, in such a" + " way that we cannot assign it a value.");
			} else {//existing intermediary level
				config = (Config)currentValue;
			}
			currentMap = config.asMap();
		}
		currentMap.put(path.get(path.size() - 1), value);
	}
}
