package com.electronwill.nightconfig.core;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;
import static com.electronwill.nightconfig.core.utils.StringUtils.splitPath;

/**
 * An unmodifiable (read-only) configuration that contains key/value mappings.
 *
 * @author TheElectronWill
 */
@SuppressWarnings("unchecked")
public interface UnmodifiableConfig {
	// --- ABSTRACT METHODS ---

	Entry getEntry(String[] path);

	/** @return an iterable on the config's entries. */
	Iterable<? extends Entry> entries();

	/** @return the number of top-level entries in this config. */
	int size();

	/**
	 * Returns a Map view of the config's values.
	 * If the config is unmodifiable then the returned map is unmodifiable too.
	 *
	 * @return a Map view of the config's values.
	 */
	Map<String, Object> valueMap();

	// --- ENTRY GETTERS ---

	/** @return a config entry */
	default Entry getEntry(String path) {
		return getEntry(splitPath(path));
	}

	default Optional<Entry> getOptionalEntry(String path) {
		return getOptionalEntry(splitPath(path));
	}

	default Optional<Entry> getOptionalEntry(String[] path) {
		return Optional.ofNullable(getEntry(path));
	}

	// --- VALUES GETTERS ---

	/**
	 * Gets a value from the config.
	 *
	 * @param path the value's path, each part separated by a dot. Example "a.b.c"
	 * @param <T>  the value's type
	 * @return the value at the given path, or {@code null} if there is no such value.
	 */
	default <T> T get(String path) {
		return get(splitPath(path));
	}

	/**
	 * Gets a value from the config.
	 *
	 * @param path the value's path, each element is a different part of the path.
	 * @param <T>  the value's type
	 * @return the value at the given path, or {@code null} if there is no such value.
	 */
	default <T> T get(String[] path) {
		Entry data = getEntry(path);
		return data == null ? null : data.getValue();
	}

	/**
	 * Gets an optional value from the config.
	 *
	 * @param path the value's path, each part separated by a dot. Example "a.b.c"
	 * @param <T>  the value's type
	 * @return an Optional containing the value at the given path, or {@code Optional.empty()} if
	 * there is no such value.
	 */
	default <T> Optional<T> getOptional(String path) {
		return getOptional(splitPath(path));
	}

	/**
	 * Gets an optional value from the config.
	 *
	 * @param path the value's path, each element is a different part of the path.
	 * @param <T>  the value's type
	 * @return an Optional containing the value at the given path, or {@code Optional.empty()} if
	 * there is no such value.
	 */
	default <T> Optional<T> getOptional(String[] path) {
		return Optional.ofNullable(get(path));
	}

	// --- ATTRIBUTES GETTERS ---

	default <T> T get(AttributeType<T> attribute, String path) {
		return get(attribute, splitPath(path));
	}

	default <T> T get(AttributeType<T> attribute, String[] path) {
		Entry entry = getEntry(path);
		return entry == null ? null : entry.get(attribute);
	}

	default <T> Optional<T> getOptional(AttributeType<T> attribute, String path) {
		return getOptional(attribute, splitPath(path));
	}

	default <T> Optional<T> getOptional(AttributeType<T> attribute, String[] path) {
		Entry entry = getEntry(path);
		return entry == null ? Optional.empty() : entry.getOptional(attribute);
	}

	// --- COMMENTS GETTERS ---

	/**
	 * Gets a comment from the config.
	 *
	 * @param path the comment's path, each part separated by a dot. Example "a.b.c"
	 * @return the comment at the given path, or {@code null} if there is none.
	 */
	default String getComment(String path) {
		return get(StandardAttributes.COMMENT, path);
	}

	/**
	 * Gets a comment from the config.
	 *
	 * @param path the comment's path, each element is a different part of the path.
	 * @return the comment at the given path, or {@code null} if there is none.
	 */
	default String getComment(String[] path) {
		return get(StandardAttributes.COMMENT, path);
	}

	/**
	 * Gets an optional comment from the config.
	 *
	 * @param path the comment's path, each part separated by a dot. Example "a.b.c"
	 * @return an Optional containing the comment at the given path, or {@code Optional.empty()} if
	 * there is no such comment.
	 */
	default Optional<String> getOptionalComment(String path) {
		return getOptional(StandardAttributes.COMMENT, path);
	}

	/**
	 * Gets an optional comment from the config.
	 *
	 * @param path the comment's path, each element is a different part of the path.
	 * @return an Optional containing the comment at the given path, or {@code Optional.empty()} if
	 * there is no such comment.
	 */
	default Optional<String> getOptionalComment(String[] path) {
		return getOptional(StandardAttributes.COMMENT, path);
	}

	// --- GETTERS FOR ENUM VALUES ---

