package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;

public final class DeserializerContext {
	final AbstractObjectDeserializer settings;

	DeserializerContext(AbstractObjectDeserializer settings) {
		this.settings = settings;
	}

	public Object deserializeValue(Object value, Optional<TypeConstraint> typeConstraint) {
		TypeConstraint t = typeConstraint.orElse(new TypeConstraint(Object.class));
		ValueDeserializer<Object, ?> deserializer = settings.findValueDeserializer(value, t);
		return deserializer.deserialize(value, typeConstraint, this);
	}

	/**
	 * Deserializes a configuration by transforming its entries into fields of the
	 * {@code destination} object.
	 */
	public void deserializeFields(UnmodifiableConfig source, Object destination) {
		// loop through the class hierarchy of the destination type
		Class<?> cls = destination.getClass();
		while (cls != Object.class) {
			for (Field field : cls.getDeclaredFields()) {
				if (preCheck(field)) {
					// get the config key
					List<String> path = Collections.singletonList(configKey(field));

					// get the config value
					Object value = source.getRaw(path);

					// skip the field if the annotation say so
					if (skipField(field, destination, value)) {
						continue; // don't deserialize, go to the next field
					}

					// deserialize, but try the default value first
					Object deserialized;
					Supplier<?> defaultValueSupplier = settings.findDefaultValueSupplier(value, field, destination);
					if (defaultValueSupplier != null) {
						// default value found, use it directly
						try {
							deserialized = defaultValueSupplier.get();
						} catch (Exception e) {
							throw new SerdeException("Error in default value provider for field " + field, e);
						}
					} else {
						// no default value, deserialize the config value
						value = normalizeForDeserialization(value, path, field);

						// find the right deserializer
						TypeConstraint resultType = new TypeConstraint(field.getGenericType());
						ValueDeserializer<Object, ?> deserializer = settings.findValueDeserializer(value, resultType);

						// deserialize
						try {
							Optional<TypeConstraint> type = Optional.of(resultType);
							deserialized = deserializer.deserialize(value, type, this);
						} catch (Exception ex) {
							throw new SerdeException(
									"Error during deserialization of value `" + value + "` to field `"
											+ field + "` with deserializer " + deserializer,
									ex);
						}
					}

					// set the field
					try {
						field.set(destination, deserialized);
					} catch (Exception e) {
						throw new SerdeException("Could not assign the deserialized value `" + deserialized
								+ "` to the field " + field + ". The original config value was " + value);
					}
				}
			}
			cls = cls.getSuperclass();
		}
	}

	private Object normalizeForDeserialization(Object configValue, List<String> path, Field field) {
		if (configValue == null) {
			// missing value
			throw new SerdeException(
					"Missing configuration entry " + path + " for field `" + field
							+ "` declared in " + field.getDeclaringClass());
		} else if (configValue == NullObject.NULL_OBJECT) {
			// null value
			return null;
		}
		return configValue;
	}

	private String configKey(Field field) {
		SerdeKey keyAnnot = field.getAnnotation(SerdeKey.class);
		return keyAnnot == null ? field.getName() : keyAnnot.value();
	}

	@SuppressWarnings("unchecked")
	private boolean skipField(Field field, Object fieldContainer, Object rawConfigValue) {
		SerdeSkipDeserializingIf annot = field.getAnnotation(SerdeSkipDeserializingIf.class);
		if (annot == null) {
			return false;
		}
		try {
			Predicate<?> skipPredicate = AnnotationProcessor.resolveSkipDeserializingIfPredicate(annot, fieldContainer);
			return ((Predicate<Object>) skipPredicate).test(rawConfigValue);
		} catch (Exception e) {
			throw new SerdeException("Failed to resolve or apply skip predicate for deserialization of field " + field, e);
		}
	}

	private boolean preCheck(Field field) {
		int mods = field.getModifiers();
		if (Modifier.isStatic(mods) || field.isSynthetic()) {
			return false;
		}
		if (Modifier.isTransient(mods) && settings.applyTransientModifier) {
			return false;
		}
		if (Modifier.isFinal(mods) || !Modifier.isPublic(mods)) {
			field.setAccessible(true);
		}
		return true;
	}

}
