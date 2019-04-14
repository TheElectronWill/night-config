package com.electronwill.nightconfig.core;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.electronwill.nightconfig.core.ConfigSpec.CorrectionAction.*;
import static com.electronwill.nightconfig.core.utils.StringUtils.split;

/**
 * Represents a specification for a configuration. With a ConfigSpec you can define mandatory
 * "properties" that the config's values must have and then check that the config is correct, and
 * even correct it automatically!
 * <h1>Defining entries</h1>
 * <p>
 * Use the "define" methods to define that some entry must be present in the config, and how its
 * value must be. You have to specify - at least - the path of the value and a default value that
 * will be used to replace the config's value in case it's incorrect.<br>
 * For instance, the following code defines a value with path "a.b" and which must be a String:
 * <pre>configSpec.define("a.b", "defaultString");</pre>
 *
 * <h2>Validators</h2>
 * <p>
 * Some methods (like the one used in the previous paragraph) automatically generate the rules that
 * make a config value correct or incorrect. But you can provide your own rule by specifying a
 * "validator": a {@link Predicate} that returns {@code true} if and only if the given value is
 * correct.<br>
 * For instance, this defines a value "arraylist" that must be an {@code ArrayList}:
 * {@code configSpec.define("arraylist", new ArrayList(), o -> o instanceof ArrayList);}
 *
 * <h2>Suppliers of default value</h2>
 * <p>
 * If the default value is heavy to create you should use a {@link Supplier} instead of creating a
 * default value, which is useless if the config's value happens to be correct.<br>
 * For instance, the code in the previous paragraph could be rewritten like this:
 * {@code configSpec.define("heavy", () -> new ArrayList(), o -> o instanceof ArrayList);}
 *
 * <h1>Checking configurations</h1>
 * <p>
 * Use the "isCorrect" methods to check whether a configuration is correct or not. A configuration
 * is correct if and only if:*
 * <ol>
 * <li>Each entry defined in the spec is present in the config.
 * <li>Each entry in the config is defined in the spec.
 * <li>Each entry in the config has a correct value according to the spec.
 * </ol>
 *
 * <h1>Correcting configurations</h1>
 * <p>
 * Use the "correct" methods to correct a configuration. The correction behaves like this:
 * <ol>
 * <li>Each entry that is defined in the spec but absent from the config is added to the config,
 * with the default value defined in the spec.
 * <li>Each entry that isn't defined in the spec is removed from the config.
 * <li>Each incorrect config value is replaced by the default value specified in the spec.
 * </ol>
 *
 * @author TheElectronWill
 */
public class ConfigSpec {
	protected final Config storage;

	public ConfigSpec() {
		this(Config.inMemoryUniversal());
	}

	ConfigSpec(Config storage) {
		this.storage = storage;
	}

	/**
	 * Defines an entry. To be correct, the type of the config value must be of the same as or a
	 * subtype of the defaultValue's type.
	 *
	 * @param path         the entry's path
	 * @param defaultValue the default entry value
	 */
	public void define(String path, Object defaultValue) {
		define(split(path, '.'), defaultValue);
	}

