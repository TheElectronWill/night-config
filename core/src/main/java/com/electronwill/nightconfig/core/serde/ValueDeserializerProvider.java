package com.electronwill.nightconfig.core.serde;

@FunctionalInterface
public interface ValueDeserializerProvider<T, R> {
	ValueDeserializer<T, R> provide(Class<?> valueClass, Class<?> resultClass);
}
