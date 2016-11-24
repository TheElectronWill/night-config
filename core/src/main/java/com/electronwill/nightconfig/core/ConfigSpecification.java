package com.electronwill.nightconfig.core;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
public class ConfigSpecification {

	protected final Map<String, ValueSpecification> map = new HashMap<>();

	public ConfigSpecification() {}

	/**
	 * Defines a specification for the given path. The validator associated with the path will check if
	 * the actual value is of the same type as, or a subtype of, defaultValue's type.
	 *
	 * @param path         the path.
	 * @param defaultValue the default value.
	 */
	public void defineValue(String path, Object defaultValue) {
		defineValue(path, defaultValue, (v) -> defaultValue.getClass().isAssignableFrom(v.getClass()));
	}

	/**
	 * Defines a specification for the given path, with the given default value and validator.
	 *
	 * @param path         the path.
	 * @param defaultValue the default value.
	 * @param validator    the validator that whill check if the actual path's value is correct.
	 */
	public void defineValue(String path, Object defaultValue, Predicate<Object> validator) {
		ValueSpecification spec = new ValueSpecification(defaultValue, validator);
		map.put(path, spec);
	}

	/**
	 * Defines a specification for the given path, with an acceptable class The validator associated with
	 * the path will check if the actual value is of the same type as, or a subtype of,
	 * {@code acceptableClass}.
	 *
	 * @param path            the path.
	 * @param defaultValue    the default value.
	 * @param acceptableClass the class that will be accepted by the validator.
	 * @param <T>             the type of the default value.
	 */
	public <T> void defineValue(String path, T defaultValue, Class<? super T> acceptableClass) {
		defineValue(path, defaultValue, (v) -> acceptableClass.isAssignableFrom(v.getClass()));
	}

	/**
	 * Defines a specification for the given path, with an array of acceptable values. The validator
	 * associated with the path will check if the actual value is contained into the {@code acceptableValues}.
	 *
	 * @param path             the path.
	 * @param defaultValue     the default value.
	 * @param acceptableValues all the acceptable values. Must contains the default value.
	 */
	public void defineString(String path, String defaultValue, String... acceptableValues) {
		defineString(path, defaultValue, Arrays.asList(acceptableValues));
	}

	/**
	 * Defines a specification for the given path, with an array of acceptable values. The validator
	 * associated with the path will check if the actual value is a String and is contained into the {@code
	 * acceptableValues}.
	 *
	 * @param path             the path.
	 * @param defaultValue     the default value.
	 * @param acceptableValues all the acceptable values. Must contains the default value.
	 */
	public void defineString(String path, String defaultValue, Collection<String> acceptableValues) {
		defineValue(path, defaultValue, acceptableValues::contains);
	}

	/**
	 * Defines a specification for the given path, with a regular expression. The validator associated
	 * with the path will check if the actual value is a String and matches the {@code regexToMatch}.
	 *
	 * @param path         the path.
	 * @param defaultValue the default value.
	 * @param regexToMatch the regular expression that the actual value has to match to be valid.
	 */
	public void defineString(String path, String defaultValue, String regexToMatch) {
		defineValue(path, defaultValue, (v) -> (v instanceof String) && ((String)v).matches(regexToMatch));
	}

	/**
	 * Defines a specification for the given path, with a minimum and a maximum.
	 *
	 * @param path         the path.
	 * @param defaultValue the default value.
	 * @param min          the smallest acceptable value.
	 * @param max          the biggest acceptable value.
	 */
	public void defineInt(String path, int defaultValue, int min, int max) {
		defineValue(path, defaultValue, (v) -> (v instanceof Integer) && ((int)v) >= min && ((int)v) <= max);
	}

	/**
	 * Defines a specification for the given path, with a minimum and a maximum.
	 *
	 * @param path         the path.
	 * @param defaultValue the default value.
	 * @param min          the smallest acceptable value.
	 * @param max          the biggest acceptable value.
	 */
	public void defineLong(String path, long defaultValue, long min, long max) {
		defineValue(path, defaultValue, (v) -> (v instanceof Long) && ((long)v) >= min && ((long)v) <= max);
	}

	/**
	 * Defines a specification for the given path, with a minimum and a maximum.
	 *
	 * @param path         the path.
	 * @param defaultValue the default value.
	 * @param min          the smallest acceptable value.
	 * @param max          the biggest acceptable value.
	 */
	public void defineFloat(String path, float defaultValue, float min, float max) {
		defineValue(path, defaultValue, (v) -> (v instanceof Float) && ((float)v) >= min && ((float)v) <= max);
	}

	/**
	 * Defines a specification for the given path, with a minimum and a maximum.
	 *
	 * @param path         the path.
	 * @param defaultValue the default value.
	 * @param min          the smallest acceptable value.
	 * @param max          the biggest acceptable value.
	 */
	public void defineDouble(String path, double defaultValue, double min, double max) {
		defineValue(path, defaultValue, (v) -> (v instanceof Double) && ((double)v) >= min && ((double)v) <= max);
	}

