package com.electronwill.nightconfig.core.serde;

import java.util.*;
import java.util.function.Supplier;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.serde.annotations.SerdeDefault;
import com.electronwill.nightconfig.core.serde.annotations.SerdePhase;

/**
 * AbstractObjectDeserializer, common to all Java versions.
 */
class AbstractObjectDeserializer {
	protected final List<ValueDeserializerProvider<?, ?>> generalProviders;
	protected ValueDeserializerProvider<?, ?> defaultProvider;
	protected final boolean applyTransientModifier;

	protected AbstractObjectDeserializer(ObjectDeserializerBuilder builder) {
		this.generalProviders = builder.deserializerProviders;
		this.defaultProvider = builder.defaultProvider;
		this.applyTransientModifier = builder.applyTransientModifier;
		assert generalProviders != null && defaultProvider != null;
	}

	// NOTE: it would make no sense to provide a method deserialize(Object) ->
	// Object, because
	// the trivial deserialization can always be applied when there is no constraint
	// on the result.
	// ObjectSerializer.serialize does exist, however, because there is a constraint
	// on the type of
	// values that the configuration can contain.

	/**
	 * Deserializes a configuration value into an instance of the collection
	 * {@code C<V>}.
	 *
	 * @param <C>             type of the collection
	 * @param <V>             type of the values in the collection
	 * @param configValue     config value to deserialize
	 * @param collectionClass class of the collection
	 * @param valueClass      class of the values in the collection
	 * @return the deserialized collection
	 */
	@SuppressWarnings("unchecked")
	protected <C extends Collection<V>, V> C deserializeToCollection(Object configValue,
			Class<C> collectionClass, Class<V> valueClass) {
		DeserializerContext ctx = new DeserializerContext(this);
		TypeConstraint t = new TypeConstraint(
				new TypeConstraint.ManuallyParameterized(collectionClass, valueClass));
		return (C) ctx.deserializeValue(configValue, Optional.of(t));
	}

	/**
	 * Deserializes a configuration value into an instance of the map
	 * {@code M<String, V>}.
	 *
	 * @param <M>         type of the map
	 * @param <V>         type of the values in the map
	 * @param configValue config value to deserialize
	 * @param mapClass    class of the map
	 * @param valueClass  class of the values in the collection
	 * @return the deserialized map
	 */
	@SuppressWarnings("unchecked")
	protected <M extends Map<String, V>, V> M deserializeToMap(Object configValue, Class<M> mapClass,
			Class<V> valueClass) {
		DeserializerContext ctx = new DeserializerContext(this);
		TypeConstraint t = new TypeConstraint(
				new TypeConstraint.ManuallyParameterized(mapClass, String.class, valueClass));
		return (M) ctx.deserializeValue(configValue, Optional.of(t));
	}

	/**
	 * Deserializes a {@code Config} as an object by transforming its entries into
	 * fields.
	 * The fields of the {@code destination} are modified through reflection.
	 *
	 * @param source      config to deserialize
	 * @param destination object to store the result in
	 */
	protected void deserializeFields(UnmodifiableConfig source, Object destination) {
		DeserializerContext ctx = new DeserializerContext(this);
		ctx.deserializeFields(source, destination);
	}

	/**
	 * Deserializes a {@code Config} as an object of type {@code R} by transforming
	 * its entries
	 * into fields. A new instance of the object is created, and its fields are
	 * modified through reflection.
	 *
	 * @param <R>                 type of the resulting object
	 * @param source              config to deserialize
	 * @param destinationSupplier supplier of the resulting object
	 * @return the deserialized object
	 */
	protected <R> R deserializeFields(UnmodifiableConfig source, Supplier<? extends R> destinationSupplier) {
		R dest = destinationSupplier.get();
		deserializeFields(source, dest);
		return dest;
	}

	@SuppressWarnings("unchecked")
	protected <T, R> ValueDeserializer<T, R> findValueDeserializer(T value, TypeConstraint resultType) {
		Class<?> valueClass = value == null ? null : value.getClass();
		ValueDeserializer<?, ?> maybeDe;
		for (ValueDeserializerProvider<?, ?> provider : generalProviders) {
			maybeDe = provider.provide(valueClass, resultType);
			if (maybeDe != null) {
				return (ValueDeserializer<T, R>) maybeDe;
			}
		}
		maybeDe = defaultProvider.provide(valueClass, resultType);
		if (maybeDe != null) {
			return (ValueDeserializer<T, R>) maybeDe;
		}
		String ofTypeStr = valueClass == null ? "" : " of type " + valueClass;
		throw new SerdeException("No suitable deserializer found for value" + ofTypeStr + ": "
				+ value + " and result constraint " + resultType);
	}

	protected Supplier<?> findDefaultValueSupplier(Object rawConfigValue, Field field, Object instance) {
		EnumMap<SerdeDefault.WhenValue, SerdeDefault> defaultForDeserializing = AnnotationProcessor
				.getConfigDefaultAnnotations(field)
				.get(SerdePhase.DESERIALIZING);

		if (defaultForDeserializing == null) {
			return null; // no default
		}

		SerdeDefault applicableDefault = null;
		if (rawConfigValue == null) {
			// missing value
			applicableDefault = defaultForDeserializing.get(SerdeDefault.WhenValue.IS_MISSING);
		} else if (rawConfigValue == NullObject.NULL_OBJECT) {
			// null value
			applicableDefault = defaultForDeserializing.get(SerdeDefault.WhenValue.IS_NULL);
		} else {
			// avoid to call Util.isEmpty() if there's no default for empty values
			SerdeDefault forEmpty = defaultForDeserializing.get(SerdeDefault.WhenValue.IS_EMPTY);
			if (forEmpty != null && Util.isEmpty(rawConfigValue)) {
				applicableDefault = forEmpty;
			}
		}

		if (applicableDefault == null) {
			return null; // no applicable default for our config value
		}

		return AnnotationProcessor.resolveConfigDefaultProvider(applicableDefault, instance);
	}
}
