package com.electronwill.nightconfig.core;

import java.util.*;

import static com.electronwill.nightconfig.core.utils.StringUtils.splitPath;

/**
 * An unmodifiable (read-only) configuration that contains key/value mappings.
 *
 * @author TheElectronWill
 */
public interface UnmodifiableConfig {

	// --- CONFIG VIEWS ---

	/** @return an iterable on the top-level entries of this config. */
	Iterable<? extends Entry> entries();

	/** @return an iterable on the top-level keys of this config. */
	Iterable<String> keys();

	/** @return an iterable on the top-level values of this config. */
	Iterable<Object> values();

	// --- CONFIG METADATA ---

	/** @return the number of top-level entries in this config. */
	int size();

	/** @return true if this configration is empty, i.e. if it contains no entry. */
	default boolean isEmpty() {
		return size() == 0;
	}


	// --- ENTRY GETTERS ---

	/**
	 * Gets a config entry.
	 * @param path path to the entry
	 * @return the entry, or {@code null} if it doesn't exist
	 */
	Entry getEntry(String[] path);

	/**
	 * Gets a config entry.
	 * @param path path to the entry
	 * @return the entry, or {@code null} if it doesn't exist
	 */
	Entry getEntry(Iterable<String> path);

	/**
	 * Gets a config entry.
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @return the entry, or {@code null} if it doesn't exist
	 */
	default Entry getEntry(String path) {
		return getEntry(splitPath(path));
	}

	/**
	 * Gets a config entry, wrapped in {@link Optional}.
	 * @param path path to the entry
	 * @return an Optional containing the entry, or an empty Optional if it doesn't exist
	 */
	default Optional<? extends Entry> getOptionalEntry(String path) {
		return getOptionalEntry(splitPath(path));
	}

	/**
	 * Gets a config entry, wrapped in {@link Optional}.
	 * @param path path to the entry
	 * @return an Optional containing the entry, or an empty Optional if it doesn't exist
	 */
	default Optional<? extends Entry> getOptionalEntry(String[] path) {
		return Optional.ofNullable(getEntry(path));
	}

	/**
	 * Gets a config entry, wrapped in {@link Optional}.
	 * @param path path to the entry
	 * @return an Optional containing the entry, or an empty Optional if it doesn't exist
	 */
	default Optional<? extends Entry> getOptionalEntry(Iterable<String> path) {
		return Optional.ofNullable(getEntry(path));
	}

	// --- VALUES GETTERS ---

	/**
	 * Gets a value from the config.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param <T>  type of value
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 */
	default <T> T get(String path) {
		return get(splitPath(path));
	}

	/**
	 * Gets a value from the config.
	 *
	 * @param path path to the entry
	 * @param <T>  type of value
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 */
	default <T> T get(String[] path) {
		Entry data = getEntry(path);
		return data == null ? null : data.getValue();
	}

	/**
	 * Gets a value from the config.
	 *
	 * @param path path to the entry
	 * @param <T>  type of value
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 */
	default <T> T get(Iterable<String> path) {
		Entry data = getEntry(path);
		return data == null ? null : data.getValue();
	}

	/**
	 * Gets a value from the config, wrapped in {@link Optional}.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param <T>  type of value
	 * @return an Optional containing the value, or an empty Optional if the entry doesn't exist
	 */
	default <T> Optional<T> getOptional(String path) {
		return getOptional(splitPath(path));
	}

	/**
	 * Gets a value from the config, wrapped in {@link Optional}.
	 *
	 * @param path path to the entry
	 * @param <T>  type of value
	 * @return an Optional containing the value, or an empty Optional if the entry doesn't exist
	 */
	default <T> Optional<T> getOptional(String[] path) {
		return Optional.ofNullable(get(path));
	}

	/**
	 * Gets a value from the config, wrapped in {@link Optional}.
	 *
	 * @param path path to the entry
	 * @param <T>  type of value
	 * @return an Optional containing the value, or an empty Optional if the entry doesn't exist
	 */
	default <T> Optional<T> getOptional(Iterable<String> path) {
		return Optional.ofNullable(get(path));
	}


	// --- ATTRIBUTES GETTERS ---

	/**
	 * Gets an attribute from the config.
	 *
	 * @param attribute type of attribute
	 * @param path      dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param <T>       type of value
	 * @return the value of the attribute, or {@code null} if the entry doesn't exist or doesn't have this attribute
	 */
	default <T> T get(AttributeType<T> attribute, String path) {
		return get(attribute, splitPath(path));
	}

