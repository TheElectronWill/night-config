package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * Implements Config to Object deserialization.
 */
public final class ObjectDeserializerBuilder {

	final List<ValueDeserializerProvider<?, ?>> deserializerProviders = new ArrayList<>();

	/** the last-resort serializer provider, used when no other provider matches */
	ValueDeserializerProvider<?, ?> defaultProvider = NoProvider.INSTANCE;

	/** setting: skip transient fields as requested by the modifier */
	boolean applyTransientModifier = true;

	ObjectDeserializerBuilder(boolean standards) {
		if (standards) {
			registerStandardDeserializers();
		}
	}

	/**
	 * Builds the {@link ObjectDeserializer} with the current settings.
	 *
	 * @return a new ObjectDeserializer
	 */
	public ObjectDeserializer build() {
		return new ObjectDeserializer(this);
	}

	/**
	 * Deserialize transient fields instead of ignoring them.
	 */
	public void deserializeTransientFields() {
		this.applyTransientModifier = false;
	}

	/**
	 * Adds a {@link ValueDeserializer} that will be used to deserialize config values
	 * of type {@code valueClass} to objects of type {@code resultClass}.
	 *
	 * @param <V>          type of the config values to deserialize
	 * @param <R>          resulting type of the deserialization
	 * @param valueClass   class of the config values to deserialize
	 * @param resultClass  class of the deserialization result
	 * @param deserializer deserializer to register
	 */
	public <V, R> void withDeserializerForClass(Class<V> valueClass, Class<R> resultClass,
			ValueDeserializer<? super V, ? extends R> deserializer) {

		withDeserializerProvider(((valueCls, resultType) -> {
			return resultType.getSatisfyingRawType().map(resultCls -> {
				if (valueCls.isAssignableFrom(valueClass) && resultCls.isAssignableFrom(resultClass)) {
					return deserializer;
				}
				return null;
			}).orElse(null);
		}));
	}

	/**
	 * Adds a {@link ValueDeserializerProvider} that provides {@link ValueDeserializer} to
	 * deserialize config values.
	 *
	 * @param <V>      type of the config values to deserialize
	 * @param <R>      resulting type of the deserialization
	 * @param provider provider to register
	 */
	public <V, R> void withDeserializerProvider(ValueDeserializerProvider<V, R> provider) {
		deserializerProviders.add(provider);
	}

	/**
	 * Sets the default serializer provider, which is called when no other {@link ValueDeserializerProvider} is
	 * able to give a {@link ValueDeserializer} for the incoming value and result type constraint.
	 * <p>
	 * This will replace any previously set default provider.
	 *
	 * @param <V>      type of the config values to deserialize
	 * @param <R>      resulting type of the deserialization
	 * @param provider the new default serializer
	 */
	public <V, R> void withDefaultDeserializerProvider(ValueDeserializerProvider<V, R> provider) {
		defaultProvider = provider;
	}

	/**
	 * Enables the standard default serializer provider.
	 * <p>
	 * This will replace any previously set default provider.
	 *
	 * @see #withDefaultDeserializerProvider(ValueDeserializerProvider)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void withDefaultDeserializerProvider() {
		ValueDeserializer pojoDe = new ConfigToPojoDeserializer();
		defaultProvider = (valueClass, resultType) -> {
			if (UnmodifiableConfig.class.isAssignableFrom(valueClass)) {
				return pojoDe;
			} else {
				return null;
			}
		};
	}

	/** registers the standard serializers */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void registerStandardDeserializers() {
		withDefaultDeserializerProvider();

		ValueDeserializer trivialDe = new StandardDeserializers.TrivialDeserializer();
		ValueDeserializer mapDe = new StandardDeserializers.MapDeserializer();
		ValueDeserializer collDe = new StandardDeserializers.CollectionDeserializer();
		ValueDeserializer arrDe = new StandardDeserializers.CollectionToArrayDeserializer();
		ValueDeserializer enumDe = new StandardDeserializers.EnumDeserializer();
		ValueDeserializer uuidDe = new StandardDeserializers.UuidDeserializer();

		withDeserializerProvider(((valueClass, resultType) -> {
			Type fullType = resultType.getFullType();
			return resultType.getSatisfyingRawType().map(resultClass -> {
				if (Util.canAssign(resultClass, valueClass)
						&& (valueClass == null || fullType instanceof Class)) {
					return trivialDe; // value to value (same type or compatible type)

					// Note that we rule out TypeConstraint where getFullType() is not a simple Class,
					// which means that there are type parameters and that we cannot just blindly assign.
				}
				if (Collection.class.isAssignableFrom(valueClass)) {
					if (Collection.class.isAssignableFrom(resultClass)) {
						return collDe; // collection<value> to collection<T>
					} else if (resultClass.isArray()) {
						return arrDe; // collection<value> to array<T>
					}
				}
				if ((UnmodifiableConfig.class.isAssignableFrom(valueClass)
						|| Map.class.isAssignableFrom(valueClass))
						&& Map.class.isAssignableFrom(resultClass)) {
					return mapDe; // config to map<K, V>
				}
				if (resultClass == UUID.class && valueClass == String.class) {
					return uuidDe;
				}
				if (valueClass == String.class && Enum.class.isAssignableFrom(resultClass)) {
					return enumDe; // value to Enum
				}
				return null; // no standard deserializer matches this case
			}).orElse(null);
		}));
	}

	/** A provider that provides nothing, {@code provide} always returns null. */
	static final class NoProvider implements ValueDeserializerProvider<Object, Object> {
		static final NoProvider INSTANCE = new NoProvider();

		@Override
		public ValueDeserializer<Object, Object> provide(Class<?> valueClass, TypeConstraint resultType) {
			return null;
		}
	}
}
