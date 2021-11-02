package com.electronwill.nightconfig.core.check;

import com.electronwill.nightconfig.core.EnumGetMethod;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
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

	/** Checks {@code this and other} */
	default ValueChecker and(ValueChecker other) {
		return value -> this.check(value) && other.check(value);
	}

	/** Checks {@code this or other} */
	default ValueChecker or(ValueChecker other) {
		return value -> this.check(value) || other.check(value);
	}

	/** Checks {@code this xor other}, ie {@code (this and not other) or (not this and other)}. */
	default ValueChecker xor(ValueChecker other) {
		return value -> this.check(value) ^ other.check(value);
	}

	/** Checks {@code not other} */
	static ValueChecker not(ValueChecker other) {
		return value -> !other.check(value);
	}

	// --- STATIC METHODS ---

	/**
	 * Checks that there exist at last one correct element in the iterable or array.
	 *
	 * @param elementChecker check for one element
	 * @return a ValueChecker that returns "correct" if the value is an iterable or an array
	 * and if at least one element is correct
	 */
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

	/**
	 * Checks that all the elements are correct in the iterable or array.
	 *
	 * @param elementChecker check for one element
	 * @return a ValueChecker that returns "correct" if the value is an iterable or an array
	 * and if all its elements are correct
	 */
	static ValueChecker all(ValueChecker elementChecker) {
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

	/**
	 * Checks that the value's class is correct.
	 *
	 * @param cls accepted class
	 * @return a ValueChecker that returns "correct" if the value is not null and of class cls
	 */
	static ValueChecker ofExactClass(Class<?> cls) {
		return value -> value != null && value.getClass() == cls;
	}

	/**
	 * Checks that the value is <b>not null</b> and assignable to a specific class.
	 *
	 * @param cls class to check
	 * @return a ValueChecker that returns "correct" if the value is not null and assignable to cls
	 * @see Class#isAssignableFrom(Class)
	 */
	static ValueChecker assignableTo(Class<?> cls) {
		return value -> value != null && cls.isAssignableFrom(value.getClass());
	}

	/**
	 * Checks that the value is <b>not null</b> and assignable to one of the given classes.
	 *
	 * @param acceptableClasses classes to check
	 * @return a ValueChecker that returns "correct" if the value is not null and assignable to one of the given classes
	 * @see Class#isAssignableFrom(Class)
	 */
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

	/**
	 * Checks that the value is null OR assignable to a specific class.
	 *
	 * @param cls class to check
	 * @return a ValueChecker that returns "correct" if the value is null OR assignable to cls
	 * @see Class#isAssignableFrom(Class)
	 */
	static ValueChecker assignableToNullable(Class<?> cls) {
		return value -> value == null || cls.isAssignableFrom(value.getClass());
	}

	/**
	 * Checks that the value is in range {@code [min,max]}.
	 *
	 * @param min minimum value, inclusive
	 * @param max maximum value, inclusive
	 * @param <T> value type
	 * @return a ValueChecker that returns "correct" if the value is in the range
	 */
	static <T extends Comparable<T>> ValueChecker inClosedRange(T min, T max) {
		return inRange(min, max, true, true);
	}

	/**
	 * Checks that the value is in range {@code ]min,max[}.
	 *
	 * @param min minimum value, exclusive
	 * @param max maximum value, exclusive
	 * @param <T> value type
	 * @return a ValueChecker that returns "correct" if the value is in the range
	 */
	static <T extends Comparable<T>> ValueChecker inOpenRange(T min, T max) {
		return inRange(min, max, false, false);
	}

	/**
	 * Checks that the value is in range {@code [min,max[} (left-closed).
	 *
	 * @param inclusiveMin minimum value, inclusive
	 * @param exclusiveMax maximum value, exclusive
	 * @param <T> value type
	 * @return a ValueChecker that returns "correct" if the value is in the range
	 */
	static <T extends Comparable<T>> ValueChecker inLeftRange(T inclusiveMin, T exclusiveMax) {
		return inRange(inclusiveMin, exclusiveMax, true, false);
	}

	/**
	 * Checks that the value is in range {@code ]min,max]} (right-closed).
	 *
	 * @param exclusiveMin minimum value, exclusive
	 * @param inclusiveMax maximum value, inclusive
	 * @param <T> value type
	 * @return a ValueChecker that returns "correct" if the value is in the range
	 */
	static <T extends Comparable<T>> ValueChecker inRightRange(T exclusiveMin, T inclusiveMax) {
		return inRange(exclusiveMin, inclusiveMax, false, true);
	}

	/**
	 * Checks that the value is in the given range, according to {@link Comparable#compareTo(Object)}.
	 */
	static <T extends Comparable<T>> ValueChecker inRange(T min, T max, boolean inclusiveMin, boolean inclusiveMax) {
		return inRange(min, max, inclusiveMin, inclusiveMax, Comparator.naturalOrder());
	}

	/**
	 * Checks that the value is in the given range, according to {@link Comparator#compare(Object, Object)}.
	 */
	@SuppressWarnings("unchecked")
	static <T> ValueChecker inRange(T min, T max, boolean inclusiveMin, boolean inclusiveMax, Comparator<T> comparator) {
		return value -> {
			if (value == null) {
				return false;
			}
			try {
				T tvalue = (T)value;
				int compMin = comparator.compare(min, tvalue);
				if (compMin > 0 || compMin == 0 && !inclusiveMin) {
					return false;
				}
				int compMax = comparator.compare(max, tvalue);
				return compMax > 0 || compMax == 0 && inclusiveMax;
			} catch (ClassCastException ex) {
				return false; // the value is of the wrong type
			}
		};
	}

	/**
	 * Checks that the value refers to a specific object.
	 *
	 * @param reference the correct reference
	 * @return a ValueChecker that returns "correct" if the value refers to the same object,
	 * ie if {@code value == reference}
	 */
	static ValueChecker sameRefAs(Object reference) {
		return v -> v == reference;
	}

	/**
	 * Checks that the value is equal to something, according to the {@link Object#equals(Object)} method.
	 * It is assumed that the {@code equals} method of the given object is symmetric.
	 *
	 * @param to the correct object
	 * @return a ValueChecker that returns "correct" if the value is non-null and equal to the given object
	 */
	static ValueChecker equalTo(Object to) {
		Objects.requireNonNull(to);
		return to::equals;
	}

	/**
	 * Checks that the value refers to one of the given objects.
	 *
	 * @param acceptableValues the acceptable references
	 * @return a ValueChecker that returns "correct" if {@code value == o} for at least one acceptable value o.
	 */
	static ValueChecker sameRefAsAny(Object... acceptableValues) {
		return value -> any(a -> a == value).check(acceptableValues);
	}

	/**
	 * Checks that the value refers to one of the given objects.
	 *
	 * @param acceptableValues the acceptable references
	 * @return a ValueChecker that returns "correct" if {@code value == o} for at least one acceptable value o.
	 */
	static ValueChecker sameRefAsAny(Iterable<?> acceptableValues) {
		return value -> any(a -> a == value).check(acceptableValues);
	}

	/**
	 * Checks that the value is equal to one of the given objects.
	 * It is assumed that the {@code equals} method of the given object is symmetric.
	 *
	 * @param acceptableValues the acceptable references
	 * @return a ValueChecker that returns "correct" if {@code Objects.equals(value,o)} for at least one acceptable value o.
	 */
	static ValueChecker containedIn(Object... acceptableValues) {
		return value -> any(a -> Objects.equals(value, a)).check(acceptableValues);
	}

	/**
	 * Checks that the value is equal to one of the given objects.
	 * It is assumed that the {@code equals} method of the given object is symmetric.
	 *
	 * @param acceptableValues the acceptable references
	 * @return a ValueChecker that returns "correct" if {@code Objects.equals(value,o)} for at least one acceptable value o.
	 */
	static ValueChecker containedIn(Iterable<?> acceptableValues) {
		return value -> any(a -> Objects.equals(value, a)).check(acceptableValues);
	}

	/**
	 * Checks that the value is contained in a collection, according to {@link Collection#contains(Object)}.
	 *
	 * @param c the collection
	 * @return a ValueChecker that returns "correct" if {@code Collection.contains(value)}.
	 */
	static ValueChecker containedIn(Collection<?> c) {
		return c::contains;
	}

	/**
	 * Checks that the value represents an enum.
	 *
	 * @param enumClass class of enum
	 * @param method how to "translate" the value to an enum value
	 * @param <T> type of enum
	 * @return a ValueChecker that returns "correct" if {@link EnumGetMethod#validate(Object, Class)} returns true.
	 */
	static <T extends Enum<T>> ValueChecker enumeration(Class<T> enumClass, EnumGetMethod method) {
		return value -> method.validate(value, enumClass);
	}
}
