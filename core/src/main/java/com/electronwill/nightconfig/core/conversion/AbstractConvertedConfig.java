package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
abstract class AbstractConvertedConfig<C extends Config> extends ConfigWrapper<C>
		implements Config {
	final Function<Object, Object> readConversion, writeConversion;
	final Predicate<Class<?>> supportPredicate;

	AbstractConvertedConfig(C config, Function<Object, Object> readConversion,
								   Function<Object, Object> writeConversion,
								   Predicate<Class<?>> supportPredicate) {
		super(config);
		this.readConversion = readConversion;
		this.writeConversion = writeConversion;
		this.supportPredicate = supportPredicate;
	}

	@Override
	public <T> T setValue(List<String> path, Object value) {
		return (T)readConversion.apply(config.setValue(path, writeConversion.apply(value)));
	}

	@Override
	public Map<String, Object> valueMap() {
		return new TransformingMap<>(config.valueMap(), readConversion, writeConversion,
									 writeConversion);
	}

	@Override
	public <T> T getValue(List<String> path) {
		return (T)readConversion.apply(config.getValue(path));
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return supportPredicate.test(type);
	}
}
