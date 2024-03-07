package com.electronwill.nightconfig.core.serde;

import com.electronwill.nightconfig.core.serde.annotations.SerdeDefault;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipSerializingIf;

import static com.electronwill.nightconfig.core.serde.annotations.SerdeDefault.SerdePhase.BOTH;
import static com.electronwill.nightconfig.core.serde.annotations.SerdeDefault.SerdePhase.DESERIALIZING;
import static com.electronwill.nightconfig.core.serde.annotations.SerdeDefault.SerdePhase.SERIALIZING;

import java.lang.reflect.*;
import java.util.EnumMap;
import java.util.function.Supplier;
import java.util.function.Predicate;

final class AnnotationProcessor {
	// ====== SerdeSkipSerializingIf ======
	static Predicate<?> resolveSkipSerializingIfPredicate(SerdeSkipSerializingIf annotation, Object currentInstance) {
		// TODO
		return null;
	}

	// ====== SerdeDefault ======
	static EnumMap<SerdeDefault.SerdePhase, EnumMap<SerdeDefault.WhenValue, SerdeDefault>> getConfigDefaultAnnotations(Field field) {
		// init top-level map
		EnumMap<SerdeDefault.SerdePhase, EnumMap<SerdeDefault.WhenValue, SerdeDefault>> byPhase = new EnumMap<>(SerdeDefault.SerdePhase.class);

		for (SerdeDefault annot : field.getAnnotationsByType(SerdeDefault.class)) {
			// normalize phases: BOTH counts as SERIALIZING and DESERIALIZING
			SerdeDefault.SerdePhase[] phases;
			if (annot.phase() == BOTH) {
				phases = new SerdeDefault.SerdePhase[] { BOTH, SERIALIZING, DESERIALIZING };
			} else {
				phases = new SerdeDefault.SerdePhase[] { annot.phase() };
			}

			// gather all annotation in the two-levels map
			for (SerdeDefault.SerdePhase phase : phases) {
				// init second-level map
				EnumMap<SerdeDefault.WhenValue, SerdeDefault> byWhen = byPhase.computeIfAbsent(phase,
						p -> new EnumMap<>(SerdeDefault.WhenValue.class));

				// populate with annotations and check for conflict
				for (SerdeDefault.WhenValue when : annot.whenValue()) {
					SerdeDefault conflict = byWhen.put(when, annot);
					if (conflict != null) {
						String msg = String.format(
								"Annotation %s is conflicting with annotation %s on field `%s`. Only one @ConfigDefault must be applicable in a given situation.",
								annot, conflict, field);
						// TODO provide a javassist module to enrich the error messages with line
						// numbers
						throw new SerdeException(msg);
					}
				}
			}
		}
		return byPhase;
	}

	static Supplier<?> resolveConfigDefaultProvider(SerdeDefault annotation, Object currentInstance) {
		Class<?> cls = annotation.cls();
		String methodOrFieldName = annotation.provider();
		Object methodOrField;
		if (cls == Object.class) {
			// look for the provider in the current instance
			methodOrField = findFieldOrMethodIn(currentInstance.getClass(), methodOrFieldName, true);
		} else {
			methodOrField = findFieldOrMethodIn(cls, methodOrFieldName, false);
		}

		if (methodOrField == null) {
			String msg = String.format("Default value provider `%s` not found for annotation %s", methodOrFieldName,
					annotation);
			throw new SerdeException(msg);
		}
		return (methodOrField instanceof Field)
				? supplierFromField((Field) methodOrField, currentInstance, cls != Object.class)
				: supplierFromMethod((Method) methodOrField, currentInstance, cls != Object.class);
	}

	private static Object findFieldOrMethodIn(Class<?> cls, String name, boolean recurse) {
		boolean methodOnly = false;
		if (name.endsWith("()")) {
			methodOnly = true;
			name = name.substring(0, name.length() - 2);
		}

		do {
			// try field first, unless methodOnly is true
			if (!methodOnly) {
				try {
					return cls.getDeclaredField(name);
				} catch (NoSuchFieldException e) {
					// ignore
				}
			}
			// try method (no parameter)
			try {
				return cls.getDeclaredMethod(name);
			} catch (NoSuchMethodException e) {
				// ignore
			}

			if (!recurse) {
				break;
			}
			cls = cls.getSuperclass();
		} while (cls != Object.class);
		return null;
	}

	private static Supplier<?> supplierFromField(Field field, Object instance, boolean mustBeStatic) {
		int mods = field.getModifiers();
		if (!Modifier.isPublic(mods)) {
			field.setAccessible(true);
		}
		if (mustBeStatic && !Modifier.isStatic(mods)) {
			String msg = String.format("Invalid default value provider: field %s should be declared as static.", field);
			throw new SerdeException(msg);
		}

		Object value;
		try {
			value = field.get(instance);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			String msg = String.format("Could not read the default value provider `%s` on object `%s`.", field,
					instance);
			throw new SerdeException(msg, e);
		}

		if (value == null) {
			throw new SerdeException(
					String.format("Invalid default value provider: field `%s` is null in object `%s`.", field,
							instance));
		} else if (!(value instanceof Supplier)) {
			throw new SerdeException(String.format(
					"Invalid default value provider: field `%s` must be of type `java.util.function.Supplier`.",
					field.getName(), instance));
		}

		return (Supplier<?>) value;
	}

	private static Supplier<?> supplierFromMethod(Method method, Object instance, boolean mustBeStatic) {
		if (method.getParameterCount() > 0) {
			throw new SerdeException(
					String.format("Invalid default value supplied: method %s should take no parameter.", method));
		}

		int mods = method.getModifiers();
		if (!Modifier.isPublic(mods)) {
			method.setAccessible(true);
		}

		if (Modifier.isStatic(mods)) {
			return () -> {
				try {
					return method.invoke(null);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new SerdeException("Could not invoke the static default value provider `" + method + "`", e);
				}
			};
		} else {
			if (mustBeStatic) {
				String msg = String.format("Invalid default value provider: method %s should be declared as static.",
						method);
				throw new SerdeException(msg);
			}
			return () -> {
				try {
					return method.invoke(instance);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new SerdeException(
							"Could not invoke the default value provider `" + method + "` on object " + instance, e);
				}
			};
		}
	}
}
