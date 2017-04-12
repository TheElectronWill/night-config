package com.electronwill.nightconfig.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.electronwill.nightconfig.core.utils.StringUtils.split;

/**
 * A modifiable config that supports comments.
 *
 * @author TheElectronWill
 */
public interface CommentedConfig extends UnmodifiableCommentedConfig, Config {
	/**
	 * Sets a config comment.
	 *
	 * @param path    the comment's path, each part separated by a dot. Example "a.b.c"
	 * @param comment the comment to set
	 * @return the old comment if any, or {@code null}
	 */
	default String setComment(String path, String comment) {
		return setComment(split(path, '.'), comment);
	}

	/**
	 * Sets a config comment.
	 *
	 * @param path    the comment's path, each element of the list is a different part of the path.
	 * @param comment the comment to set
	 * @return the old comment if any, or {@code null}
	 */
	String setComment(List<String> path, String comment);

	/**
	 * Removes a comment from the config.
	 *
	 * @param path the comment's path, each part separated by a dot. Example "a.b.c"
	 */
	default void removeComment(String path) {
		removeComment(split(path, '.'));
	}

	/**
	 * Removes a comment from the config.
	 *
	 * @param path the comment's path, each element of the list is a different part of the path.
	 */
	void removeComment(List<String> path);

	@Override
	default UnmodifiableCommentedConfig unmodifiable() {
		return new UnmodifiableCommentedConfig() {
			@Override
			public <T> T getValue(List<String> path) {
				return CommentedConfig.this.getValue(path);
			}

			@Override
			public String getComment(List<String> path) {
				return CommentedConfig.this.getComment(path);
			}

			@Override
			public boolean containsValue(List<String> path) {
				return CommentedConfig.this.containsValue(path);
			}

			@Override
			public boolean containsComment(List<String> path) {
				return CommentedConfig.this.containsComment(path);
			}

			@Override
			public int size() {
				return CommentedConfig.this.size();
			}

			@Override
			public Map<String, Object> valueMap() {
				return Collections.unmodifiableMap(CommentedConfig.this.valueMap());
			}

			@Override
			public Set<? extends Entry> entrySet() {
				return CommentedConfig.this.entrySet();
			}
		};
	}

	@Override
	Set<? extends Entry> entrySet();

	/**
	 * A modifiable commented config entry.
	 */
	interface Entry extends Config.Entry, UnmodifiableCommentedConfig.Entry {
		/**
		 * Sets the entry's comment.
		 *
		 * @param comment the comment to set, may contain several lines.
		 * @return the previous comment, or {@code null} if none.
		 */
		String setComment(String comment);

		/**
		 * Removes the entry's comment.
		 *
		 * @return the previous comment, or {@code null} if none.
		 */
		void removeComment();
	}
}
