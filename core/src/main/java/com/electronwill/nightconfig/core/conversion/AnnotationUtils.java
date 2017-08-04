package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.utils.StringUtils;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utility class for the annotations.
 *
 * @author TheElectronWill
 */
final class AnnotationUtils {
	private AnnotationUtils() {}

	/**
	 * Checks if an annotated element is annotated with {@link PreserveNotNull}.
	 */
	static boolean hasPreserveNotNull(AnnotatedElement annotatedElement) {
		return annotatedElement.isAnnotationPresent(PreserveNotNull.class);
	}

	/**
	 * Checks if a field or its class is annotated with {@link PreserveNotNull}
	 */
	static boolean mustPreserve(Field field, Class<?> fieldClass) {
		return hasPreserveNotNull(field) || hasPreserveNotNull(fieldClass);
	}

	/**
	 * Creates and returns an instance of the converter specified by the @Conversion annotation
	 * of the field. If there is no @Conversion annotation, returns {@code null}.
	 *
	 * @return the field's converter, or {@code null} if there is none
	 */
	static Converter<Object, Object> getConverter(Field field) {
		Conversion conversion = field.getAnnotation(Conversion.class);
		if (conversion != null) {
			try {
				Constructor<? extends Converter> constructor = conversion.value()
																		 .getDeclaredConstructor();
				if (!constructor.isAccessible()) {
					constructor.setAccessible(true);
				}
				return (Converter<Object, Object>)constructor.newInstance();
			} catch (ReflectiveOperationException ex) {
				throw new ReflectionException("Cannot create a converter for field " + field, ex);
			}
		}
		return null;
	}

	/**
	 * Gets the path of a field: returns the annotated path, or the field's name if there is no
	 * annotated path.
	 *
	 * @return the annotated path, if any, or the field name
	 */
	static List<String> getPath(Field field) {
		List<String> annotatedPath = getPath((AnnotatedElement)field);
		return (annotatedPath == null) ? Collections.singletonList(field.getName()) : annotatedPath;
	}

	/**
	 * Gets the annotated path (specified with @Path or @AdvancedPath) of an annotated element.
	 *
	 * @return the annotated path, or {@code null} if there is none.
	 */
	static List<String> getPath(AnnotatedElement annotatedElement) {
		Path path = annotatedElement.getDeclaredAnnotation(Path.class);
		if (path != null) {
			return StringUtils.split(path.value(), '.');
		}
		AdvancedPath advancedPath = annotatedElement.getDeclaredAnnotation(AdvancedPath.class);
		if (advancedPath != null) {
			return Arrays.asList(advancedPath.value());
		}
		return null;
	}

	/**
	 * Checks that the value of a field corresponds to its spec annotation, if any.
	 * <p>
	 * The check should apply to the field's value, not the config value. That is, when
	 * converting a field to a config value, the check should apply before the conversion
	 * [fieldValue -> configValue] and, when converting a config value to a field, the check
	 * should apply after the conversion [configValue -> fieldValue].
	 *
	 * @param field the field to check
	 * @param value the field's value
	 */
	static void checkField(Field field, Object value) {
		//--- Misc checks ---
		SpecNotNull specNotNull = field.getDeclaredAnnotation(SpecNotNull.class);
		if (specNotNull != null) {
			checkNotNull(field, value);
			return;
		}
		SpecClassInArray specClassInArray = field.getDeclaredAnnotation(SpecClassInArray.class);
		if (specClassInArray != null) {
			checkFieldSpec(field, value, specClassInArray);
			return;
		}

		//--- String checks ---
		SpecStringInArray specStringInArray = field.getDeclaredAnnotation(SpecStringInArray.class);
		if (specStringInArray != null) {
			checkFieldSpec(field, value, specStringInArray);
			return;
		}
		SpecStringInRange specStringInRange = field.getDeclaredAnnotation(SpecStringInRange.class);
		if (specStringInRange != null) {
			checkFieldSpec(field, value, specStringInRange);
			return;
		}

		//--- Primitive checks ---
		SpecDoubleInRange specDoubleInRange = field.getDeclaredAnnotation(SpecDoubleInRange.class);
		if (specDoubleInRange != null) {
			checkFieldSpec(field, value, specDoubleInRange);
			return;
		}
		SpecFloatInRange specFloatInRange = field.getDeclaredAnnotation(SpecFloatInRange.class);
		if (specFloatInRange != null) {
			checkFieldSpec(field, value, specFloatInRange);
			return;
		}
		SpecLongInRange specLongInRange = field.getDeclaredAnnotation(SpecLongInRange.class);
		if (specLongInRange != null) {
			checkFieldSpec(field, value, specLongInRange);
			return;
		}
		SpecIntInRange specIntInRange = field.getDeclaredAnnotation(SpecIntInRange.class);
		if (specIntInRange != null) {
			checkFieldSpec(field, value, specIntInRange);
		}

		// --- Custom check with a validator --
		SpecValidator specValidator = field.getDeclaredAnnotation(SpecValidator.class);
		if (specValidator != null) {
			checkFieldSpec(field, value, specValidator);
		}
	}

