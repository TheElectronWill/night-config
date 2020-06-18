package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.check.CheckedConfig;
import com.electronwill.nightconfig.core.check.UpdateChecker;
import com.electronwill.nightconfig.core.utils.MapSupplier;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.electronwill.nightconfig.core.utils.StringUtils.splitPath;

/**
 * A (modifiable) configuration that contains key/value mappings.
 * Configurations are generally <b>not</b> thread-safe.
 *
 * @author TheElectronWill
 */
public interface Config extends UnmodifiableConfig {

	// --- OVERRIDES ---
	@Override
	Entry getEntry(String[] path);

	@Override
	Entry getEntry(Iterable<String> path);

	@Override
	default Entry getEntry(String path) {
		return getEntry(splitPath(path));
	}

	@Override
	default Optional<? extends Entry> getOptionalEntry(String path) {
		return Optional.empty();
	}

	@Override
	default Optional<? extends Entry> getOptionalEntry(String[] path) {
		return Optional.empty();
	}

	@Override
	default Optional<? extends Entry> getOptionalEntry(Iterable<String> path) {
		return Optional.empty();
	}

	/**
	 * Returns a Set view of the config's entries.
	 * Any change to the set is reflected in the config and vice-versa.
	 *
	 * @return a Set view of the (top-level) entries
	 */
	@Override
	Set<Entry> entries();

	/**
	 * Returns a Map view of the config's values.
	 * Any change to the map is reflected in the config and vice-versa.
	 *
	 * @return a Map view of the values
	 */
	@Override
	Map<String, Object> valueMap();


	// --- ENTRY CREATION ---

	/**
	 * If no entry exists at the given path, creates a new entry with the given value.
	 * If an entry exists, returns it.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @return the entry, new or existing
	 */
	default Entry addEntry(String path, Object value) {
		return addEntry(splitPath(path), value);
	}

	/**
	 * If no entry exists at the given path, creates a new entry with the given value
	 * If an entry exists, returns it.
	 *
	 * @param path path to the entry
	 * @return the entry, new or existing
	 */
	Entry addEntry(String[] path, Object value);

	/**
	 * If no entry exists at the given path, creates a new entry with the given value.
	 * If an entry exists, returns it.
	 *
	 * @param path path to the entry
	 * @return the entry, new or existing
	 */
	Entry addEntry(Iterable<String> path, Object value);


	// --- GENERAL OPERATIONS ---

	/** Removes all entries from the config. */
	void clear();

	/** Removes all comments from the config entries. */
	void clearComments();

	/** Removes all attributes from the config entries. */
	void clearAttributes();

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


	// --- GROUPED OPERATIONS ---

	/**
	 * Adds to this configuration a copy of all "new" entries coming from another configuration.
	 * No existing entry is replaced.
	 *
	 * @param config source config
	 */
	void addAll(UnmodifiableConfig config, Depth depth);

	/**
	 * Adds to this configuration a copy of all the entries of another configuration.
	 * New "foreign" entries replace local entries.
	 *
	 * @param config source config
	 */
	void putAll(UnmodifiableConfig config);

	/**
	 * Removes from this config all the entries that exist in another config.
	 * This is based on the name of the entries, not their content.
	 *
	 * @param config config to compare
	 */
	void removeAll(UnmodifiableConfig config);


