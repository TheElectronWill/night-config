package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

public final class DeserializerContext {
	final ObjectDeserializer settings;

	DeserializerContext(ObjectDeserializer settings) {
		this.settings = settings;
	}

	public Object deserializeValue(Object value, Optional<TypeConstraint> typeConstraint) {
		TypeConstraint t = typeConstraint.orElse(new TypeConstraint(Object.class));
		Class<?> resultClass = t.getSatisfyingRawType()
				.orElseThrow(() -> new DeserializationException(
						"Could not find a concrete type that can satisfy the constraint " + t));
		ValueDeserializer<Object, ?> deserializer = settings.findValueDeserializer(value,
				resultClass);
		return deserializer.deserialize(value, typeConstraint, this);
	}

	/**
	 * Deserializes a configuration by transforming its entries into fields of the {@code destination} object.
	 */
	public void deserializeFields(UnmodifiableConfig source, Object destination) {
		// loop through the class hierarchy of the destination type
		Class<?> cls = source.getClass();
		while (cls != Object.class) {
			for (Field field : cls.getDeclaredFields()) {
				if (preCheck(field)) {
					// todo read annotations

					// get the config value
					List<String> path = Collections.singletonList(field.getName());
					Object value = source.getOptional(path).orElseThrow();

					// find the right deserializer
					Class<?> resultClass = field.getType();
					ValueDeserializer<Object, ?> deserializer = settings
							.findValueDeserializer(value, resultClass);

					// deserialize
					TypeConstraint resultType = new TypeConstraint(field.getGenericType());
					try {
						Object deserialized = deserializer.deserialize(value,
								Optional.of(resultType),
								this);
						field.set(destination, deserialized);
					} catch (Exception ex) {
						throw new DeserializationException(
								"Error during deserialization of value " + value + " to field "
										+ field + " with deserializer " + deserializer,
								ex);
					}
				}
			}
			cls = cls.getSuperclass();
		}
	}

	private boolean preCheck(Field field) {
		int mods = field.getModifiers();
		if (Modifier.isStatic(mods)) {
			return false;
		}
		if (Modifier.isTransient(mods) && settings.applyTransientModifier) {
			return false;
		}
		if (Modifier.isFinal(mods)) {
			field.setAccessible(true);
		}
		return true;
	}

}
