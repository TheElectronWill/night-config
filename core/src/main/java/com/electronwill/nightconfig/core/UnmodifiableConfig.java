package com.electronwill.nightconfig.core;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

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
	default <T> T get(String path) {
		return get(split(path, '.'));
	}

	/**
	 * Gets a value from the config.
	 *
	 * @param path the value's path, each element of the list is a different part of the path.
	 * @param <T>  the value's type
	 * @return the value at the given path, or {@code null} if there is no such value.
	 */
	<T> T get(List<String> path);

	/**
	 * Gets an optional value from the config.
	 *
	 * @param path the value's path, each part separated by a dot. Example "a.b.c"
	 * @param <T>  the value's type
	 * @return an Optional containing the value at the given path, or {@code Optional.empty()} if
	 * there is no such value.
	 */
	default <T> Optional<T> getOptional(String path) {
		return getOptional(split(path, '.'));
	}

	/**
	 * Gets an optional value from the config.
	 *
	 * @param path the value's path, each element of the list is a different part of the path.
	 * @param <T>  the value's type
	 * @return an Optional containing the value at the given path, or {@code Optional.empty()} if
	 * there is no such value.
	 */
	default <T> Optional<T> getOptional(List<String> path) {
		return Optional.ofNullable(get(path));
	}

	/**
	 * Gets a value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path         the value's path, each part separated by a dot. Example "a.b.c"
	 * @param defaultValue the default value to return if not found
	 * @param <T>          the value's type
	 * @return the value at the given path, or the default value if not found.
	 */
	default <T> T getOrElse(String path, T defaultValue) {
		T value = get(path);
		return (value == null) ? defaultValue : value;
	}

	/**
	 * Gets a value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path         the value's path, each element of the list is a different part of the path.
	 * @param defaultValue the default value to return if not found
	 * @param <T>          the value's type
	 * @return the value at the given path, or the default value if not found.
	 */
	default <T> T getOrElse(List<String> path, T defaultValue) {
		T value = get(path);
		return (value == null) ? defaultValue : value;
	}

	/**
	 * Gets a value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path                 the value's path, each element of the list is a different part of the path.
	 * @param defaultValueSupplier the Supplier of the default value
	 * @param <T>                  the value's type
	 * @return the value at the given path, or the default value if not found.
	 */
	default <T> T getOrElse(List<String> path, Supplier<T> defaultValueSupplier) {
		T value = get(path);
		return (value == null) ? defaultValueSupplier.get() : value;
	}

	/**
	 * Gets a value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path                 the value's path, each part separated by a dot. Example "a.b.c"
	 * @param defaultValueSupplier the Supplier of the default value
	 * @param <T>                  the value's type
	 * @return the value at the given path, or the default value if not found.
	 */
	default <T> T getOrElse(String path, Supplier<T> defaultValueSupplier) {
		T value = get(path);
		return (value == null) ? defaultValueSupplier.get() : value;
	}

	// ---- Primitive getters: int ----
	/**
	 * Like {@link #get(String)} but returns a primitive int. The config's value must be a
	 * {@link Number}.
	 */
	default int getInt(String path) {
		return this.<Number>get(path).intValue();
	}

	/**
	 * Like {@link #get(List)} but returns a primitive int. The config's value must be a
	 * {@link Number}.
	 */
	default int getInt(List<String> path) {
		return this.<Number>get(path).intValue();
	}

	/**
	 * Like {@link #getOptional(String)} but returns a primitive int. The config's value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalInt getOptionalInt(String path) {
		return getOptionalInt(split(path, '.'));
	}

	/**
	 * Like {@link #getOptional(List)} but returns a primitive int. The config's value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalInt getOptionalInt(List<String> path) {
		Number n = get(path);
		return (n == null) ? OptionalInt.empty() : OptionalInt.of(n.intValue());
	}

	/**
	 * Like {@link #getOrElse(String, Object)} but returns a primitive int.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default int getIntOrElse(String path, int defaultValue) {
		return getIntOrElse(split(path, '.'), defaultValue);
	}

	/**
	 * Like {@link #getOrElse(List, Object)} but returns a primitive int.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default int getIntOrElse(List<String> path, int defaultValue) {
		Number n = get(path);
		return (n == null) ? defaultValue : n.intValue();
	}

	/**
	 * Like {@link #getOrElse(String, Supplier)} but returns a primitive int.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default int getIntOrElse(String path, IntSupplier defaultValueSupplier) {
		return getIntOrElse(split(path, '.'), defaultValueSupplier);
	}

	/**
	 * Like {@link #getOrElse(List, Supplier)} but returns a primitive int.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default int getIntOrElse(List<String> path, IntSupplier defaultValueSupplier) {
		Number n = get(path);
		return (n == null) ? defaultValueSupplier.getAsInt() : n.intValue();
	}

	// ---- Primitive getters: long ----
	/**
	 * Like {@link #get(String)} but returns a primitive long. The config's value must be a
	 * {@link Number}.
	 */
	default long getLong(String path) {
		return this.<Number>get(path).longValue();
	}

	/**
	 * Like {@link #get(List)} but returns a primitive long. The config's value must be a
	 * {@link Number}.
	 */
	default long getLong(List<String> path) {
		return this.<Number>get(path).longValue();
	}

	/**
	 * Like {@link #getOptional(String)} but returns a primitive long. The config's value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalLong getOptionalLong(String path) {
		return getOptionalLong(split(path, '.'));
	}

	/**
	 * Like {@link #getOptional(List)} but returns a primitive long. The config's value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalLong getOptionalLong(List<String> path) {
		Number n = get(path);
		return (n == null) ? OptionalLong.empty() : OptionalLong.of(n.longValue());
	}

	/**
	 * Like {@link #getOrElse(String, Object)} but returns a primitive long.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default long getLongOrElse(String path, long defaultValue) {
		return getLongOrElse(split(path, '.'), defaultValue);
	}

	/**
	 * Like {@link #getOrElse(List, Object)} but returns a primitive long.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default long getLongOrElse(List<String> path, long defaultValue) {
		Number n = get(path);
		return (n == null) ? defaultValue : n.longValue();
	}

	/**
	 * Like {@link #getOrElse(String, Supplier)} but returns a primitive long.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default long getLongOrElse(String path, LongSupplier defaultValueSupplier) {
		return getLongOrElse(split(path, '.'), defaultValueSupplier);
	}

	/**
	 * Like {@link #getOrElse(List, Supplier)} but returns a primitive long.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default long getLongOrElse(List<String> path, LongSupplier defaultValueSupplier) {
		Number n = get(path);
		return (n == null) ? defaultValueSupplier.getAsLong() : n.longValue();
	}

	// ---- Primitive getters: byte ----
	default byte getByte(String path) {
		return this.<Number>get(path).byteValue();
	}

	default byte getByte(List<String> path) {
		return this.<Number>get(path).byteValue();
	}

	default byte getByteOrElse(String path, byte defaultValue) {
		return getByteOrElse(split(path, '.'), defaultValue);
	}

	default byte getByteOrElse(List<String> path, byte defaultValue) {
		Number n = get(path);
		return (n == null) ? defaultValue : n.byteValue();
	}

	// ---- Primitive getters: short ----
	default short getShort(String path) {
		return this.<Number>get(path).shortValue();
	}

	default short getShort(List<String> path) {
		return this.<Number>get(path).shortValue();
	}

	default short getShortOrElse(String path, short defaultValue) {
		return getShortOrElse(split(path, '.'), defaultValue);
	}

	default short getShortOrElse(List<String> path, short defaultValue) {
		Number n = get(path);
		return (n == null) ? defaultValue : n.shortValue();
	}

	// ---- Primitive getters: char ----

	/**
	 * Returns a char value from the configuration.
	 * If the value is a Number, returns {@link Number#intValue()}, cast to char.
	 * If the value is a CharSequence, returns its first character.
	 * Otherwise, attempts to cast the value to a char.
	 *
	 * @param path the value's path as a dot-separated String
	 * @return the value, as a single char
	 */
	default char getChar(String path) {
		return (char)getInt(path);
	}

	/**
	 * Returns a char value from the configuration.
	 * If the value is a Number, returns {@link Number#intValue()}, cast to char.
	 * If the value is a CharSequence, returns its first character.
	 * Otherwise, attempts to cast the value to a char.
	 *
	 * @param path the value's path as a list of String
	 * @return the value, as a single char
	 */
	default char getChar(List<String> path) {
		Object value = get(path);
		if (value instanceof Number) {
			return (char)((Number)value).intValue();
		} else if (value instanceof CharSequence) {
			return ((CharSequence)value).charAt(0);
		} else {
			return (char)value;
		}
	}

	/**
	 * Returns a char value from the configuration.
	 * <p>
	 * If the value is nonexistant, returns defaultValue.
	 * If the value is a Number, returns {@link Number#intValue()}, cast to char.
	 * If the value is a CharSequence, returns its first character.
	 * Otherwise, attempts to cast the value to a char.
	 *
	 * @param path the value's path
	 * @param defaultValue the char to return if the value doesn't exist in the config
	 * @return the value, as a single char
	 */
	default char getCharOrElse(String path, char defaultValue) {
		return getCharOrElse(split(path, '.'), defaultValue);
	}

	/**
	 * Returns a char value from the configuration.
	 * <p>
	 * If the value is nonexistant, returns defaultValue.
	 * If the value is a Number, returns {@link Number#intValue()}, cast to char.
	 * If the value is a CharSequence, returns its first character.
	 * Otherwise, attempts to cast the value to a char.
	 *
	 * @param path the value's path
	 * @param defaultValue the char to return if the value doesn't exist in the config
	 * @return the value, as a single char
	 */
	default char getCharOrElse(List<String> path, char defaultValue) {
		Object value = get(path);
		if (value == null) {
			return defaultValue;
		} else if (value instanceof Number) {
			return (char)((Number)value).intValue();
		} else if (value instanceof CharSequence) {
			return ((CharSequence)value).charAt(0);
		} else {
			return (char)value;
		}
	}
	// ---- End of getters ----


	/**
	 * Checks if the config contains a value at some path.
	 *
	 * @param path the path to check, each part separated by a dot. Example "a.b.c"
	 * @return {@code true} if the path is associated with a value, {@code false} if it's not.
	 */
	default boolean contains(String path) {
		return contains(split(path, '.'));
	}

	/**
	 * Checks if the config contains a value at some path.
	 *
	 * @param path the path to check, each element of the list is a different part of the path.
	 * @return {@code true} if the path is associated with a value, {@code false} if it's not.
	 */
	boolean contains(List<String> path);

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

	/**
	 * Returns the config's format.
	 *
	 * @return the config's format
	 */
	ConfigFormat<?> configFormat();

	//--- Scala convenience methods ---

	/**
	 * For scala: gets a config value.
	 *
	 * @param path the value's path, each part separated by a dot. Example "a.b.c"
	 * @param <T>  the value's type
	 * @see #get(String)
	 */
	default <T> T apply(String path) {
		return get(path);
	}

	/**
	 * For scala: gets a config value.
	 *
	 * @param path the value's path, each element of the list is a different part of the path.
	 * @param <T>  the value's type
	 * @see #get(List)
	 */
	default <T> T apply(List<String> path) {
		return get(path);
	}
}