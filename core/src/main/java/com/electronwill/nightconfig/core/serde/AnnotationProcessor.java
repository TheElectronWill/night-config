package com.electronwill.nightconfig.core.serde;

import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.serde.annotations.*;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipSerializingIf.SkipSerIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeAssert.AssertThat;

import static com.electronwill.nightconfig.core.serde.annotations.SerdePhase.BOTH;
import static com.electronwill.nightconfig.core.serde.annotations.SerdePhase.DESERIALIZING;
import static com.electronwill.nightconfig.core.serde.annotations.SerdePhase.SERIALIZING;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.function.Supplier;
import java.util.*;
import java.util.function.Predicate;

/**
 * Internal class to process serde annotations.
 */
final class AnnotationProcessor {
	// ====== SerdeAssert ======
	/**
	 * Combines all {@link SerdeAssert} annotations that apply to the
	 * {@code currentPhase} into a {@link Predicate}.
	 *
	 * @param annotation      array of {@link SerdeAssert} annotations
	 * @param currentInstance the object that is being (de)serialized
	 * @param currentPhase    the phase to look for
	 * @return a Predicate that combines every applicable assertion, or {@code null}
	 *         if none applies
	 */
	@SuppressWarnings("unchecked")
	static Predicate<?> resolveAssertPredicate(SerdeAssert[] annotations, Object currentInstance,
			SerdePhase currentPhase, Field field) {

		List<Predicate<Object>> predicates = new ArrayList<>(annotations.length);
		for (SerdeAssert annot : annotations) {
			SerdePhase annotPhase = annot.phase();
			AssertThat[] conditions = annot.value();
			SerdeAssertSanityCheck sanityCheck = new SerdeAssertSanityCheck();

			if (annotPhase == currentPhase || annotPhase == SerdePhase.BOTH) {
				for (int i = 0; i < conditions.length; i++) {
					AssertThat condition = conditions[i];
					predicates.add((Predicate<Object>) resolveAssertPredicate1(condition, annot, currentInstance,
							currentPhase, field.getType(), sanityCheck));
				}
			}
			sanityCheck.check(annot);
		}
		return combineAnd(predicates);
	}

	private static class SerdeAssertSanityCheck {
		boolean hasCustomAssert;
		boolean hasCustomParam;

		void check(SerdeAssert annotation) {
			if (hasCustomParam && !hasCustomAssert) {
				throw new SerdeException(String.format(
						"Invalid annotation %s: without AssertThat.CUSTOM, no additional parameter must be specified.",
						annotToString(annotation)));
			}
		}
	}

	private static Predicate<?> resolveAssertPredicate1(AssertThat assertThat,
			SerdeAssert annotation, Object currentInstance, SerdePhase currentPhase, Class<?> fieldType,
			SerdeAssertSanityCheck sanityCheck) {
		Class<?> cls = annotation.customClass();
		String methodOrFieldName = annotation.customCheck();

		if (assertThat == AssertThat.CUSTOM) {
			sanityCheck.hasCustomAssert = true;
			if (methodOrFieldName.isEmpty()) {
				throw new SerdeException(String.format(
						"Invalid annotation %s: with AssertThat.CUSTOM, parameter `customCheck` must be provided and non-empty.",
						annotToString(annotation)));
			}
			return findCustomPredicate("assert predicate", annotation, cls, methodOrFieldName, currentInstance,
					fieldType);
		} else {
			sanityCheck.hasCustomParam = !methodOrFieldName.isEmpty() || cls != Object.class;
			// if a "custom" parameter is specified, don't throw an exception yet, because
			// there may be an
			// AssertThat.CUSTOM somewhere in the list of assertions.

			if (assertThat == AssertThat.NOT_NULL) {
				// SerdeAssert is applied on the Java value (after deserialization / before
				// serialization),
				// so there is no NULL_VALUE.
				return v -> v != null;
			} else if (assertThat == AssertThat.NOT_EMPTY) {
				// returns true if null, false if not isEmpty
				return v -> v == null || !Util.isEmpty(v);
			} else {
				assert false : "missing case";
			}
			return null;
		}
	}

	// ====== SerdeSkipDeserializingIf ======
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static Predicate<?> resolveSkipDeserializingIfPredicate(SerdeSkipDeserializingIf annotation,
			Object currentInstance) {
		SkipDeIf[] conditions = annotation.value();
		Predicate[] predicates = new Predicate[conditions.length];
		for (int i = 0; i < predicates.length; i++) {
			SkipDeIf condition = conditions[i];
			predicates[i] = resolveSkipDeserializingIfPredicate1(condition, annotation, currentInstance, Object.class);
		}
		return combineOr(predicates);
	}