	private static void checkFieldSpec(Field field, Object value, SpecValidator spec) {
		final Predicate<Object> validatorInstance;
		try {
			Constructor<? extends Predicate<Object>> constructor = spec.value()
																	   .getDeclaredConstructor();
			constructor.setAccessible(true);
			validatorInstance = constructor.newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new ReflectionException("Cannot create a converter for field " + field, ex);
		}
		if (!validatorInstance.test(value)) {
			throw new InvalidValueException(
					"Invalid value \"%s\" for field %s: it doesn't conform to " + "%s", value,
					field, spec);
		}
	}

	private static void checkFieldSpec(Field field, Object value, SpecClassInArray spec) {
		checkNotNull(field, value);
		final Class<?> valueClass = value.getClass();
		if (spec.strict()) {
			for (Class<?> aClass : spec.value()) {
				if (aClass.isAssignableFrom(valueClass)) { return; }
			}
		} else {
			for (Class<?> aClass : spec.value()) {
				if (aClass.equals(valueClass)) { return; }
			}
		}
		throw new InvalidValueException(
				"Invalid value \"%s\" for field %s: it doesn't conform to " + "%s", value, field,
				spec);
	}

	private static void checkFieldSpec(Field field, Object value, SpecStringInRange spec) {
		checkClass(field, value, String.class);
		String s = (String)value;
		if (s.compareTo(spec.min()) < 0 || s.compareTo(spec.max()) > 0) {
			throw new InvalidValueException(
					"Invalid value \"%s\" for field %s: it doesn't conform to %s", value, field,
					spec);
		}
	}

	private static void checkFieldSpec(Field field, Object value, SpecStringInArray spec) {
		checkClass(field, value, String.class);
		String s = (String)value;
		if (spec.ignoreCase()) {
			for (String acceptable : spec.value()) {
				if (s.equalsIgnoreCase(acceptable)) { return; }
			}
		} else {
			for (String acceptable : spec.value()) {
				if (s.equals(acceptable)) { return; }
			}
		}
		throw new InvalidValueException(
				"Invalid value \"%s\" for field %s: it doesn't conform to %s", value, field, spec);
	}

	private static void checkFieldSpec(Field field, Object value, SpecDoubleInRange spec) {
		checkClass(field, value, Double.class);
		double d = (double)value;
		if (d < spec.min() || d > spec.max()) {
			throw new InvalidValueException(
					"Invalid value %f for field %s: it doesn't conform to %s", value, field, spec);
		}
	}

	private static void checkFieldSpec(Field field, Object value, SpecFloatInRange spec) {
		checkClass(field, value, Float.class);
		float d = (float)value;
		if (d < spec.min() || d > spec.max()) {
			throw new InvalidValueException(
					"Invalid value %f for field %s: it doesn't conform to %s", value, field, spec);
		}
	}

	private static void checkFieldSpec(Field field, Object value, SpecLongInRange spec) {
		checkClass(field, value, Long.class);
		long d = (long)value;
		if (d < spec.min() || d > spec.max()) {
			throw new InvalidValueException(
					"Invalid value %d for field %s: it doesn't conform to %s", value, field, spec);
		}
	}

	private static void checkFieldSpec(Field field, Object value, SpecIntInRange spec) {
		checkClass(field, value, Integer.class);
		int d = (int)value;
		if (d < spec.min() || d > spec.max()) {
			throw new InvalidValueException(
					"Invalid value %d for field %s: it doesn't conform to %s", value, field, spec);
		}
	}

	private static void checkNotNull(Field field, Object value) {
		if (value == null) {
			throw new InvalidValueException("Invalid null value for field %s", field);
		}
	}

	private static void checkClass(Field field, Object value, Class<?> expectedClass) {
		checkNotNull(field, value);
		Class<?> valueClass = value.getClass();
		if (valueClass != expectedClass) {
			throw new InvalidValueException("Invalid type %s for field %s, expected %s", valueClass,
											field, expectedClass);
		}
	}
}