package com.electronwill.nightconfig.core.conversion;

import java.lang.reflect.Field;

/**
 * Utility class for the @Spec(something) annotations.
 *
 * @author TheElectronWill
 */
final class AnnotationSpecs {
	private AnnotationSpecs() {}

	/**
	 * Checks that the value of a field corresponds to its spec annotation, if any.
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
	}

	private static void checkFieldSpec(Field field, Object value, SpecClassInArray spec) {
		checkNotNull(field, value);
		final Class<?> valueClass = value.getClass();
		if (spec.strict()) {
			for (Class<?> aClass : spec.acceptableClasses()) {
				if (aClass.isAssignableFrom(valueClass)) { return; }
			}
		} else {
			for (Class<?> aClass : spec.acceptableClasses()) {
				if (aClass.equals(valueClass)) { return; }
			}
		}
		throw new InvalidValueException(
				"Invalid value \"%s\" for field %s: it doesn't conform to " + "%s", value, spec);
	}

	private static void checkFieldSpec(Field field, Object value, SpecStringInRange spec) {
		checkClass(field, value, String.class);
		String s = (String)value;
		if (s.compareTo(spec.min()) < 0 || s.compareTo(spec.max()) > 0) {
			throw new InvalidValueException(
					"Invalid value \"%s\" for field %s: it doesn't conform to %s", value, spec);
		}
	}

	private static void checkFieldSpec(Field field, Object value, SpecStringInArray spec) {
		checkClass(field, value, String.class);
		String s = (String)value;
		if (spec.ignoreCase()) {
			for (String acceptable : spec.acceptableValues()) {
				if (s.equalsIgnoreCase(acceptable)) { return; }
			}
		} else {
			for (String acceptable : spec.acceptableValues()) {
				if (s.equals(acceptable)) { return; }
			}
		}
		throw new InvalidValueException(
				"Invalid value \"%s\" for field %s: it doesn't conform to %s", value, spec);
	}

	private static void checkFieldSpec(Field field, Object value, SpecDoubleInRange spec) {
		checkClass(field, value, Double.class);
		double d = (double)value;
		if (d < spec.min() || d > spec.max()) {
			throw new InvalidValueException(
					"Invalid value %f for field %s: it doesn't conform to %s", value, spec);
		}
	}

	private static void checkFieldSpec(Field field, Object value, SpecFloatInRange spec) {
		checkClass(field, value, Float.class);
		float d = (float)value;
		if (d < spec.min() || d > spec.max()) {
			throw new InvalidValueException(
					"Invalid value %f for field %s: it doesn't conform to %s", value, spec);
		}
	}

	private static void checkFieldSpec(Field field, Object value, SpecLongInRange spec) {
		checkClass(field, value, Long.class);
		long d = (long)value;
		if (d < spec.min() || d > spec.max()) {
			throw new InvalidValueException(
					"Invalid value %d for field %s: it doesn't conform to %s", value, spec);
		}
	}

	private static void checkFieldSpec(Field field, Object value, SpecIntInRange spec) {
		checkClass(field, value, Integer.class);
		int d = (int)value;
		if (d < spec.min() || d > spec.max()) {
			throw new InvalidValueException(
					"Invalid value %d for field %s: it doesn't conform to %s", value, spec);
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