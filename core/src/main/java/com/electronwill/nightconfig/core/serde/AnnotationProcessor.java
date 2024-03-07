package com.electronwill.nightconfig.core.serde;

import com.electronwill.nightconfig.core.serde.annotations.SerdeDefault;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipSerializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipSerializingIf.SkipIf;

import static com.electronwill.nightconfig.core.serde.annotations.SerdeDefault.SerdePhase.BOTH;
import static com.electronwill.nightconfig.core.serde.annotations.SerdeDefault.SerdePhase.DESERIALIZING;
import static com.electronwill.nightconfig.core.serde.annotations.SerdeDefault.SerdePhase.SERIALIZING;

import java.lang.reflect.*;
import java.util.EnumMap;
import java.util.function.Supplier;
import java.util.function.Predicate;

/**
 * Internal class to process serde annotations.
 */
final class AnnotationProcessor {
	// ====== SerdeSkipSerializingIf ======
	static Predicate<?> resolveSkipSerializingIfPredicate(SerdeSkipSerializingIf annotation, Object currentInstance) {
		SkipIf skipIf = annotation.value();
		Class<?> cls = annotation.customClass();
		String methodOrFieldName = annotation.customCheck();

		Object methodOrField;
		if (skipIf == SerdeSkipSerializingIf.SkipIf.CUSTOM) {
			if (methodOrFieldName.isEmpty()) {
				throw new SerdeException(String.format("Invalid annotation %s: with SkipIf.CUSTOM, parameter `customCheck` must be provided and non-empty.", annotation));
			}
		} else {
			if (!methodOrFieldName.isEmpty() || cls != Object.class) {
				throw new SerdeException(String.format("Invalid annotation %s: with SkipIf.%s, no additional parameter must be specified.", annotation, skipIf.name()));
			}
			if (skipIf == SkipIf.IS_NULL) {
				return v -> v == null;
			} else if (skipIf == SkipIf.IS_EMPTY) {
				return Util::isEmpty;
			} else {
				assert false : "missing case";
			}
		}

		// skipIf is CUSTOM, use reflection to find the field or method that we're going to use
		if (cls == Object.class) {
			// look for the predicate in the current instance
			methodOrField = findFieldOrMethodIn(currentInstance.getClass(), methodOrFieldName, true);
		} else {
			// look for the predicate in the specified class
			methodOrField = findFieldOrMethodIn(cls, methodOrFieldName, false);
		}

		if (methodOrField == null) {
			String msg = String.format("Custom skip predicate `%s` not found for annotation %s", methodOrFieldName,
					annotation);
			throw new SerdeException(msg);
		}

		return (methodOrField instanceof Field)
				? skipPredicateFromField((Field) methodOrField, currentInstance, cls != Object.class)
				: skipPredicateFromMethod((Method) methodOrField, currentInstance, cls != Object.class);
	}

	private static Predicate<?> skipPredicateFromField(Field field, Object instance, boolean mustBeStatic) {
		return anyFromField("skip predicate", field, instance, mustBeStatic);
	}

	private static Predicate<?> skipPredicateFromMethod(Method method, Object instance, boolean mustBeStatic) {
		return predicateFromMethod("skip predicate", method, instance, mustBeStatic);
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
				? defaultSupplierFromField((Field) methodOrField, currentInstance, cls != Object.class)
				: defaultSupplierFromMethod((Method) methodOrField, currentInstance, cls != Object.class);
	}

	// ====== private utilities ======
	private static Object findFieldOrMethodIn(Class<?> cls, String name, boolean recurse) {
		boolean methodOnly = false;
		if (name.endsWith("()")) {
			// useful to disambiguate: if a Class has a method and a field with the same name x,
			// "x" will be the field and "x()" will be the method.
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

	private static Supplier<?> defaultSupplierFromField(Field field, Object instance, boolean mustBeStatic) {
		return anyFromField("default value provider", field, instance, mustBeStatic);
	}

	private static Supplier<?> defaultSupplierFromMethod(Method method, Object instance, boolean mustBeStatic) {
		return supplierFromMethod("default value provider", method, instance, mustBeStatic);
	}

	@SuppressWarnings("unchecked")
	private static <T> T anyFromField(String label, Field field, Object instance, boolean mustBeStatic) {
		int mods = field.getModifiers();
		if (!Modifier.isPublic(mods)) {
			field.setAccessible(true);
		}
		if (mustBeStatic && !Modifier.isStatic(mods)) {
			String msg = String.format("Invalid %s: field %s should be declared as static.", label, field);
			throw new SerdeException(msg);
		}

		Object value;
		try {
			value = field.get(instance);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			String msg = String.format("Could not read the %s `%s` on object `%s`.", label, field,
					instance);
			throw new SerdeException(msg, e);
		}

		if (value == null) {
			throw new SerdeException(
					String.format("Invalid %s: field `%s` is null in object `%s`.", label, field,
							instance));
		} else if (!(value instanceof Supplier)) {
			throw new SerdeException(String.format(
					"Invalid %s: field `%s` must be of type `java.util.function.Supplier`.",
					label, field.getName(), instance));
		}

		return (T) value;
	}

	private static Supplier<?> supplierFromMethod(String label, Method method, Object instance, boolean mustBeStatic) {
		if (method.getParameterCount() > 0) {
			throw new SerdeException(
					String.format("Invalid %s: method %s should take no parameter.", label, method));
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
					throw new SerdeException(String.format("Could not invoke the %s `%s`", label, e));
				}
			};
		} else {
			if (mustBeStatic) {
				String msg = String.format("Invalid %s: method %s should be declared as static.",
						label, method);
				throw new SerdeException(msg);
			}
			return () -> {
				try {
					return method.invoke(instance);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new SerdeException(
							String.format("Could not invoke the %s `%s` on object %s", label, method, instance), e);
				}
			};
		}
	}

	private static Predicate<?> predicateFromMethod(String label, Method method, Object instance, boolean mustBeStatic) {
		if (method.getParameterCount() > 0) {
			throw new SerdeException(
					String.format("Invalid %s: method %s should take no parameter.", label, method));
		}
		if (method.getReturnType() != Boolean.TYPE) {
			throw new SerdeException(String.format("Invalid %s: method %s should return a boolean.", label, method));
		}

		int mods = method.getModifiers();
		if (!Modifier.isPublic(mods)) {
			method.setAccessible(true);
		}

		if (Modifier.isStatic(mods)) {
			return x -> {
				try {
					return (boolean)method.invoke(null, x);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new SerdeException(String.format("Could not invoke the %s `%s`", label, e));
				}
			};
		} else {
			if (mustBeStatic) {
				String msg = String.format("Invalid %s: method %s should be declared as static.",
						label, method);
				throw new SerdeException(msg);
			}
			return x -> {
				try {
					return (boolean)method.invoke(instance, x);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new SerdeException(
							String.format("Could not invoke the %s `%s` on object %s", label, method, instance), e);
				}
			};
		}
	}
}