	// --- VALUES OPERATIONS ---

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists but has a null value, replaces its value.
	 * If an entry exists and has a non-null value, does nothing.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param value value to add
	 * @param <T> type of the previous value
	 * @return the previous value, or {@code null} if there was no entry
	 */
	default <T> T add(String path, Object value) {
		return add(splitPath(path), value);
	}

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists but has a null value, replaces its value.
	 * If an entry exists and has a non-null value, does nothing.
	 *
	 * @param path path to the entry
	 * @param value value to add
	 * @param <T> type of the previous value
	 * @return the previous value, or {@code null} if there was no entry
	 */
	<T> T add(String[] path, Object value);

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists but has a null value, replaces its value.
	 * If an entry exists and has a non-null value, does nothing.
	 *
	 * @param path path to the entry
	 * @param value value to add
	 * @param <T> type of the previous value
	 * @return the previous value, or {@code null} if there was no entry
	 */
	<T> T add(Iterable<String> path, Object value);

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists, replaces its value.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param value new value to set
	 * @param <T> type of the previous value
	 * @return the previous value, or {@code null} if there was no entry
	 */
	default <T> T set(String path, Object value) {
		return set(splitPath(path), value);
	}

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists, replaces its value.
	 *
	 * @param path path to the entry
	 * @param value new value to set
	 * @param <T> type of the previous value
	 * @return the previous value, or {@code null} if there was no entry
	 */
	<T> T set(String[] path, Object value);

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists, replaces its value.
	 *
	 * @param path path to the entry
	 * @param value new value to set
	 * @param <T> type of the previous value
	 * @return the previous value, or {@code null} if there was no entry
	 */
	<T> T set(Iterable<String> path, Object value);

	/**
	 * Removes an entry from the config and returns its value.
	 * The whole entry is deleted, not just its value.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param <T> type of the previous value
	 * @return the previous value of the entry, or {@code null} if there was no entry
	 */
	default <T> T remove(String path) {
		return remove(splitPath(path));
	}

	/**
	 * Removes an entry from the config and returns its value.
	 * The whole entry is deleted, not just its value.
	 *
	 * @param path path to the entry
	 * @param <T> type of the previous value
	 * @return the previous value of the entry, or {@code null} if there was no entry
	 */
	<T> T remove(String[] path);

	/**
	 * Removes an entry from the config and returns its value. The whole entry is deleted.
	 *
	 * @param path path to the entry
	 * @param <T> type of the previous value
	 * @return the previous value of the entry, or {@code null} if there was no entry
	 */
	<T> T remove(Iterable<String> path);


	// --- ATTRIBUTES OPERATIONS ---

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists but doesn't have the given attribute, adds it.
	 * If an entry exists and has the given attribute, does nothing.
	 *
	 * @param attribute attribute to add
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param value value to add
	 * @param <T> type of the previous value
	 * @return the previous value of the attribute, or {@code null} if there was none
	 */
	default <T> T add(AttributeType<T> attribute, String path, T value) {
		return add(attribute, splitPath(path), value);
	}

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists but doesn't have the given attribute, adds it.
	 * If an entry exists and has the given attribute, does nothing.
	 *
	 * @param attribute attribute to add
	 * @param path path to the entry
	 * @param value value to add
	 * @param <T> type of the previous value
	 * @return the previous value of the attribute, or {@code null} if there was none
	 */
	<T> T add(AttributeType<T> attribute, String[] path, T value);

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists but doesn't have the given attribute, adds it.
	 * If an entry exists and has the given attribute, does nothing.
	 *
	 * @param attribute attribute to add
	 * @param path path to the entry
	 * @param value value to add
	 * @param <T> type of the previous value
	 * @return the previous value of the attribute, or {@code null} if there was none
	 */
	<T> T add(AttributeType<T> attribute, Iterable<String> path, T value);

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists, sets the value of its given attribute.
	 *
	 * @param attribute attribute to set
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param value new value to set
	 * @param <T> type of the previous value
	 * @return the previous value of the attribute, or {@code null} if there was none
	 */
	default <T> T set(AttributeType<T> attribute, String path, T value) {
		return set(attribute, splitPath(path), value);
	}

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists, sets the value of its given attribute.
	 *
	 * @param attribute attribute to set
	 * @param path path to the entry
	 * @param value new value to set
	 * @param <T> type of the previous value
	 * @return the previous value of the attribute, or {@code null} if there was none
	 */
	<T> T set(AttributeType<T> attribute, String[] path, T value);

