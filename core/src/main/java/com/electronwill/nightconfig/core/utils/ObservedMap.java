package com.electronwill.nightconfig.core.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author TheElectronWill
 */
public final class ObservedMap<K, V> extends AbstractObserved implements Map<K, V> {
	private final Map<K, V> map;

	public ObservedMap(Map<K, V> map, Runnable callback) {
		super(callback);
		this.map = map;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public V put(K key, V value) {
		V result = map.put(key, value);
		callback.run();
		return result;
	}

	@Override
	public V remove(Object key) {
		V result = map.remove(key);
		callback.run();
		return result;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
		callback.run();
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		map.replaceAll(function);
		callback.run();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		V result = map.putIfAbsent(key, value);
		if (result != value) { callback.run(); }
		return result;
	}

	@Override
	public boolean remove(Object key, Object value) {
		boolean removed = map.remove(key, value);
		if (removed) { callback.run(); }
		return removed;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		boolean replaced = map.replace(key, oldValue, newValue);
		if (replaced) { callback.run(); }
		return replaced;
	}

	@Override
	public V replace(K key, V value) {
		V result = map.replace(key, value);
		if (result != value) { callback.run(); }
		return result;
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		V result = map.computeIfAbsent(key, mappingFunction);
		if (result != null) { callback.run(); }
		return result;
	}

	@Override
	public V computeIfPresent(K key,
							  BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		V result = map.computeIfPresent(key, remappingFunction);
		callback.run();
		return result;
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		V result = map.compute(key, remappingFunction);
		callback.run();
		return result;
	}

	@Override
	public V merge(K key, V value,
				   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		V result = map.merge(key, value, remappingFunction);
		callback.run();
		return result;
	}

	@Override
	public void clear() {
		map.clear();
		callback.run();
	}

	@Override
	public Set<K> keySet() {
		return new ObservedSet<>(map.keySet(), callback);
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		Function<Entry<K, V>, ObservedEntry<K, V>> readT = e -> new ObservedEntry<>(e, callback);
		Function<ObservedEntry<K, V>, Entry<K, V>> writeT = oe -> oe.entry;
		Function<Object, Object> searchT = o -> {
			if (o instanceof ObservedEntry) {
				ObservedEntry<?, ?> observedEntry = (ObservedEntry<?, ?>) o;
				return observedEntry.entry;
			}
			return o;
		};
		TransformingSet<Entry<K, V>, ObservedEntry<K, V>> tSet = new TransformingSet<>(
				map.entrySet(), readT, writeT, searchT);
		return new ObservedSet2<>(callback, tSet, mapEntry -> new ObservedEntry<>(mapEntry, callback));
	}

	@Override
	public boolean equals(Object obj) {
		return map.equals(obj);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}
}