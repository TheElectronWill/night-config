package com.electronwill.nightconfig.core;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;
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
	default <T> T get(List<String> path) {
		Object raw = getRaw(path);
		return (raw == NULL_OBJECT) ? null : (T)raw;
	}

	/**
	 * Gets a value from the config. Doesn't convert {@link NullObject#NULL_OBJECT} to {@code null}.
	 *
	 * @param path the value's path, each part separated by a dot. Example "a.b.c"
	 * @param <T>  the value's type
	 * @return the value at the given path, or {@code null} if there is no such value.
	 */
	default <T> T getRaw(String path) {
		return getRaw(split(path, '.'));
	}

	/**
	 * Gets a value from the config. Doesn't convert {@link NullObject#NULL_OBJECT} to {@code null}.
	 *
	 * @param path the value's path, each element of the list is a different part of the path.
	 * @param <T>  the value's type
	 * @return the value at the given path, or {@code null} if there is no such value.
	 */
	<T> T getRaw(List<String> path);

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
		return getOrElse(split(path, '.'), defaultValue);
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
		T value = getRaw(path);
		return (value == null || value == NULL_OBJECT) ? defaultValue : value;
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
		T value = getRaw(path);
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
		return getOrElse(split(path, '.'), defaultValueSupplier);
	}

	// ---- Enum getters ----
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
		return getEnum(split(path, '.'), enumType, method);
	}

	/**
	 * Calls {@link #getEnum(String, Class, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> T getEnum(String path, Class<T> enumType) {
		return getEnum(split(path, '.'), enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an Enum value from the config. If the value doesn't exist, returns null.
	 *
	 * @param path     the value's path, each element of the list is a different part of the path.
	 * @param enumType the class of the Enum
	 * @param method   the method to use when converting a non-enum value like a String or an int
	 * @param <T>      the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> T getEnum(List<String> path, Class<T> enumType, EnumGetMethod method) {
		final Object value = getRaw(path);
		return method.get(value, enumType);
	}

	/**
	 * Calls {@link #getEnum(List, Class, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> T getEnum(List<String> path, Class<T> enumType) {
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
		return getOptionalEnum(split(path, '.'), enumType, method);
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
	 * @param path     the value's path, each element of the list is a different part of the path.
	 * @param enumType the class of the Enum
	 * @param method   the method to use when converting a non-enum value like a String or an int
	 * @param <T>      the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(List<String> path, Class<T> enumType, EnumGetMethod method) {
		return Optional.ofNullable(getEnum(path, enumType, method));
	}

	/**
	 * Calls {@link #getOptionalEnum(List, Class, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(List<String> path, Class<T> enumType) {
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
		return getEnumOrElse(split(path, '.'), defaultValue, method);
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
	 * @param path         the value's path, each element of the list is a different part of the path.
	 * @param defaultValue the default value
	 * @param method       the method to use when converting a non-enum value like a String or an int
	 * @param <T>          the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> T getEnumOrElse(List<String> path, T defaultValue, EnumGetMethod method) {
		T value = getEnum(path, defaultValue.getDeclaringClass(), method);
		return (value == null) ? defaultValue : value;
	}

	/**
	 * Calls {@link #getEnumOrElse(List, Enum, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> T getEnumOrElse(List<String> path, T defaultValue) {
		return getEnumOrElse(path, defaultValue, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an Enum value from the config. If the value doesn't exist, returns the default value.
	 *
	 * @param path                 the value's path, each element of the list is a different part of the path.
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
		return getEnumOrElse(split(path, '.'), enumType, method, defaultValueSupplier);
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
	 * @param path                 the value's path, each element of the list is a different part of the path.
	 * @param defaultValueSupplier Supplier of the default value, only used if needed
	 * @param method               the method to use when converting a non-enum value like a String or an int
	 * @param <T>                  the value's type
	 * @return the value at the given path as an enum, or null value if not found.
	 * @throws IllegalArgumentException if the config contains a String that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant, like a List
	 */
	default <T extends Enum<T>> T getEnumOrElse(List<String> path,
												Class<T> enumType,
												EnumGetMethod method,
												Supplier<T> defaultValueSupplier) {
		// The enumType is needed to avoid using the Supplier when the raw value is an enum constant
		T value = getEnum(path, enumType, method);
		return (value == null) ? defaultValueSupplier.get() : value;
	}

	/**
	 * Calls {@link #getEnumOrElse(List, Class, EnumGetMethod, Supplier)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 */
	default <T extends Enum<T>> T getEnumOrElse(List<String> path,
												Class<T> enumType,
												Supplier<T> defaultValueSupplier) {
		return getEnumOrElse(path, enumType, EnumGetMethod.NAME_IGNORECASE, defaultValueSupplier);
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
		return this.<Number>getRaw(path).intValue();
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
		return this.<Number>getRaw(path).longValue();
	}

	/**
	 * Like {@link #get(List)} but returns a primitive long. The config's value must be a
	 * {@link Number}.
	 */
	default long getLong(List<String> path) {
		return this.<Number>getRaw(path).longValue();
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
		return this.<Number>getRaw(path).byteValue();
	}

	default byte getByte(List<String> path) {
		return this.<Number>getRaw(path).byteValue();
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
		return this.<Number>getRaw(path).shortValue();
	}

	default short getShort(List<String> path) {
		return this.<Number>getRaw(path).shortValue();
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
	default char getChar(List<String> path) {
		Object value = getRaw(path);
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
		Object value = getRaw(path);
		if (value == null || value == NULL_OBJECT) {
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
	 * Checks if the config contains a null value at some path.
	 *
	 * @param path the path to check, each part separated by a dot. Example "a.b.c"
	 * @return {@code true} if the path is associated with {@link NullObject#NULL_OBJECT},
	 * {@code false} if it's associated with another value or with no value.
	 */
	default boolean isNull(String path) {
		return isNull(split(path, '.'));
	}

	/**
	 * Checks if the config contains a null value at some path.
	 *
	 * @param path the path to check, each element of the list is a different part of the path.
	 * @return {@code true} if the path is associated with {@link NullObject#NULL_OBJECT},
	 * {@code false} if it's associated with another value or with no value.
	 */
	default boolean isNull(List<String> path) {
		return getRaw(path) == NULL_OBJECT;
	}

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
		 * Returns the entry's value without converting {@link NullObject#NULL_OBJECT} to {@code null}.
		 *
		 * @param <T> the value's type
		 * @return the entry's value
		 */
		<T> T getRawValue();

		/**
		 * @param <T> the value's type
		 * @return the entry's value
		 */
		default <T> T getValue() {
			Object raw = getRawValue();
			return (raw == NULL_OBJECT) ? null : (T)raw;
		}

		/**
		 * @return {@code true} if the value is {@link NullObject#NULL_OBJECT}.
		 */
		default boolean isNull() {
			return getRawValue() == NULL_OBJECT;
		}

		/**
		 * @param <T> the value's type
		 * @return the entry's value, wrapped in {@link Optional}
		 */
		default <T> Optional<T> getOptional() {
			return Optional.ofNullable(getValue());
		}

		default <T> T getOrElse(T defaultValue) {
			T value = getRawValue();
			return (value == null || value == NULL_OBJECT) ? defaultValue : value;
		}

		// ---- Primitive getters: int ----

		/**
		 * @return the entry's value as an int
		 */
		default int getInt() {
			return this.<Number>getRawValue().intValue();
		}

		default OptionalInt getOptionalInt() {
			Number value = getRawValue();
			return (value == null) ? OptionalInt.empty() : OptionalInt.of(value.intValue());
		}

		default int getIntOrElse(int defaultValue) {
			Number value = getRawValue();
			return (value == null) ? defaultValue : value.intValue();
		}

		// ---- Primitive getters: long ----

		/**
		 * @return the entry's value as a long
		 */
		default long getLong() {
			return this.<Number>getRawValue().longValue();
		}

		default OptionalLong getOptionalLong() {
			Number value = getRawValue();
			return (value == null) ? OptionalLong.empty() : OptionalLong.of(value.longValue());
		}

		default long getLongOrElse(long defaultValue) {
			Number value = getRawValue();
			return (value == null) ? defaultValue : value.longValue();
		}

		// ---- Primitive getters: byte ----

		/**
		 * @return the entry's value as a byte
		 */
		default byte getByte() {
			return this.<Number>getRawValue().byteValue();
		}

		default byte getByteOrElse(byte defaultValue) {
			Number value = getRawValue();
			return (value == null) ? defaultValue : value.byteValue();
		}

		/**
		 * @return the entry's value as a short
		 */
		default short getShort() {
			return this.<Number>getRawValue().shortValue();
		}

		default short getShortOrElse(short defaultValue) {
			Number value = getRawValue();
			return (value == null) ? defaultValue : value.shortValue();
		}

		// ---- Primitive getters: char ----

		/**
		 * If the value is a Number, returns {@link Number#intValue()}, cast to char.
		 * If the value is a CharSequence, returns its first character.
		 * Otherwise, attempts to cast the value to a char.
		 *
		 * @return the entry's value as a char
		 */
		default char getChar() {
			Object value = getRawValue();
			if (value instanceof Number) {
				return (char)((Number)value).intValue();
			} else if (value instanceof CharSequence) {
				return ((CharSequence)value).charAt(0);
			} else {
				return (char)value;
			}
		}

		default char getCharOrElse(char defaultValue) {
			Object value = getRawValue();
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