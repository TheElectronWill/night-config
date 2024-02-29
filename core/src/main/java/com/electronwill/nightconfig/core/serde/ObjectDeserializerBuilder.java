package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * Implements Config to Object deserialization.
 */
public final class ObjectDeserializerBuilder {

	final List<ValueDeserializerProvider<?, ?>> deserializerProviders = new ArrayList<>();

	/** the last-resort serializer provider, used when no other provider matches */
	ValueDeserializerProvider<?, ?> defaultProvider = new NoProvider();

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
	 * If enabled, transient fields will be deserialized instead of ignored.
	 */
	public void ignoreTransientModifier() {
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
	 * @see #withDefaultSerializer(ValueDeserializerProvider)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void withDefaultSerializerProvider() {
		ValueDeserializer pojoDe = new PojoDeserializer();
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
		withDefaultSerializerProvider();

		ValueDeserializer trivialDe = new TrivialDeserializer();
		ValueDeserializer mapDe = new MapDeserializer();
		ValueDeserializer collDe = new CollectionDeserializer();
		ValueDeserializer enumDe = new EnumDeserializer();

		withDeserializerProvider(((valueClass, resultType) -> {
			return resultType.getSatisfyingRawType().map(resultClass -> {
				if (Util.canAssign(resultClass, valueClass)) {
					return trivialDe; // value to value (same type or compatible type)
				}
				if (Collection.class.isAssignableFrom(valueClass)
						&& Collection.class.isAssignableFrom(resultClass)) {
					return collDe; // collection<value> to collection<T>
				}
				if (UnmodifiableConfig.class.isAssignableFrom(valueClass)
						&& Map.class.isAssignableFrom(resultClass)) {
					return mapDe; // config to map<K, V>
				}
				if ((resultClass == String.class || resultClass == Integer.class
						|| resultClass == int.class) && Enum.class.isAssignableFrom(resultClass)) {
					return enumDe; // value to Enum
				}
				return null; // no standard deserializer matches this case
			}).orElse(null);
		}));
	}

	private static final class TrivialDeserializer implements ValueDeserializer<Object, Object> {
		@Override
		public Object deserialize(Object value, Optional<TypeConstraint> resultType,
				DeserializerContext ctx) {
			return value;
		}
	}

	/**
	 * The standard deserializer for fields of type {@code Map<String,?>} and values of type
	 * {@code UnmodifiableCommentedConfig}.
	 */
	private static final class MapDeserializer
			implements ValueDeserializer<UnmodifiableCommentedConfig, Map<String, ?>> {

		@Override
		public Map<String, ?> deserialize(UnmodifiableCommentedConfig configValue,
				Optional<TypeConstraint> resultType,
				DeserializerContext ctx) {

			int size = configValue.size();

			// Look for the type of the values to insert in the map,
			// and create a map of the right type.

			Optional<TypeConstraint[]> mapKVType;
			Map<String, Object> res;
			if (resultType.isPresent()) {
				TypeConstraint mapType = resultType.get();
				res = createMapInstance(mapType.getSatisfyingRawType().get(), size);
				mapKVType = extractMapKVType(mapType);
			} else {
				mapKVType = Optional.empty();
				res = Config.isInsertionOrderPreserved() ? new java.util.LinkedHashMap<>(size)
						: new java.util.HashMap<>(size);
			}

			// separate types of Key and Value
			Optional<TypeConstraint> mapKeyType = mapKVType.map(arr -> arr[0]);
			Optional<TypeConstraint> mapValueType = mapKVType.map(arr -> arr[1]);

			// check the type of keys
			if (mapKeyType.isPresent()) {
				if (!mapKeyType.get().getSatisfyingRawType().equals(Optional.of(String.class))) {
					throw new DeserializationException(
							"Invalid map type for deserialization, the keys should be of type String instead of "
									+ mapKeyType.get() + ". Full map type: " + resultType.get());
				}
			}

			// deserialize config entries to map values
			for (UnmodifiableCommentedConfig.Entry entry : configValue.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				Object deserialized = ctx.deserializeValue(value, mapValueType);
				res.put(key, deserialized);
			}
			return res;
		}

		private Optional<TypeConstraint[]> extractMapKVType(TypeConstraint mapTypeC) {
			// return collType.resolveTypeArgumentsFor(Collection.class).map(c -> c[0]).orElse(null);
			return mapTypeC.resolveTypeArgumentsFor(Map.class);
		}

		@SuppressWarnings("unchecked")
		private Map<String, Object> createMapInstance(Class<?> cls, int sizeHint) {
			if (cls == Map.class) {
				return Config.isInsertionOrderPreserved() ? new java.util.LinkedHashMap<>(sizeHint)
						: new java.util.HashMap<>(sizeHint);
			}
			if (cls == java.util.LinkedHashMap.class) {
				return new java.util.LinkedHashMap<>(sizeHint);
			}
			if (cls == java.util.HashMap.class) {
				return new java.util.HashMap<>(sizeHint);
			}
			if (cls == java.util.IdentityHashMap.class) {
				return new java.util.IdentityHashMap<>(sizeHint);
			}
			if (cls.isAssignableFrom(java.util.HashMap.class)) {
				// We use isAssignableFrom to cover other superclasses or superinterfaces of HashMap,$
				// such as NavigableMap.
				if (Config.isInsertionOrderPreserved()
						&& cls.isAssignableFrom(java.util.LinkedHashMap.class)) {
					return new java.util.LinkedHashMap<>(sizeHint);
				} else {
					return new java.util.HashMap<>(sizeHint);
				}
			}
			if (cls.isAssignableFrom(java.util.concurrent.ConcurrentHashMap.class)) {
				return new java.util.concurrent.ConcurrentHashMap<>(sizeHint);
			}

			// unknown Map type, try the public parameterless constructor
			try {
				return (Map<String, Object>) cls.getDeclaredConstructor().newInstance();
			} catch (Exception ex) {
				throw new DeserializationException("Failed to create an instance of " + cls, ex);
			}
		}
	}

