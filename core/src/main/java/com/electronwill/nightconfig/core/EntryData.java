package com.electronwill.nightconfig.core;

import java.util.Map;
import java.util.Optional;

public interface EntryData extends UnmodifiableEntryData {
	<T> T addValue(Object value);

	<T> T setValue(Object value);

	<T> T set(AttributeType<T> attribute, T value);

	<T> T add(AttributeType<T> attribute, T value);

	<T> T remove(AttributeType<T> attribute);

	<T> Optional<T> getOptional(AttributeType<T> attribute);

	void clearExtraAttributes();

	Iterable<? extends Config.AttributeEntry<?>> attributes();

	Config.Entry toConfigEntry(String key);

	<K, V> Map.Entry<K, V> toMapEntry(K key);
}