	private static Predicate<?> resolveSkipDeserializingIfPredicate1(SkipDeIf skipIf,
			SerdeSkipDeserializingIf annotation, Object currentInstance, Class<?> configValueType) {
		Class<?> cls = annotation.customClass();
		String methodOrFieldName = annotation.customCheck();

		if (skipIf == SkipDeIf.CUSTOM) {
			if (methodOrFieldName.isEmpty()) {
				throw new SerdeException(String.format(
						"Invalid annotation %s: with SkipDeIf.CUSTOM, parameter `customCheck` must be provided and non-empty.",
						annotToString(annotation)));
			}
			return findCustomPredicate("skip predicate", annotation, cls, methodOrFieldName, currentInstance,
					configValueType);
		} else {
			if (!methodOrFieldName.isEmpty() || cls != Object.class) {
				throw new SerdeException(String.format(
						"Invalid annotation %s: with SkipDeIf.%s, no additional parameter must be specified.",
						annotToString(annotation), skipIf.name()));
			}
			if (skipIf == SkipDeIf.IS_MISSING) {
				return v -> v == null;
			} else if (skipIf == SkipDeIf.IS_NULL) {
				return v -> v == NullObject.NULL_OBJECT;
			} else if (skipIf == SkipDeIf.IS_EMPTY) {
				return v -> v != null && Util.isEmpty(v);
			} else {
				assert false : "missing case";
			}
			return null;
		}
	}

	// ====== SerdeSkipSerializingIf ======
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static Predicate<?> resolveSkipSerializingIfPredicate(SerdeSkipSerializingIf annotation, Object currentInstance,
			Field field) {
		SkipSerIf[] conditions = annotation.value();
		Predicate[] predicates = new Predicate[conditions.length];
		for (int i = 0; i < predicates.length; i++) {
			SkipSerIf condition = conditions[i];
			predicates[i] = resolveSkipSerializingIfPredicate1(condition, annotation, currentInstance, field.getType());
		}
		return combineOr(predicates);
	}

	private static Predicate<?> resolveSkipSerializingIfPredicate1(SkipSerIf skipIf, SerdeSkipSerializingIf annotation,
			Object currentInstance, Class<?> fieldType) {
		Class<?> cls = annotation.customClass();
		String methodOrFieldName = annotation.customCheck();

		if (skipIf == SkipSerIf.CUSTOM) {
			if (methodOrFieldName.isEmpty()) {
				throw new SerdeException(String.format(
						"Invalid annotation %s: with SkipSerIf.CUSTOM, parameter `customCheck` must be provided and non-empty.",
						annotToString(annotation)));
			}
			return findCustomPredicate("skip predicate", annotation, cls, methodOrFieldName, currentInstance,
					fieldType);
		} else {
			if (!methodOrFieldName.isEmpty() || cls != Object.class) {
				throw new SerdeException(String.format(
						"Invalid annotation %s: with SkipSerIf.%s, no additional parameter must be specified.",
						annotToString(annotation), skipIf.name()));
			}
			if (skipIf == SkipSerIf.IS_NULL) {
				return v -> v == null;
			} else if (skipIf == SkipSerIf.IS_EMPTY) {
				return v -> v != null && Util.isEmpty(v);
			} else {
				assert false : "missing case";
			}
			return null;
		}
	}

	private static Predicate<?> pedicateFromField(String label, Field field, Object instance, boolean mustBeStatic) {
		return anyFromField(Predicate.class, label, field, instance, mustBeStatic);
	}

