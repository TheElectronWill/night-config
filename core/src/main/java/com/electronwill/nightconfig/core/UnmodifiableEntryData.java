package com.electronwill.nightconfig.core;

import java.util.Map;

public interface UnmodifiableEntryData {
	<T> T getValue();

	boolean has(AttributeType<?> attribute);

	<T> T get(AttributeType<T> attribute);

	Iterable<? extends UnmodifiableConfig.AttributeEntry<?>> attributes();

	Config.Entry toConfigEntry(String key);

	<K, V> Map.Entry<K, V> toMapEntry(K key);
}
