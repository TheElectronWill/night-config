package com.electronwill.nightconfig.core.utils;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * A TransformingMap contains an internal {@code Map<K, InternalV>} values, and exposes the
 * features of a {@code Map<K, ExternalV>} applying transformations to the values.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingMap.
 * <p>
 * For instance, if you have a {@code Map<String, String>} and you want to convert its values
 * "just in time" to integers, you use a {@code TransformingMap<String, String, Integer>}.
 * To get one, you create these three functions:
 * <ul>
 * <li>one that converts a String to an Integer: that's the parse transformation. It converts an
 * Integer read from the internal map to a String.
 * <li>one that converts an Integer to a String: that's the write transformation. It converts a
 * String given to the TransformingMap to an Integer.
 * <li>one that converts an Object to another Object: that's the search transformation. It is used
 * (mainly) by the {@link #containsKey(Object)} method of the TransformingMap. If its argument is
 * an Integer then it should convert it to an String in the same way as the write transformation.
 * Otherwise, it is free to try to convert it to a String if possible, or not to.
 * </ul>
 *
 * @author TheElectronWill
 */
@SuppressWarnings("unchecked")
public final class TransformingMap<K, I, E> extends AbstractMap<K, E> {
	private final BiFunction<K, ? super I, ? extends E> readTransform;
	private final BiFunction<K, ? super E, ? extends I> writeTransform;
	private final Function<Object, ? extends I> searchTransform;
	private final Map<K, I> internalMap;

	/**
	 * Create a new TransformingMap.
	 *
	 * @param map                  the internal map to use
	 * @param readTransform   the parse transformation (see javadoc of the class)
	 * @param writeTransform  the write transformation (see javadoc of the class)
	 * @param searchTransform the search transformation (see javadoc of the class)
	 */
	public TransformingMap(Map<K, I> map,
						   Function<? super I, ? extends E> readTransform,
						   Function<? super E, ? extends I> writeTransform,
						   Function<Object, ? extends I> searchTransform) {
		this.internalMap = map;
		this.readTransform = (k, v) -> readTransform.apply(v);
		this.writeTransform = (k, v) -> writeTransform.apply(v);
		this.searchTransform = searchTransform;
	}

	/**
	 * Create a new TransformingMap.
	 *
	 * @param map                  the internal map to use
	 * @param readTransform   the parse transformation (see javadoc of the class)
	 * @param writeTransform  the write transformation (see javadoc of the class)
	 * @param searchTransform the search transformation (see javadoc of the class)
	 */
	public TransformingMap(Map<K, I> map,
						   BiFunction<K, ? super I, ? extends E> readTransform,
						   BiFunction<K, ? super E, ? extends I> writeTransform,
						   Function<Object, ? extends I> searchTransform) {
		this.internalMap = map;
		this.readTransform = readTransform;
		this.writeTransform = writeTransform;
		this.searchTransform = searchTransform;
	}

	private E read(Object key, I value) {
		return readTransform.apply((K)key, value);
	}

	private I write(Object key, E value) {
		return writeTransform.apply((K)key, value);
	}

	private I search(Object arg) {
		return searchTransform.apply(arg);
	}

	@Override
	public int size() {
		return internalMap.size();
	}

	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return internalMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return internalMap.containsValue(searchTransform.apply(value));
	}

	@Override
	public E get(Object key) {
		return read(key, internalMap.get(key));
	}

	@Override
	public E put(K key, E value) {
		return read(key, internalMap.put(key, write(key, value)));
	}

	@Override
	public E remove(Object key) {
		return read(key, internalMap.remove(key));
	}

	@Override
	public void putAll(Map<? extends K, ? extends E> m) {
		internalMap.putAll(new TransformingMap(m, writeTransform, (k, o) -> o, o -> o));
	}

	@Override
	public void clear() {
		internalMap.clear();
	}

	@Override
	public Set<K> keySet() {
		return internalMap.keySet();
	}

	@Override
	public Collection<E> values() {
		return new TransformingCollection<>(internalMap.values(), o->read(null,o),
											o->write(null,o), searchTransform);
	}

	@Override
	public Set<Map.Entry<K, E>> entrySet() {
		Function<Entry<K, I>, Entry<K, E>> read =
			i -> TransformingMapEntry.from(i, readTransform, writeTransform);

		Function<Entry<K, E>, Entry<K, I>> write =
			e -> TransformingMapEntry.from(e, writeTransform, readTransform);

		Function<Object, Map.Entry<K, I>> search = o -> {
			if (o instanceof Map.Entry) {
				Map.Entry<K, E> entry = (Map.Entry)o;
				return TransformingMapEntry.from(entry, writeTransform, readTransform);
			}
			return null;
		};
		return new TransformingSet<>(internalMap.entrySet(), read, write, search);
	}

	@Override
	public E getOrDefault(Object key, E defaultValue) {
		I result = internalMap.get(key);
		return (result == null || result == defaultValue) ? defaultValue : read(key, result);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super E> action) {
		internalMap.forEach((k, o) -> action.accept(k, read(k, o)));
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super E, ? extends E> function) {
		internalMap.replaceAll(transform(function));
	}

	@Override
	public E putIfAbsent(K key, E value) {
		return read(key, internalMap.putIfAbsent(key, write(key, value)));
	}

	@Override
	public boolean remove(Object key, Object value) {
		return internalMap.remove(key, search(value));
	}

	@Override
	public boolean replace(K key, E oldValue, E newValue) {
		return internalMap.replace(key, search(oldValue), write(key, newValue));
	}

	@Override
	public E replace(K key, E value) {
		return read(key, internalMap.replace(key, write(key, value)));
	}

	@Override
	public E computeIfAbsent(K key, Function<? super K, ? extends E> mappingFunction) {
		Function<K, I> function = k -> write(k, mappingFunction.apply(k));
		return read(key, internalMap.computeIfAbsent(key, function));
	}

	@Override
	public E computeIfPresent(K key,
							  BiFunction<? super K, ? super E, ? extends E> remappingFunction) {
		I computed = internalMap.computeIfPresent(key, transform(remappingFunction));
		return read(key, computed);
	}

	@Override
	public E compute(K key,
					 BiFunction<? super K, ? super E, ? extends E> remappingFunction) {
		I computed = internalMap.compute(key, transform(remappingFunction));
		return read(key, computed);
	}

	@Override
	public E merge(K key,
				   E value,
				   BiFunction<? super E, ? super E, ? extends E> remappingFunction) {
		I merged = internalMap.merge(key, write(key, value), transform2(key, remappingFunction));
		return read(key, merged);
	}

	private BiFunction<K, I, I> transform(
			BiFunction<? super K, ? super E, ? extends E> remappingFunction) {
		return (k, i) -> write(k, remappingFunction.apply(k, read(k, i)));
	}

	private BiFunction<I, I, I> transform2(K key,
										   BiFunction<? super E, ? super E, ? extends E> remappingFunction) {

		return (i1, i2) -> {
			E remapped = remappingFunction.apply(read(key, i1), read(key, i2));
			return write(key, remapped);
		};
	}
}