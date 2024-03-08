package com.electronwill.nightconfig.core.serde;

/**
 * Provides {@link ValueDeserializer} to deserialize configuration values.
 * <p>
 * The {@link #provide(Class, TypeConstraint)} method returns {@code null} when
 * it cannot provide a deserializer for the given value type and result type.
 * In that case, other providers will be called, until a suitable deserializer is found
 * or all the providers have been tried.
 * 
 * @param <T> type of the config values to deserialize
 * @param <R> resulting type of the deserialization of these values
 */
@FunctionalInterface
public interface ValueDeserializerProvider<T, R> {
	/**
	 * Provides a deserializer for a value of class {@code valueClass} and a constraint
	 * on the result type. The returned deserializer must be able to handle a value of this
	 * type <em>and</em> to produce a value that satisfies the {@code resultType} constraint.
	 * 
	 * @param valueClass class of the config values to deserialize
	 * @param resultType constraint that the deserialization result must satisfy
	 * @return a deserializer, or {@code null} to try the next provider
	 * @see ValueDeserializerProvider
	 */
	ValueDeserializer<T, R> provide(Class<?> valueClass, TypeConstraint resultType);
}
