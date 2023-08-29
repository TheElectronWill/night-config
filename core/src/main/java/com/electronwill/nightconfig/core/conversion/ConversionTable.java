package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import com.electronwill.nightconfig.core.utils.TransformingSet;
import com.electronwill.nightconfig.core.utils.UnmodifiableConfigWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Contains conversions functions organized by value's type. A ConversionTable grows as necessary.
 *
 * @author TheElectronWill
 */
public final class ConversionTable implements Cloneable {
	/**
	 * Maps a class (of a value) to its conversion function.
	 */
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
	 * @return the result of the conversion
	 */
	public Object convert(Object value) {
		Function<Object, Object> conversionFunction = getConversionFunction(value);
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
		for (Map.Entry<String, Object> configEntry : config.valueMap().entrySet()) {
			final Object value = configEntry.getValue();
			Function<Object, Object> conversionFunction = getConversionFunction(value);
			if (conversionFunction != null) {
				configEntry.setValue(conversionFunction.apply(value));
			}
		}
	}

	/**
	 * Performs a deep in-place conversion of a Config. Each simple (ie non-Config) value of the
	 * config is converted using the conversion function that corresponds to its type.
	 *
	 * @param config the config to convert
	 */
	public void convertDeep(Config config) {
		for (Map.Entry<String, Object> configEntry : config.valueMap().entrySet()) {
			final Object value = configEntry.getValue();
			if (value instanceof Config) {// Sub config
				convertDeep(config);
			} else {// Plain value
				Function<Object, Object> conversionFunction = getConversionFunction(value);
				if (conversionFunction != null) {
					configEntry.setValue(conversionFunction.apply(value));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Function<Object, Object> getConversionFunction(Object value) {
		if (value == null) {
			return (Function<Object, Object>)conversionMap.get(null);
		} else {
			Class<?> clazz = value.getClass();
			Function<?, Object> conversionFunction = conversionMap.get(clazz);
			// Traverses the class hierarchy:
			while (conversionFunction == null) {
				clazz = clazz.getSuperclass();
				if (clazz == null) {
					break;
				}
				conversionFunction = conversionMap.get(clazz);
			}
			return (Function<Object, Object>)conversionFunction;
		}
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

	/**
	 * Returns an UnmodifiableConfig that converts "just-in-time" the values of the specified
	 * UnmodifiableConfig.
	 *
	 * @param config the config to wrap
	 * @return a wrapper that converts the config's values using this conversion table.
	 */
	public UnmodifiableConfig wrap(UnmodifiableConfig config) {
		return new UnmodifiableConfigWrapper<UnmodifiableConfig>(config) {
			@Override
			public <T> T getRaw(List<String> path) {
				return (T)convert(config.getRaw(path));
			}

			@Override
			public Map<String, Object> valueMap() {
				return new TransformingMap<>(config.valueMap(), v -> convert(v), v -> v, v -> v);
			}

			@Override
			public Set<? extends Entry> entrySet() {
				Function<Entry, Entry> readTransfo = entry -> new Entry() {
					@Override
					public String getKey() {
						return entry.getKey();
					}

					@Override
					public <T> T getRawValue() {
						return (T)convert(entry.getRawValue());
					}
				};
				return new TransformingSet<>(config.entrySet(), readTransfo, o -> null, e -> e);
			}

			@Override
			public ConfigFormat<?> configFormat() {
				return config.configFormat();
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
		return new ConvertedConfig(config, this::convert, v -> v,
								   config.configFormat()::supportsType);
	}

	/**
	 * Returns an Config that converts "just-in-time" the values that are read from the specified
	 * Config.
	 *
	 * @param config the config to wrap
	 * @return a wrapper that converts the values read from the config
	 */
	public CommentedConfig wrapRead(CommentedConfig config) {
		return new ConvertedCommentedConfig(config, this::convert, v -> v,
											config.configFormat()::supportsType);
	}

	/**
	 * Returns an Config that converts "just-in-time" the values that are read from the specified
	 * Config.
	 *
	 * @param config the config to wrap
	 * @return a wrapper that converts the values read from the config
	 */
	public FileConfig wrapRead(FileConfig config) {
		return new ConvertedFileConfig(config, this::convert, v -> v,
									   config.configFormat()::supportsType);
	}

	/**
	 * Returns an Config that converts "just-in-time" the values that are read from the specified
	 * Config.
	 *
	 * @param config the config to wrap
	 * @return a wrapper that converts the values read from the config
	 */
	public CommentedFileConfig wrapRead(CommentedFileConfig config) {
		return new ConvertedCommentedFileConfig(config, this::convert, v -> v,
												config.configFormat()::supportsType);
	}

	/**
	 * Returns an Config that converts "just-in-time" the values that are put into the specified
	 * Config.
	 *
	 * @param config                    the config to wrap
	 * @param supportValueTypePredicate Predicate that checks if a given class is supported by the
	 *                                  returned config
	 * @return a wrapper that converts the values put into the config
	 */
	public Config wrapWrite(Config config, Predicate<Class<?>> supportValueTypePredicate) {
		return new ConvertedConfig(config, v -> v, this::convert, supportValueTypePredicate);
	}

	/**
	 * Returns an Config that converts "just-in-time" the values that are put into the specified
	 * Config.
	 *
	 * @param config                    the config to wrap
	 * @param supportValueTypePredicate Predicate that checks if a given class is supported by the
	 *                                  returned config
	 * @return a wrapper that converts the values put into the config
	 */
	public CommentedConfig wrapWrite(CommentedConfig config,
									 Predicate<Class<?>> supportValueTypePredicate) {
		return new ConvertedCommentedConfig(config, v -> v, this::convert,
											supportValueTypePredicate);
	}

	/**
	 * Returns an Config that converts "just-in-time" the values that are put into the specified
	 * Config.
	 *
	 * @param config                    the config to wrap
	 * @param supportValueTypePredicate Predicate that checks if a given class is supported by the
	 *                                  returned config
	 * @return a wrapper that converts the values put into the config
	 */
	public FileConfig wrapWrite(FileConfig config, Predicate<Class<?>> supportValueTypePredicate) {
		return new ConvertedFileConfig(config, v -> v, this::convert, supportValueTypePredicate);
	}

	/**
	 * Returns an Config that converts "just-in-time" the values that are put into the specified
	 * Config.
	 *
	 * @param config                    the config to wrap
	 * @param supportValueTypePredicate Predicate that checks if a given class is supported by the
	 *                                  returned config
	 * @return a wrapper that converts the values put into the config
	 */
	public CommentedFileConfig wrapWrite(CommentedFileConfig config,
										 Predicate<Class<?>> supportValueTypePredicate) {
		return new ConvertedCommentedFileConfig(config, v -> v, this::convert,
												supportValueTypePredicate);
	}

	@Override
	public ConversionTable clone() {
		return new ConversionTable(this);
	}

	@Override
	public String toString() {
		return "ConversionTable: " + conversionMap;
	}
}