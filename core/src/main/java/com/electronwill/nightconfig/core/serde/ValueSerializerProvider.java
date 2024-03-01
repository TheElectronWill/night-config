package com.electronwill.nightconfig.core.serde;

/**
 * Provides {@link ValueSerializer} to serialize configuration values.
 * <p>
 * The {@link #provide(Class)} method returns {@code null} when
 * it cannot provide a serializer for the given value type.
 * In that case, other providers will be called, until a suitable serializer is found
 * or all the providers have been tried.
 * 
 * @param <T> type of the config values to serialize
 * @param <R> resulting type of the deserialization of these values
 */
@FunctionalInterface
public interface ValueSerializerProvider<V, R> {
    /**
     * Provides a serializer for a value of class {@code valueClass}.
     * The returned serializer must be able to handle a value of this
     * type.
     * 
     * @param valueClass class of the config values to serialize
     * @return a serializer, or {@code null} to try the next provider
     * @see ValueSerializerProvider
     */
    ValueSerializer<V, R> provide(Class<?> valueClass, SerializerContext ctx);
}
