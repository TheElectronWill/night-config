package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.SimpleCommentedConfig;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
public class InMemoryCommentedFormat
		implements ConfigFormat<SimpleCommentedConfig, Config, Config> {
	private static final InMemoryCommentedFormat DEFAULT_INSTANCE = new InMemoryCommentedFormat(
			InMemoryFormat.DEFAULT_PREDICATE);

	public static InMemoryCommentedFormat defaultInstance() {
		return DEFAULT_INSTANCE;
	}

	public static InMemoryCommentedFormat withSupport(Predicate<Class<?>> supportPredicate) {
		return new InMemoryCommentedFormat(supportPredicate);
	}

	private final Predicate<Class<?>> supportPredicate;

	private InMemoryCommentedFormat(Predicate<Class<?>> supportPredicate) {
		this.supportPredicate = supportPredicate;
	}

	@Override
	public ConfigWriter<Config> createWriter() {
		throw new UnsupportedOperationException(
				"In memory configurations aren't mean to be " + "written.");
	}

	@Override
	public ConfigParser<SimpleCommentedConfig, Config> createParser() {
		throw new UnsupportedOperationException(
				"In memory configurations aren't mean to be " + "parsed.");
	}

	@Override
	public SimpleCommentedConfig createConfig() {
		return new SimpleCommentedConfig();
	}

	@Override
	public boolean supportsComments() {
		return true;
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return true;
	}

	@Override
	public boolean isInMemory() {
		return true;
	}
}