	/**
	 * Defines a specification for the given path, with a {@code Predicate} that checks every value of the
	 * list.
	 *
	 * @param path               the path.
	 * @param defaultValue       the default value.
	 * @param listValueValidator the validator that checks if every value of the list is correct. It
	 *                           returns {@code true} if the value is correct, {@code false} if it isn't.
	 * @param <T>                the type of the list.
	 */
	public <T> void defineList(String path, List<T> defaultValue, Predicate<? super T> listValueValidator) {
		defineValue(path, defaultValue, (v) -> {
			if (v instanceof List) {
				List<T> list = (List)v;
				for (T element : list) {
					if (!listValueValidator.test(element))
						return false;
				}
				return true;
			}
			return false;
		});
	}

	/**
	 * Deletes the specification of the given path, if any. If the path has no specification, this method
	 * does nothing.
	 *
	 * @param path the path.
	 */
	public void undefine(String path) {
		map.remove(path);
	}

	/**
	 * Checks if a specification has been defined for the given path.
	 *
	 * @param path the path to check.
	 * @return {@code true} if a specification has been defined for this path, else {@code false}.
	 */
	public boolean isDefined(String path) {
		return map.containsKey(path);
	}

	/**
	 * Checks if the given value is acceptable for the given path.
	 *
	 * @param path  the path.
	 * @param value the value to check.
	 * @return {@code true} if it is acceptable, {@code false} if it isn't.
	 */
	public boolean check(String path, Object value) {
		ValueSpecification spec = map.get(path);
		if (spec == null) {
			return false;
		}
		return spec.validator.test(value);
	}

	/**
	 * Checks if the given config complies with this specification.
	 *
	 * @param config the config to check.
	 * @return {@code true} if the config is correct, {@code false} if it isn't.
	 */
	public boolean check(Config config) {
		return check(config, (r, p, v) -> {}, true);
	}

	/**
	 * Checks if the given config complies with this specification. Calls the {@code listener}
	 * for each config value that does not complies with this specification, and for each missing config
	 * value.
	 *
	 * @param config           the config to check.
	 * @param listener         the listener to call for each incorrect (or missing, or unwanted) config value.
	 * @param stopAfterOneFail {@code true} to stop after one check fails, {@code false} to continue and
	 *                         check everything.
	 * @return {@code true} if the config is correct, {@code false} if it isn't.
	 */
	public boolean check(Config config, CheckListener listener, boolean stopAfterOneFail) {
		// Step 1 - check for incorrect or unwanted config values
		boolean valid = checkValues(config, listener, stopAfterOneFail, "");//recursive

		// Step 2 - check for missing values
		for (Map.Entry<String, ValueSpecification> specEntry : map.entrySet()) {
			String path = specEntry.getKey();
			ValueSpecification spec = specEntry.getValue();
			if (!config.containsValue(path)) {
				listener.onCheckFailed(CheckFailReason.MISSING, path, null);
				if (stopAfterOneFail)
					return false;
			}
		}
		return valid;
	}

	private boolean checkValues(Config config, CheckListener listener, boolean stopAfterOneFail, String configPath) {
		boolean valid = true;
		Set<Map.Entry<String, Object>> configEntries = config.asMap().entrySet();
		for (Iterator<Map.Entry<String, Object>> it = configEntries.iterator(); it.hasNext(); ) {
			Map.Entry<String, Object> configEntry = it.next();
			String key = configEntry.getKey();
			Object value = configEntry.getValue();

			String path = (configPath.isEmpty()) ? key : configPath + '.' + key;
			ValueSpecification spec = map.get(path);

			if (value instanceof Config) {
				valid &= checkValues(config, listener, stopAfterOneFail, path);//process the content of the config
				if (stopAfterOneFail && !valid) {
					return false;
				}
			} else {
				if (spec == null) {//no specification = unwanted value
					listener.onCheckFailed(CheckFailReason.UNWANTED, path, value);
					it.remove();
					if (stopAfterOneFail) {
						return false;
					}
					valid = false;
				} else if (!spec.validator.test(value)) {//incorrect value
					listener.onCheckFailed(CheckFailReason.INCORRECT_VALUE, path, value);
					if (stopAfterOneFail) {
						return false;
					}
					valid = false;
				}
			}
		}
		return valid;
	}

	/**
	 * Listen for the checks made by the methods {@link #check(Config)} and
	 * {@link #check(Config, CheckListener, boolean)}.
	 */
	@FunctionalInterface
	public static interface CheckListener {
		/**
		 * Called when a config check fail, that is, when the {@code check} method detects that a
		 * config value is missing, is incorrect or is unwanted.
		 *
		 * @param reason the reason of the failure.
		 * @param path   the path of the value.
		 * @param value  the config value. May be null.
		 */
		void onCheckFailed(CheckFailReason reason, String path, Object value);
	}

