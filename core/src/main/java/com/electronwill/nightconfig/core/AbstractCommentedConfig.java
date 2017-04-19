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
	private final Map<String, CommentInfos> commentMap;

	public AbstractCommentedConfig() {
		this.commentMap = new HashMap<>();
	}

	/**
	 * Creates an AbstractCommentedConfig backed by the specified map
	 *
	 * @param valuesMap the map containing the config's values
	 */
	public AbstractCommentedConfig(Map<String, Object> valuesMap) {
		super(valuesMap);
		this.commentMap = new HashMap<>();
	}

	/**
	 * Creates an AbstractCommentedConfig that is a copy of the specified config.
	 *
	 * @param toCopy the config to copy
	 */
	public AbstractCommentedConfig(UnmodifiableConfig toCopy) {
		super(toCopy);
		this.commentMap = new HashMap<>();
	}

	/**
	 * Creates an AbstractCommentedConfig that is a copy of the specified config.
	 *
	 * @param toCopy the config to copy
	 */
	public AbstractCommentedConfig(AbstractCommentedConfig toCopy) {
		super(toCopy.valueMap());
		this.commentMap = new HashMap<>(toCopy.commentMap);
	}

	/**
	 * Creates an AbstractCommentedConfig that is a copy of the specified config.
	 *
	 * @param toCopy the config to copy
	 */
	public AbstractCommentedConfig(UnmodifiableCommentedConfig toCopy) {
		super(toCopy);
		Set<? extends UnmodifiableCommentedConfig.Entry> entries = toCopy.entrySet();
		commentMap = new HashMap<>(entries.size());
		for (UnmodifiableCommentedConfig.Entry entry : entries) {
			final String key = entry.getKey();
			final String comment = entry.getComment();
			final Object value = entry.getValue();
			final Map<String, CommentInfos> subInfos = (value instanceof Config) ? new HashMap<>() : null;
			if (comment != null || subInfos != null) {
				commentMap.put(key, new CommentInfos(comment, subInfos));
			}
		}
	}

	@Override
	public String getComment(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, CommentInfos> currentMap = commentMap;
		for (String key : path.subList(0, lastIndex)) {
			CommentInfos infos = currentMap.get(key);
			if (infos == null || !infos.hasSubInfos()) {//no comment associated to this path
				return null;
			}
			currentMap = infos.subInfos;
		}
		String lastKey = path.get(lastIndex);
		CommentInfos lastInfos = currentMap.get(lastKey);
		return (lastInfos == null) ? null : lastInfos.comment;
	}

	@Override
	public String setComment(List<String> path, String comment) {
		final int lastIndex = path.size() - 1;
		Map<String, CommentInfos> currentMap = commentMap;
		for (String currentKey : path.subList(0, lastIndex)) {
			final CommentInfos infos = currentMap.get(currentKey);
			if (infos == null) {//missing intermediary level
				CommentInfos newInfos = new CommentInfos(new HashMap<>());
				currentMap.put(currentKey, newInfos);
				currentMap = newInfos.subInfos;
			} else if (!infos.hasSubInfos()) {//missing sub level
				currentMap = new HashMap<>();
				infos.subInfos = currentMap;
			} else {//existing intermediary level
				currentMap = infos.subInfos;
			}
		}
		String lastKey = path.get(lastIndex);
		CommentInfos lastInfos = currentMap.get(lastKey);
		if (lastInfos == null) {
			currentMap.put(lastKey, new CommentInfos(comment));
			return null;
		}
		String previousComment = lastInfos.comment;
		lastInfos.comment = comment;
		return previousComment;
	}

	@Override
	public String removeComment(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, CommentInfos> currentMap = commentMap;
		for (String key : path.subList(0, lastIndex)) {
			CommentInfos infos = currentMap.get(key);
			if (infos == null || !infos.hasSubInfos()) {//no comment associated to this path
				return null;
			}
			currentMap = infos.subInfos;
		}
		String lastKey = path.get(lastIndex);
		CommentInfos lastInfos = currentMap.remove(lastKey);
		return (lastInfos == null) ? null: lastInfos.comment;
	}

	@Override
	public boolean containsComment(List<String> path) {
		final int lastIndex = path.size() - 1;
		Map<String, CommentInfos> currentMap = commentMap;
		for (String key : path.subList(0, lastIndex)) {
			CommentInfos infos = currentMap.get(key);
			if (infos == null || !infos.hasSubInfos()) {//no comment associated to this path
				return false;
			}
			currentMap = infos.subInfos;
		}
		String lastKey = path.get(lastIndex);
		return currentMap.containsKey(lastKey);
	}

	@Override
	public Set<? extends CommentedConfig.Entry> entrySet() {
		return new TransformingSet<>(map.entrySet(), CommentedEntryWrapper::new, o -> null, o -> o);
	}

	/**
	 * @return a new config that contains the same entries (including comments) as this config.
	 */
	@Override
	public abstract AbstractCommentedConfig clone();

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
		public String removeComment() {
			return AbstractCommentedConfig.this.removeComment(getPath());
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

	private static class CommentInfos {
		String comment;
		Map<String, CommentInfos> subInfos;

		CommentInfos(String comment) {
			this.comment = comment;
		}

		CommentInfos(Map<String, CommentInfos> subInfos) {
			this.subInfos = subInfos;
		}

		CommentInfos(String comment, Map<String, CommentInfos> subInfos) {
			this.comment = comment;
			this.subInfos = subInfos;
		}

		boolean hasSubInfos() {
			return subInfos != null;
		}
	}
}