	/**
	 * If no entry exists at the given path, creates a new entry and every required intermediate
	 * level (sub-configurations). If an entry exists, sets the value of its given attribute.
	 *
	 * @param attribute attribute to set
	 * @param path path to the entry
	 * @param value new value to set
	 * @param <T> type of the previous value
	 * @return the previous value of the attribute, or {@code null} if there was none
	 */
	<T> T set(AttributeType<T> attribute, Iterable<String> path, T value);

	/**
	 * Removes an attribute from an entry. The entry itself is not removed.
	 *
	 * @param attribute attribute to remove
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param <T> type of the previous value
	 * @return the previous value of the attribute, or {@code null} if there was none
	 */
	default <T> T remove(AttributeType<T> attribute, String path) {
		return remove(attribute, splitPath(path));
	}

	/**
	 * Removes an attribute from an entry. The entry itself is not removed.
	 *
	 * @param attribute attribute to remove
	 * @param path path to the entry
	 * @param <T> type of the previous value
	 * @return the previous value of the attribute, or {@code null} if there was none
	 */
	<T> T remove(AttributeType<T> attribute, String[] path);

	/**
	 * Removes an attribute from an entry. The entry itself is not removed.
	 *
	 * @param attribute attribute to remove
	 * @param path path to the entry
	 * @param <T> type of the previous value
	 * @return the previous value of the attribute, or {@code null} if there was none
	 */
	<T> T remove(AttributeType<T> attribute, Iterable<String> path);


	// --- COMMENTS ---

	/**
	 * Sets the comment of a config entry.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param comment comment to set
	 * @return the old comment, or {@code null} if the entry doesn't exist or doesn't have a comment
	 */
	default String setComment(String path, String comment) {
		return setComment(splitPath(path), comment);
	}

	/**
	 * Sets the comment of a config entry.
	 *
	 * @param path path to the entry
	 * @param comment comment to set
	 * @return the old comment, or {@code null} if the entry doesn't exist or doesn't have a comment
	 */
	default String setComment(String[] path, String comment) {
		return set(StandardAttributes.COMMENT, path, comment);
	}

	/**
	 * Sets the comment of a config entry.
	 *
	 * @param path path to the entry
	 * @param comment comment to set
	 * @return the old comment, or {@code null} if the entry doesn't exist or doesn't have a comment
	 */
	default String setComment(Iterable<String> path, String comment) {
		return set(StandardAttributes.COMMENT, path, comment);
	}

	/**
	 * Removes the comment of a config entry.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @return the old comment, or {@code null} if the entry doesn't exist or doesn't have a comment
	 */
	default String removeComment(String path) {
		return removeComment(splitPath(path));
	}

	/**
	 * Removes the comment of a config entry.
	 *
	 * @param path path to the entry
	 * @return the old comment, or {@code null} if the entry doesn't exist or doesn't have a comment
	 */
	default String removeComment(String[] path) {
		return remove(StandardAttributes.COMMENT, path);
	}

	/**
	 * Removes the comment of a config entry.
	 *
	 * @param path path to the entry
	 * @return the old comment, or {@code null} if the entry doesn't exist or doesn't have a comment
	 */
	default String removeComment(Iterable<String> path) {
		return remove(StandardAttributes.COMMENT, path);
	}


	// --- OTHER METHODS ---

	/**
	 * Returns an Unmodifiable view of the config. Any change to the original (modifiable) config
	 * is still reflected to the returned UnmodifiableConfig.
	 *
	 * @return an Unmodifiable view of the config.
	 */
	default UnmodifiableConfig unmodifiable() {
		return new CheckedConfig(this, UpdateChecker.freeze());
	}


	/**
	 * A modifiable config entry.
	 */
	interface Entry extends UnmodifiableConfig.Entry {
		@Override
		Iterable<? extends Attribute<?>> attributes();

		/** Removes all the attributes. */
		void clearAttributes();

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

	/**
	 * A modifiable attribute.
	 * @param <T> type of value
	 */
	interface Attribute<T> extends UnmodifiableConfig.Attribute<T> {
		/**
		 * Sets the attribute's value and returns the old one.
		 * @param value the new value
		 * @return the old value
		 */
		T setValue(T value);
	}
}
