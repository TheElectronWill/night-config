package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.TransformingSet;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * An abstract Config that uses a {@link java.util.Map} to store its values. In practice it's
 * often a HashMap, or a ConcurrentHashMap if the config is concurrent, but it accepts any type
 * of Map.
 *
 * @author TheElectronWill
 */
@SuppressWarnings("unchecked")
public abstract class AbstractConfig implements Config, Cloneable {

	protected final Supplier<Map<String, Object>> mapCreator;

	protected final Map<String, Object> map;

	/**
	 * Creates a new AbstractConfig backed by a new {@link Map}.
	 */
	public AbstractConfig(boolean concurrent) {
		this(getDefaultMapCreator(concurrent));
	}

	/**
	 * Creates a new AbstractConfig with all backing maps supplied by the given {@link Supplier}.
	 *
	 * @param mapCreator A supplier that will be called to create all config maps
	 */
	public AbstractConfig(Supplier<Map<String, Object>> mapCreator) {
		this.mapCreator = mapCreator;
		this.map = mapCreator.get();
	}

	/**
	 * Creates a new AbstractConfig backed by the specified {@link Map}.
	 *
	 * @param map the map to use to store the config values.
	 */
	public AbstractConfig(Map<String, Object> map) {
		this.map = map;
		this.mapCreator = getDefaultMapCreator(map instanceof ConcurrentMap);
	}

	/**
	 * Creates a new AbstractConfig that is a copy of the specified config.
	 *
	 * @param toCopy the config to copy
	 */
	public AbstractConfig(UnmodifiableConfig toCopy, boolean concurrent) {
		this(toCopy, getDefaultMapCreator(concurrent));
	}

	/**
	 * Creates a new AbstractConfig that is a copy of the specified config, and with
	 * all backing maps supplied by the given {@link Supplier}.
	 *
	 * @param toCopy     the config to copy
	 * @param mapCreator A supplier that will be called to create all config maps
	 */
	public AbstractConfig(UnmodifiableConfig toCopy, Supplier<Map<String, Object>> mapCreator) {
		this.map = mapCreator.get();
		this.map.putAll(toCopy.valueMap());
		this.mapCreator = mapCreator;
	}

	protected static <T> Supplier<Map<String, T>> getDefaultMapCreator(boolean concurrent) {
		return Config.getDefaultMapCreator(concurrent);
	}

	protected static <T> Supplier<Map<String, T>> getWildcardMapCreator(Supplier<Map<String, Object>> mapCreator) {
		return () -> {
			Map<String, Object> map = mapCreator.get();
			map.clear(); // Make sure there's no naughty people putting starting entries in the map, so we can unsafely cast
			return (Map<String, T>)map;
		};
	}

	@Override
	public <T> T getRaw(List<String> path) {
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
		Object nonNull = (value == null) ? NULL_OBJECT : value;
		return (T)parentMap.put(lastKey, nonNull);
	}

	@Override
	public boolean add(List<String> path, Object value) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> parentMap = getOrCreateMap(path.subList(0, lastIndex));
		String lastKey = path.get(lastIndex);
		Object nonNull = (value == null) ? NULL_OBJECT : value;
		return parentMap.putIfAbsent(lastKey, nonNull) == null;
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

	@Override
	public boolean isNull(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> parentMap = getMap(path.subList(0, lastIndex));
		if (parentMap == null) {
			return false;
		}
		String lastKey = path.get(lastIndex);
		Object value = parentMap.get(lastKey);
		return value == NULL_OBJECT;
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
		if (obj instanceof AbstractConfig) {
			return map.equals(((AbstractConfig)obj).map);
		} else if (obj instanceof UnmodifiableConfig) {
			UnmodifiableConfig conf = (UnmodifiableConfig)obj;
			if (conf.size() != size()) {
				return false;
			}
			for (UnmodifiableConfig.Entry entry : entrySet()) {
				Object value = entry.getValue();
				Object otherEntry = conf.get(Collections.singletonList(entry.getKey()));
				if (value == null) {
					if (otherEntry != null) {
						return false;
					}
				} else {
					return value.equals(otherEntry);
				}
			}
			return true;
		} else {
			return false;
		}
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

		@Override
		public <T> T getRawValue() {
			return (T)mapEntry.getValue();
		}

		@Override
		public <T> T setValue(Object value) {
			return (T)mapEntry.setValue(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof EntryWrapper) {
				EntryWrapper other = (EntryWrapper)obj;
				return Objects.equals(getKey(), other.getKey())
					&& Objects.equals(getValue(), other.getValue());
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