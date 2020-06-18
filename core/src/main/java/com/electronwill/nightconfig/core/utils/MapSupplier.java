package com.electronwill.nightconfig.core.utils;

import java.util.Map;

@FunctionalInterface
public interface MapSupplier {
	<K, V> Map<K, V> get();

	default <K, V> Map<K, V> copy(Map<K, V> map) {
		Map<K, V> m = get();
		m.putAll(map);
		return m;
	}
}
