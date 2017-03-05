package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.TransparentWrapper;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A checker wrapped around a configuration. It checks that all the values put into the config are
 * supported (as per the {@link Config#supportsType(Class)} method. Trying to insert an unsupported value
 * throws an IllegalArgumentException.
 *
 * @author TheElectronWill
 */
public final class CheckedConfig implements Config {
	private final Config config;

	/**
	 * Creates a new CheckedConfig around a given configuration.
	 *
	 * @param config the configuration to wrap
	 */
	public CheckedConfig(Config config) {
		this.config = Objects.requireNonNull(config, "The config to wrap must not be null!");
		config.asMap().forEach((k, v) -> checkValue(v));
		//The config might already contain some elements and we must be sure that they are all supported
	}

	@Override
	public <T> T getValue(List<String> path) {
		return config.getValue(path);
	}

	@Override
	public void setValue(List<String> path, Object value) {
		config.setValue(path, checkedValue(value));
	}

	@Override
	public void removeValue(List<String> path) {
		config.removeValue(path);
	}

	@Override
	public boolean containsValue(List<String> path) {
		return config.containsValue(path);
	}

	@Override
	public int size() {
		return config.size();
	}

	@Override
	public Map<String, Object> asMap() {
		return new CheckedMap(config.asMap());
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return config.supportsType(type);
	}

	@Override
	public boolean equals(Object obj) {
		return config.equals(obj);
	}

	@Override
	public int hashCode() {
		return config.hashCode();
	}

	@Override
	public String toString() {
		return "CheckedConfig of " + super.toString();
	}

	/**
	 * Checks that a value is supported by the config. Throws an unchecked exception if the value isn't
	 * supported.
	 *
	 * @param value the value to check
	 * @throws IllegalArgumentException if the value isn't supported
	 */
	private void checkValue(Object value) {
		if (value != null && !supportsType(value.getClass())) {
			throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getTypeName());
		} else if (value == null && !supportsType(null)) {
			throw new IllegalArgumentException("Null values aren't supported by this configuration.");
		}
		if (value instanceof Config) {
			((Config)value).asMap().forEach((k, v) -> checkValue(v));
		}
	}

	/**
	 * Checks that a value is supported by the config, and returns it if it's supported. Throws an
	 * unchecked exception if the value isn't supported.
	 *
	 * @param value the value to check
	 * @param <T>   the value's type
	 * @return the value, if it's supported
	 * @throws IllegalArgumentException if the value isn't supported
	 */
	private <T> T checkedValue(T value) {
		checkValue(value);
		return value;
	}

	/**
	 * A checked, transparent wrapper around a {@code Map<String, Object>}.
	 */
	private final class CheckedMap extends TransparentWrapper<Map<String, Object>>
		implements Map<String, Object> {

		private CheckedMap(Map<String, Object> map) {super(map);}

		@Override
		public int size() {
			return wrapped.size();
		}

		@Override
		public boolean isEmpty() {
			return wrapped.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return wrapped.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return wrapped.containsValue(value);
		}

		@Override
		public Object get(Object key) {
			return wrapped.get(key);
		}

		@Override
		public Object put(String key, Object value) {
			return wrapped.put(key, checkedValue(value));
		}

		@Override
		public Object remove(Object key) {
			return wrapped.remove(key);
		}

		@Override
		public void putAll(Map<? extends String, ?> m) {
			m.forEach(this::put);
		}

		@Override
		public void clear() {
			wrapped.clear();
		}

		@Override
		public Set<String> keySet() {
			return wrapped.keySet();
		}

		@Override
		public Collection<Object> values() {
			return wrapped.values();
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			return new CheckedEntrySet(wrapped.entrySet());
		}

		@Override
		public Object getOrDefault(Object key, Object defaultValue) {
			return wrapped.getOrDefault(key, checkedValue(defaultValue));
			/* Any value returned by a get operation should be supported by the Config, so we check the
			default value, even if it's not in the Map.
			 */
		}

		@Override
		public Object putIfAbsent(String key, Object value) {
			return wrapped.putIfAbsent(key, checkedValue(value));
		}

		@Override
		public boolean remove(Object key, Object value) {
			return wrapped.remove(key, value);
		}

		@Override
		public boolean replace(String key, Object oldValue, Object newValue) {
			return wrapped.replace(key, oldValue, checkedValue(newValue));
		}

		@Override
		public Object replace(String key, Object value) {
			return wrapped.replace(key, checkedValue(value));
		}

		@Override
		public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
			wrapped.replaceAll(new CheckedRemappingFunction(function));
		}

		@Override
		public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
			return wrapped.compute(key, new CheckedRemappingFunction(remappingFunction));
		}

		@Override
		public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
			return wrapped.computeIfPresent(key, new CheckedRemappingFunction(remappingFunction));
		}

		@Override
		public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
			Object currentValue = wrapped.computeIfAbsent(key, mappingFunction);
			return (currentValue == null) ? null : checkedValue(currentValue);
		}

		@Override
		public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
			Object newValue = wrapped.merge(key, value, remappingFunction);
			return (newValue == null) ? null : checkedValue(newValue);
		}

		@Override
		public void forEach(BiConsumer<? super String, ? super Object> action) {
			wrapped.forEach(action);
		}
	}

	/**
	 * A checked, transparent wrapper around a remapping function <tt>(String, Object) -> Object</tt>.
	 */
	private final class CheckedRemappingFunction extends TransparentWrapper<BiFunction<? super String, ? super Object, ?>>
		implements BiFunction<String, Object, Object> {

		CheckedRemappingFunction(BiFunction<? super String, ? super Object, ?> wrapped) {super(wrapped);}

		@Override
		public Object apply(String s, Object o) {
			Object result = wrapped.apply(s, o);
			return (result == null) ? null : checkedValue(result);
		}
	}

	/**
	 * A checked, transparent wrapper around a map entry.
	 */
	private final class CheckedEntry extends TransparentWrapper<Entry<String, Object>>
		implements Entry<String, Object> {

		private CheckedEntry(Entry<String, Object> entry) {super(entry);}

		@Override
		public String getKey() {
			return wrapped.getKey();
		}

		@Override
		public Object getValue() {
			return wrapped.getValue();
		}

		@Override
		public Object setValue(Object value) {
			return wrapped.setValue(checkedValue(value));
		}
	}

	/**
	 * A checked, transparent wrapper around a Set of map entries.
	 */
	private final class CheckedEntrySet extends TransparentWrapper<Set<Entry<String, Object>>>
		implements Set<Entry<String, Object>> {

		private CheckedEntrySet(Set<Entry<String, Object>> set) {super(set);}

		@Override
		public int size() {
			return wrapped.size();
		}

		@Override
		public boolean isEmpty() {
			return wrapped.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return wrapped.contains(o);
		}

		@Override
		public Iterator<Entry<String, Object>> iterator() {
			return new CheckedEntryIterator(wrapped.iterator());
		}

		@Override
		public Object[] toArray() {
			return wrapped.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return wrapped.toArray(a);
		}

		@Override
		public boolean add(Entry<String, Object> entry) {
			checkValue(entry.getValue());
			return wrapped.add(entry);
		}

		@Override
		public boolean remove(Object o) {
			return wrapped.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return wrapped.contains(c);
		}

		@Override
		public boolean addAll(Collection<? extends Entry<String, Object>> c) {
			boolean changed = false;
			for (Entry<String, Object> entry : c) {
				changed |= add(entry);//the check is performed in the add method
			}
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return wrapped.retainAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return wrapped.removeAll(c);
		}

		@Override
		public void clear() {
			wrapped.clear();
		}

		@Override
		public Spliterator<Entry<String, Object>> spliterator() {
			return new CheckedEntrySpliterator(wrapped.spliterator());
		}

		@Override
		public void forEach(Consumer<? super Entry<String, Object>> action) {
			wrapped.forEach(new CheckedEntryAction(action));
		}
	}

	/**
	 * A checked, transparent wrapper around an Iterator of map entries.
	 */
	private final class CheckedEntryIterator extends TransparentWrapper<Iterator<Entry<String, Object>>>
		implements Iterator<Entry<String, Object>> {

		private CheckedEntryIterator(Iterator<Entry<String, Object>> iterator) {super(iterator);}

		@Override
		public boolean hasNext() {
			return wrapped.hasNext();
		}

		@Override
		public Entry<String, Object> next() {
			return new CheckedEntry(wrapped.next());
			//returns a CheckedEntry in order to check the use of the Entry.setValue method
		}

		@Override
		public void remove() {
			wrapped.remove();
		}

		@Override
		public void forEachRemaining(Consumer<? super Entry<String, Object>> action) {
			wrapped.forEachRemaining(new CheckedEntryAction(action));
		}
	}

	/**
	 * A checked, transparent wrapper around a Spliterator of map entries.
	 */
	private final class CheckedEntrySpliterator extends TransparentWrapper<Spliterator<Entry<String, Object>>>
		implements Spliterator<Entry<String, Object>> {

		private CheckedEntrySpliterator(Spliterator<Entry<String, Object>> spliterator) {super(spliterator);}

		@Override
		public boolean tryAdvance(Consumer<? super Entry<String, Object>> action) {
			return wrapped.tryAdvance(new CheckedEntryAction(action));
		}

		@Override
		public void forEachRemaining(Consumer<? super Entry<String, Object>> action) {
			wrapped.forEachRemaining(new CheckedEntryAction(action));
		}

		@Override
		public Spliterator<Entry<String, Object>> trySplit() {
			return new CheckedEntrySpliterator(wrapped.trySplit());
		}

		@Override
		public long estimateSize() {
			return wrapped.estimateSize();
		}

		@Override
		public long getExactSizeIfKnown() {
			return wrapped.getExactSizeIfKnown();
		}

		@Override
		public int characteristics() {
			return wrapped.characteristics();
		}

		@Override
		public boolean hasCharacteristics(int characteristics) {
			return wrapped.hasCharacteristics(characteristics);
		}

		@Override
		public Comparator<? super Entry<String, Object>> getComparator() {
			return wrapped.getComparator();
		}
	}

	/**
	 * A checked action on a Map entry.
	 */
	private final class CheckedEntryAction extends TransparentWrapper<Consumer<? super Entry<String, Object>>>
		implements Consumer<Entry<String, Object>> {

		private CheckedEntryAction(Consumer<? super Entry<String, Object>> consumer) {super(consumer);}

		@Override
		public void accept(Entry<String, Object> entry) {
			wrapped.accept(new CheckedEntry(entry));
		}
	}

}
