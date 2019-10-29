package com.electronwill.nightconfig.core.utils;

import java.util.Map;

@FunctionalInterface
public interface MapSupplier {
	<K, V> Map<K, V> get();
}
