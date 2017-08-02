package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.TransformingSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract Config that uses a {@link java.util.Map} to store its values. In practise it's
 * often a HashMap, or a ConcurrentHashMap if the config is concurrent, but it accepts any type
 * of Map.
 *
 * @author TheElectronWill
 */
public abstract class AbstractConfig implements Config, Cloneable {
	final Map<String, Object> map;

	/**
	 * Creates a new AbstractConfig backed by a new {@link Map}.
	 */
	public AbstractConfig(boolean concurrent) {
		this.map = concurrent ? new ConcurrentHashMap<>() : new HashMap<>();
	}

	/**
	 * Creates a new AbstractConfig backed by the specified {@link Map}.
	 *
	 * @param map the map to use to store the config values.
	 */
	public AbstractConfig(Map<String, Object> map) {
		this.map = map;
	}

	/**
	 * Creates a new AbstractConfig that is a copy of the specified config.
	 *
	 * @param toCopy the config to copy
	 */
	public AbstractConfig(UnmodifiableConfig toCopy, boolean concurrent) {
		Map<String, Object> valueMap = toCopy.valueMap();
		this.map = concurrent ? new ConcurrentHashMap<>(valueMap) : new HashMap<>(valueMap);
	}

	@Override
	public <T> T get(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> parentMap = getMap(path.subList(0, lastIndex));
		if (parentMap == null) {
			return null;
		}
		String lastKey = path.get(lastIndex);
		return (T)parentMap.get(lastKey);
	}

	@Override
	public <T> T set(List<String> path, Object value) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> parentMap = getOrCreateMap(path.subList(0, lastIndex));
		String lastKey = path.get(lastIndex);
		return (T)parentMap.put(lastKey, value);
	}

	@Override
	public void add(List<String> path, Object value) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> parentMap = getOrCreateMap(path.subList(0, lastIndex));
		String lastKey = path.get(lastIndex);
		parentMap.putIfAbsent(lastKey, value);
	}

	@Override
	public <T> T remove(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> parentMap = getMap(path.subList(0, lastIndex));
		if (parentMap == null) {
			return null;
		}
		String lastKey = path.get(lastIndex);
		return (T)parentMap.remove(lastKey);
	}

	@Override
	public boolean contains(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> parentMap = getMap(path.subList(0, lastIndex));
		if (parentMap == null) {
			return false;
		}
		String lastKey = path.get(lastIndex);
		return parentMap.containsKey(lastKey);
	}

	/**
	 * Returns the Map associated to the given path. Any missing level is created.
	 *
	 * @param path the map's path
	 * @return the Map, not null
	 */
	private Map<String, Object> getOrCreateMap(List<String> path) {
		Map<String, Object> currentMap = map;
		for (String currentKey : path) {
			final Object currentValue = currentMap.get(currentKey);
			final Config config;
			if (currentValue == null) {// missing intermediary level
				config = createSubConfig();
				currentMap.put(currentKey, config);
			} else if (!(currentValue instanceof Config)) {// incompatible intermediary level
				throw new IllegalArgumentException(
						"Cannot add an element to an intermediary value of type: "
						+ currentValue.getClass());
			} else {//existing intermediary level
				config = (Config)currentValue;
			}
			currentMap = config.valueMap();
		}
		return currentMap;
	}

	/**
	 * Returns the Map associated to the given path, or null if there is none.
	 *
	 * @param path the map's path
	 * @return the Map if any, or null if none
	 */
	private Map<String, Object> getMap(List<String> path) {
		Map<String, Object> currentMap = map;
		for (String key : path) {
			Object value = currentMap.get(key);
			if (!(value instanceof Config)) {// missing or incompatible intermediary level
				return null;// the specified path doesn't exist -> stop here
			}
			currentMap = ((Config)value).valueMap();
		}
		return currentMap;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Map<String, Object> valueMap() {
		return map;
	}

	@Override
	public Set<? extends Entry> entrySet() {
		return new TransformingSet<>(map.entrySet(), EntryWrapper::new, o -> null, o -> o);
		/* the writeTransformation is not important because we can't write to the set anyway,
		   since it's a generic Set<? extends Entry> */
	}

	/**
	 * Creates and return a copy of this config.
	 *
	 * @return a new Config that contains the same entries as this config.
	 */
	@Override
	public abstract AbstractConfig clone();

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) { return true; }
		if (!(obj instanceof AbstractConfig)) { return false; }
		AbstractConfig other = (AbstractConfig)obj;
		return map.equals(other.map);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ':' + valueMap();
	}

	/**
	 * A wrapper around a {@code Map.Entry<String, Object>}.
	 *
	 * @see Map.Entry
	 */
	protected static class EntryWrapper implements Entry {
		protected final Map.Entry<String, Object> mapEntry;

		public EntryWrapper(Map.Entry<String, Object> mapEntry) {
			this.mapEntry = mapEntry;
		}

		@Override
		public String getKey() {
			return mapEntry.getKey();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue() {
			return (T)mapEntry.getValue();
		}

		@Override
		public Object setValue(Object value) {
			return mapEntry.setValue(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof EntryWrapper) {
				EntryWrapper other = (EntryWrapper)obj;
				return Objects.equals(getKey(), other.getKey()) && Objects.equals(getValue(),
																				  other.getValue());
			}
			return false;
		}

		@Override
		public int hashCode() {
			int result = 1;
			result = 31 * result + Objects.hashCode(getKey());
			result = 31 * result + Objects.hashCode(getValue());
			return result;
		}
	}
}