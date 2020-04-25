package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.check.CheckedConfig;
import com.electronwill.nightconfig.core.check.ConfigChecker;
import com.electronwill.nightconfig.core.utils.MapSupplier;

import java.util.*;

import static com.electronwill.nightconfig.core.utils.StringUtils.splitPath;

/**
 * A (modifiable) configuration that contains key/value mappings. Configurations are generally
 * <b>not</b> thread-safe.
 *
 * @author TheElectronWill
 */
@SuppressWarnings("unchecked")
public interface Config extends UnmodifiableConfig {
	// --- ABSTRACT METHODS ---
	@Override
	Entry getEntry(String[] path);

	@Override
	Set<Entry> entries();

	<T> T add(AttributeType<T> attribute, String[] path, T value);

	<T> T set(AttributeType<T> attribute, String[] path, T value);

	<T> T remove(AttributeType<T> attribute, String[] path);

	<T> T remove(String[] path);

	/** Removes all entries from the config. */
	void clear();

	/** Removes all comments from the config. */
	void clearComments();

	/** Removes all non-value attributes from the config. */
	void clearExtraAttributes();

	/**
	 * Returns a Map view of the config's values.
	 * Any change to the map is reflected in the config and vice-versa.
	 */
	Map<String, Object> valueMap();

	/**
	 * Creates a new sub-configuration for a value of this config.
	 * This method can be called (among others) by {@link #set(String, Object)}
	 * and {@link #add(String, Object)}.
	 * <p>
	 * Sub-configurations must use the same {@link MapSupplier} as their parent.
	 * Sub-configurations are usually of the exact same type as their parent, but this is not
	 * strictly required.
	 *
	 * @return a new subconfig
	 */
	Config createSubConfig();

	// --- DEFAULT METHODS ---
	default Entry getEntry(String path) {
		return getEntry(splitPath(path));
	}

	// --- SETTERS FOR VALUES ---
	/**
	 * Adds or modifies a value.
	 *
	 * @param path the value's path, each part separated by a dot. Example "a.b.c"
	 * @param value the value to set
	 * @param <T> the type of the old value
	 * @return the old value if any, or {@code null}
	 */
	default <T> T set(String path, Object value) {
		return set(splitPath(path), value);
	}

	/**
	 * Adds or modifies a value.
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
	 * Removes all the values of the given config from this config.
	 *
	 * @param config the values to remove
	 */
	default void removeAll(UnmodifiableConfig config) {
		valueMap().keySet().removeAll(config.valueMap().keySet());
	}



	// --- SETTERS FOR ATTRIBUTES ---
	default <T> T set(AttributeType<T> attribute, String path, T value) {
		return set(attribute, splitPath(path), value);
	}


	default <T> T add(AttributeType<T> attribute, String path, T value) {
		return add(attribute, splitPath(path), value);
	}


	default <T> T remove(AttributeType<T> attribute, String path) {
		return remove(attribute, splitPath(path));
	}



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
	 * A modifiable config entry.
	 */
	interface Entry extends UnmodifiableConfig.Entry {
		@Override
		Iterable<? extends Attribute<?>> attributes();

		/** Removes all the attributes except the value. */
		void clearExtraAttributes();

		/** @return an instance of Map.Entry with the same key and value as this entry. */
		<T> Map.Entry<String, T> toMapEntry();

		/**
		 * Adds a value if there is none.
		 *
		 * @param value the new value
		 * @param <T> the type of the old value
		 * @return the old value, if any
		 */
		<T> T addValue(Object value);

		/**
		 * Sets the entry's value.
		 *
		 * @param value the new value
		 * @param <T> the type of the old value
		 * @return the old value, if any
		 */
		<T> T setValue(Object value);

		/**
		 * Removes the entry's value but keep its other attributes.
		 *
		 * @param <T> the type of the old value
		 * @return the old value, if any
		 */
		<T> T removeValue();

		/**
		 * Sets the value of an attribute.
		 *
		 * @param attribute the type of attribute
		 * @param value the new value
		 * @param <T> the type of value
		 * @return the old value if any
		 */
		<T> T set(AttributeType<T> attribute, T value);

		/**
		 * Adds an attribute to this entry, if it has no attribute of the given type.
		 *
		 * @param attribute the type of attribute
		 * @param value the new value
		 * @param <T> the type of value
		 * @return the old value if any
		 */
		<T> T add(AttributeType<T> attribute, T value);

		/**
		 * Removes an attribute from this entry.
		 *
		 * @param attribute the type of attribute
		 * @param <T> the type of the old value
		 * @return the old value if any
		 */
		<T> T remove(AttributeType<T> attribute);

		/**
		 * Sets the entry's comment.
		 *
		 * @param comment the new comment
		 * @return the old comment if any
		 */
		default String setComment(String comment) {
			return set(StandardAttributes.COMMENT, comment);
		}
	}

	interface Attribute<T> extends UnmodifiableConfig.Attribute<T> {
		/**
		 * Sets the attribute's value and returns the old one.
		 * @param value the new value
		 * @return the old value (if any)
		 */
		T setValue(T value);
	}
}
