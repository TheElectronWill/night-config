package com.electronwill.nightconfig.core.utils;

import java.util.Map;

/**
 * @author TheElectronWill
 */
public final class ObservedEntry<K, V> extends AbstractObserved implements Map.Entry<K, V> {
	final Map.Entry<K, V> entry;

	public ObservedEntry(Map.Entry<K, V> entry, Runnable callback) {
		super(callback);
		this.entry = entry;
	}

	@Override
	public K getKey() {
		return entry.getKey();
	}

	@Override
	public V getValue() {
		return entry.getValue();
	}

	@Override
	public V setValue(V value) {
		V result = entry.setValue(value);
		callback.run();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return entry.equals(obj);
	}

	@Override
	public int hashCode() {
		return entry.hashCode();
	}
}