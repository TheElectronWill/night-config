package com.electronwill.nightconfig.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
	 * Gets an optional comment from the config.
	 *
	 * @param path the comment's path, each part separated by a dot. Example "a.b.c"
	 * @return an Optional containing the comment at the given path, or {@code Optional.empty()} if
	 * there is no such comment.
	 */
	default Optional<String> getOptionalComment(String path) {
		return getOptionalComment(split(path, '.'));
	}

	/**
	 * Gets an optional comment from the config.
	 *
	 * @param path the comment's path, each element of the list is a different part of the path.
	 * @return an Optional containing the comment at the given path, or {@code Optional.empty()} if
	 * there is no such comment.
	 */
	default Optional<String> getOptionalComment(List<String> path) {
		return Optional.ofNullable(getComment(path));
	}

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

	/**
	 * Returns a Map view of the config's comments. If the config is unmodifiable then the returned
	 * map is unmodifiable too.
	 * <p>
	 * The comment map contains only the comments of the direct elements of the
	 * configuration, not the comments of their sub-elements.
	 *
	 * @return a Map view of the config's comments.
	 */
	Map<String, String> commentMap();

	@Override
	Set<? extends Entry> entrySet();

	/**
	 * An unmodifiable commented config entry.
	 */
	interface Entry extends UnmodifiableConfig.Entry {
		/**
		 * @return the entry's comment, may contain several lines
		 */
		String getComment();
	}
}
