package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.MapSupplier;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import com.electronwill.nightconfig.core.utils.TransformingSet;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.electronwill.nightconfig.core.StandardAttributes.VALUE;
import static com.electronwill.nightconfig.core.utils.StringUtils.single;

/**
 * Base class for configurations. It uses a {@link java.util.Map} to store the config entries.
 *
 * @author TheElectronWill
 */
@SuppressWarnings("unchecked")
public abstract class AbstractConfig implements Config, Cloneable {
	protected static final int REQUIRE = 0, CREATE = 1, OPTIONAL = 2;

	protected final Entry root; // stores the "global" attributes
	protected final Map<String, Entry> storage;
	protected final MapSupplier mapSupplier;

	public AbstractConfig(MapSupplier mapSupplier) {
		this.mapSupplier = mapSupplier;
		this.storage = mapSupplier.get();
		this.root = new Entry(null, this);
	}

	protected Entry findEntry(String[] path, int mode) {
		return findEntry(path, path.length, mode);
	}

	protected Entry findEntry(String[] path, int len, int mode) {
		if (path == null || len <= 0) return root;

		// Finds the map that contains the last entry of the path (the leaf)
		Map<String, Entry> current = storage;
		final int leafIdx = len-1;
		for (int i = 0; i < leafIdx; i++) {
			final String key = path[i];
			Entry entry = current.get(key);
			if (entry == null) {
				if (mode == CREATE) {
					// The entry doesn't exist, we can create it
					AbstractConfig sub = createSubConfig();
					entry = new Entry(key, sub);
					current.put(key, entry);
					current = sub.storage;
				} else if (mode == OPTIONAL) {
					return null;
				} else {
					throw new WrongPathException(path, len, i, null);
				}
			} else {
				final Object v = entry.getValue();
				if (v instanceof AbstractConfig) {
					current = ((AbstractConfig)v).storage;
				} else if (mode == OPTIONAL) {
					return null;
				} else {
					// If the entry has a null or incompatible value, throw an error
					throw new WrongPathException(path, len, i, NullObject.or(v));
				}
			}
		}
		// Checks that the leaf is valid
		String leafKey = path[leafIdx];
		Entry leaf = current.get(leafKey);
		if (leaf == null) {
			if (mode == CREATE) {
				leaf = new Entry(leafKey, null);
				current.put(leafKey, leaf);
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
	public void clearExtraAttributes() {
		storage.forEach((key, data) -> data.clearExtraAttributes());
	}

	@Override
	public void clearComments() {
		storage.forEach((key, data) -> data.remove(StandardAttributes.COMMENT));
	}

	@Override
	public Config.Entry getEntry(String[] path) {
		return findEntry(path, OPTIONAL);
	}

	// --- VALUES ---

	@Override
	public <T> T set(String[] path, Object value) {
		return findEntry(path, CREATE).setValue(value);
	}

	@Override
	public Object add(String[] path, Object value) {
		return findEntry(path, CREATE).setValue(value);
	}

	@Override
	public <T> T get(String[] path) {
		Entry entry = findEntry(path, OPTIONAL);
		return entry == null ? null : entry.getValue();
	}

	// --- OTHER ATTRIBUTES ---

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
		Entry entry = findEntry(path, OPTIONAL);
		return entry == null ? null : entry.remove(attribute);
	}

	@Override
	public <T> T get(AttributeType<T> attribute, String[] path) {
		Entry entry = findEntry(path, OPTIONAL);
		return entry == null ? null : entry.get(attribute);
	}

	// --- VIEWS ---

	@Override
	public Map<String, Object> valueMap() {
		BiFunction<String, Entry, Object> read = (k, e) -> e.getValue();
		BiFunction<String, Object, Entry> write = Entry::new;
		Function<Object, Entry> search = o -> o instanceof Entry ? (Entry)o : null;
		return new TransformingMap<>(storage, read, write, search);
	}

	@Override
	public Set<Config.Entry> entries() {
		Function<Map.Entry<String, Entry>, Config.Entry> read = Map.Entry::getValue;

		Function<Config.Entry, Map.Entry<String, Entry>> write =
			e -> new Entry(e.getKey(), e.getValue()).toMapEntry();

		Function<Object, Map.Entry<String, Entry>> search = o -> {
			if (o instanceof Map.Entry) {
				Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
				Object key = entry.getKey();
				Object val = entry.getValue();
				if (val instanceof Entry) {
					return (Map.Entry<String, Entry>)entry;
				} else {
					return new Entry(String.valueOf(key), val).toMapEntry();
				}
			} else if (o instanceof Entry) {
				return ((Entry)o).toMapEntry();
			} else {
				return null;
			}
		};
		Set<Map.Entry<String, Entry>> storageEntries = storage.entrySet();
		return new TransformingSet<>(storageEntries, read, write, search);
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

	@SuppressWarnings("unchecked")
	protected static final class Entry implements Config.Entry, Cloneable {
		private final String key;
		private Object value;
		private Map<AttributeType<?>, Object> extra = null;

		public Entry(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		private Map<AttributeType<?>, Object> extraAttributesMap() {
			if (extra == null) {
				extra = new HashMap<>();
			}
			return extra;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public <T> T getValue() {
			return (T)value;
		}

		@Override
		public <T> T addValue(Object value) {
			T old = (T)this.value;
			if (old == null) {
				this.value = value;
			}
			return old;
		}

		@Override
		public <T> T setValue(Object value) {
			T old = (T)this.value;
			this.value = value;
			return old;
		}

		@Override
		public <T> T removeValue() {
			return setValue(null);
		}

		@Override
		public <T> T set(AttributeType<T> attribute, T value) {
			if (attribute == VALUE) {
				return setValue(value);
			}
			return (T)extraAttributesMap().put(attribute, value);
		}

		@Override
		public <T> T add(AttributeType<T> attribute, T value) {
			if (attribute == VALUE) {
				return addValue(value);
			}
			return (T)extraAttributesMap().putIfAbsent(attribute, value);
		}

		@Override
		public <T> T remove(AttributeType<T> attribute) {
			if (attribute == VALUE) {
				return removeValue();
			} else if (extra == null) {
				return null;
			} else {
				return (T)extra.remove(attribute);
			}
		}

		@Override
		public boolean has(AttributeType<?> attribute) {
			return attribute == VALUE || (extra != null && extra.containsKey(attribute));
		}

		@Override
		public <T> T get(AttributeType<T> attribute) {
			if (attribute == VALUE) {
				return (T)value;
			}
			return extra == null ? null : (T)extra.get(attribute);
		}

		@Override
		public void clearExtraAttributes() {
			extra = null;
		}

		@Override
		public Iterable<Config.Attribute<?>> attributes() {
			return AttributesIterator::new;
		}

		@Override
		public String toString() {
			if (extra == null) {
				return String.valueOf(value);
			}
			return String.format("%s {attributes: %s}", value, extra);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Entry)) return false;
			Entry entry = (Entry)o;
			return Objects.equals(key, entry.key) &&
				   Objects.equals(value, entry.value) &&
				   Objects.equals(extra, entry.extra);
		}

		@Override
		public int hashCode() {
			return Objects.hash(key, value, extra);
		}

		@Override
		public <T> Map.Entry<String, T> toMapEntry() {
			return new Map.Entry<String, T>() {
				@Override
				public String getKey() {
					return Entry.this.getKey();
				}

				@Override
				public T getValue() {
					return Entry.this.getValue();
				}

				@Override
				public T setValue(T value) {
					return Entry.this.setValue(value);
				}
			};
		}

		private final class AttributesIterator implements Iterator<Attribute<?>> {
			private boolean passedValue = false;
			private final Iterator<Map.Entry<AttributeType<?>, Object>> extraIterator =
				extra == null ? null : extra.entrySet().iterator();

			@Override
			public boolean hasNext() {
				return !passedValue || (extraIterator != null && extraIterator.hasNext());
			}

			@Override
			public Config.Attribute<?> next() {
				if (passedValue) {
					if (extraIterator == null) throw new NoSuchElementException();
					final Map.Entry<AttributeType<?>, Object> entry = extraIterator.next();
					return new Config.Attribute<Object>() {
						@Override
						public Object setValue(Object value) {
							return entry.setValue(value);
						}

						@Override
						public AttributeType<Object> getType() {
							return (AttributeType<Object>)entry.getKey();
						}

						@Override
						public Object getValue() {
							return entry.getValue();
						}
					};
				} else {
					passedValue = true;
					return new Config.Attribute<Object>() {
						@Override
						public Object setValue(Object value) {
							return Entry.this.setValue(value);
						}

						@Override
						public AttributeType<Object> getType() {
							return VALUE;
						}

						@Override
						public Object getValue() {
							return Entry.this.getValue();
						}
					};
				}
			}
		}
	}

	protected final class EntrySet implements Set<Entry> {
		@Override
		public int size() {
			return storage.size();
		}

		@Override
		public boolean isEmpty() {
			return storage.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			if (o instanceof Entry) {
				Entry query = (Entry)o;
				Entry data = storage.get(query.getKey());
				return data != null && Objects.equals(data.getValue(), query.getValue());
			}
			return false;
		}

		@Override
		public Iterator<Entry> iterator() {
			return storage.values().iterator();
		}

		@Override
		public Object[] toArray() {
			return storage.values().toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return storage.values().toArray(a);
		}

		@Override
		public boolean add(Entry entry) {
			return AbstractConfig.this.add(single(entry.getKey()), entry.getValue()) == null;
		}

		@Override
		public boolean remove(Object o) {
			if (o instanceof Entry) {
				Entry query = (Entry)o;
				return AbstractConfig.this.remove(single(query.getKey())) != null;
			}
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return storage.values().containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends Entry> c) {
			return storage.values().addAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return storage.values().retainAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return storage.values().removeAll(c);
		}

		@Override
		public void clear() {
			AbstractConfig.this.clear();
		}
	}
}
