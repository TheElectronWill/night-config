package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import java.util.function.Predicate;

/**
 * The commented version of {@link InMemoryFormat}
 *
 * @author TheElectronWill
 */
public class InMemoryCommentedFormat implements ConfigFormat<CommentedConfig, Config, Config> {
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
	public ConfigParser<CommentedConfig, Config> createParser() {
		throw new UnsupportedOperationException(
				"In memory configurations aren't mean to be " + "parsed.");
	}

	@Override
	public CommentedConfig createConfig() {
		return CommentedConfig.of(this);
	}

	@Override
	public CommentedConfig createConcurrentConfig() {
		return CommentedConfig.ofConcurrent(this);
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