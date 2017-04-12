package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.TransformingSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract configuration that uses a {@link java.util.Map} to store its values.
 *
 * @author TheElectronWill
 */
public abstract class AbstractConfig implements Config {
	final Map<String, Object> map;

	/**
	 * Creates a new AbstractConfig backed by a new {@link Map}.
	 */
	public AbstractConfig() {
		this(new HashMap<>());
	}

	/**
	 * Creates a new AbstractConfig backed by the specified {@link Map}.
	 *
	 * @param map the map to use to store the config values.
	 */
	public AbstractConfig(Map<String, Object> map) {
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
			currentMap = ((Config)value).valueMap();
		}
		String lastKey = path.get(lastIndex);
		return (T)currentMap.get(lastKey);
	}

	@Override
	public Object setValue(List<String> path, Object value) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> currentMap = map;
		for (String currentKey : path.subList(0, lastIndex)) {
			final Object currentValue = currentMap.get(currentKey);
			final Config config;
			if (currentValue == null) {//missing intermediary level
				config = createSubConfig();
				currentMap.put(currentKey, config);
			} else if (!(currentValue instanceof Config)) {//incompatible intermediary level
				throw new IllegalArgumentException(
						"Cannot add an element to an intermediary value of type: "
						+ currentValue.getClass());
			} else {//existing intermediary level
				config = (Config)currentValue;
			}
			currentMap = config.valueMap();
		}
		String lastKey = path.get(lastIndex);
		return currentMap.put(lastKey, value);
	}

	protected abstract AbstractConfig createSubConfig();

	@Override
	public void removeValue(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> currentMap = map;
		for (String key : path.subList(0, lastIndex)) {
			Object value = currentMap.get(key);
			if (!(value instanceof Config)) {//missing or incompatible intermediary level
				return;//the specified path doesn't exist -> stop here
			}
			currentMap = ((Config)value).valueMap();
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
			currentMap = ((Config)value).valueMap();
		}
		String lastKey = path.get(lastIndex);
		return currentMap.containsKey(lastKey);
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
		return valueMap().toString();
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
			if (obj instanceof Entry) {
				Entry other = (Entry)obj;
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