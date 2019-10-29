package com.electronwill.nightconfig.core;

import java.util.*;

import static com.electronwill.nightconfig.core.StandardAttributes.VALUE;

@SuppressWarnings("unchecked")
final class EntryDataImpl implements EntryData, Cloneable {
	private Object value;
	private Map<AttributeType<?>, Object> extra = null;

	public EntryDataImpl() {
		this(null);
	}

	public EntryDataImpl(Object value) {
		this.value = value;
	}

	private Map<AttributeType<?>, Object> extraAttributesMap() {
		if (extra == null) {
			extra = new HashMap<>();
		}
		return extra;
	}

	@Override
	public <T> T getValue() {
		return (T)value;
	}

	@Override
	public <T> T addValue(Object value) {
		if (this.value == null) {
			this.value = value;
		}
		return (T)this.value;
	}

	@Override
	public <T> T setValue(Object value) {
		T old = (T)this.value;
		this.value = value;
		return old;
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
		if (extra == null) {
			return null;
		}
		return (T)extra.remove(attribute);
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
	public <T> Optional<T> getOptional(AttributeType<T> attribute) {
		return Optional.ofNullable(get(attribute));
	}

	@Override
	public void clearExtraAttributes() {
		extra = null;
	}

	@Override
	public Iterable<Config.AttributeEntry<?>> attributes() {
		return AttributesIterator::new;
	}

	@Override
	public String toString() {
		if (extra == null) {
			return String.valueOf(value);
		} else {
			return String.format("%s {attributes: %s}", value, extra);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof EntryDataImpl)) return false;
		EntryDataImpl entryData = (EntryDataImpl)o;
		return Objects.equals(value, entryData.value) && Objects.equals(extra, entryData.extra);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, extra);
	}

	@Override
	public Config.Entry toConfigEntry(String key) {
		return new Config.Entry() {
			@Override
			public <T> T set(AttributeType<T> attribute, T value) {
				return EntryDataImpl.this.set(attribute, value);
			}

			@Override
			public String getKey() {
				return key;
			}

			@Override
			public <T> T get(AttributeType<T> attribute) {
				return EntryDataImpl.this.get(attribute);
			}

			@Override
			public <T> Optional<T> getOptional(AttributeType<T> attribute) {
				return EntryDataImpl.this.getOptional(attribute);
			}

			@Override
			public Iterable<? extends Config.AttributeEntry<?>> attributes() {
				return EntryDataImpl.this.attributes();
			}
		};
	}

	@Override
	public <K, V> Map.Entry<K, V> toMapEntry(K key) {
		return new Map.Entry<K, V>() {
			@Override
			public K getKey() {
				return key;
			}

			@Override
			public V getValue() {
				return EntryDataImpl.this.getValue();
			}

			@Override
			public V setValue(V value) {
				return EntryDataImpl.this.setValue(value);
			}
		};
	}

	private class AttributesIterator implements Iterator<Config.AttributeEntry<?>> {
		private boolean passedValue = false;
		private Iterator<Map.Entry<AttributeType<?>, Object>> extraIterator =
			extra == null ? null : extra.entrySet().iterator();

		@Override
		public boolean hasNext() {
			return !passedValue || (extraIterator != null && extraIterator.hasNext());
		}

		@Override
		public Config.AttributeEntry<?> next() {
			if (passedValue) {
				if (extraIterator == null) throw new NoSuchElementException();
				final Map.Entry<AttributeType<?>, Object> entry = extraIterator.next();
				return new Config.AttributeEntry<Object>() {
					@Override
					public void set(Object value) {
						entry.setValue(value);
					}

					@Override
					public AttributeType<Object> attribute() {
						return (AttributeType<Object>)entry.getKey();
					}

					@Override
					public Object get() {
						return entry.getValue();
					}
				};
			} else {
				passedValue = true;
				return new Config.AttributeEntry<Object>() {
					@Override
					public void set(Object value) {
						EntryDataImpl.this.setValue(value);
					}

					@Override
					public AttributeType<Object> attribute() {
						return VALUE;
					}

					@Override
					public Object get() {
						return EntryDataImpl.this.getValue();
					}
				};
			}
		}
	}
}