	/**
	 * Gets an attribute from the config.
	 *
	 * @param attribute type of attribute
	 * @param path      path to the entry
	 * @param <T>       type of value
	 * @return the value of the attribute, or {@code null} if the entry doesn't exist or doesn't have this attribute
	 */
	default <T> T get(AttributeType<T> attribute, String[] path) {
		Entry entry = getEntry(path);
		return entry == null ? null : entry.get(attribute);
	}

	/**
	 * Gets an attribute from the config.
	 *
	 * @param attribute type of attribute
	 * @param path      path to the entry
	 * @param <T>       type of value
	 * @return the value of the attribute, or {@code null} if the entry doesn't exist or doesn't have this attribute
	 */
	default <T> T get(AttributeType<T> attribute, Iterable<String> path) {
		Entry entry = getEntry(path);
		return entry == null ? null : entry.get(attribute);
	}

	/**
	 * Gets an attribute from the config, wrapped in {@link Optional}.
	 *
	 * @param attribute type of attribute
	 * @param path      dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param <T>       type of value
	 * @return an Optional containing the value of the attribute,
	 * or an empty Optional if the entry doesn't exist or doesn't have this attribute
	 */
	default <T> Optional<T> getOptional(AttributeType<T> attribute, String path) {
		return getOptional(attribute, splitPath(path));
	}

	/**
	 * Gets an attribute from the config, wrapped in {@link Optional}.
	 *
	 * @param attribute type of attribute
	 * @param path      path to the entry
	 * @param <T>       type of value
	 * @return an Optional containing the value of the attribute,
	 * or an empty Optional if the entry doesn't exist or doesn't have this attribute
	 */
	default <T> Optional<T> getOptional(AttributeType<T> attribute, String[] path) {
		Entry entry = getEntry(path);
		return entry == null ? Optional.empty() : entry.getOptional(attribute);
	}

	/**
	 * Gets an attribute from the config, wrapped in {@link Optional}.
	 *
	 * @param attribute type of attribute
	 * @param path      path to the entry
	 * @param <T>       type of value
	 * @return an Optional containing the value of the attribute,
	 * or an empty Optional if the entry doesn't exist or doesn't have this attribute
	 */
	default <T> Optional<T> getOptional(AttributeType<T> attribute, Iterable<String> path) {
		Entry entry = getEntry(path);
		return entry == null ? Optional.empty() : entry.getOptional(attribute);
	}

	// --- COMMENTS GETTERS ---

	/**
	 * Gets a comment from the config.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @return the old comment, or {@code null} if the entry doesn't exist or doesn't have a comment
	 */
	default String getComment(String path) {
		return get(StandardAttributes.COMMENT, path);
	}

	/**
	 * Gets a comment from the config.
	 *
	 * @param path path to the entry
	 * @return the old comment, or {@code null} if the entry doesn't exist or doesn't have a comment
	 */
	default String getComment(String[] path) {
		return get(StandardAttributes.COMMENT, path);
	}

	/**
	 * Gets a comment from the config.
	 *
	 * @param path path to the entry
	 * @return the old comment, or {@code null} if the entry doesn't exist or doesn't have a comment
	 */
	default String getComment(Iterable<String> path) {
		return get(StandardAttributes.COMMENT, path);
	}

	/**
	 * Gets a comment from the config, wrapped in {@link Optional}.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @return an Optional containing the comment,
	 * or an empty Optional if the entry doesn't exist or doesn't have a comment
	 */
	default Optional<String> getOptionalComment(String path) {
		return getOptional(StandardAttributes.COMMENT, path);
	}

	/**
	 * Gets a comment from the config, wrapped in {@link Optional}.
	 *
	 * @param path path to the entry
	 * @return an Optional containing the comment,
	 * or an empty Optional if the entry doesn't exist or doesn't have a comment
	 */
	default Optional<String> getOptionalComment(String[] path) {
		return getOptional(StandardAttributes.COMMENT, path);
	}

	/**
	 * Gets a comment from the config, wrapped in {@link Optional}.
	 *
	 * @param path path to the entry
	 * @return an Optional containing the comment,
	 * or an empty Optional if the entry doesn't exist or doesn't have a comment
	 */
	default Optional<String> getOptionalComment(Iterable<String> path) {
		return getOptional(StandardAttributes.COMMENT, path);
	}

	// --- GETTERS FOR ENUM VALUES ---

