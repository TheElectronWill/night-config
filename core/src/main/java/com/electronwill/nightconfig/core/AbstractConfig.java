package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.MapSupplier;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import com.electronwill.nightconfig.core.utils.TransformingSet;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Base class for configurations. It uses a {@link java.util.Map} to store the config entries.
 *
 * @author TheElectronWill
 */
@SuppressWarnings("unchecked")
public abstract class AbstractConfig implements Config, Cloneable {
	protected static final int REQUIRE = 0, CREATE = 1, OPTIONAL = 2;

	protected final EntryData root; // stores the "global" attributes
	protected final Map<String, EntryData> storage;
	protected final MapSupplier mapSupplier;

	public AbstractConfig(MapSupplier mapSupplier) {
		this.mapSupplier = mapSupplier;
		this.storage = mapSupplier.get();
		this.root = new EntryDataImpl(this);
	}

	protected EntryData findEntry(String[] path, int mode) {
		return findEntry(path, path.length, mode);
	}

	protected EntryData findEntry(String[] path, int len, int mode) {
		if (path == null || len <= 0) return root;

		// Finds the map that contains the last entry of the path (the leaf)
		Map<String, EntryData> current = storage;
		for (int i = 0; i < len-1; i++) {
			final String part = path[i];
			EntryData entry = current.get(part);
			if (entry == null) {
				if (mode == CREATE) {
					// The entry doesn't exist, we can create it
					AbstractConfig sub = createSubConfig();
					entry = new EntryDataImpl(sub);
					current.put(part, entry);
					current = sub.storage;
				} else if (mode == OPTIONAL) {
					return null;
				} else {
					throw new WrongPathException(path, len, i, null);
				}
			} else {
				final Object v = entry.getValue();
				if (v instanceof Config) {
					current = ((Config)v).dataMap();
				} else if (mode == OPTIONAL) {
					return null;
				} else {
					// If the entry has a null or incompatible value, throw an error
					throw new WrongPathException(path, len, i, NullObject.or(v));
				}
			}
		}
		// Checks that the leaf is valid
		final int leafIdx = len-1;
		final String leafPart = path[leafIdx];

		EntryData leaf = current.get(leafPart);
		if (leaf == null) {
			if (mode == CREATE) {
				leaf = new EntryDataImpl();
				current.put(leafPart, leaf);
			} else if (mode == REQUIRE) {
				throw new WrongPathException(path, len, leafIdx, NullObject.instance());
			}
		}
		return leaf;
	}

	@Override
	public int size() {
		return storage.size();
	}

	@Override
	public void clear() {
		storage.clear();
	}

	@Override
	public void clearAttributes() {
		storage.forEach((key, data) -> data.clearExtraAttributes());
	}

	@Override
	public void clearComments() {
		storage.forEach((key, data) -> data.remove(StandardAttributes.COMMENT));
	}

	@Override
	public <T> T set(AttributeType<T> attribute, String[] path, T value) {
		return findEntry(path, CREATE).set(attribute, value);
	}

	@Override
	public <T> T add(AttributeType<T> attribute, String[] path, T value) {
		return findEntry(path, CREATE).add(attribute, value);
	}

	@Override
	public <T> T remove(AttributeType<T> attribute, String[] path) {
		EntryData data = findEntry(path, OPTIONAL);
		return data == null ? null : data.remove(attribute);
	}

	@Override
	public <T> T get(AttributeType<T> attribute, String[] path) {
		EntryData data = findEntry(path, OPTIONAL);
		return data == null ? null : data.get(attribute);
	}

	@Override
	public EntryData getData(String[] path) {
		return findEntry(path, OPTIONAL);
	}

	@Override
	public boolean contains(String[] path) {
		return findEntry(path, OPTIONAL) != null;
	}

	@Override
	public boolean has(AttributeType<?> attribute, String[] path) {
		EntryData entry = findEntry(path, OPTIONAL);
		return entry != null && entry.has(attribute);
	}

	@Override
	public Map<String, Object> valueMap() {
		return new TransformingMap<>(storage, EntryData::getValue, EntryDataImpl::new, EntryDataImpl::new);
	}

	@Override
	public Map<String, EntryData> dataMap() {
		return storage;
	}

	@Override
	public Set<Entry> entries() {
		Function<Map.Entry<String, EntryData>, Config.Entry> read =
			e -> e.getValue().toConfigEntry(e.getKey());

		Function<Config.Entry, Map.Entry<String, EntryData>> write =
			e -> new EntryDataImpl(e.getValue()).toMapEntry(e.getKey());

		Function<Object, Map.Entry<String, EntryData>> search = o -> {
			if (o instanceof Map.Entry) {
				Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
				Object key = entry.getKey();
				Object val = entry.getValue();
				if (val instanceof EntryData) {
					return (Map.Entry<String, EntryData>)entry;
				} else {
					return new EntryDataImpl(val).toMapEntry((String)key);
				}
			} else if (o instanceof Config.Entry) {
				return write.apply((Config.Entry)o);
			} else {
				return null;
			}
		};
		return new TransformingSet<>(storage.entrySet(), read, write, search);
	}

	public abstract AbstractConfig createSubConfig();

	public abstract AbstractConfig clone();

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AbstractConfig))
			return false;
		AbstractConfig that = (AbstractConfig)o;
		return storage.equals(that.storage);
	}

	@Override
	public int hashCode() {
		return storage.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + storage;
	}
}
