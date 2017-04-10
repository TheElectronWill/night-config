package com.electronwill.nightconfig.core;

import java.util.List;

import static com.electronwill.nightconfig.core.utils.StringUtils.split;

/**
 * An unmodifiable config that supports comments.
 *
 * @author TheElectronWill
 */
public interface UnmodifiableCommentedConfig extends UnmodifiableConfig {
	/**
	 * Gets a comment from the config.
	 *
	 * @param path the comment's path, each part separated by a dot. Example "a.b.c"
	 * @return the comment at the given path, or {@code null} if there is none.
	 */
	default String getComment(String path) {
		return getComment(split(path, '.'));
	}

	/**
	 * Gets a comment from the config.
	 *
	 * @param path the comment's path, each element of the list is a different part of the path.
	 * @return the comment at the given path, or {@code null} if there is none.
	 */
	String getComment(List<String> path);

	/**
	 * Checks if the config contains a comment at some path.
	 *
	 * @param path the path to check, each part separated by a dot. Example "a.b.c"
	 * @return {@code true} if the path is associated with a comment, {@code false} if it's not.
	 */
	default boolean containsComment(String path) {
		return containsComment(split(path, '.'));
	}

	/**
	 * Checks if the config contains a comment at some path.
	 *
	 * @param path the path to check, each element of the list is a different part of the path.
	 * @return {@code true} if the path is associated with a comment, {@code false} if it's not.
	 */
	boolean containsComment(List<String> path);
}
