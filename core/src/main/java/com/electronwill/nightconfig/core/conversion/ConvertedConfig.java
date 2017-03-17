package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
public final class ConvertedConfig implements Config {
	private final Function<Object, Object> readConversion, writeConversion;
	private final Predicate<Class<?>> supportPredicate;
	private final Config config;

	public ConvertedConfig(Config config, ConversionTable readTable, ConversionTable writeTable,
						   Predicate<Class<?>> supportPredicate) {
		this(config, readTable::convert, writeTable::convert, supportPredicate);
	}

	public ConvertedConfig(Config config, Function<Object, Object> readConversion,
						   Function<Object, Object> writeConversion,
						   Predicate<Class<?>> supportPredicate) {
		this.config = config;
		this.readConversion = readConversion;
		this.writeConversion = writeConversion;
		this.supportPredicate = supportPredicate;
	}

	@Override
	public void setValue(List<String> path, Object value) {
		config.setValue(path, writeConversion.apply(value));
	}

	@Override
	public void removeValue(List<String> path) {
		config.removeValue(path);
	}

	@Override
	public Map<String, Object> asMap() {
		return new TransformingMap<>(config.asMap(), readConversion, writeConversion,
									 writeConversion);
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return supportPredicate.test(type);
	}

	@Override
	public <T> T getValue(List<String> path) {
		return (T)readConversion.apply(config.getValue(path));
	}

	@Override
	public boolean containsValue(List<String> path) {
		return config.containsValue(path);
	}

	@Override
	public int size() {
		return config.size();
	}
}