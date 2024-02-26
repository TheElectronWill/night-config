package com.electronwill.nightconfig.core.serde;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

public final class ObjectDeserializer {

	final List<ValueDeserializerProvider<?, ?>> generalDeserializers;
	final boolean applyTransientModifier;

	ObjectDeserializer(ObjectDeserializerBuilder builder) {
		this.generalDeserializers = builder.generalDeserializers;
		this.applyTransientModifier = builder.applyTransientModifier;
	}

	// NOTE: it would make no sense to provide a method deserialize(Object) -> Object, because
	// the trivial deserialization can always be applied when there is no constraint on the result.
	// ObjectSerializer.serialize does exist, however, because there is a constraint on the type of
	// values that the configuration can contain.

	/**
	 * Deserializes a configuration value into an instance of the collection {@code C<V>}.
	 * 
	 * @param <C>             type of the collection
	 * @param <V>             type of the values in the collection
	 * @param configValue     config value to deserialize
	 * @param collectionClass class of the collection
	 * @param valueClass      class of the values in the collection
	 * @return the deserialized collection
	 */
	@SuppressWarnings("unchecked")
	public <C extends Collection<V>, V> C deserializeToCollection(Object configValue,
			Class<C> collectionClass, Class<V> valueClass) {
		DeserializerContext ctx = new DeserializerContext(this);
		TypeConstraint t = new TypeConstraint(
				new TypeConstraint.ManuallyParameterized(collectionClass, valueClass));
		return (C) ctx.deserializeValue(configValue, Optional.of(t));
	}

	/**
	 * Deserializes a configuration value into an instance of the map {@code M<String, V>}.
	 * 
	 * @param <M>         type of the map
	 * @param <V>         type of the values in the map
	 * @param configValue config value to deserialize
	 * @param mapClass    class of the map
	 * @param valueClass  class of the values in the collection
	 * @return the deserialized map
	 */
	@SuppressWarnings("unchecked")
	public <M extends Map<String, V>, V> M deserializeToMap(Object configValue, Class<M> mapClass,
			Class<V> valueClass) {
		DeserializerContext ctx = new DeserializerContext(this);
		TypeConstraint t = new TypeConstraint(
				new TypeConstraint.ManuallyParameterized(mapClass, String.class, valueClass));
		return (M) ctx.deserializeValue(configValue, Optional.of(t));
	}

	/**
	 * Deserializes a {@code Config} as an object by transforming its entries into fields.
	 * The fields of the {@code destination} are modified through reflection.
	 * 
	 * @param source      config to deserialize
	 * @param destination object to store the result in
	 */
	public void deserializeFields(UnmodifiableConfig source, Object destination) {
		DeserializerContext ctx = new DeserializerContext(this);
		ctx.deserializeFields(source, destination);
	}

	/**
	 * Deserializes a {@code Config} as an object of type {@code R} by transforming its entries
	 * into fields. A new instance of the object is created, and its fields are modified through reflection.
	 * 
	 * @param <R>                 type of the resulting object
	 * @param source              config to deserialize
	 * @param destinationSupplier supplier of the resulting object
	 * @return the deserialized object
	 */
	public <R> R deserializeFields(UnmodifiableConfig source,
			Supplier<? extends R> destinationSupplier) {
		R dest = destinationSupplier.get();
		deserializeFields(source, dest);
		return dest;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	<T, R> ValueDeserializer<T, R> findValueDeserializer(T value, Class<?> resultClass) {
		Class<?> valueClass = value == null ? null : value.getClass();

		for (ValueDeserializerProvider provider : this.generalDeserializers) {
			ValueDeserializer<?, ?> maybeDeser = provider.provide(valueClass, resultClass);
			if (maybeDeser != null) {
				return (ValueDeserializer<T, R>) maybeDeser;
			}
		}
		String ofTypeStr = valueClass == null ? "" : " of type " + valueClass;
		throw new DeserializationException(
				"No suitable deserializer found for value" + ofTypeStr + ":" + value);
	}
}
