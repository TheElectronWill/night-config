package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;
import com.electronwill.nightconfig.core.utils.TransformingMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
@SuppressWarnings("unchecked")
abstract class AbstractConvertedConfig<C extends Config> extends ConfigWrapper<C>
		implements Config {
	final Function<Object, Object> readConversion, writeConversion;

	AbstractConvertedConfig(C config, Function<Object, Object> readConversion,
							Function<Object, Object> writeConversion) {
		super(config);
		this.readConversion = readConversion;
		this.writeConversion = writeConversion;
	}

	@Override
	public <T> T set(String[] path, Object value) {
		return (T)readConversion.apply(config.set(path, writeConversion.apply(value)));
	}

	@Override
	public Map<String, Object> valueMap() {
		return new TransformingMap<>(config.valueMap(), readConversion, writeConversion,
									 writeConversion);
	}

	@Override
	public <T> T get(String[] path) {
		return (T)readConversion.apply(config.get(path));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ':' + valueMap() + " (original: " + config + ')';
	}
}