	/**
	 * Gets an Enum value from the config.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @param path     dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param enumType class of the Enum
	 * @param method   way of converting a non-enum value (like a String or an int) to an enum value
	 * @param <T>      generic enum type
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> T getEnum(String path, Class<T> enumType, EnumGetMethod method) {
		return getEnum(splitPath(path), enumType, method);
	}

	/**
	 * Calls {@link #getEnum(String, Class, EnumGetMethod)} with method
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> T getEnum(String path, Class<T> enumType) {
		return getEnum(splitPath(path), enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an Enum value from the config.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @param path     path to the entry
	 * @param enumType class of the Enum
	 * @param method   way of converting a non-enum value (like a String or an int) to an enum value
	 * @param <T>      generic enum type
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> T getEnum(String[] path, Class<T> enumType, EnumGetMethod method) {
		final Object value = get(path);
		return method.get(value, enumType);
	}

	/**
	 * Calls {@link #getEnum(String[], Class, EnumGetMethod)} with
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> T getEnum(String[] path, Class<T> enumType) {
		return getEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an Enum value from the config.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @param path     path to the entry
	 * @param enumType class of the Enum
	 * @param method   way of converting a non-enum value (like a String or an int) to an enum value
	 * @param <T>      generic enum type
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> T getEnum(Iterable<String> path, Class<T> enumType, EnumGetMethod method) {
		final Object value = get(path);
		return method.get(value, enumType);
	}

	/**
	 * Calls {@link #getEnum(Iterable, Class, EnumGetMethod)} with
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> T getEnum(Iterable<String> path, Class<T> enumType) {
		return getEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an Enum value from the config, wrapped in {@link Optional}.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @param path     dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @param enumType class of the Enum
	 * @param method   way of converting a non-enum value (like a String or an int) to an enum value
	 * @param <T>      generic enum type
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(String path, Class<T> enumType, EnumGetMethod method) {
		return getOptionalEnum(splitPath(path), enumType, method);
	}

	/**
	 * Calls {@link #getOptionalEnum(String, Class, EnumGetMethod)} with
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(String path, Class<T> enumType) {
		return getOptionalEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an Enum value from the config, wrapped in {@link Optional}.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @param path     path to the entry
	 * @param enumType class of the Enum
	 * @param method   way of converting a non-enum value (like a String or an int) to an enum value
	 * @param <T>      generic enum type
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(String[] path, Class<T> enumType, EnumGetMethod method) {
		return Optional.ofNullable(getEnum(path, enumType, method));
	}

	/**
	 * Calls {@link #getOptionalEnum(String[], Class, EnumGetMethod)} with
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(String[] path, Class<T> enumType) {
		return getOptionalEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	/**
	 * Gets an Enum value from the config, wrapped in {@link Optional}.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @param path     path to the entry
	 * @param enumType class of the Enum
	 * @param method   way of converting a non-enum value (like a String or an int) to an enum value
	 * @param <T>      generic enum type
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(Iterable<String> path, Class<T> enumType, EnumGetMethod method) {
		return Optional.ofNullable(getEnum(path, enumType, method));
	}

	/**
	 * Calls {@link #getOptionalEnum(Iterable, Class, EnumGetMethod)} with
	 * {@link EnumGetMethod#NAME_IGNORECASE}.
	 * <p>
	 * Returns null if the entry doesn't exist.
	 * Throws an exception if the value cannot be converted to the given enum.
	 *
	 * @return the value of the entry at that path, or {@code null} if the entry doesn't exist
	 * @throws IllegalArgumentException if the config contains a value that doesn't match any of
	 *                                  the enum constants, with regards to the given method
	 * @throws ClassCastException       if the config contains a value that cannot be converted to
	 *                                  an enum constant because of its type, for instance a List
	 */
	default <T extends Enum<T>> Optional<T> getOptionalEnum(Iterable<String> path, Class<T> enumType) {
		return getOptionalEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
	}

	// --- PRIMITIVE INT GETTERS ---

	/**
	 * Like {@link #get(String)} but returns a primitive int. The value must be a {@link Number}.
	 */
	default int getInt(String path) {
		return this.<Number>get(path).intValue();
	}

	/**
	 * Like {@link #get(String[])} but returns a primitive int. The value must be a {@link Number}.
	 */
	default int getInt(String[] path) {
		return this.<Number>get(path).intValue();
	}

	/**
	 * Like {@link #get(Iterable)} but returns a primitive int. The value must be a {@link Number}.
	 */
	default int getInt(Iterable<String> path) {
		return this.<Number>get(path).intValue();
	}

