package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

public final class DeserializerContext {
	final ObjectDeserializer settings;

	DeserializerContext(ObjectDeserializer settings) {
		this.settings = settings;
	}

	public Object deserializeValue(Object value, Optional<TypeConstraint> typeConstraint) {
		TypeConstraint t = typeConstraint.orElse(new TypeConstraint(Object.class));
		ValueDeserializer<Object, ?> deserializer = settings.findValueDeserializer(value, t);
		return deserializer.deserialize(value, typeConstraint, this);
	}

	/**
	 * Deserializes a configuration by transforming its entries into fields of the {@code destination} object.
	 */
	public void deserializeFields(UnmodifiableConfig source, Object destination) {
		// loop through the class hierarchy of the destination type
		Class<?> cls = destination.getClass();
		while (cls != Object.class) {
			for (Field field : cls.getDeclaredFields()) {
				if (preCheck(field)) {
					// todo read annotations

					// get the config value
					List<String> path = Collections.singletonList(field.getName());
					Object value = source.getRaw(path);
					if (value == null) {
						 // missing value
						throw new DeserializationException(
								"Missing configuration entry " + path + " for field " + field
										+ " declared in " + field.getDeclaringClass());
					} else if (value == NullObject.NULL_OBJECT) {
						// null value
						value = null;
					}

					// find the right deserializer
					TypeConstraint resultType = new TypeConstraint(field.getGenericType());
					ValueDeserializer<Object, ?> deserializer = settings
							.findValueDeserializer(value, resultType);

					// deserialize
					try {
						Optional<TypeConstraint> type = Optional.of(resultType);
						Object deserialized = deserializer.deserialize(value, type, this);
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
