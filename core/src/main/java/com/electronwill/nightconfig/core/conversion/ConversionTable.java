package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Contains conversions functions organized by value's type. A ConversionTable grows as necessary.
 *
 * @author TheElectronWill
 */
public final class ConversionTable {
	private final Map<Class<?>, Function<?, Object>> conversionMap;

	/**
	 * Creates a new empty ConversionTable.
	 */
	public ConversionTable() {
		this.conversionMap = new HashMap<>();
	}

	/**
	 * Creates a copy of a ConversionTable.
	 *
	 * @param toCopy the table to copy
	 */
	private ConversionTable(ConversionTable toCopy) {
		this.conversionMap = new HashMap<>(toCopy.conversionMap);
	}

	/**
	 * Puts a conversion function to the table. If a function is already defined for the
	 * specified class then it is replaced.
	 *
	 * @param classToConvert     the class of the values that the function can convert, may be null
	 * @param conversionFunction the conversion function
	 * @param <T>                the type of the values that the function can convert
	 */
	public <T> void put(Class<T> classToConvert, Function<? super T, Object> conversionFunction) {
		conversionMap.put(classToConvert, conversionFunction);
	}

	/**
	 * Removes the function that is currently defined for the specified class, if any.
	 *
	 * @param classToConvert the class of the values that the function we want to remove converts
	 */
	public void remove(Class<?> classToConvert) {
		conversionMap.remove(classToConvert);
	}

	/**
	 * Checks that a function is defined for the specified class.
	 *
	 * @param classToConvert the class of the values that the function converts
	 * @return {@code true} if the table contains a function for the class, {@code false} otherwise
	 */
	public boolean contains(Class<?> classToConvert) {
		return conversionMap.containsKey(classToConvert);
	}

	/**
	 * Converts a value using the conversion function that corresponds to its type.
	 *
	 * @param value the value to convert, may be null
	 * @param <T>   the value's type
	 * @return the result of the conversion
	 */
	public <T> Object convert(T value) {
		Class<?> classToConvert = (value == null) ? null : value.getClass();
		Function<T, Object> conversionFunction = (Function)conversionMap.get(classToConvert);
		if (conversionFunction == null) {
			return value;
		}
		return conversionFunction.apply(value);
	}

	/**
	 * Performs a shallow in-place conversion of a Config. Each first-level value of the config
	 * is converted using the conversion function that corresponds to its type.
	 *
	 * @param config the config to convert
	 */
	public void convertShallow(Config config) {
		for (Map.Entry<String, Object> configEntry : config.asMap().entrySet()) {
			configEntry.setValue(convert(configEntry.getValue()));
		}
	}

	/**
	 * Performs a deep in-place conversion of a Config. Each simple (ie non-Config) value of the
	 * config is converted using the conversion function that corresponds to its type.
	 *
	 * @param config the config to convert
	 */
	public void convertDeep(Config config) {
		for (Map.Entry<String, Object> configEntry : config.asMap().entrySet()) {
			final Object configValue = configEntry.getValue();
			if (configValue instanceof Config) {// Sub config
				convertDeep(config);
			} else {// Plain value
				configEntry.setValue(convert(configValue));
			}
		}
	}

	/**
	 * Returns an UnmodifiableConfig that converts "just-in-time" the values of the specified
	 * UnmodifiableConfig.
	 *
	 * @param config the config to wrap
	 * @return a wrapper that converts the config's values using this conversion table.
	 */
	public UnmodifiableConfig wrap(UnmodifiableConfig config) {
		return new UnmodifiableConfig() {
			@Override
			public <T> T getValue(List<String> path) {
				return (T)convert(config.getValue(path));
			}

			@Override
			public boolean containsValue(List<String> path) {
				return config.containsValue(path);
			}

			@Override
			public int size() {
				return config.size();
			}

			@Override
			public Map<String, Object> asMap() {
				return new TransformingMap<>(config.asMap(), v -> convert(v), v -> v, v -> v);
			}
		};
	}

	/**
	 * Returns an Config that converts "just-in-time" the values that are read from the specified
	 * Config.
	 *
	 * @param config the config to wrap
	 * @return a wrapper that converts the values read from the config
	 */
	public Config wrapRead(Config config) {
		return new Config() {
			@Override
			public void setValue(List<String> path, Object value) {
				config.setValue(path, value);
			}

			@Override
			public void removeValue(List<String> path) {
				config.removeValue(path);
			}

			@Override
			public Map<String, Object> asMap() {
				return new TransformingMap<>(config.asMap(), v -> convert(v), v -> v, v -> v);
			}

			@Override
			public boolean supportsType(Class<?> type) {
				return config.supportsType(type);
			}

			@Override
			public <T> T getValue(List<String> path) {
				return (T)convert(config.getValue(path));
			}

			@Override
			public boolean containsValue(List<String> path) {
				return config.containsValue(path);
			}

			@Override
			public int size() {
				return config.size();
			}
		};
	}

	/**
	 * Returns an Config that converts "just-in-time" the values that are put into the specified
	 * Config.
	 *
	 * @param config the config to wrap
	 * @return a wrapper that converts the values put into the config
	 */
	public Config wrapWrite(Config config, Predicate<Class<?>> supportValueTypePredicate) {
		return new Config() {
			@Override
			public void setValue(List<String> path, Object value) {
				config.setValue(path, convert(value));
			}

			@Override
			public void removeValue(List<String> path) {
				config.removeValue(path);
			}

			@Override
			public Map<String, Object> asMap() {
				return new TransformingMap<>(config.asMap(), v -> v, v -> convert(v),
											 v -> convert(v));
			}

			@Override
			public boolean supportsType(Class<?> type) {
				return supportValueTypePredicate.test(type);
			}

			@Override
			public <T> T getValue(List<String> path) {
				return config.getValue(path);
			}

			@Override
			public boolean containsValue(List<String> path) {
				return config.containsValue(path);
			}

			@Override
			public int size() {
				return config.size();
			}
		};
	}

	/**
	 * Returns a ConversionTable that behaves as if the specified table was applied just after
	 * this table, for every conversion.
	 *
	 * @param after the table to apply after this one
	 * @return a ConversionTable that applies this table and then the specified table
	 */
	public ConversionTable chainThen(ConversionTable after) {
		ConversionTable result = new ConversionTable(this);// copies this table
		// Applies after.convert after each conversion copied from this table:
		for (Map.Entry<Class<?>, Function<?, Object>> entry : result.conversionMap.entrySet()) {
			entry.setValue(entry.getValue().andThen(after::convert));
		}
		// Adds the missing conversions:
		for (Map.Entry<Class<?>, Function<?, Object>> entry : after.conversionMap.entrySet()) {
			result.conversionMap.putIfAbsent(entry.getKey(), entry.getValue());
		}
		return result;
	}
}