	// ====== SerdeDefault ======
	static EnumMap<SerdePhase, EnumMap<SerdeDefault.WhenValue, SerdeDefault>> getConfigDefaultAnnotations(
			Field field) {
		// init top-level map
		EnumMap<SerdePhase, EnumMap<SerdeDefault.WhenValue, SerdeDefault>> byPhase = new EnumMap<>(
				SerdePhase.class);

		for (SerdeDefault annot : field.getAnnotationsByType(SerdeDefault.class)) {
			// normalize phases: BOTH counts as SERIALIZING and DESERIALIZING
			SerdePhase[] phases;
			if (annot.phase() == BOTH) {
				phases = new SerdePhase[] { BOTH, SERIALIZING, DESERIALIZING };
			} else {
				phases = new SerdePhase[] { annot.phase() };
			}

			// gather all annotation in the two-levels map
			for (SerdePhase phase : phases) {
				// init second-level map
				EnumMap<SerdeDefault.WhenValue, SerdeDefault> byWhen = byPhase.computeIfAbsent(phase,
						p -> new EnumMap<>(SerdeDefault.WhenValue.class));

				// populate with annotations and check for conflict
				for (SerdeDefault.WhenValue when : annot.whenValue()) {
					SerdeDefault conflict = byWhen.put(when, annot);
					if (conflict != null) {
						String msg = String.format(
								"Annotation %s is conflicting with annotation %s on field `%s`. Only one @SerdeDefault must be applicable in a given situation.",
								annotToString(annot), conflict, field);
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
		Class<?>[] noParameters = new Class<?>[] {};
		if (cls == Object.class) {
			// look for the provider in the current instance
			methodOrField = findFieldOrMethodIn(currentInstance.getClass(), methodOrFieldName, true, noParameters);
		} else {
			methodOrField = findFieldOrMethodIn(cls, methodOrFieldName, false, noParameters);
		}

		if (methodOrField == null) {
			String msg = String.format("Default value provider `%s` not found for annotation %s", methodOrFieldName,
					annotToString(annotation));
			throw new SerdeException(msg);
		}
		return (methodOrField instanceof Field)
				? defaultSupplierFromField((Field) methodOrField, currentInstance, cls != Object.class)
				: defaultSupplierFromMethod((Method) methodOrField, currentInstance, cls != Object.class);
	}

	// ====== private utilities ======
	private static <T> Predicate<T> combineOr(Predicate<T>[] predicates) {
		if (predicates.length == 1) {
			return predicates[0];
		} else {
			return o -> {
				for (Predicate<T> p : predicates) {
					if (p.test(o)) {
						return true;
					}
				}
				return false;
			};
		}
	}

	private static <T> Predicate<T> combineAnd(List<Predicate<T>> predicates) {
		if (predicates.isEmpty()) {
			return null;
		}
		if (predicates.size() == 1) {
			return predicates.get(0);
		} else {
			return o -> {
				for (Predicate<T> p : predicates) {
					if (!p.test(o)) {
						return false;
					}
				}
				return true;
			};
		}
	}

	private static Predicate<?> findCustomPredicate(String label, Annotation annotation, Class<?> cls,
			String methodOrFieldName, Object currentInstance, Class<?> predicateParameter) {
		Object methodOrField;
		Class<?>[] methodParameters = new Class<?>[] { predicateParameter };

		// skipIf is CUSTOM, use reflection to find the field or method that we're going
		// to use
		if (cls == Object.class) {
			// look for the predicate in the current instance
			methodOrField = findFieldOrMethodIn(currentInstance.getClass(), methodOrFieldName, true, methodParameters);
		} else {
			// look for the predicate in the specified class
			methodOrField = findFieldOrMethodIn(cls, methodOrFieldName, false, methodParameters);
		}

		if (methodOrField == null) {
			String msg = String.format(
					"Custom %s `%s` not found for annotation %s",
					label,
					methodOrFieldName,
					annotToString(annotation));
			throw new SerdeException(msg);
		}

		return (methodOrField instanceof Field)
				? pedicateFromField(label, (Field) methodOrField, currentInstance, cls != Object.class)
				: predicateFromMethod(label, (Method) methodOrField, currentInstance, cls != Object.class,
						predicateParameter);
	}

	private static Object findFieldOrMethodIn(Class<?> cls, String name, boolean recurse, Class<?>[] methodParameters) {
		boolean methodOnly = false;
		if (name.endsWith("()")) {
			// useful to disambiguate: if a Class has a method and a field with the same
			// name x, "x" will be the field and "x()" will be the method.
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
				return cls.getDeclaredMethod(name, methodParameters);
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
		return anyFromField(Supplier.class, "default value provider", field, instance, mustBeStatic);
	}

	private static Supplier<?> defaultSupplierFromMethod(Method method, Object instance, boolean mustBeStatic) {
		return supplierFromMethod("default value provider", method, instance, mustBeStatic);
	}

	@SuppressWarnings("unchecked")
	private static <T> T anyFromField(Class<T> t, String label, Field field, Object instance, boolean mustBeStatic) {
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
		} else if (!(t.isAssignableFrom(value.getClass()))) {
			throw new SerdeException(String.format(
					"Invalid %s: field `%s` must be of type `%s`.",
					label, field.getName(), t));
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

	private static Predicate<?> predicateFromMethod(String label, Method method, Object instance,
			boolean mustBeStatic, Class<?> parameterType) {
		if (method.getParameterCount() != 1) {
			throw new SerdeException(
					String.format("Invalid %s: method %s should take exactly one parameter of type %s.", label, method,
							parameterType));
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
					return (boolean) method.invoke(null, x);
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
					return (boolean) method.invoke(instance, x);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new SerdeException(
							String.format("Could not invoke the %s `%s` on object %s", label, method, instance), e);
				}
			};
		}
	}

	// ====== Printing ======
	static String annotToString(Annotation annotation) {
		return annotation.toString().replace("@com.electronwill.nightconfig.core.serde.annotations.", "@");
	}
}
