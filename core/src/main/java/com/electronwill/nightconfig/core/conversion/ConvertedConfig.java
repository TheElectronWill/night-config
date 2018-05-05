package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.TransformingSet;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A Config's wrapper that converts the values that are read from and put into the config
 *
 * @author TheElectronWill
 */
public final class ConvertedConfig extends AbstractConvertedConfig<Config> {
	/**
	 * Creates a new ConvertedConfig that uses two conversion tables.
	 *
	 * @param config           the config to wrap
	 * @param readTable        the ConversionTable used for parse operations (like getValue)
	 * @param writeTable       the ConversionTable used for write operations (like setValue)
	 * @param supportPredicate a Predicate that checks if a given class is supported by the
	 *                         ConvertedConfig
	 */
	public ConvertedConfig(Config config, ConversionTable readTable, ConversionTable writeTable,
						   Predicate<Class<?>> supportPredicate) {
		this(config, readTable::convert, writeTable::convert, supportPredicate);
	}

	/**
	 * Creates a new ConvertedConfig that uses two custom conversion functions.
	 *
	 * @param config           the config to wrap
	 * @param readConversion   the Function used for parse operations (like getValue)
	 * @param writeConversion  the Function used for write operations (like setValue)
	 * @param supportPredicate a Predicate that checks if a given class is supported by the
	 *                         ConvertedConfig
	 */
	public ConvertedConfig(Config config, Function<Object, Object> readConversion,
						   Function<Object, Object> writeConversion,
						   Predicate<Class<?>> supportPredicate) {
		super(config, readConversion, writeConversion, supportPredicate);
	}

	@Override
	public Set<? extends Config.Entry> entrySet() {
		Function<Config.Entry, Config.Entry> readTransfo = entry -> new Config.Entry() {
			@Override
			public Object setValue(Object value) {
				return readConversion.apply(entry.setValue(writeConversion.apply(value)));
			}

			@Override
			public String getKey() {
				return entry.getKey();
			}

			@Override
			public <T> T getValue() {
				return (T)readConversion.apply(entry.getValue());
			}
		};
		return new TransformingSet<>(config.entrySet(), readTransfo, o -> null, e -> e);
	}
}