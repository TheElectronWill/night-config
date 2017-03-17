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
 * @author TheElectronWill
 */
public final class ConversionTable {
	private final Map<Class<?>, Function<?, Object>> conversionMap;

	public ConversionTable() {
		this.conversionMap = new HashMap<>();
	}

	private ConversionTable(ConversionTable toCopy) {
		this.conversionMap = new HashMap<>(toCopy.conversionMap);
	}

	public <T> void put(Class<T> classToConvert, Function<? super T, Object> conversionFunction) {
		conversionMap.put(classToConvert, conversionFunction);
	}

	public void remove(Class<?> classToConvert) {
		conversionMap.remove(classToConvert);
	}

	public boolean contains(Class<?> classToConvert) {
		return conversionMap.containsKey(classToConvert);
	}

	public <T> Object convert(T value) {
		Class<?> classToConvert = (value == null) ? null : value.getClass();
		Function<T, Object> conversionFunction = (Function)conversionMap.get(classToConvert);
		if (conversionFunction == null) {
			return value;
		}
		return conversionFunction.apply(value);
	}

	public void convertShallow(Config config) {
		for (Map.Entry<String, Object> configEntry : config.asMap().entrySet()) {
			configEntry.setValue(convert(configEntry.getValue()));
		}
	}

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