	/**
	 * Gets an Enum value from the config. If the value doesn't exist, returns null.
	 *
	 * @param path     the value's path, each part separated by a dot. Example "a.b.c"
	 * @param enumType the class of the Enum
	 * @param method   the method to use when converting a non-enum value like a String or an int
	 * @param <T>      the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> T getEnum(String path, Class<T> enumType, EnumGetMethod method) {
		return getEnum(splitPath(path), enumType, method);
	}

	/**
	 * Calls {@link #getEnum(String, Class, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> T getEnum(String path, Class<T> enumType) {
		return getEnum(splitPath(path), enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an Enum value from the config. If the value doesn't exist, returns null.
	 *
	 * @param path     the value's path, each element is a different part of the path.
	 * @param enumType the class of the Enum
	 * @param method   the method to use when converting a non-enum value like a String or an int
	 * @param <T>      the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> T getEnum(String[] path, Class<T> enumType, EnumGetMethod method) {
		final Object value = get(path);
		return method.get(value, enumType);
	}

	/**
	 * Calls {@link #getEnum(String[], Class, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> T getEnum(String[] path, Class<T> enumType) {
		return getEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an optional Enum value from the config.
	 *
	 * @param path     the value's path, each part separated by a dot. Example "a.b.c"
	 * @param enumType the class of the Enum
	 * @param method   the method to use when converting a non-enum value like a String or an int
	 * @param <T>      the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(String path, Class<T> enumType, EnumGetMethod method) {
		return getOptionalEnum(splitPath(path), enumType, method);
	}

	/**
	 * Calls {@link #getOptionalEnum(String, Class, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(String path, Class<T> enumType) {
		return getOptionalEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an optional Enum value from the config.
	 *
	 * @param path     the value's path, each element is a different part of the path.
	 * @param enumType the class of the Enum
	 * @param method   the method to use when converting a non-enum value like a String or an int
	 * @param <T>      the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(String[] path, Class<T> enumType, EnumGetMethod method) {
		return Optional.ofNullable(getEnum(path, enumType, method));
	}

	/**
	 * Calls {@link #getOptionalEnum(String[], Class, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(String[] path, Class<T> enumType) {
		return getOptionalEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	// --- PRIMITIVE INT GETTERS ---

	/**
	 * Like {@link #get(String)} but returns a primitive int. The config's value must be a
	 * {@link Number}.
	 */
	default int getInt(String path) {
		return this.<Number>get(path).intValue();
	}

	/**
	 * Like {@link #get(String[])} but returns a primitive int. The config's value must be a
	 * {@link Number}.
	 */
	default int getInt(String[] path) {
		return this.<Number>get(path).intValue();
	}

	/**
	 * Like {@link #getOptional(String)} but returns a primitive int. The config's value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalInt getOptionalInt(String path) {
		return getOptionalInt(splitPath(path));
	}

	/**
	 * Like {@link #getOptional(String[])} but returns a primitive int. The config's value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalInt getOptionalInt(String[] path) {
		Number n = get(path);
		return (n == null) ? OptionalInt.empty() : OptionalInt.of(n.intValue());
	}

	// --- PRIMITIVE LONG GETTERS ---
	/**
	 * Like {@link #get(String)} but returns a primitive long. The config's value must be a
	 * {@link Number}.
	 */
	default long getLong(String path) {
		return this.<Number>get(path).longValue();
	}

	/**
	 * Like {@link #get(String[])} but returns a primitive long. The config's value must be a
	 * {@link Number}.
	 */
	default long getLong(String[] path) {
		return this.<Number>get(path).longValue();
	}

	/**
	 * Like {@link #getOptional(String)} but returns a primitive long. The config's value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalLong getOptionalLong(String path) {
		return getOptionalLong(splitPath(path));
	}

	/**
	 * Like {@link #getOptional(String[])} but returns a primitive long. The config's value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalLong getOptionalLong(String[] path) {
		Number n = get(path);
		return (n == null) ? OptionalLong.empty() : OptionalLong.of(n.longValue());
	}

	// --- PRIMITIVE BYTE GETTERS ---

	default byte getByte(String path) {
		return this.<Number>get(path).byteValue();
	}

	default byte getByte(String[] path) {
		return this.<Number>get(path).byteValue();
	}

	// --- PRIMITIVE SHORT GETTERS ---

	default short getShort(String path) {
		return this.<Number>get(path).shortValue();
	}

	default short getShort(String[] path) {
		return this.<Number>get(path).shortValue();
	}

	// --- PRIMITIVE CHAR GETTERS ---

	/**
	 * Returns a char value from the configuration.
	 * <p>
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
	 * <p>
	 * If the value is a Number, returns {@link Number#intValue()}, cast to char.
	 * If the value is a CharSequence, returns its first character.
	 * Otherwise, attempts to cast the value to a char.
	 *
	 * @param path the value's path as a list of String
	 * @return the value, as a single char
	 */
	default char getChar(String[] path) {
		Object value = get(path);
		if (value instanceof Number) {
			return (char)((Number)value).intValue();
		} else if (value instanceof CharSequence) {
			return ((CharSequence)value).charAt(0);
		} else {
			return (char)value;
		}
	}

	// --- BOOLEAN QUERIES ---

