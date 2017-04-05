package com.electronwill.nightconfig.core.utils;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
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
 * Integer parse from the internal map to a String.
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
public final class TransformingMap<K, InternalV, ExternalV> extends AbstractMap<K, ExternalV> {
	private final Function<InternalV, ExternalV> readTransformation;
	private final Function<ExternalV, InternalV> writeTransformation;
	private final Function<Object, Object> searchTransformation;
	private final Map<K, InternalV> internalMap;

	/**
	 * Create a new TransformingMap.
	 *
	 * @param map                  the internal map to use
	 * @param readTransformation   the parse transformation (see javadoc of the class)
	 * @param writeTransformation  the write transformation (see javadoc of the class)
	 * @param searchTransformation the search transformation (see javadoc of the class)
	 */
	public TransformingMap(Map<K, InternalV> map, Function<InternalV, ExternalV> readTransformation,
						   Function<ExternalV, InternalV> writeTransformation,
						   Function<Object, Object> searchTransformation) {
		this.internalMap = map;
		this.readTransformation = readTransformation;
		this.writeTransformation = writeTransformation;
		this.searchTransformation = searchTransformation;
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
		return internalMap.containsValue(searchTransformation.apply(value));
	}

	@Override
	public ExternalV get(Object key) {
		return readTransformation.apply(internalMap.get(key));
	}

	@Override
	public ExternalV put(K key, ExternalV value) {
		return readTransformation.apply(internalMap.put(key, writeTransformation.apply(value)));
	}

	@Override
	public ExternalV remove(Object key) {
		return readTransformation.apply(internalMap.remove(key));
	}

	@Override
	public void putAll(Map<? extends K, ? extends ExternalV> m) {
		internalMap.putAll(new TransformingMap(m, writeTransformation, o -> o, o -> o));
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
	public Collection<ExternalV> values() {
		return new TransformingCollection<>(internalMap.values(), readTransformation,
											writeTransformation, searchTransformation);
	}

	@Override
	public Set<Map.Entry<K, ExternalV>> entrySet() {
		Function<Entry<K, InternalV>, Entry<K, ExternalV>> internalToExternal = internalEntry -> new TransformingMapEntry<>(
				internalEntry, readTransformation, writeTransformation);

		Function<Entry<K, ExternalV>, Entry<K, InternalV>> externalToInternal = externalEntry -> new TransformingMapEntry<>(
				externalEntry, writeTransformation, readTransformation);

		Function<Object, Object> searchTranformation = o -> {
			if (o instanceof Map.Entry) {
				Map.Entry<K, InternalV> entry = (Map.Entry)o;
				return new TransformingMapEntry<>(entry, readTransformation, writeTransformation);
			}
			return o;
		};
		return new TransformingSet<>(internalMap.entrySet(), internalToExternal, externalToInternal,
									 searchTranformation);
	}

	@Override
	public ExternalV getOrDefault(Object key, ExternalV defaultValue) {
		InternalV result = internalMap.get(key);
		return (result == defaultValue) ? defaultValue : readTransformation.apply(result);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super ExternalV> action) {
		internalMap.forEach((k, o) -> action.accept(k, readTransformation.apply(o)));
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super ExternalV, ? extends ExternalV> function) {
		internalMap.replaceAll(transform(function));
	}

	@Override
	public ExternalV putIfAbsent(K key, ExternalV value) {
		return readTransformation.apply(
				internalMap.putIfAbsent(key, writeTransformation.apply(value)));
	}

	@Override
	public boolean remove(Object key, Object value) {
		return internalMap.remove(key, searchTransformation.apply(value));
	}

	@Override
	public boolean replace(K key, ExternalV oldValue, ExternalV newValue) {
		return internalMap.replace(key, writeTransformation.apply(oldValue),
								   writeTransformation.apply(newValue));
	}

	@Override
	public ExternalV replace(K key, ExternalV value) {
		return readTransformation.apply(internalMap.replace(key, writeTransformation.apply(value)));
	}

	@Override
	public ExternalV computeIfAbsent(K key,
									 Function<? super K, ? extends ExternalV> mappingFunction) {
		Function<K, InternalV> function = k -> writeTransformation.apply(mappingFunction.apply(k));
		return readTransformation.apply(internalMap.computeIfAbsent(key, function));
	}

	@Override
	public ExternalV computeIfPresent(K key,
									  BiFunction<? super K, ? super ExternalV, ? extends ExternalV> remappingFunction) {
		return readTransformation.apply(
				internalMap.computeIfPresent(key, transform(remappingFunction)));
	}

	@Override
	public ExternalV compute(K key,
							 BiFunction<? super K, ? super ExternalV, ? extends ExternalV> remappingFunction) {
		return readTransformation.apply(internalMap.compute(key, transform(remappingFunction)));
	}

	@Override
	public ExternalV merge(K key, ExternalV value,
						   BiFunction<? super ExternalV, ? super ExternalV, ? extends ExternalV> remappingFunction) {
		return readTransformation.apply(internalMap.merge(key, writeTransformation.apply(value),
														  transform2(remappingFunction)));
	}

	private BiFunction<K, InternalV, InternalV> transform(
			BiFunction<? super K, ? super ExternalV, ? extends ExternalV> remappingFunction) {
		return (k, internalV) -> writeTransformation.apply(
				remappingFunction.apply(k, readTransformation.apply(internalV)));
	}

	private BiFunction<InternalV, InternalV, InternalV> transform2(
			BiFunction<? super ExternalV, ? super ExternalV, ? extends ExternalV> remappingFunction) {
		return (internalV1, internalV2) -> writeTransformation.apply(
				remappingFunction.apply(readTransformation.apply(internalV1),
										readTransformation.apply(internalV2)));
	}
}