	/**
	 * Like {@link #getOptional(String)} but returns a primitive int. The value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalInt getOptionalInt(String path) {
		return getOptionalInt(splitPath(path));
	}

	/**
	 * Like {@link #getOptional(String[])} but returns an {@link OptionalInt}. The value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalInt getOptionalInt(String[] path) {
		Number n = get(path);
		return n == null ? OptionalInt.empty() : OptionalInt.of(n.intValue());
	}

	/**
	 * Like {@link #getOptional(Iterable)} but returns an {@link OptionalInt}. The value must be a
	 * {@link Number} or null or nonexistant.
	 */
	default OptionalInt getOptionalInt(Iterable<String> path) {
		Number n = get(path);
		return n == null ? OptionalInt.empty() : OptionalInt.of(n.intValue());
	}

	// --- PRIMITIVE LONG GETTERS ---
	/**
	 * Like {@link #get(String)} but returns a primitive long. The value must be a {@link Number}.
	 */
	default long getLong(String path) {
		return this.<Number>get(path).longValue();
	}

	/**
	 * Like {@link #get(String[])} but returns a primitive long. The value must be a {@link Number}.
	 */
	default long getLong(String[] path) {
		return this.<Number>get(path).longValue();
	}

	/**
	 * Like {@link #get(Iterable)} but returns a primitive long. The value must be a {@link Number}.
	 */
	default long getLong(Iterable<String> path) {
		return this.<Number>get(path).longValue();
	}

	/**
	 * Like {@link #getOptional(String)} but returns an {@link OptionalLong}. The value must be a
	 * {@link Number}.
	 */
	default OptionalLong getOptionalLong(String path) {
		return getOptionalLong(splitPath(path));
	}

	/**
	 * Like {@link #getOptional(String[])} but returns an {@link OptionalLong}. The value must be a
	 * {@link Number}.
	 */
	default OptionalLong getOptionalLong(String[] path) {
		Number n = get(path);
		return n == null ? OptionalLong.empty() : OptionalLong.of(n.longValue());
	}

	/**
	 * Like {@link #getOptional(Iterable)} but returns an {@link OptionalLong}. The value must be a
	 * {@link Number}.
	 */
	default OptionalLong getOptionalLong(Iterable<String> path) {
		Number n = get(path);
		return n == null ? OptionalLong.empty() : OptionalLong.of(n.longValue());
	}

	// --- PRIMITIVE BYTE GETTERS ---

	/**
	 * Like {@link #get(String)} but returns a primitive byte. The value must be a {@link Number}.
	 */
	default byte getByte(String path) {
		return this.<Number>get(path).byteValue();
	}

	/**
	 * Like {@link #get(String[])} but returns a primitive long. The value must be a {@link Number}.
	 */
	default byte getByte(String[] path) {
		return this.<Number>get(path).byteValue();
	}

	/**
	 * Like {@link #get(Iterable)} but returns a primitive long. The value must be a {@link Number}.
	 */
	default byte getByte(Iterable<String> path) {
		return this.<Number>get(path).byteValue();
	}

	// --- PRIMITIVE SHORT GETTERS ---
	/**
	 * Like {@link #get(String)} but returns a primitive short. The value must be a {@link Number}.
	 */
	default short getShort(String path) {
		return this.<Number>get(path).shortValue();
	}

	/**
	 * Like {@link #get(String)} but returns a primitive short. The value must be a {@link Number}.
	 */
	default short getShort(String[] path) {
		return this.<Number>get(path).shortValue();
	}

	/**
	 * Like {@link #get(Iterable)} but returns a primitive short. The value must be a {@link Number}.
	 */
	default short getShort(Iterable<String> path) {
		return this.<Number>get(path).shortValue();
	}

	// --- PRIMITIVE DOUBLE GETTERS ---

	/**
	 * Like {@link #get(String)} but returns a primitive double. The value must be a {@link Number}.
	 */
	default double getDouble(String path) {
		return this.<Number>get(path).doubleValue();
	}

	/**
	 * Like {@link #get(String[])} but returns a primitive double. The value must be a {@link Number}.
	 */
	default double getDouble(String[] path) {
		return this.<Number>get(path).doubleValue();
	}

	/**
	 * Like {@link #get(Iterable)} but returns a primitive double. The value must be a {@link Number}.
	 */
	default double getDouble(Iterable<String> path) {
		return this.<Number>get(path).doubleValue();
	}

	/**
	 * Like {@link #getOptional(String)} but returns a primitive double. The value must be a {@link Number}.
	 */
	default OptionalDouble getOptionalDouble(String path) {
		return getOptionalDouble(splitPath(path));
	}

	/**
	 * Like {@link #getOptional(String[])} but returns a primitive double. The value must be a {@link Number}.
	 */
	default OptionalDouble getOptionalDouble(String[] path) {
		Number n = get(path);
		return n == null ? OptionalDouble.empty() : OptionalDouble.of(n.doubleValue());
	}

