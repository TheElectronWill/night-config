package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

final class StandardDeserializers {
	/**
	 * The trivial deserializer: deserialize(value) == value.
	 */
    static final class TrivialDeserializer implements ValueDeserializer<Object, Object> {
		@Override
		public Object deserialize(Object value, Optional<TypeConstraint> resultType,
				DeserializerContext ctx) {
			return value;
		}
	}

	/**
	 * Deserializes {@code Map<String, Value>} or {@code UnmodifiableConfig} to {@code Map<String, Result>}.
	 */
	static final class MapDeserializer implements ValueDeserializer<Object, Map<String, ?>> {

		@Override
		public Map<String, ?> deserialize(Object mapValue,
				Optional<TypeConstraint> resultType,
				DeserializerContext ctx) {

			int size;
			if (mapValue instanceof UnmodifiableConfig) {
				size = ((UnmodifiableConfig) mapValue).size();
			} else {
				size = ((Map<?, ?>) mapValue).size();
			}

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

			if (mapValue instanceof UnmodifiableConfig) {
				// deserialize config entries to map values, converting each value
				for (UnmodifiableConfig.Entry entry : ((UnmodifiableConfig) mapValue).entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					Object deserialized = ctx.deserializeValue(value, mapValueType);
					res.put(key, deserialized);
				}
			} else {
				// deserialize map entries to map values, converting each value
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) mapValue).entrySet()) {
					Object key = entry.getKey();
					if (!(key instanceof String)) {
						String keyClassStr = key == null ? "null" : key.getClass().toString();
						throw new DeserializationException(
								"Invalid map type for deserialization, the keys should be of type String instead of "
										+ keyClassStr + ". Full map type: " + resultType.get());
					}
					Object value = entry.getValue();
					Object deserialized = ctx.deserializeValue(value, mapValueType);
					res.put((String) key, deserialized);
				}
			}
			return res;
		}

		private static Optional<TypeConstraint[]> extractMapKVType(TypeConstraint mapTypeC) {
			// return collType.resolveTypeArgumentsFor(Collection.class).map(c -> c[0]).orElse(null);
			return mapTypeC.resolveTypeArgumentsFor(Map.class);
		}

		@SuppressWarnings("unchecked")
		private static Map<String, Object> createMapInstance(Class<?> cls, int sizeHint) {
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

	/**
	 * Deserializes {@code Collection<Value>} to {@code Collection<Result>}.
	 */
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

	/**
	 * Deserializes a {@code Collection<V>} to an {@code Array<R>} (i.e. {@code R[]}).
	 */
	static final class CollectionToArrayDeserializer
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

	/**
	 * Deserializes a {@code String} into an {@code Enum}.
	 */
	static final class EnumDeserializer implements ValueDeserializer<String, Enum<?>> {

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
}
