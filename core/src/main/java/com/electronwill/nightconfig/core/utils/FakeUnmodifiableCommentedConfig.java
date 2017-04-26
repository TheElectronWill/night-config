package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fake UnmodifiableCommentedConfig that wraps a config that doesn't support comments.
 *
 * @author TheElectronWill
 */
public final class FakeUnmodifiableCommentedConfig
		extends UnmodifiableConfigWrapper<UnmodifiableConfig>
		implements UnmodifiableCommentedConfig {
	/**
	 * If config is an UnmodifiableCommentedConfig, returns it, otherwise returns a new
	 * FakeUnmodifiableCommentedConfig.
	 *
	 * @param config the config to be commented
	 * @return a CommentedConfig with the same values as the specified config
	 */
	public static UnmodifiableCommentedConfig getCommented(UnmodifiableConfig config) {
		if (config instanceof UnmodifiableCommentedConfig) {
			return (UnmodifiableCommentedConfig)config;
		}
		return new FakeUnmodifiableCommentedConfig(config);
	}

	/**
	 * Creates a FakeUnmodifiableCommentedConfig that gets all its values from the given config.
	 * The FakeUnmodifiableCommentedConfig implements CommentedConfig but all operations on
	 * comments do nothing.
	 *
	 * @param config the config to use for the values
	 */
	public FakeUnmodifiableCommentedConfig(UnmodifiableConfig config) {
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
	public Map<String, CommentNode> getComments() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, String> commentMap() {
		return Collections.emptyMap();
	}

	@Override
	public Set<? extends UnmodifiableCommentedConfig.Entry> entrySet() {
		return new TransformingSet<>(config.entrySet(), FakeCommentedEntry::new, o -> null, o -> o);
	}

	private static final class FakeCommentedEntry implements UnmodifiableCommentedConfig.Entry {
		private final UnmodifiableConfig.Entry entry;

		private FakeCommentedEntry(UnmodifiableConfig.Entry entry) {
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
	}
}