package com.electronwill.nightconfig.core;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author TheElectronWill
 */
public final class ConfigSpec {
	private final Config storage = new SimpleConfig(SimpleConfig.STRATEGY_SUPPORT_ALL);

	public void defineValue(List<String> path, Object defaultValue) {
		defineValue(path, defaultValue, o -> o != null && defaultValue.getClass().isAssignableFrom(o.getClass()));
	}

	public void defineValue(List<String> path, Object defaultValue, Predicate<Object> validator) {
		storage.setValue(path, new ValueSpec(defaultValue, validator));
	}

	public void defineValueInList(List<String> path, Object defaultValue, List<?> acceptableValues) {
		defineValue(path, defaultValue, acceptableValues::contains);
	}

	public <T> void defineValueInRange(List<String> path, T defaultValue, Comparable<T> min, Comparable<T> max) {
		defineValue(path, defaultValue, o -> {
			try {
				T c = (T)o;
				return max.compareTo(c) >= 0 && min.compareTo(c) <= 0;
			} catch (ClassCastException ex) {//cannot check if(o instanceof T) so we catch the Exception
				return false;
			}
		});
	}

	public <T> void defineValueInRange(List<String> path, Comparable<T> defaultValue, T min, T max) {
		defineValue(path, defaultValue, o -> {
			if (!(o instanceof Comparable)) return false;
			Comparable<T> c = (Comparable<T>)o;
			try {
				return c.compareTo(min) >= 0 && c.compareTo(max) <= 0;
			} catch (ClassCastException ex) {//cannot check if c is really Comparable<T> or
				// Comparable<Other incompatible type> so we catch the exception
				return false;
			}
		});
	}

	public <T extends Comparable<? super T>> void defineValueInRange(List<String> path, T defaultValue,
																	 T min, T max) {
		if (min.compareTo(max) > 0) {//Supplementary check that isn't possible with dVIR(Comparable<T>, T, T)
			throw new IllegalArgumentException("The minimum must be less than the maximum.");
		}
		defineValue(path, defaultValue, o -> {
			if (!(o instanceof Comparable)) return false;
			Comparable<T> c = (Comparable<T>)o;
			try {
				return c.compareTo(min) >= 0 && c.compareTo(max) <= 0;
			} catch (ClassCastException ex) {//cannot check if c is really Comparable<T> or
				// Comparable<Other incompatible type> so we catch the exception
				return false;
			}
		});
	}

	public void defineListValue(List<String> path, List<?> defaultValue, Predicate<Object> elementValidator) {
		defineValue(path, defaultValue, o -> {
			if (!(o instanceof List)) return false;
			List<?> list = (List<?>)o;
			for (Object element : list) {
				if (!elementValidator.test(element)) return false;
			}
			return true;
		});
	}

	private static final class ValueSpec {
		private final Object defaultValue;
		private final Predicate<Object> validator;

		ValueSpec(Object defaultValue, Predicate<Object> validator) {
			this.defaultValue = defaultValue;
			this.validator = Objects.requireNonNull(validator, "The validator must not be null.");
		}
	}
}