	/**
	 * Like {@link #getOptional(Iterable)} but returns a primitive double. The value must be a {@link Number}.
	 */
	default OptionalDouble getOptionalDouble(Iterable<String> path) {
		Number n = get(path);
		return n == null ? OptionalDouble.empty() : OptionalDouble.of(n.doubleValue());
	}

	// --- PRIMITIVE FLOAT GETTERS ---

	/**
	 * Like {@link #get(String)} but returns a primitive float. The config's value must be a
	 * {@link Number}.
	 */
	default float getFloat(String path) {
		return this.<Number>get(path).floatValue();
	}

	/**
	 * Like {@link #get(String[])} but returns a primitive float. The config's value must be a
	 * {@link Number}.
	 */
	default float getFloat(String[] path) {
		return this.<Number>get(path).floatValue();
	}

	// --- PRIMITIVE CHAR GETTERS ---

	/**
	 * Like {@link #get(String)} but returns a primitive char.
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
	 * Like {@link #get(String[])} but returns a primitive char.
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
		}
		return (char)value;
	}

	/**
	 * Like {@link #get(String[])} but returns a primitive char.
	 * <p>
	 * If the value is a Number, returns {@link Number#intValue()}, cast to char.
	 * If the value is a CharSequence, returns its first character.
	 * Otherwise, attempts to cast the value to a char.
	 *
	 * @param path the value's path as a list of String
	 * @return the value, as a single char
	 */
	default char getChar(Iterable<String> path) {
		Object value = get(path);
		if (value instanceof Number) {
			return (char)((Number)value).intValue();
		} else if (value instanceof CharSequence) {
			return ((CharSequence)value).charAt(0);
		}
		return (char)value;
	}

	// --- BOOLEAN QUERIES ---

	/**
	 * Returns true if there exist an entry at the given path in this config.
	 *
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @return {@code true} if the entry exist, {@code false} if it doesn't.
	 */
	default boolean contains(String path) {
		return contains(splitPath(path));
	}

	/**
	 * Returns true if there exist an entry at the given path in this config.
	 *
	 * @param path path to the entry
	 * @return {@code true} if the entry exist, {@code false} if it doesn't.
	 */
	default boolean contains(String[] path) {
		return getEntry(path) != null;
	}

	/**
	 * Returns true if there exist an entry at the given path in this config.
	 *
	 * @param path path to the entry
	 * @return {@code true} if the entry exist, {@code false} if it doesn't.
	 */
	default boolean contains(Iterable<String> path) {
		return getEntry(path) != null;
	}

	/**
	 * Returns true if there exist an entry at the given path and this entry has the given attribute.
	 *
	 * @param attribute attribute type
	 * @param path dot-separated string, for example "a.b.c" refers to path {@code ["a", "b", "c"]}
	 * @return {@code true} if the entry exist and it has the given attribute, {@code false} otherwise
	 */
	default boolean has(AttributeType<?> attribute, String path) {
		return has(attribute, splitPath(path));
	}

	/**
	 * Returns true if there exist an entry at the given path and this entry has the given attribute.
	 *
	 * @param attribute attribute type
	 * @param path path to the entry
	 * @return {@code true} if the entry exist and it has the given attribute, {@code false} otherwise
	 */
	default boolean has(AttributeType<?> attribute, String[] path) {
		Entry entry = getEntry(path);
		return entry != null && entry.has(attribute);
	}

	/**
	 * Returns true if there exist an entry at the given path and this entry has the given attribute.
	 *
	 * @param attribute attribute type
	 * @param path path to the entry
	 * @return {@code true} if the entry exist and it has the given attribute, {@code false} otherwise
	 */
	default boolean has(AttributeType<?> attribute, Iterable<String> path) {
		Entry entry = getEntry(path);
		return entry != null && entry.has(attribute);
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
			return value == null ? OptionalInt.empty() : OptionalInt.of(value.intValue());
		}

		/**
		 * @return the entry's value as a long
		 */
		default long getLong() {
			return this.<Number>getValue().longValue();
		}

		default OptionalLong getOptionalLong() {
			Number value = getValue();
			return value == null ? OptionalLong.empty() : OptionalLong.of(value.longValue());
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
			return value == null ? OptionalDouble.empty() : OptionalDouble.of(value.doubleValue());
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
			}
			return (char)value;
		}
	}

	interface Attribute<T> {
		AttributeType<T> getType();
		T getValue();
	}
}
