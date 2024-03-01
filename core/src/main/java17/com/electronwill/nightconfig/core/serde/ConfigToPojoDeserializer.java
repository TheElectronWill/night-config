package com.electronwill.nightconfig.core.serde;

import java.util.Optional;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * Deserialize a {@code Config} to the fields of a Java object.
 */
final class ConfigToPojoDeserializer
		implements ValueDeserializer<UnmodifiableConfig, Object> {

	@Override
	public Object deserialize(UnmodifiableConfig value, Optional<TypeConstraint> resultType,
			DeserializerContext ctx) {
		if (!resultType.isPresent()) {
			// no constraint, we don't know the type of the POJO!
			// Assume the easiest result: return the value as is
			return value;
		} else {
			TypeConstraint t = resultType.get();
			Class<?> cls = t.getSatisfyingRawType().orElseThrow(() -> new DeserializationException(
					"Could not find a concrete type that can satisfy the constraint " + t));

			if (cls.isRecord()) {
				return deserializeToRecord(value, cls);
			} else {
				return deserializeToNormalClass(value, cls, ctx);
			}
		}
	}

	private Object deserializeToNormalClass(UnmodifiableConfig value, Class<?> cls,
			DeserializerContext ctx) {

		Object instance;
		try {
			Constructor<?> constructor = cls.getDeclaredConstructor();
			if (!Modifier.isPublic(constructor.getModifiers())) {
				constructor.setAccessible(true);
			}
			instance = constructor.newInstance();
		} catch (Exception e) {
			throw new DeserializationException("Failed to create an instance of " + cls, e);
		}
		ctx.deserializeFields(value, instance);
		return instance;
	}

	private Object deserializeToRecord(UnmodifiableConfig value, Class<?> objectClass) {
		var components = objectClass.getRecordComponents();
		var constructor = getCanonicalRecordConstructor(objectClass, components);
		var componentValues = new Object[components.length];
		for (int i = 0; i < components.length; i++) {
			RecordComponent comp = components[i];
			Object configValue = value.getRaw(Collections.singletonList(comp.getName()));
			if (configValue == null) {
				// missing component!
				// find all the missing components to emit a more helpful error message
				List<String> missingComponents = Arrays.stream(components).map(c -> c.getName())
						.filter(c -> !value.contains(c)).collect(Collectors.toList());
				var missingComponentsStr = String.join(", ", missingComponents);
				throw new DeserializationException(
						"Could not deserialize this configuration to a record of type " + objectClass
								+ " because the following components (entries) are missing: "
								+ missingComponentsStr);
			}
			if (configValue == NULL_OBJECT) {
				// component of value null
				configValue = null;
			}
			componentValues[i] = configValue;
		}
		try {
			return constructor.newInstance(componentValues);
		} catch (Exception e) {
			throw new DeserializationException(
					"Failed to create an instance of record " + objectClass, e);
		}
	}

	private static Constructor<?> getCanonicalRecordConstructor(Class<?> cls,
			RecordComponent[] components) {
		Class<?>[] paramTypes = Arrays.stream(components)
				.map(RecordComponent::getType)
				.toArray(Class<?>[]::new);
		try {
			return cls.getDeclaredConstructor(paramTypes);
		} catch (Exception e) {
			throw new DeserializationException(
					"Failed to get the canonical constructor of record " + cls, e);
		}
	}
}
