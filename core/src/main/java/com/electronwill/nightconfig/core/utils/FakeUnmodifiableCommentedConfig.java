package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.List;
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
	 */
	public static UnmodifiableCommentedConfig getCommented(UnmodifiableConfig config) {
		if (config instanceof UnmodifiableCommentedConfig) {
			return (UnmodifiableCommentedConfig)config;
		}
		return new FakeUnmodifiableCommentedConfig(config);
	}

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