	public static enum CheckFailReason {
		/**
		 * Means that a value is missing for this path, that is, it should be added to the config. In
		 * that case, {@code value} is always {@code null}.
		 */
		MISSING,
		/**
		 * Means that the value is incorrect, that is, it doesn't conform to the specification).
		 */
		INCORRECT_VALUE,
		/**
		 * Means that the value is unwanted, that is, it should be removed from the config.
		 */
		UNWANTED
	}

	/**
	 * Corrects the given config so that it complies with this specification. Any value that is specified
	 * but does not exist in the config is added to it. Any value that is not specified but does exist
	 * in the config is removed from it. Any value that is specified, that exists in the config,
	 * but that is incorrect (that doesn't complies to its specification) is replaced by the default value
	 * defined in this specification.
	 *
	 * @param config the config to correct.
	 * @return the number of corrected values.
	 */
	public int correct(Config config) {
		return correct(config, (a, p, i, c) -> {});
	}

	/**
	 * Corrects the given config so that it complies with this spefication. Call the {@code
	 * listener} for each config value that is added, replaced or removed.
	 *
	 * @param config   the config to correct.
	 * @param listener the action to do for each corrected config value.
	 * @return the number of corrected values.
	 */
	public int correct(Config config, CorrectionListener listener) {
		// Step 1 - modify or remove the incorrect config values
		int corrected = correctBadValues(config, listener, "");//recursive

		// Step 2 - add the missing values to the config
		for (Map.Entry<String, ValueSpecification> specEntry : map.entrySet()) {
			String path = specEntry.getKey();
			ValueSpecification spec = specEntry.getValue();
			if (!config.containsValue(path)) {
				listener.onCorrect(CorrectionAction.ADD, path, null, spec.defaultValue);
				config.setValue(path, spec.defaultValue);
				corrected++;
			}
		}
		return corrected;
	}

	private int correctBadValues(Config config, CorrectionListener listener, String configPath) {
		int corrected = 0;
		Set<Map.Entry<String, Object>> configEntries = config.asMap().entrySet();
		for (Iterator<Map.Entry<String, Object>> it = configEntries.iterator(); it.hasNext(); ) {
			Map.Entry<String, Object> configEntry = it.next();
			String key = configEntry.getKey();
			Object value = configEntry.getValue();

			String path = (configPath.isEmpty()) ? key : configPath + '.' + key;
			ValueSpecification spec = map.get(path);

			if (value instanceof Config) {
				corrected += correctBadValues(config, listener, path);//process the content of the config
			} else {
				if (spec == null) {//no specification -> remove the value
					listener.onCorrect(CorrectionAction.REMOVE, path, value, null);
					it.remove();
					corrected++;
				} else if (!spec.validator.test(value)) {//incorrect value -> replace by the default value
					listener.onCorrect(CorrectionAction.REPLACE, path, value, spec.defaultValue);
					configEntry.setValue(spec.defaultValue);
					corrected++;
				}
			}
		}
		return corrected;
	}

	/**
	 * Listens for corrections made by the methods {@link #correct(Config)} and
	 * {@link #correct(Config, CorrectionListener)}.
	 */
	@FunctionalInterface
	public static interface CorrectionListener {
		/**
		 * Called when a config value is added, modified or removed by the correction.
		 *
		 * @param action         the action that was taken.
		 * @param path           the path of the value.
		 * @param incorrectValue the old, incorrect value. May be null if the value didn't exist before the
		 *                       correction, <b>or if the value was actually null.</b>
		 * @param correctedValue the new, corrected value. May be null if the value has been removed by the
		 *                       correction, <b>or if the default value in the specification is null.</b>
		 */
		void onCorrect(CorrectionAction action, String path, Object incorrectValue, Object correctedValue);

	}

	public static enum CorrectionAction {
		/**
		 * Means that the value was added to the config. In that case, {@code incorrectValue} is {@code
		 * null}.
		 */
		ADD,
		/**
		 * Means that the value was replaced. In that case, {@code incorrectValue} and/or {@code
		 * correctedValue} <b>may be</b> {@code null}.
		 */
		REPLACE,
		/**
		 * Means that the value was removed from the config. In that case, {@code correctedValue} is
		 * {@code null}.
		 */
		REMOVE
	}

	/**
	 * Defines a specification for a particular value.
	 */
	private static class ValueSpecification {

		private final Object defaultValue;
		private final Predicate<Object> validator;

		ValueSpecification(Object defaultValue, Predicate<Object> validator) {
			Objects.requireNonNull(validator, "The validator must not be null.");
			this.defaultValue = defaultValue;
			this.validator = validator;
		}
	}
}
