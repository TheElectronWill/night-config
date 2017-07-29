package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
public final class ConvertedFormat<C extends D, D extends Config, W extends UnmodifiableConfig, F extends ConfigFormat<C, D, W>>
		implements ConfigFormat<C, D, W> {
	private final F initialFormat;
	private final Predicate<Class<?>> supportPredicate;

	public ConvertedFormat(F initialFormat, Predicate<Class<?>> supportPredicate) {
		this.initialFormat = initialFormat;
		this.supportPredicate = supportPredicate;
	}

	@Override
	public ConfigWriter<W> createWriter() {
		return initialFormat.createWriter();
	}

	@Override
	public ConfigParser<C, D> createParser() {
		return initialFormat.createParser();
	}

	@Override
	public C createConfig() {
		return initialFormat.createConfig();
	}

	@Override
	public boolean supportsComments() {
		return initialFormat.supportsComments();
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return supportPredicate.test(type);
	}
}