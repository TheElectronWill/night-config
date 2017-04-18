package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import java.util.List;
import java.util.Set;

/**
 * A fake CommentedConfig that wraps a config that doesn't support comments.
 *
 * @author TheElectronWill
 */
public final class FakeCommentedConfig extends ConfigWrapper<Config> implements CommentedConfig {
	/**
	 * If config is a CommentedConfig, returns it, otherwise returns a new FakeCommentedConfig.
	 *
	 * @param config the config to be commented
	 * @return a CommentedConfig with the same values as the specified config
	 */
	public static CommentedConfig getCommented(Config config) {
		if (config instanceof CommentedConfig) {
			return (CommentedConfig)config;
		}
		return new FakeCommentedConfig(config);
	}

	/**
	 * Creates a FakeUnmodifiableCommentedConfig that gets all its values from the given config.
	 * The FakeUnmodifiableCommentedConfig implements CommentedConfig but all operations on
	 * comments do nothing.
	 *
	 * @param config the config to use for the values
	 */

	public FakeCommentedConfig(Config config) {
		super(config);
	}

	@Override
	public String getComment(List<String> path) {
		return null;
	}

	@Override
	public boolean containsComment(List<String> path) {
		return false;
	}

	@Override
	public String setComment(List<String> path, String comment) {
		return null;
	}

	@Override
	public String removeComment(List<String> path) {
		return null;
	}

	@Override
	public Set<? extends CommentedConfig.Entry> entrySet() {
		return new TransformingSet<>(config.entrySet(), FakeCommentedEntry::new, o -> null, o -> o);
	}

	private static final class FakeCommentedEntry implements CommentedConfig.Entry {
		private final Config.Entry entry;

		private FakeCommentedEntry(Config.Entry entry) {
			this.entry = entry;
		}

		@Override
		public String getComment() {
			return null;
		}

		@Override
		public String getKey() {
			return entry.getKey();
		}

		@Override
		public <T> T getValue() {
			return entry.getValue();
		}

		@Override
		public String setComment(String comment) {
			return null;
		}

		@Override
		public String removeComment() {
			return null;
		}

		@Override
		public Object setValue(Object value) {
			return entry.setValue(value);
		}
	}
}