package com.electronwill.nightconfig.core.check;

import com.electronwill.nightconfig.core.EnumGetMethod;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

/**
 * Checks configuration values, with {@link #check(Object)}.
 */
@FunctionalInterface
public interface ValueChecker {
	/**
	 * Checks that a value is correct.
	 *
	 * @param value the value to check, may be null
	 * @return true if it's correct, false if it's incorrect.
	 */
	boolean check(Object value);

	default ValueChecker and(ValueChecker other) {
		return value -> this.check(value) && other.check(value);
	}

	default ValueChecker or(ValueChecker other) {
		return value -> this.check(value) || other.check(value);
	}

	// --- STATIC METHODS ---
	static ValueChecker any(ValueChecker elementChecker) {
		return value -> {
			if (value instanceof Iterable) {
				for (Object elem : (Iterable<?>)value) {
					if (elementChecker.check(elem)) {
						return true;
					}
				}
			} else if (value != null && value.getClass().isArray()) {
				int len = Array.getLength(value);
				for (int i = 0; i < len; i++) {
					if (elementChecker.check(Array.get(value, i))) {
						return true;
					}
				}
			}
			return false;
		};
	}

	static ValueChecker each(ValueChecker elementChecker) {
		return value -> {
			if (value instanceof Iterable) {
				for (Object elem : (Iterable<?>)value) {
					if (!elementChecker.check(elem)) {
						return false;
					}
				}
			} else if (value != null && value.getClass().isArray()) {
				int len = Array.getLength(value);
				for (int i = 0; i < len; i++) {
					if (!elementChecker.check(Array.get(value, i))) {
						return false;
					}
				}
			}
			return true;
		};
	}

	static ValueChecker assignableTo(Class<?> cls) {
		return value -> value != null && cls.isAssignableFrom(value.getClass());
	}

	static ValueChecker assignableTo(Class<?>... acceptableClasses) {
		return value -> {
			if (value == null) {
				return false;
			}
			Class<?> vclass = value.getClass();
			for (Class<?> cls : acceptableClasses) {
				if (cls.isAssignableFrom(vclass)) {
					return true;
				}
			}
			return false;
		};
	}

	static ValueChecker assignableToNullable(Class<?> cls) {
		return value -> value == null || cls.isAssignableFrom(value.getClass());
	}

	static <T extends Comparable<T>> ValueChecker closedRange(T min, T max) {
		return range(min, max, true, true);
	}

	static <T extends Comparable<T>> ValueChecker openRange(T min, T max) {
		return range(min, max, false, false);
	}

	static <T extends Comparable<T>> ValueChecker leftRange(T excludedMin, T includedMax) {
		return range(excludedMin, includedMax, true, false);
	}

	static <T extends Comparable<T>> ValueChecker rightRange(T includedMin, T excludedMax) {
		return range(includedMin, excludedMax, false, true);
	}

	@SuppressWarnings("unchecked")
	static <T extends Comparable<T>> ValueChecker range(T min, T max, boolean inclusiveMin,
														boolean inclusiveMax) {
		return value -> {
			if (value == null) {
				return false;
			}
			try {
				T tvalue = (T)value;
				int compMin = min.compareTo(tvalue);
				int compMax = max.compareTo(tvalue);
				boolean okMin = inclusiveMin ? compMin <= 0 : compMin < 0;
				boolean okMax = inclusiveMax ? compMax >= 0 : compMax > 0;
				return okMin && okMax;
			} catch (ClassCastException ex) {
				return false; // the value is of the wrong type
			}
		};
	}

	static ValueChecker isRef(Object reference) {
		return v -> v == reference;
	}

	static ValueChecker equalTo(Object to) {
		assert to != null;
		return to::equals;
	}

	static ValueChecker refIn(Object... acceptableValues) {
		return value -> any(a -> a == value).check(acceptableValues);
	}

	static ValueChecker refIn(Iterable<?> acceptableValues) {
		return value -> any(a -> a == value).check(acceptableValues);
	}

	static ValueChecker containedIn(Object... acceptableValues) {
		return value -> any(a -> Objects.equals(value, a)).check(acceptableValues);
	}

	static ValueChecker containedIn(Iterable<?> acceptableValues) {
		return value -> any(a -> Objects.equals(value, a)).check(acceptableValues);
	}

	static <T extends Enum<T>> ValueChecker enumeration(Class<T> enumClass, EnumGetMethod method) {
		return value -> method.validate(value, enumClass);
	}
}
