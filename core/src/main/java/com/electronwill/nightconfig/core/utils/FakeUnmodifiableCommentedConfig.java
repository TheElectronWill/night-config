package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fake UnmodifiableCommentedConfig that wraps a config that doesn't support comments.
 *
 * @author TheElectronWill
 */
public final class FakeUnmodifiableCommentedConfig implements UnmodifiableCommentedConfig {
	private final UnmodifiableConfig config;

	public FakeUnmodifiableCommentedConfig(UnmodifiableConfig config) {
		this.config = config;
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
	public Set<? extends Entry> entrySet() {
		return new TransformingSet<>(config.entrySet(), FakeCommentedEntry::new, o -> null, o -> o);
	}

	@Override
	public <T> T getValue(List<String> path) {
		return config.getValue(path);
	}

	@Override
	public boolean containsValue(List<String> path) {
		return config.containsValue(path);
	}

	@Override
	public int size() {
		return config.size();
	}

	@Override
	public Map<String, Object> valueMap() {
		return config.valueMap();
	}

	@Override
	public boolean equals(Object obj) {
		return config.equals(obj);
	}

	@Override
	public int hashCode() {
		return config.hashCode();
	}

	private static final class FakeCommentedEntry implements Entry {
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