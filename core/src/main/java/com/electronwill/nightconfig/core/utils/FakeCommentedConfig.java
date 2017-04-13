package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fake CommentedConfig that wraps a config that doesn't support comments.
 *
 * @author TheElectronWill
 */
public final class FakeCommentedConfig implements CommentedConfig {
	private final Config config;

	public FakeCommentedConfig(Config config) {
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
	public String setComment(List<String> path, String comment) {
		return null;
	}

	@Override
	public void removeComment(List<String> path) {}

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
	public <T> T setValue(List<String> path, Object value) {
		return config.setValue(path, value);
	}

	@Override
	public <T> T removeValue(List<String> path) {
		return config.removeValue(path);
	}

	@Override
	public void clear() {
		config.clear();
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

	@Override
	public boolean supportsType(Class<?> type) {
		return config.supportsType(type);
	}

	private static final class FakeCommentedEntry implements Entry {
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
		public void removeComment() {}

		@Override
		public Object setValue(Object value) {
			return entry.setValue(value);
		}
	}
}