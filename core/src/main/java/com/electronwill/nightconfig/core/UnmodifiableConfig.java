package com.electronwill.nightconfig.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.electronwill.nightconfig.core.utils.StringUtils.split;

/**
 * An unmodifiable (read-only) configuration that contains key/value mappings.
 *
 * @author TheElectronWill
 */
public interface UnmodifiableConfig {
	/**
	 * Gets a value from the config.
	 *
	 * @param path the value's path, each part separated by a dot. Example "a.b.c"
	 * @param <T>  the value's type
	 * @return the value at the given path, or {@code null} if there is no such value.
	 */
	default <T> T getValue(String path) {
		return getValue(split(path, '.'));
	}

	/**
	 * Gets a value from the config.
	 *
	 * @param path the value's path, each element of the list is a different part of the path.
	 * @param <T>  the value's type
	 * @return the value at the given path, or {@code null} if there is no such value.
	 */
	<T> T getValue(List<String> path);

	/**
	 * Gets an optional value from the config.
	 *
	 * @param path the value's path, each part separated by a dot. Example "a.b.c"
	 * @param <T>  the value's type
	 * @return an Optional containing the value at the given path, or {@code Optional.empty()} if
	 * there is no such value.
	 */
	default <T> Optional<T> getOptionalValue(String path) {
		return getOptionalValue(split(path, '.'));
	}

	/**
	 * Gets an optional value from the config.
	 *
	 * @param path the value's path, each element of the list is a different part of the path.
	 * @param <T>  the value's type
	 * @return an Optional containing the value at the given path, or {@code Optional.empty()} if
	 * there is no such value.
	 */
	default <T> Optional<T> getOptionalValue(List<String> path) {
		return Optional.ofNullable(getValue(path));
	}

	/**
	 * Checks if the config contains a value at some path.
	 *
	 * @param path the path to check, each part separated by a dot. Example "a.b.c"
	 * @return {@code true} if the path is associated with a value, {@code false} if it's not.
	 */
	default boolean containsValue(String path) {
		return containsValue(split(path, '.'));
	}

	/**
	 * Checks if the config contains a value at some path.
	 *
	 * @param path the path to check, each element of the list is a different part of the path.
	 * @return {@code true} if the path is associated with a value, {@code false} if it's not.
	 */
	boolean containsValue(List<String> path);

	/**
	 * Gets the size of the config.
	 *
	 * @return the number of top-level elements in the config.
	 */
	int size();

	/**
	 * Checks if the config is empty.
	 *
	 * @return {@code true} if the config is empty, {@code false} if it contains at least one
	 * element.
	 */
	default boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Returns a Map view of the config's values. If the config is unmodifiable then the returned
	 * map is unmodifiable too.
	 *
	 * @return a Map view of the config's values.
	 */
	Map<String, Object> valueMap();

	/**
	 * Returns a Set view of the config's entries. If the config is unmodifiable then the returned
	 * set is unmodifiable too.
	 *
	 * @return a Set view of the config's entries.
	 */
	Set<? extends Entry> entrySet();

	/**
	 * An unmodifiable config entry.
	 */
	interface Entry {
		/**
		 * @return the entry's key
		 */
		String getKey();

		/**
		 * @param <T> the value's type
		 * @return the entry's value
		 */
		<T> T getValue();
	}
}