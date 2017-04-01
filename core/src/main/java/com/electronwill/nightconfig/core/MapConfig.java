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
	private final Map<String, Object> map;

	/**
	 * Creates a new MapConfig backed by a new {@link Map}.
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
	@SuppressWarnings("unchecked")
	public <T> T getValue(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> currentMap = map;
		for (String key : path.subList(0, lastIndex)) {
			Object value = currentMap.get(key);
			if (!(value instanceof Config)) {//missing or incompatible intermediary level
				return null;//the specified path doesn't exist -> return null
			}
			currentMap = ((Config)value).asMap();
		}
		String lastKey = path.get(lastIndex);
		return (T)currentMap.get(lastKey);
	}

	@Override
	public void setValue(List<String> path, Object value) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> currentMap = map;
		for (String currentKey : path.subList(0, lastIndex)) {
			final Object currentValue = currentMap.get(currentKey);
			final Config config;
			if (currentValue == null) {//missing intermediary level
				config = createSubConfig();
				currentMap.put(currentKey, config);
			} else if (!(currentValue instanceof Config)) {//incompatible intermediary level
				throw new IllegalArgumentException("Cannot add an element to an intermediary value of " +
					"type: " + currentValue.getClass());
			} else {//existing intermediary level
				config = (Config)currentValue;
			}
			currentMap = config.asMap();
		}
		String lastKey = path.get(lastIndex);
		currentMap.put(lastKey, value);
	}

	protected abstract MapConfig createSubConfig();

	@Override
	public void removeValue(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> currentMap = map;
		for (String key : path.subList(0, lastIndex)) {
			Object value = currentMap.get(key);
			if (!(value instanceof Config)) {//missing or incompatible intermediary level
				return;//the specified path doesn't exist -> stop here
			}
			currentMap = ((Config)value).asMap();
		}
		String lastKey = path.get(lastIndex);
		currentMap.remove(lastKey);
	}

	@Override
	public boolean containsValue(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> currentMap = map;
		for (String key : path.subList(0, lastIndex)) {
			Object value = currentMap.get(key);
			if (!(value instanceof Config)) {//missing or incompatible intermediary level
				return false;//the specified path doesn't exist -> return false
			}
			currentMap = ((Config)value).asMap();
		}
		String lastKey = path.get(lastIndex);
		return currentMap.containsKey(lastKey);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Map<String, Object> asMap() {
		return map;
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof MapConfig)) return false;
		MapConfig other = (MapConfig)obj;
		return map.equals(other.map);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + asMap();
	}
}
