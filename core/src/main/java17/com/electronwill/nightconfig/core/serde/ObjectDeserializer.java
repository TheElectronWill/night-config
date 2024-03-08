package com.electronwill.nightconfig.core.serde;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.Optional;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

public final class ObjectDeserializer extends AbstractObjectDeserializer {
	// This is the Java 17 version of ObjectDeserializer,
	// substituted to add methods related to the new Java Records.

	/**
	 * Creates a new {@link ObjectDeserializerBuilder} with some standard deserializers already registered.
	 *
	 * @return a new builder
	 */
	public static ObjectDeserializerBuilder builder() {
		return new ObjectDeserializerBuilder(true);
	}

	/**
	 * Creates a new {@link ObjectDeserializerBuilder} without the standard deserializers already registered.
	 *
	 * @return a new builder
	 */
	public static ObjectDeserializerBuilder blankBuilder() {
		return new ObjectDeserializerBuilder(false);
	}

	/**
	 * Creates a new {@link ObjectDeserializer} with the standard deserializers.
	 * <p>
	 * This is equivalent to {@code ObjectDeserializer.builder().build()}.
	 */
	public static ObjectDeserializer standard() {
		return builder().build();
	}

	ObjectDeserializer(ObjectDeserializerBuilder builder) {
		super(builder);
	}

	/**
	 * Deserializes a value into an instance of the collection {@code C<V>}.
	 *
	 * @param <C>             type of the collection
	 * @param <V>             type of the values in the collection
	 * @param configValue     config value to deserialize
	 * @param collectionClass class of the collection
	 * @param valueClass      class of the values in the collection
	 * @return the deserialized collection
	 */
	public <C extends Collection<V>, V> C deserializeToCollection(Object configValue,
			Class<C> collectionClass, Class<V> valueClass) {
		return super.deserializeToCollection(configValue, collectionClass, valueClass);
	}

	/**
	 * Deserializes a value into an instance of the map {@code M<String, V>}.
	 *
	 * @param <M>         type of the map
	 * @param <V>         type of the values in the map
	 * @param configValue config value to deserialize
	 * @param mapClass    class of the map
	 * @param valueClass  class of the values in the collection
	 * @return the deserialized map
	 */
	public <M extends Map<String, V>, V> M deserializeToMap(Object configValue, Class<M> mapClass,
			Class<V> valueClass) {
		return super.deserializeToMap(configValue, mapClass, valueClass);
	}

	/**
	 * Deserializes a {@code Config} as an object by transforming the config entries into fields.
	 * The fields of the {@code destination} are modified through reflection.
	 *
	 * @param source      config to deserialize
	 * @param destination object to store the result in
	 */
	public void deserializeFields(UnmodifiableConfig source, Object destination) {
		super.deserializeFields(source, destination);
	}

	/**
	 * Deserializes a {@code Config} as an object of type {@code R} by transforming the config entries
	 * into fields. A new instance of the object is created, and its fields are modified through reflection.
	 *
	 * @param <R>                 type of the resulting object
	 * @param source              config to deserialize
	 * @param destinationSupplier supplier of the resulting object
	 * @return the deserialized object
	 */
	public <R> R deserializeFields(UnmodifiableConfig source,
			Supplier<? extends R> destinationSupplier) {
		return super.deserializeFields(source, destinationSupplier);
	}

	// ------ Additional methods for Java 17+ ------

	/**
	 * Deserializes a {@code Config} to a record of type {@code R} by transforming the config entries
	 * into record's components.
	 *
	 * @param <R>         type of the record
	 * @param source      config to deserialize
	 * @param recordClass class of the record
	 * @return a new, deserialized record
	 */
	@SuppressWarnings("unchecked")
	public <R extends Record> R deserializeToRecord(UnmodifiableConfig source, Class<R> recordClass) {
		if (!recordClass.isRecord()) {
			throw new IllegalArgumentException(
					"Argument recordClass = " + recordClass
							+ " is not the class of a record! Please don't silence errors about incompatible types :)");
		}
		DeserializerContext ctx = new DeserializerContext(this);
		TypeConstraint t = new TypeConstraint(recordClass);
		return (R) ctx.deserializeValue(source, Optional.of(t));
	}
}
