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
	 */
	public ConvertedConfig(Config config, ConversionTable readTable, ConversionTable writeTable) {
		this(config, readTable::convert, writeTable::convert);
	}

	/**
	 * Creates a new ConvertedConfig that uses two custom conversion functions.
	 *
	 * @param config           the config to wrap
	 * @param readConversion   the Function used for parse operations (like getValue)
	 * @param writeConversion  the Function used for write operations (like setValue)
	 */
	public ConvertedConfig(Config config, Function<Object, Object> readConversion,
						   Function<Object, Object> writeConversion) {
		super(config, readConversion, writeConversion);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Config.Entry> entries() {
		Function<Config.Entry, Config.Entry> readTransfo = entry -> null;/*TODO
		new Config.Entry() {
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
		};*/
		return new TransformingSet<>(config.entries(), readTransfo, o -> null, e -> e);
	}
}