	static final class CollectionDeserializer
			implements ValueDeserializer<Collection<?>, Collection<?>> {

		@Override
		public Collection<?> deserialize(Collection<?> collectionValue,
				Optional<TypeConstraint> resultType,
				DeserializerContext ctx) {

			int size = collectionValue.size();
			Collection<Object> res;
			Optional<TypeConstraint> valueType;
			if (resultType.isPresent()) {
				TypeConstraint collectionType = resultType.get();
				res = createCollectionInstance(collectionType.getSatisfyingRawType().get(), size);
				valueType = extractCollectionValueType(collectionType);
			} else {
				// no constraint, choose arbitrarily: it will be ArrayList
				res = new ArrayList<>(size);
				valueType = Optional.empty();
			}

			// convert the values
			for (Object v : collectionValue) {
				Object deserialized = ctx.deserializeValue(v, valueType);
				res.add(deserialized);
			}
			return res;
		}

		@SuppressWarnings("unchecked")
		private Collection<Object> createCollectionInstance(Class<?> cls, int sizeHint) {
			if (cls.isAssignableFrom(java.util.ArrayList.class)) {
				return new java.util.ArrayList<>(sizeHint);
			}
			if (cls.isAssignableFrom(java.util.LinkedList.class)) {
				return new java.util.LinkedList<>();
			}
			if (cls.isAssignableFrom(java.util.ArrayDeque.class)) {
				return new java.util.ArrayDeque<>(sizeHint);
			}

			// unknown Collection type, try the public parameterless constructor
			try {
				return (Collection<Object>) cls.getDeclaredConstructor().newInstance();
			} catch (Exception ex) {
				throw new DeserializationException("Failed to create an instance of " + cls, ex);
			}
		}

		private static Optional<TypeConstraint> extractCollectionValueType(
				TypeConstraint collType) {
			return collType.resolveTypeArgumentsFor(Collection.class).map(c -> c[0]);
		}
	}

	static final class ArrayDeserializer
			implements ValueDeserializer<Collection<?>, Object> {

		@Override
		public Object deserialize(Collection<?> collectionValue,
				Optional<TypeConstraint> resultType,
				DeserializerContext ctx) {

			int size = collectionValue.size();
			Object res;
			Optional<TypeConstraint> valueType;
			if (resultType.isPresent()) {
				TypeConstraint arrayType = resultType.get();
				Class<?> componentType = ((Class<?>) arrayType.getFullType()).getComponentType();
				assert componentType != null;
				res = Array.newInstance(componentType, size);
				valueType = Optional.of(new TypeConstraint(componentType));
			} else {
				// no constraint, choose arbitrarily: it will be Object[]
				res = new Object[size];
				valueType = Optional.empty();
			}

			// convert the values
			int i = 0;
			for (Object v : collectionValue) {
				Object deserialized = ctx.deserializeValue(v, valueType);
				Array.set(res, i, deserialized);
				i++;
			}
			return res;
		}
	}

	private static final class EnumDeserializer implements ValueDeserializer<String, Enum<?>> {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Enum<?> deserialize(String value, Optional<TypeConstraint> resultType,
				DeserializerContext ctx) {
			TypeConstraint enumType = resultType.orElseThrow(() -> new DeserializationException(
					"Cannot deserialize a value to an enum without knowing the enum type"));
			Class<?> cls = enumType.getSatisfyingRawType()
					.orElseThrow(() -> new DeserializationException(
							"Could not find a concrete enum type that can satisfy the constraint "
									+ enumType));
			// TODO use the field's annotations, if any, to get the right variant of EnumGetMethod
			return EnumGetMethod.NAME.get(value, (Class) cls);
		}
	}

	/** Deserialize to the fields of a Plain-Old-Java-Object (POJO). */
	private static final class PojoDeserializer
			implements ValueDeserializer<UnmodifiableConfig, Object> {

		@Override
		public Object deserialize(UnmodifiableConfig value, Optional<TypeConstraint> resultType,
				DeserializerContext ctx) {
			if (resultType.isEmpty()) {
				// no constraint, we don't know the type of the POJO!
				// Assume the easiest result: return the value as is
				return value;
			} else {
				TypeConstraint t = resultType.get();
				Class<?> cls = t.getSatisfyingRawType().orElseThrow(() -> new DeserializationException(
						"Could not find a concrete type that can satisfy the constraint " + t));
				try {
					Object instance = cls.getDeclaredConstructor().newInstance();
					ctx.deserializeFields(value, instance);
				} catch (Exception e) {
					throw new DeserializationException("Failed to create an instance of " + cls, e);
				}
			}
			return null;
		}
	}

	/** A provider that provides nothing, {@code provide} always returns null. */
	private static final class NoProvider implements ValueDeserializerProvider<Object, Object> {
		@Override
		public ValueDeserializer<Object, Object> provide(Class<?> valueClass,
				TypeConstraint resultType) {
			return null;
		}
	}
}
