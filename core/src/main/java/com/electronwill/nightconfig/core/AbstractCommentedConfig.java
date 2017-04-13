package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.TransformingSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author TheElectronWill
 */
public abstract class AbstractCommentedConfig extends AbstractConfig implements CommentedConfig {
	private final Map<String, Object> commentsMap;

	public AbstractCommentedConfig() {
		this.commentsMap = new HashMap<>();
	}

	public AbstractCommentedConfig(Map<String, Object> valuesMap) {
		this(valuesMap, new HashMap<>(0));
	}

	public AbstractCommentedConfig(Map<String, Object> valuesMap, Map<String, Object> commentsMap) {
		super(valuesMap);
		this.commentsMap = commentsMap;
	}

	@Override
	public String getComment(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> currentMap = commentsMap;
		for (String key : path.subList(0, lastIndex)) {
			Object value = currentMap.get(key);
			if (!(value instanceof Map)) {//missing or incompatible intermediary level
				return null;//the specified path doesn't exist -> return null
			}
			currentMap = (Map)value;
		}
		String lastKey = path.get(lastIndex);
		return (String)currentMap.get(lastKey);
	}

	@Override
	public String setComment(List<String> path, String comment) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> currentMap = commentsMap;
		for (String currentKey : path.subList(0, lastIndex)) {
			final Object currentValue = currentMap.get(currentKey);
			if (currentValue == null) {//missing intermediary level
				Map newMap = new HashMap<>();
				currentMap.put(currentKey, newMap);
				currentMap = newMap;
			} else if (!(currentValue instanceof Map)) {//incompatible intermediary level
				throw new IllegalArgumentException(
						"Cannot add a comment to an intermediary value of type: "
						+ currentValue.getClass());
			} else {//existing intermediary level
				currentMap = (Map)currentValue;
			}
		}
		String lastKey = path.get(lastIndex);
		return (String)currentMap.put(lastKey, comment);
	}

	@Override
	public void removeComment(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> currentMap = commentsMap;
		for (String key : path.subList(0, lastIndex)) {
			Object value = currentMap.get(key);
			if (!(value instanceof Map)) {//missing or incompatible intermediary level
				return;//the specified path doesn't exist -> stop here
			}
			currentMap = (Map)value;
		}
		String lastKey = path.get(lastIndex);
		currentMap.remove(lastKey);
	}

	@Override
	public boolean containsComment(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, Object> currentMap = commentsMap;
		for (String key : path.subList(0, lastIndex)) {
			Object value = currentMap.get(key);
			if (!(value instanceof Map)) {//missing or incompatible intermediary level
				return false;//the specified path doesn't exist -> return false
			}
			currentMap = (Map)value;
		}
		String lastKey = path.get(lastIndex);
		return currentMap.containsKey(lastKey);
	}

	@Override
	public Set<? extends CommentedConfig.Entry> entrySet() {
		return new TransformingSet<>(map.entrySet(), CommentedEntryWrapper::new, o -> null, o -> o);
	}

	protected class CommentedEntryWrapper extends EntryWrapper implements CommentedConfig.Entry {
		private List<String> path = null;

		public CommentedEntryWrapper(Map.Entry<String, Object> mapEntry) {
			super(mapEntry);
		}

		protected List<String> getPath() {
			if (path == null) {
				path = Collections.singletonList(getKey());
			}
			return path;
		}

		@Override
		public String getComment() {
			return AbstractCommentedConfig.this.getComment(getPath());
		}

		@Override
		public String setComment(String comment) {
			return AbstractCommentedConfig.this.setComment(getPath(), comment);
		}

		@Override
		public void removeComment() {
			AbstractCommentedConfig.this.removeComment(getPath());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof CommentedEntryWrapper) {
				CommentedEntryWrapper other = (CommentedEntryWrapper)obj;
				return Objects.equals(getKey(), other.getKey())
					   && Objects.equals(getValue(), other.getValue())
					   && Objects.equals(getComment(), other.getComment());
			}
			return false;
		}

		@Override
		public int hashCode() {
			int result = 1;
			result = 31 * result + Objects.hashCode(getKey());
			result = 31 * result + Objects.hashCode(getValue());
			result = 31 * result + Objects.hashCode(getComment());
			return result;
		}
	}
}
