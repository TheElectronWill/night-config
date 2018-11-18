package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * A ConfigFormat that is only in memory. Its method {@link #isInMemory()} returns always true.
 *
 * @author TheElectronWill
 */
public final class InMemoryFormat implements ConfigFormat<Config> {
	static final Predicate<Class<?>> DEFAULT_PREDICATE = type -> type.isPrimitive()
																 || type == Integer.class
																 || type == Long.class
																 || type == Float.class
																 || type == Double.class
																 || type == Boolean.class
																 || type == String.class
																 || type == NullObject.class
																 || Collection.class.isAssignableFrom(type)
																 || Config.class.isAssignableFrom(type);

	private static final InMemoryFormat DEFAULT_INSTANCE = new InMemoryFormat(DEFAULT_PREDICATE);
	private static final InMemoryFormat UNIVERSAL_INSTANCE = new InMemoryFormat(t -> true);

	public static InMemoryFormat defaultInstance() {
		return DEFAULT_INSTANCE;
	}

	public static InMemoryFormat withSupport(Predicate<Class<?>> supportPredicate) {
		return new InMemoryFormat(supportPredicate);
	}

	public static InMemoryFormat withUniversalSupport() {
		return UNIVERSAL_INSTANCE;
	}

	private final Predicate<Class<?>> supportPredicate;

	private InMemoryFormat(Predicate<Class<?>> supportPredicate) {
		this.supportPredicate = supportPredicate;
	}

	@Override
	public ConfigWriter createWriter() {
		throw new UnsupportedOperationException(
			"In memory configurations aren't meant to be written.");
	}

	@Override
	public ConfigParser<Config> createParser() {
		throw new UnsupportedOperationException(
			"In memory configurations aren't meant to be parsed.");
	}

	@Override
	public Config createConfig() {
		return Config.of(this);
	}

	@Override
	public Config createConcurrentConfig() {
		return Config.ofConcurrent(this);
	}

	@Override
	public boolean supportsComments() {
		return false;
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return supportPredicate.test(type);
	}

	@Override
	public boolean isInMemory() {
		return true;
	}
}