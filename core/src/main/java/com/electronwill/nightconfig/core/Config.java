package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.check.CheckedConfig;
import com.electronwill.nightconfig.core.check.ConfigChecker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.electronwill.nightconfig.core.utils.StringUtils.splitPath;

/**
 * A (modifiable) configuration that contains key/value mappings. Configurations are generally
 * <b>not</b> thread-safe.
 *
 * @author TheElectronWill
 */
@SuppressWarnings("unchecked")
public interface Config extends UnmodifiableConfig {
	default EntryData getData(String path) {
		return getData(splitPath(path));
	}

	EntryData getData(String[] path);

	// --- SETTERS FOR VALUES ---
	/**
	 * Adds or modify a value.
	 *
	 * @param path  the value's path, each part separated by a dot. Example "a.b.c"
	 * @param value the value to set
	 * @param <T>   the type of the old value
	 * @return the old value if any, or {@code null}
	 */
	default <T> T set(String path, Object value) {
		return set(splitPath(path), value);
	}

	/**
	 * Adds or modify a value.
	 *
	 * @param path  the value's path, each element is a different part of the path.
	 * @param value the value to set
	 * @param <T>   the type of the old value
	 * @return the old value if any, or {@code null}
	 */
	default <T> T set(String[] path, Object value) {
		return (T)set(StandardAttributes.VALUE, path, value);
	}

	/**
	 * Adds a value to the config. The value is set iff there is no value associated with the given path.
	 *
	 * @param path  the value's path, each part separated by a dot. Example "a.b.c"
	 * @param value the value to set
	 * @return the existing value if any, or {@code null}
	 */
	default Object add(String path, Object value) {
		return add(splitPath(path), value);
	}

	/**
	 * Adds a value to the config. The value is set iff there is no value associated with the given path.
	 *
	 * @param path  the value's path, each element is a different part of the path.
	 * @param value the value to set
	 * @return the existing value if any, or {@code null}
	 */
	default Object add(String[] path, Object value) {
		return add(StandardAttributes.VALUE, path, value);
	}

	/**
	 * Copies a config's entries, without replacing existing entries.
	 *
	 * @param config the source config
	 */
	default void addAll(UnmodifiableConfig config) {
		for (UnmodifiableConfig.Entry entry : config.entries()) {
			String[] key = {entry.getKey()};
			Object value = entry.getValue();
			Object existingValue = add(key, value);
			if (existingValue instanceof Config && value instanceof UnmodifiableConfig) {
				((Config)existingValue).addAll((UnmodifiableConfig)value);
			}
		}
	}

	/**
	 * Copies a config's entries, replacing any existing entries.
	 *
	 * @param config the source config
	 */
	default void putAll(UnmodifiableConfig config) {
		valueMap().putAll(config.valueMap());
	}

	/**
	 * Removes an entry from the config.
	 *
	 * @param path the entry's path, each part separated by a dot. Example "a.b.c"
	 * @param <T>  the type of the old value
	 * @return the old value if any, or {@code null}
	 */
	default <T> T remove(String path) {
		return remove(splitPath(path));
	}

	/**
	 * Removes a value from the config.
	 *
	 * @param path the entry's path, each element is a different part of the path.
	 * @param <T>  the type of the old value
	 * @return the old value if any, or {@code null}
	 */
	default <T> T remove(String[] path) {
		return (T)remove(StandardAttributes.VALUE, path);
	}

	/**
	 * Removes all the values of the given config from this config.
	 *
	 * @param config the values to remove
	 */
	default void removeAll(UnmodifiableConfig config) {
		valueMap().keySet().removeAll(config.valueMap().keySet());
	}

	/** Removes all entries from the config. */
	void clear();

	// --- SETTERS FOR ATTRIBUTES ---
	default <T> T set(AttributeType<T> attribute, String path, T value) {
		return set(attribute, splitPath(path), value);
	}

	<T> T set(AttributeType<T> attribute, String[] path, T value);

	default <T> T add(AttributeType<T> attribute, String path, T value) {
		return add(attribute, splitPath(path), value);
	}

	<T> T add(AttributeType<T> attribute, String[] path, T value);

	default <T> T remove(AttributeType<T> attribute, String path) {
		return remove(attribute, splitPath(path));
	}

	<T> T remove(AttributeType<T> attribute, String[] path);

	/** Removes all non-value attributes from the config. */
	void clearAttributes();

	// --- SETTERS FOR COMMENTS ---
	/**
	 * Sets a config comment.
	 *
	 * @param path    the comment's path, each part separated by a dot. Example "a.b.c"
	 * @param comment the comment to set
	 * @return the old comment if any, or {@code null}
	 */
	default String setComment(String path, String comment) {
		return setComment(splitPath(path), comment);
	}

	/**
	 * Sets a config comment.
	 *
	 * @param path    the comment's path, each element is a different part of the path.
	 * @param comment the comment to set
	 * @return the old comment if any, or {@code null}
	 */
	default String setComment(String[] path, String comment) {
		return set(StandardAttributes.COMMENT, path, comment);
	}

	/**
	 * Removes a comment from the config.
	 *
	 * @param path the comment's path, each part separated by a dot. Example "a.b.c"
	 * @return the old comment if any, or {@code null}
	 */
	default String removeComment(String path) {
		return removeComment(splitPath(path));
	}

	/**
	 * Removes a comment from the config.
	 *
	 * @param path the comment's path, each element is a different part of the path.
	 * @return the old comment if any, or {@code null}
	 */
	default String removeComment(String[] path) {
		return remove(StandardAttributes.COMMENT, path);
	}

	/**
	 * Removes all the comments from the config.
	 */
	void clearComments();

	// --- OTHER METHODS ---
	/**
	 * Returns an Unmodifiable view of the config. Any change to the original (modifiable) config
	 * is still reflected to the returned UnmodifiableConfig, so it's unmodifiable but not
	 * immutable.
	 *
	 * @return an Unmodifiable view of the config.
	 */
	default UnmodifiableConfig unmodifiable() {
		return new CheckedConfig(this, ConfigChecker.freeze());
	}

	/**
	 * Returns a Map view of the config's values. Any change to the map is reflected in the config
	 * and vice-versa.
	 */
	Map<String, Object> valueMap();

	Map<String, EntryData> dataMap();

	@Override
	Set<Entry> entries();

	/**
	 * A modifiable config entry.
	 */
	interface Entry extends UnmodifiableConfig.Entry {
		/**
		 * Sets the entry's value.
		 *
		 * @param value the new value
		 * @param <T>   the type of the old value
		 * @return the old value, if any
		 */
		default <T> T setValue(Object value) {
			return (T)set(StandardAttributes.VALUE, value);
		}

		/**
		 * Sets the entry's comment.
		 *
		 * @param comment the new comment
		 * @return the old comment if any
		 */
		default String setComment(String comment) {
			return set(StandardAttributes.COMMENT, comment);
		}

		/**
		 * Sets an entry's attribute.
		 *
		 * @param attribute the type of attribute
		 * @param value the new value
		 * @param <T> the type of value
		 * @return the old value if any
		 */
		<T> T set(AttributeType<T> attribute, T value);

		@Override
		Iterable<? extends AttributeEntry<?>> attributes();
	}

	interface AttributeEntry<T> extends UnmodifiableConfig.AttributeEntry<T> {
		void set(T value);
	}

	/**
	 * Creates a new sub config of this config, as created when a subconfig's creation is
	 * implied by {@link #set(String, Object)} or {@link #add(String, Object)}.
	 *
	 * @return a new sub config
	 */
	Config createSubConfig();
}