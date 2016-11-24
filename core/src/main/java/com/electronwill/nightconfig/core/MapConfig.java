package com.electronwill.nightconfig.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A configuration that uses a {@link java.util.Map} to store its values.
 *
 * @author TheElectronWill
 */
public class MapConfig implements Config {
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
	public boolean containsValue(String path) {
		final List<String> keys = StringUtils.split(path, ',');
		final int lastIndex = keys.size() - 1;
		return containsValue(keys, lastIndex);
	}

	protected boolean containsValue(List<String> keys, int lastIndex) {
		Map<String, Object> currentMap = map;
		for (String currentKey : keys.subList(0, lastIndex)) {
			Object currentValue = currentMap.get(currentKey);
			if (!(currentValue instanceof Config)) {//missing or incompatible intermediary level
				return false;
			}
			currentMap = ((Config)currentValue).asMap();
		}
		return currentMap.containsKey(keys.get(lastIndex));
	}

	@Override
	public Object getValue(String path) {
		final List<String> keys = StringUtils.split(path, ',');
		final int lastIndex = keys.size() - 1;
		return getValue(keys, lastIndex);
	}

	protected Object getValue(List<String> keys, int lastIndex) {
		Map<String, Object> currentMap = map;
		for (String currentKey : keys.subList(0, lastIndex)) {
			Object currentValue = currentMap.get(currentKey);
			if (!(currentValue instanceof Config)) {//missing or incompatible intermediary level
				return null;
			}
			currentMap = ((Config)currentValue).asMap();
		}
		return currentMap.get(keys.get(lastIndex));
	}

	@Override
	public void setValue(String path, Object value) {
		final List<String> keys = StringUtils.split(path, ',');
		final int lastIndex = keys.size() - 1;
		setValue(keys, lastIndex, value);
	}

	protected void setValue(List<String> keys, int lastIndex, Object value) {
		Map<String, Object> currentMap = map;
		for (String currentKey : keys.subList(0, lastIndex)) {
			Object currentValue = currentMap.get(currentKey);
			Config config;
			if (currentValue == null) {//missing intermediary level
				config = new MapConfig();
				currentMap.put(currentKey, config);
			} else if (!(currentValue instanceof Config)) {//incompatible intermediary level
				throw new IllegalArgumentException("The specified path is already partially used, in such a" +
						" way that we cannot assign it a value.");
			} else {//existing intermediary level
				config = (Config)currentValue;
			}
			currentMap = config.asMap();
		}
		currentMap.put(keys.get(lastIndex), value);
	}


}