	/**
	 * Defines an entry. To be correct, the type of the config value must be of the same as or a
	 * subtype of the defaultValue's type.
	 *
	 * @param path         the entry's path
	 * @param defaultValue the default entry value
	 */
	public void define(List<String> path, Object defaultValue) {
		define(path, defaultValue,
			   o -> o != null && defaultValue.getClass().isAssignableFrom(o.getClass()));
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if
	 * {@code validator.test(configValue)} returns true.
	 *
	 * @param path         the entry's path
	 * @param defaultValue the default entry value
	 * @param validator    the Predicate that determines if the value is correct or not
	 */
	public void define(String path, Object defaultValue, Predicate<Object> validator) {
		define(split(path, '.'), defaultValue, validator);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if
	 * {@code validator.test(configValue)} returns true.
	 *
	 * @param path                 the entry's path
	 * @param defaultValueSupplier the Supplier of the default entry value
	 * @param validator            the Predicate that determines if the value is correct or not
	 */
	public void define(String path, Supplier<?> defaultValueSupplier, Predicate<Object> validator) {
		define(split(path, '.'), defaultValueSupplier, validator);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if
	 * {@code validator.test(configValue)} returns true.
	 *
	 * @param path         the entry's path
	 * @param defaultValue the default entry value
	 * @param validator    the Predicate that determines if the value is correct or not
	 */
	public void define(List<String> path, Object defaultValue, Predicate<Object> validator) {
		storage.set(path, new ValueSpec(defaultValue, validator));
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if
	 * {@code validator.test(configValue)} returns true.
	 *
	 * @param path                 the entry's path
	 * @param defaultValueSupplier the Supplier of the default entry value
	 * @param validator            the Predicate that determines if the value is correct or not
	 */
	public void define(List<String> path, Supplier<?> defaultValueSupplier,
					   Predicate<Object> validator) {
		storage.set(path, new ValueSpec(defaultValueSupplier, validator));
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is of the same type
	 * as, or of a subtype of the specified class.
	 *
	 * @param path                 the entry's path
	 * @param defaultValue         the default entry value
	 * @param acceptableValueClass the class that a value of this entry must have
	 */
	public <V> void defineOfClass(String path, V defaultValue,
								  Class<? super V> acceptableValueClass) {
		defineOfClass(split(path, '.'), new DumbSupplier<>(defaultValue), acceptableValueClass);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is of the same type
	 * as, or of a subtype of the specified class.
	 *
	 * @param path                 the entry's path
	 * @param defaultValueSupplier the Supplier of the default entry value
	 * @param acceptableValueClass the class that a value of this entry must have
	 */
	public <V> void defineOfClass(String path, Supplier<V> defaultValueSupplier,
								  Class<? super V> acceptableValueClass) {
		defineOfClass(split(path, '.'), defaultValueSupplier, acceptableValueClass);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is of the same type
	 * as, or of a subtype of the specified class.
	 *
	 * @param path                 the entry's path
	 * @param defaultValue         the default entry value
	 * @param acceptableValueClass the class that a value of this entry must have
	 */
	public <V> void defineOfClass(List<String> path, V defaultValue,
								  Class<? super V> acceptableValueClass) {
		defineOfClass(path, new DumbSupplier<>(defaultValue), acceptableValueClass);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is of the same type
	 * as, or of a subtype of the specified class.
	 *
	 * @param path                 the entry's path
	 * @param defaultValueSupplier the Supplier of the default entry value
	 * @param acceptableValueClass the class that a value of this entry must have
	 */
	public <V> void defineOfClass(List<String> path, Supplier<V> defaultValueSupplier,
								  Class<? super V> acceptableValueClass) {
		define(path, defaultValueSupplier,
			   o -> o != null && acceptableValueClass.isAssignableFrom(o.getClass()));
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is contained in the
	 * specified collection.
	 *
	 * @param path             the entry's path
	 * @param defaultValue     the default entry value
	 * @param acceptableValues the Collection containing all the acceptable values
	 */
	public void defineInList(String path, Object defaultValue, Collection<?> acceptableValues) {
		defineInList(split(path, '.'), defaultValue, acceptableValues);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is contained in the
	 * specified collection.
	 *
	 * @param path                 the entry's path
	 * @param defaultValueSupplier the Supplifer of the default entry value
	 * @param acceptableValues     the Collection containing all the acceptable values
	 */
	public void defineInList(String path, Supplier<?> defaultValueSupplier,
							 Collection<?> acceptableValues) {
		defineInList(split(path, '.'), defaultValueSupplier, acceptableValues);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is contained in the
	 * specified collection.
	 *
	 * @param path             the entry's path
	 * @param defaultValue     the default entry value
	 * @param acceptableValues the Collection containing all the acceptable values
	 */
	public void defineInList(List<String> path, Object defaultValue,
							 Collection<?> acceptableValues) {
		defineInList(path, new DumbSupplier<>(defaultValue), acceptableValues);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is contained in the
	 * specified collection.
	 *
	 * @param path                 the entry's path
	 * @param defaultValueSupplier the Supplier of the default entry value
	 * @param acceptableValues     the Collection containing all the acceptable values
	 */
	public void defineInList(List<String> path, Supplier<?> defaultValueSupplier,
							 Collection<?> acceptableValues) {
		define(path, defaultValueSupplier, acceptableValues::contains);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is less than or
	 * equal to {@code min} and bigger than or equal to {@code max}.
	 *
	 * @param path         the entry's path
	 * @param defaultValue the default entry value
	 * @param min          the minimum, inclusive
	 * @param max          the maximum, inclusive
	 */
	public <V extends Comparable<? super V>> void defineInRange(String path, V defaultValue, V min,
																V max) {
		defineInRange(split(path, '.'), defaultValue, min, max);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is less than or
	 * equal to {@code min} and bigger than or equal to {@code max}.
	 *
	 * @param path                 the entry's path
	 * @param defaultValueSupplier the Supplier of the default entry value
	 * @param min                  the minimum, inclusive
	 * @param max                  the maximum, inclusive
	 */
	public <V extends Comparable<? super V>> void defineInRange(String path,
																Supplier<V> defaultValueSupplier,
																V min, V max) {
		defineInRange(split(path, '.'), defaultValueSupplier, min, max);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is less than or
	 * equal to {@code min} and bigger than or equal to {@code max}.
	 *
	 * @param path         the entry's path
	 * @param defaultValue the default entry value
	 * @param min          the minimum, inclusive
	 * @param max          the maximum, inclusive
	 */
	public <V extends Comparable<? super V>> void defineInRange(List<String> path, V defaultValue,
																V min, V max) {
		defineInRange(path, new DumbSupplier<>(defaultValue), min, max);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if it is less than or
	 * equal to {@code min} and bigger than or equal to {@code max}.
	 *
	 * @param path                 the entry's path
	 * @param defaultValueSupplier the Supplier of the default entry value
	 * @param min                  the minimum, inclusive
	 * @param max                  the maximum, inclusive
	 */
	public <V extends Comparable<? super V>> void defineInRange(List<String> path,
																Supplier<V> defaultValueSupplier,
																V min, V max) {
		if (min.compareTo(max) > 0) {
			throw new IllegalArgumentException("The minimum must be less than the maximum.");
		}
		define(path, defaultValueSupplier, o -> {
			if (!(o instanceof Comparable)) { return false; }
			Comparable<V> c = (Comparable<V>)o;
			try {
				return c.compareTo(min) >= 0 && c.compareTo(max) <= 0;
			} catch (ClassCastException ex) {//cannot check if c is really Comparable<V> or
				// Comparable<Other incompatible type> so we catch the exception
				return false;
			}
		});
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if all its element are
	 * valid according to the {@code elementValidator}, that is, if and only if for all element e
	 * in the list the call {@code elementValidator.test(e)} returns true.
	 *
	 * @param path             the entry's path
	 * @param defaultValue     the default entry value
	 * @param elementValidator the Predicate that checks that every element of the list is correct
	 */
	public void defineList(String path, List<?> defaultValue, Predicate<Object> elementValidator) {
		defineList(split(path, '.'), defaultValue, elementValidator);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if all its element are
	 * valid according to the {@code elementValidator}, that is, if and only if for all element e
	 * in the list the call {@code elementValidator.test(e)} returns true.
	 *
	 * @param path                 the entry's path
	 * @param defaultValueSupplier the Supplier of the default entry value
	 * @param elementValidator     the Predicate that checks that every element of the list is
	 *                             correct
	 */
	public void defineList(String path, Supplier<List<?>> defaultValueSupplier,
						   Predicate<Object> elementValidator) {
		defineList(split(path, '.'), defaultValueSupplier, elementValidator);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if all its element are
	 * valid according to the {@code elementValidator}, that is, if and only if for all element e
	 * in the list the call {@code elementValidator.test(e)} returns true.
	 *
	 * @param path             the entry's path
	 * @param defaultValue     the default entry value
	 * @param elementValidator the Predicate that checks that every element of the list is correct
	 */
	public void defineList(List<String> path, List<?> defaultValue,
						   Predicate<Object> elementValidator) {
		defineList(path, new DumbSupplier<>(defaultValue), elementValidator);
	}

	/**
	 * Defines an entry. A config value is considered correct if and only if all its element are
	 * valid according to the {@code elementValidator}, that is, if and only if for all element e
	 * in the list the call {@code elementValidator.test(e)} returns true.
	 *
	 * @param path                 the entry's path
	 * @param defaultValueSupplier the Supplier of the default entry value
	 * @param elementValidator     the Predicate that checks that every element of the list is
	 *                             correct
	 */
	public void defineList(List<String> path, Supplier<List<?>> defaultValueSupplier,
						   Predicate<Object> elementValidator) {
		define(path, defaultValueSupplier, o -> {
			if (!(o instanceof List)) { return false; }
			List<?> list = (List<?>)o;
			for (Object element : list) {
				if (!elementValidator.test(element)) { return false; }
			}
			return true;
		});
	}

	//
	// --- defineEnum ---

	public <T extends Enum<T>> void defineEnum(String path, T defaultValue, EnumGetMethod method) {
		defineEnum(split(path, '.'), defaultValue, method);
	}

	public <T extends Enum<T>> void defineEnum(List<String> path,
											  T defaultValue,
											  EnumGetMethod method) {
		defineEnum(path, defaultValue.getDeclaringClass(), method, new DumbSupplier<>(defaultValue));
	}

	public <T extends Enum<T>> void defineEnum(String path,
											   Class<T> enumType,
											   EnumGetMethod method,
											   Supplier<T> defaultValueSupplier) {
		defineEnum(split(path, '.'), enumType, method, defaultValueSupplier);
	}

	public <T extends Enum<T>> void defineEnum(List<String> path,
											   Class<T> enumType,
											   EnumGetMethod method,
											   Supplier<T> defaultValueSupplier) {
		define(path, defaultValueSupplier, o -> method.validate(o, enumType));
	}

	//
	// --- defineRestrictedEnum ---

	public <T extends Enum<T>> void defineRestrictedEnum(String path,
														 T defaultValue,
														 Collection<T> acceptableValues,
														 EnumGetMethod method) {
		defineRestrictedEnum(split(path, '.'), defaultValue, acceptableValues, method);
	}

	public <T extends Enum<T>> void defineRestrictedEnum(List<String> path,
														 T defaultValue,
														 Collection<T> acceptableValues,
														 EnumGetMethod method) {
		defineRestrictedEnum(path, defaultValue.getDeclaringClass(), acceptableValues, method, new DumbSupplier<>(defaultValue));
	}

	public <T extends Enum<T>> void defineRestrictedEnum(String path,
													     Class<T> enumType,
													     Collection<T> acceptableValues,
													     EnumGetMethod method,
													     Supplier<T> defaultValueSupplier) {
		defineRestrictedEnum(split(path, '.'), enumType, acceptableValues, method, defaultValueSupplier);
	}

	public <T extends Enum<T>> void defineRestrictedEnum(List<String> path,
													     Class<T> enumType,
													     Collection<T> acceptableValues,
													     EnumGetMethod method,
													     Supplier<T> defaultValueSupplier) {
		define(path, defaultValueSupplier, o ->
			method.validate(o, enumType) && acceptableValues.contains(method.get(o, enumType))
		);
	}

	/**
	 * Undefines an entry.
	 *
	 * @param path the entry's path
	 */
	public void undefine(String path) {
		undefine(split(path, '.'));
	}

	/**
	 * Undefines an entry.
	 *
	 * @param path the entry's path
	 */
	public void undefine(List<String> path) {
		storage.remove(path);
	}

	/**
	 * Checks if an entry has been defined.
	 *
	 * @param path the entry's path
	 * @return {@code true} if it has been defined, {@code false} otherwise
	 */
	public boolean isDefined(String path) {
		return isDefined(split(path, '.'));
	}

	/**
	 * Checks if an entry has been defined.
	 *
	 * @param path the entry's path
	 * @return {@code true} if it has been defined, {@code false} otherwise
	 */
	public boolean isDefined(List<String> path) {
		return storage.contains(path);
	}

	/**
	 * Checks that a value is conform to the specification.
	 *
	 * @param path  the entry's path
	 * @param value the entry's value
	 * @return {@code true} if it's correct, {@code false} if it's incorrect
	 */
	public boolean isCorrect(String path, Object value) {
		return isCorrect(split(path, '.'), value);
	}

	/**
	 * Checks that a value is conform to the specification.
	 *
	 * @param path  the entry's path
	 * @param value the entry's value
	 * @return {@code true} if it's correct, {@code false} if it's incorrect
	 */
	public boolean isCorrect(List<String> path, Object value) {
		ValueSpec spec = storage.getRaw(path);
		return spec.validator.test(value);
	}

	/**
	 * Checks that a configuration is conform to the specification.
	 *
	 * @param config the config to check
	 * @return {@code true} if it's correct, {@code false} if it's incorrect
	 */
	public boolean isCorrect(Config config) {
		return isCorrect(config.valueMap(), storage.valueMap());
	}

	/**
	 * Checks that a configuration is conform to the specification.
	 *
	 * @return {@code true} if it's correct, {@code false} if it's incorrect
	 */
	private boolean isCorrect(Map<String, Object> configMap, Map<String, Object> specMap) {
		// First step: checks for incorrect and missing values
		for (Map.Entry<String, Object> specEntry : specMap.entrySet()) {
			final String key = specEntry.getKey();
			final Object specValue = specEntry.getValue();
			final Object configValue = configMap.get(key);
			if (configValue == null) {// In the spec but not in the config: missing value!
				return false;
			}
			if (specValue instanceof Config) {
				if (!(configValue instanceof Config)) {
					return false;// Missing sublevel in config
				}
				if (!isCorrect(((Config)configValue).valueMap(), ((Config)specValue).valueMap())) {
					return false;// Incorrect sublevel
				}
			} else {
				ValueSpec valueSpec = (ValueSpec)specValue;
				if (!valueSpec.validator.test(configValue)) {
					return false;// Incorrect value
				}
			}
		}
		// Second step: checks for unspecified value
		for (Map.Entry<String, Object> configEntry : configMap.entrySet()) {
			final String key = configEntry.getKey();
			final Object specValue = specMap.get(key);
			if (specValue == null) {// Unspecified value that shouldn't exist
				return false;
			}
		}
		return true;
	}

	/**
	 * Corrects a value.
	 *
	 * @param path  the value's path
	 * @param value the value to correct
	 * @return the corrected value, or the value itself if's it already correct
	 */
	public Object correct(String path, Object value) {
		return correct(split(path, '.'), value);
	}

	/**
	 * Corrects a value.
	 *
	 * @param path  the value's path
	 * @param value the value to correct
	 * @return the corrected value, or the value itself if's it already correct
	 */
	public Object correct(List<String> path, Object value) {
		ValueSpec spec = storage.getRaw(path);
		return spec.validator.test(value) ? value : spec.defaultValueSupplier.get();
	}

	/**
	 * Corrects a configuration.
	 *
	 * @param config the config to correct
	 * @return the number of added, removed or replaced values.
	 */
	public int correct(Config config) {
		return correct(config, (action, path, incorrectValue, correctedValue) -> {});
	}

	/**
	 * Corrects a configuration.
	 *
	 * @param config   the config to correct
	 * @param listener the listener that will be notified of every change made during the
	 *                 correction of the config.
	 * @return the number of added, removed or replaced values.
	 */
	public int correct(Config config, CorrectionListener listener) {
		return correct(config.valueMap(), storage.valueMap(), new ArrayList<>(), listener, config::createSubConfig);
	}

	/**
	 * Recursively corrects a configuration.
	 *
	 * @param configMap  the config's map
	 * @param specMap    the specification's map
	 * @param parentPath the path of the parent entry (may be empty)
	 * @param listener   the listener to notify of each correction
	 * @return the number of added, removed of replaced values.
	 */
	private int correct(Map<String, Object> configMap,
						Map<String, Object> specMap,
						List<String> parentPath,
						CorrectionListener listener,
						Supplier<Config> subConfigSupplier) {
		int count = 0;
		// First step: replaces the incorrect values and add the missing ones
		for (Map.Entry<String, Object> specEntry : specMap.entrySet()) {
			final String key = specEntry.getKey();
			final Object specValue = specEntry.getValue();

			Object configValue = configMap.get(key);
			if (specValue instanceof Config) {// sublevel that contains ValueSpecs, or other sublevels
				if (!(configValue instanceof Config)) {// Missing or invalid (ie not a Config) sublevel
					// Creates the sublevel in the config:
					Config newValue = subConfigSupplier.get();
					configMap.put(key, newValue);
					// Notifies the listener:
					CorrectionAction correctionAction = (configValue == null) ? ADD : REPLACE;
					handleCorrection(parentPath, key, configValue, newValue, listener,
									 correctionAction);
					count++;
					configValue = newValue;
				}
				// Existing or added sublevel
				// Checks the sublevel recursively:
				parentPath.add(key);
				Map<String, Object> configValueMap = ((Config)configValue).valueMap();
				Map<String, Object> specValueMap = ((Config)specValue).valueMap();
				count += correct(configValueMap, specValueMap, parentPath, listener, subConfigSupplier);
				parentPath.remove(parentPath.size() - 1);

			} else {
				ValueSpec valueSpec = (ValueSpec)specValue;
				if (!valueSpec.validator.test(configValue)) {
					// Corrects the value in the config:
					Object newValue = valueSpec.defaultValueSupplier.get();// Gets the corrected value
					configMap.put(key, newValue);
					// Notifies the listener:
					CorrectionAction correctionAction = (configValue == null) ? ADD : REPLACE;
					handleCorrection(parentPath, key, configValue, newValue, listener,
									 correctionAction);
					count++;
				}
			}
		}
		// Second step: removes the unspecified values
		for (Iterator<Map.Entry<String, Object>> it = configMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Object> configEntry = it.next();
			final String key = configEntry.getKey();
			final Object configValue = configEntry.getValue();
			final Object specValue = specMap.get(key);
			if (specValue == null) {
				// Removes the value and notifies the listener:
				it.remove();
				handleCorrection(parentPath, key, configValue, null, listener, REMOVE);
				count++;
			}
		}
		return count;
	}

	/**
	 * Notifies the {@link CorrectionListener} of some correction.
	 */
	private void handleCorrection(List<String> parentPath, String key, Object value,
								  Object newValue, CorrectionListener listener,
								  CorrectionAction action) {
		parentPath.add(key);// Adds the current key to the path
		List<String> valuePath = Collections.unmodifiableList(
				parentPath);// Creates an unmodifiable version of the list
		listener.onCorrect(action, valuePath, value, newValue);// Notifies the listener
		parentPath.remove(parentPath.size() - 1);// Removes the last element, ie the path
	}

	/**
	 * Listens to the corrections made by the methods {@link #correct(Config)} and
	 * {@link #correct(Config, CorrectionListener)}.
	 */
	@FunctionalInterface
	public interface CorrectionListener {
		/**
		 * Called when a config value is added, modified or removed by the correction.
		 *
		 * @param action         the action that was taken.
		 * @param path           the path of the value, <b>unmodifiable.</b>
		 * @param incorrectValue the old, incorrect value. May be null if the value didn't exist
		 *                       before the correction, <b>or if the value was actually null.</b>
		 * @param correctedValue the new, corrected value. May be null if the value has been
		 *                       removed by the correction, <b>or if the default value in the
		 *                       specification is null.</b>
		 */
		void onCorrect(CorrectionAction action, List<String> path, Object incorrectValue,
					   Object correctedValue);

	}

	public enum CorrectionAction {
		/**
		 * Means that the value was added to the config. In that case, {@code incorrectValue} is
		 * {@code null}.
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
	 * Container for the supplier of the default value and the validator.
	 */
	private static final class ValueSpec {
		private final Supplier<?> defaultValueSupplier;
		private final Predicate<Object> validator;

		private ValueSpec(Object defaultValue, Predicate<Object> validator) {
			this(new DumbSupplier<>(
						 Objects.requireNonNull(defaultValue, "The default value must not be null.")),
				 validator);
		}

		private ValueSpec(Supplier<?> defaultValueSupplier, Predicate<Object> validator) {
			this.defaultValueSupplier = Objects.requireNonNull(defaultValueSupplier,
															   "The supplier of the default value must not be null.");
			this.validator = Objects.requireNonNull(validator, "The validator must not be null.");
		}
	}

	/**
	 * A Supplier that always returns the value it has been created with.
	 *
	 * @param <T> the value's type
	 */
	private static final class DumbSupplier<T> implements Supplier<T> {
		private final T value;

		private DumbSupplier(T value) {this.value = value;}

		@Override
		public T get() {
			return value;
		}
	}
}