	/**
	 * Checks if the config contains a value at some path.
	 *
	 * @param path the path to check, each part separated by a dot. Example "a.b.c"
	 * @return {@code true} if the path is associated with a value, {@code false} if it's not.
	 */
	default boolean contains(String path) {
		return contains(splitPath(path));
	}

	/**
	 * Checks if the config contains a value at some path.
	 *
	 * @param path the path to check, each element is a different part of the path.
	 * @return {@code true} if the path is associated with a value, {@code false} if it's not.
	 */
	default boolean contains(String[] path) {
		return getEntry(path) != null;
	}

	/**
	 * Checks if an attribute is present at some path.
	 *
	 * @param path the path to check, each part separated by a dot. Example "a.b.c"
	 * @return {@code true} if the path is associated with a value, {@code false} if it's not.
	 */
	default boolean has(AttributeType<?> attribute, String path) {
		return has(attribute, splitPath(path));
	}

	/**
	 * Checks if an attribute is present at some path.
	 *
	 * @param path the path to check, each element is a different part of the path.
	 * @return {@code true} if the path is associated with a value, {@code false} if it's not.
	 */
	default boolean has(AttributeType<?> attribute, String[] path) {
		Entry entry = getEntry(path);
		return entry != null && entry.has(attribute);
	}

	/**
	 * Checks if the config contains a null value at some path.
	 *
	 * @param path the path to check, each part separated by a dot. Example "a.b.c"
	 * @return {@code true} if the path is associated with null.
	 * {@code false} if it's associated with another value or with no value.
	 */
	default boolean isNull(String path) {
		return isNull(splitPath(path));
	}

	/**
	 * Checks if the config contains a null value at some path.
	 *
	 * @param path the path to check, each element is a different part of the path.
	 * @return {@code true} if the path is associated with null.
	 * {@code false} if it's associated with another value or with no value.
	 */
	default boolean isNull(String[] path) {
		Entry entry = getEntry(path);
		return entry != null && entry.getValue() == null;
	}

	default boolean isEmpty() {
		return size() == 0;
	}

	default boolean nonEmpty() {
		return size() != 0;
	}

	/**
	 * An unmodifiable config entry.
	 */
	interface Entry {
		String getKey();

		// --- VALUE GETTERS ---
		<T> T getValue();

		default <T> Optional<T> getOptionalValue() {
			return Optional.ofNullable(getValue());
		}

		default <T> T getValueOrElse(T defaultValue) {
			T value = getValue();
			return value == null ? defaultValue : value;
		}

		// --- ATTRIBUTES ---
		Iterable<? extends Attribute<?>> attributes();

		boolean has(AttributeType<?> attribute);

		<T> T get(AttributeType<T> attribute);

		default <T> Optional<T> getOptional(AttributeType<T> attribute) {
			return Optional.ofNullable(get(attribute));
		}

		default String getComment() {
			return get(StandardAttributes.COMMENT);
		}

		default Optional<String> getOptionalComment() {
			return getOptional(StandardAttributes.COMMENT);
		}

		// --- PRIMITIVE GETTERS ---
		/**
		 * @return the entry's value as an int
		 */
		default int getInt() {
			return this.<Number>getValue().intValue();
		}

		default OptionalInt getOptionalInt() {
			Number value = getValue();
			return (value == null) ? OptionalInt.empty() : OptionalInt.of(value.intValue());
		}

		/**
		 * @return the entry's value as a long
		 */
		default long getLong() {
			return this.<Number>getValue().longValue();
		}

		default OptionalLong getOptionalLong() {
			Number value = getValue();
			return (value == null) ? OptionalLong.empty() : OptionalLong.of(value.longValue());
		}

		/**
		 * @return the entry's value as a float
		 */
		default float getFloat() {
			return this.<Number>getValue().floatValue();
		}

		/**
		 * @return the entry's value as a double
		 */
		default double getDouble() {
			return this.<Number>getValue().doubleValue();
		}

		default OptionalDouble getOptionalDouble() {
			Number value = getValue();
			return (value == null) ? OptionalDouble.empty() : OptionalDouble.of(value.doubleValue());
		}

		/**
		 * @return the entry's value as a byte
		 */
		default byte getByte() {
			return this.<Number>getValue().byteValue();
		}

		/**
		 * @return the entry's value as a short
		 */
		default short getShort() {
			return this.<Number>getValue().shortValue();
		}

		/**
		 * If the value is a Number, returns {@link Number#intValue()}, cast to char.
		 * If the value is a CharSequence, returns its first character.
		 * Otherwise, attempts to cast the value to a char.
		 *
		 * @return the entry's value as a char
		 */
		default char getChar() {
			Object value = getValue();
			if (value instanceof Number) {
				return (char)((Number)value).intValue();
			} else if (value instanceof CharSequence) {
				return ((CharSequence)value).charAt(0);
			} else {
				return (char)value;
			}
		}
	}

	interface Attribute<T> {
		AttributeType<T> getType();
		T getValue();
	}
}
