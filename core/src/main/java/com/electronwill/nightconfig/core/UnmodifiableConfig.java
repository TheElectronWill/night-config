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
	UnmodifiableEntryData getData(String[] path);

	default UnmodifiableEntryData getData(String path) {
		return getData(splitPath(path));
	}

	// --- GETTER FOR VALUES ---
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
		UnmodifiableEntryData data = getData(path);
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

	/**
	 * Gets a value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path         the value's path, each part separated by a dot. Example "a.b.c"
	 * @param defaultValue the default value to return if not found
	 * @param <T>          the value's type
	 * @return the value at the given path, or the default value if not found.
	 */
	default <T> T getOrElse(String path, T defaultValue) {
		return getOrElse(splitPath(path), defaultValue);
	}

	/**
	 * Gets a value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path         the value's path, each element is a different part of the path.
	 * @param defaultValue the default value to return if not found
	 * @param <T>          the value's type
	 * @return the value at the given path, or the default value if not found.
	 */
	default <T> T getOrElse(String[] path, T defaultValue) {
		T value = get(path);
		return (value == null || value == NULL_OBJECT) ? defaultValue : value;
	}

	/**
	 * Gets a value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path                 the value's path, each element is a different part of the path.
	 * @param defaultValueSupplier the Supplier of the default value
	 * @param <T>                  the value's type
	 * @return the value at the given path, or the default value if not found.
	 */
	default <T> T getOrElse(String[] path, Supplier<T> defaultValueSupplier) {
		T value = get(path);
		return (value == null || value == NULL_OBJECT) ? defaultValueSupplier.get() : value;
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
		return getOrElse(splitPath(path), defaultValueSupplier);
	}

	// --- GETTERS FOR ATTRIBUTES ---
	default <T> T get(AttributeType<T> attribute, String path) {
		return get(attribute, splitPath(path));
	}

	default <T> T get(AttributeType<T> attribute, String[] path) {
		UnmodifiableEntryData data = getData(path);
		return data == null ? null : data.get(attribute);
	}

	default <T> Optional<T> getOptional(AttributeType<T> attribute, String path) {
		return getOptional(attribute, splitPath(path));
	}

	default <T> Optional<T> getOptional(AttributeType<T> attribute, String[] path) {
		return Optional.ofNullable(get(attribute, path));
	}

	default <T> T getOrElse(AttributeType<T> attribute, String path, T defaultValue) {
		return getOrElse(attribute, splitPath(path), defaultValue);
	}

	default <T> T getOrElse(AttributeType<T> attribute, String[] path, T defaultValue) {
		T value = get(attribute, path);
		return (value == null) ? defaultValue : value;
	}

	default <T> T getOrElse(AttributeType<T> attribute, String path, Supplier<T> defaultValueSupplier) {
		return getOrElse(attribute, splitPath(path), defaultValueSupplier);
	}

	default <T> T getOrElse(AttributeType<T> attribute, String[] path, Supplier<T> defaultValueSupplier) {
		T value = get(attribute, path);
		return (value == null) ? defaultValueSupplier.get() : value;
	}

	// --- GETTERS FOR COMMENTS ---
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

	/**
	 * Gets an Enum value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path         the value's path, each part separated by a dot. Example "a.b.c"
	 * @param defaultValue the default value
	 * @param method       the method to use when converting a non-enum value like a String or an int
	 * @param <T>          the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> T getEnumOrElse(String path, T defaultValue, EnumGetMethod method) {
		return getEnumOrElse(splitPath(path), defaultValue, method);
	}

	/**
	 * Calls {@link #getEnumOrElse(String, Enum, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> T getEnumOrElse(String path, T defaultValue) {
		return getEnumOrElse(path, defaultValue, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an Enum value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path         the value's path, each element is a different part of the path.
	 * @param defaultValue the default value
	 * @param method       the method to use when converting a non-enum value like a String or an int
	 * @param <T>          the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> T getEnumOrElse(String[] path, T defaultValue, EnumGetMethod method) {
		T value = getEnum(path, defaultValue.getDeclaringClass(), method);
		return (value == null) ? defaultValue : value;
	}

	/**
	 * Calls {@link #getEnumOrElse(String[], Enum, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> T getEnumOrElse(String[] path, T defaultValue) {
		return getEnumOrElse(path, defaultValue, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an Enum value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path                 the value's path, each element is a different part of the path.
	 * @param defaultValueSupplier Supplier of the default value, only used if needed
	 * @param method               the method to use when converting a non-enum value like a String or an int
	 * @param <T>                  the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> T getEnumOrElse(String path,
												Class<T> enumType,
												EnumGetMethod method,
												Supplier<T> defaultValueSupplier) {
		return getEnumOrElse(splitPath(path), enumType, method, defaultValueSupplier);
	}

	/**
	 * Calls {@link #getEnumOrElse(String, Class, EnumGetMethod, Supplier)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> T getEnumOrElse(String path,
												Class<T> enumType,
												Supplier<T> defaultValueSupplier) {
		return getEnumOrElse(path, enumType, EnumGetMethod.NAME_IGNORECASE, defaultValueSupplier);
	}

	/**
	 * Gets an Enum value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path                 the value's path, each element is a different part of the path.
	 * @param defaultValueSupplier Supplier of the default value, only used if needed
	 * @param method               the method to use when converting a non-enum value like a String or an int
	 * @param <T>                  the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> T getEnumOrElse(String[] path,
												Class<T> enumType,
												EnumGetMethod method,
												Supplier<T> defaultValueSupplier) {
		// The enumType is needed to avoid using the Supplier when the raw value is an enum constant
		T value = getEnum(path, enumType, method);
		return (value == null) ? defaultValueSupplier.get() : value;
	}

	/**
	 * Calls {@link #getEnumOrElse(String[], Class, EnumGetMethod, Supplier)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> T getEnumOrElse(String[] path,
												Class<T> enumType,
												Supplier<T> defaultValueSupplier) {
		return getEnumOrElse(path, enumType, EnumGetMethod.NAME_IGNORECASE, defaultValueSupplier);
	}

	// --- Primitive getters: int ---
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

	/**
	 * Like {@link #getOrElse(String, Object)} but returns a primitive int.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default int getIntOrElse(String path, int defaultValue) {
		return getIntOrElse(splitPath(path), defaultValue);
	}

	/**
	 * Like {@link #getOrElse(String[], Object)} but returns a primitive int.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default int getIntOrElse(String[] path, int defaultValue) {
		Number n = get(path);
		return (n == null) ? defaultValue : n.intValue();
	}

	/**
	 * Like {@link #getOrElse(String, Supplier)} but returns a primitive int.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default int getIntOrElse(String path, IntSupplier defaultValueSupplier) {
		return getIntOrElse(splitPath(path), defaultValueSupplier);
	}

	/**
	 * Like {@link #getOrElse(String[], Supplier)} but returns a primitive int.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default int getIntOrElse(String[] path, IntSupplier defaultValueSupplier) {
		Number n = get(path);
		return (n == null) ? defaultValueSupplier.getAsInt() : n.intValue();
	}

	// --- Primitive getters: long ---
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

	/**
	 * Like {@link #getOrElse(String, Object)} but returns a primitive long.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default long getLongOrElse(String path, long defaultValue) {
		return getLongOrElse(splitPath(path), defaultValue);
	}

	/**
	 * Like {@link #getOrElse(String[], Object)} but returns a primitive long.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default long getLongOrElse(String[] path, long defaultValue) {
		Number n = get(path);
		return (n == null) ? defaultValue : n.longValue();
	}

	/**
	 * Like {@link #getOrElse(String, Supplier)} but returns a primitive long.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default long getLongOrElse(String path, LongSupplier defaultValueSupplier) {
		return getLongOrElse(splitPath(path), defaultValueSupplier);
	}

	/**
	 * Like {@link #getOrElse(String[], Supplier)} but returns a primitive long.
	 * The config's value must be a {@link Number} or null or nonexistant.
	 */
	default long getLongOrElse(String[] path, LongSupplier defaultValueSupplier) {
		Number n = get(path);
		return (n == null) ? defaultValueSupplier.getAsLong() : n.longValue();
	}

	// --- Primitive getters: byte ---
	default byte getByte(String path) {
		return this.<Number>get(path).byteValue();
	}

	default byte getByte(String[] path) {
		return this.<Number>get(path).byteValue();
	}

	default byte getByteOrElse(String path, byte defaultValue) {
		return getByteOrElse(splitPath(path), defaultValue);
	}

	default byte getByteOrElse(String[] path, byte defaultValue) {
		Number n = get(path);
		return (n == null) ? defaultValue : n.byteValue();
	}

	// ---- Primitive getters: short ----
	default short getShort(String path) {
		return this.<Number>get(path).shortValue();
	}

	default short getShort(String[] path) {
		return this.<Number>get(path).shortValue();
	}

	default short getShortOrElse(String path, short defaultValue) {
		return getShortOrElse(splitPath(path), defaultValue);
	}

	default short getShortOrElse(String[] path, short defaultValue) {
		Number n = get(path);
		return (n == null) ? defaultValue : n.shortValue();
	}

	// ---- Primitive getters: char ----

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
		return getCharOrElse(splitPath(path), defaultValue);
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
	default char getCharOrElse(String[] path, char defaultValue) {
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
		return contains(splitPath(path));
	}

	/**
	 * Checks if the config contains a value at some path.
	 *
	 * @param path the path to check, each element is a different part of the path.
	 * @return {@code true} if the path is associated with a value, {@code false} if it's not.
	 */
	boolean contains(String[] path);

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
	boolean has(AttributeType<?> attribute, String[] path);

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
		return contains(path) && get(path) == null;
	}

	/**
	 * Returns a Map view of the config's values. If the config is unmodifiable then the returned
	 * map is unmodifiable too.
	 *
	 * @return a Map view of the config's values.
	 */
	Map<String, Object> valueMap();

	Map<String, ? extends UnmodifiableEntryData> dataMap();

	Iterable<? extends Entry> entries();

	/** @return the number of top-level entries in this config. */
	int size();

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
		default String key() {
			return getKey();
		}

		String getKey();

		default <T> T getValue() {
			return (T)get(StandardAttributes.VALUE);
		}

		default <T> Optional<T> getOptional() {
			return Optional.ofNullable(getValue());
		}

		default <T> T getOrElse(T defaultValue) {
			T value = getValue();
			return value == null ? defaultValue : value;
		}

		default String getComment() {
			return get(StandardAttributes.COMMENT);
		}

		<T> T get(AttributeType<T> attribute);

		<T> Optional<T> getOptional(AttributeType<T> attribute);

		default <T> T getOrElse(AttributeType<T> attribute, T defaultValue) {
			T value = get(attribute);
			return value == null ? defaultValue : value;
		}

		Iterable<? extends AttributeEntry<?>> attributes();

		// --- Primitive getters ---
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

	interface AttributeEntry<T> {
		AttributeType<T> attribute();
		T get();
	}
}