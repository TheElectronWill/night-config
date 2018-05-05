package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.TransformingSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract CommentedConfig backed by two maps: one for the values and one for the comments.
 * The comments of the subconfigs are stored in these subconfigs.
 *
 * @author TheElectronWill
 */
public abstract class AbstractCommentedConfig extends AbstractConfig implements CommentedConfig {
	private final Map<String, String> commentMap;

	public AbstractCommentedConfig(boolean concurrent) {
		super(concurrent);
		this.commentMap = concurrent ? new ConcurrentHashMap<>() : new HashMap<>();
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
	public AbstractCommentedConfig(UnmodifiableConfig toCopy, boolean concurrent) {
		super(toCopy, concurrent);
		this.commentMap = concurrent ? new ConcurrentHashMap<>() : new HashMap<>();
	}

	/**
	 * Creates an AbstractCommentedConfig that is a copy of the specified config.
	 *
	 * @param toCopy the config to copy
	 */
	public AbstractCommentedConfig(UnmodifiableCommentedConfig toCopy, boolean concurrent) {
		super(toCopy.valueMap());
		this.commentMap = new HashMap<>(toCopy.commentMap());
	}

	@Override
	public String getComment(List<String> path) {
		final int lastIndex = path.size() - 1;
		final String lastKey = path.get(lastIndex);
		if (lastIndex == 0) {
			return commentMap.get(lastKey);
		}
		Object parent = get(path.subList(0, lastIndex));
		if (parent instanceof UnmodifiableCommentedConfig) {
			List<String> lastPath = Collections.singletonList(lastKey);
			return ((UnmodifiableCommentedConfig)parent).getComment(lastPath);
		}
		return null;
	}

	@Override
	public String setComment(List<String> path, String comment) {
		final int lastIndex = path.size() - 1;
		final String lastKey = path.get(lastIndex);
		if (lastIndex == 0) {
			return commentMap.put(lastKey, comment);
		}
		final List<String> parentPath = path.subList(0, lastIndex);
		Object parent = get(parentPath);
		List<String> lastPath = Collections.singletonList(lastKey);
		if (parent instanceof CommentedConfig) {
			return ((CommentedConfig)parent).setComment(lastPath, comment);
		} else if (parent == null) {
			CommentedConfig commentedParent = createSubConfig();
			set(parentPath, commentedParent);
			return commentedParent.setComment(lastPath, comment);
		}
		throw new IllegalArgumentException("Cannot set a comment to path "
										   + path
										   + " because the parent entry is of incompatible type "
										   + parent.getClass());
	}

	@Override
	public String removeComment(List<String> path) {
		final int lastIndex = path.size() - 1;
		final String lastKey = path.get(lastIndex);
		if (lastIndex == 0) {
			return commentMap.remove(lastKey);
		}
		Object parent = get(path.subList(0, lastIndex));
		if (parent instanceof CommentedConfig) {
			List<String> lastPath = Collections.singletonList(lastKey);
			return ((CommentedConfig)parent).removeComment(lastPath);
		}
		return null;
	}

	@Override
	public boolean containsComment(List<String> path) {
		final int lastIndex = path.size() - 1;
		final String lastKey = path.get(lastIndex);
		if (lastIndex == 0) {
			return commentMap.containsKey(lastKey);
		}
		Object parent = get(path.subList(0, lastIndex));
		if (parent instanceof CommentedConfig) {
			List<String> lastPath = Collections.singletonList(lastKey);
			return ((CommentedConfig)parent).containsComment(lastPath);
		}
		return false;
	}

	@Override
	public Map<String, String> commentMap() {
		return commentMap;
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

	@Override
	public void clear() {
		super.clear();
		clearComments();
	}

	@Override
	public void clearComments() {
		commentMap.clear();
		// Recursively clears the comments of the subconfigs:
		for (Object o : map.values()) {
			if (o instanceof CommentedConfig) {
				((CommentedConfig)o).clearComments();
			}
		}
		//NB: The UnmodifiableCommentedConfigs cannot be cleared from their comments.
	}
}
