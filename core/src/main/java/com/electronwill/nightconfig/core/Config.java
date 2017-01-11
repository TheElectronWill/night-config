package com.electronwill.nightconfig.core;

import java.util.List;
import java.util.Map;

/**
 * A configuration that contains key-value mappings. Configurations are generally <b>not</b> thread-safe.
 * <h1>Paths</h1>
 * <p>
 * Each value is accessible by its path. A path is a String of one or more part, each separated by a dot.
 * For instance, take a config like this: <code>{ infos = { name = "M. Smith", age = 42} }</code>. To access
 * the name "M. Smith" you would use the path {@code "infos.name"}.
 * </p>
 * <p>
 * Note that, with paths, is is not possible to access a key if its name contains one or more dots. To do
 * that, use the {@link #asMap()} method and traverse the Map manually.
 * </p>
 *
 * @author TheElectronWill
 */
public interface Config {

	/**
	 * Checks if the config is empty.
	 *
	 * @return {@code true} if it is empty, {@code false} if it not.
	 */
	default boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * @return the number of top-level elements in this config.
	 */
	int size();

	/**
	 * Returns a map view of this config. Any change to the map is reflected in the config and vice-versa.
	 * <p>
	 * The returned map doesn't check if the values are supported by this configuration, and doesn't
	 * perform (unless otherwise stated) any kind of synchronization nor anything else to ensure
	 * thread-safety. The caller of this method is responsible for taking care of these things.
	 * </p>
	 *
	 * @return a map view of this config.
	 */
	Map<String, Object> asMap();

	/**
	 * Checks if the given type is supported by this config. Please note that an implementation of the
	 * Config interface is <b>not</b> required to check the type of the values that you add to it.
	 * And actually, the usual and default behavior is not to check.
	 *
	 * @param type the type's class.
	 * @return {@code true} if it is supported, {@code false} otherwise.
	 */
	boolean supportsType(Class<?> type);

	/**
	 * Checks if the given path is associated with a value.
	 *
	 * @param path the path.
	 * @return {@code true} if the value exist, {@code false} if it doesn't.
	 */
	boolean containsValue(List<String> path);

	/**
	 * Checks if the given path is associated with a value.
	 *
	 * @param path the path.
	 * @return {@code true} if the value exist, {@code false} if it doesn't.
	 */
	default boolean containsValue(String path) {
		return containsValue(StringUtils.split(path, '.'));
	}

	/**
	 * Gets the value at the given path.
	 *
	 * @param path the path.
	 * @return the value at the given path, or {@code null} if no value is associated with this path.
	 */
	Object getValue(List<String> path);

	/**
	 * Gets the value at the given path, as an instance of the specified class.
	 *
	 * @param path  the path.
	 * @param clazz the class to cast the value to.
	 * @param <T>   the type of the class
	 * @return a value of type T, {@code null} if no value is associated with this path.
	 */
	default <T> T getValue(List<String> path, Class<T> clazz) {
		return clazz.cast(getValue(path));
	}

	/**
	 * Gets the value at the given path.
	 *
	 * @param path the path.
	 * @return the value at the given path, or {@code null} if no value is associated with this path.
	 */
	default Object getValue(String path) {
		return getValue(StringUtils.split(path, '.'));
	}

	/**
	 * Gets the value at the given path, as an instance of the specified class.
	 *
	 * @param path  the path.
	 * @param clazz the class to cast the value to.
	 * @param <T>   the type of the class
	 * @return a value of type T, {@code null} if no value is associated with this path.
	 */
	default <T> T getValue(String path, Class<T> clazz) {
		return clazz.cast(getValue(path));
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	void setValue(List<String> path, Object value);

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setValue(String path, Object value) {
		setValue(StringUtils.split(path, '.'), value);
	}

	/**
	 * Gets the int value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type int, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the int value at the given path.
	 */
	default Integer getInt(List<String> path) {
		return (Integer)getValue(path);
	}

	/**
	 * Gets the int value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type int, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the int value at the given path.
	 */
	default Integer getInt(String path) {
		return (Integer)getValue(path);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setInt(List<String> path, int value) {
		setValue(path, value);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setInt(String path, int value) {
		setValue(path, value);
	}

	/**
	 * Gets the long value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type long, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the long value at the given path.
	 */
	default Long getLong(List<String> path) {
		return (Long)getValue(path);
	}

	/**
	 * Gets the long value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type long, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the long value at the given path.
	 */
	default Long getLong(String path) {
		return (Long)getValue(path);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setLong(List<String> path, long value) {
		setValue(path, value);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setLong(String path, long value) {
		setValue(path, value);
	}

	/**
	 * Gets the float value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type float, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the float value at the given path.
	 */
	default Float getFloat(List<String> path) {
		return (Float)getValue(path);
	}

	/**
	 * Gets the float value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type float, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the float value at the given path.
	 */
	default Float getFloat(String path) {
		return (Float)getValue(path);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setFloat(List<String> path, float value) {
		setValue(path, value);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setFloat(String path, float value) {
		setValue(path, value);
	}

	/**
	 * Gets the double value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type double, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the double value at the given path.
	 */
	default Double getDouble(List<String> path) {
		return (Double)getValue(path);
	}

	/**
	 * Gets the double value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type double, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the double value at the given path.
	 */
	default Double getDouble(String path) {
		return (Double)getValue(path);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setDouble(List<String> path, double value) {
		setValue(path, value);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setDouble(String path, double value) {
		setValue(path, value);
	}

	/**
	 * Gets the boolean value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type boolean, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the boolean value at the given path.
	 */
	default Boolean getBoolean(List<String> path) {
		return (Boolean)getValue(path);
	}

	/**
	 * Gets the boolean value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type boolean, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the boolean value at the given path.
	 */
	default Boolean getBoolean(String path) {
		return (Boolean)getValue(path);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setBoolean(List<String> path, boolean value) {
		setValue(path, value);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setBoolean(String path, boolean value) {
		setValue(path, value);
	}

	/**
	 * Gets the String value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type String, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the String value at the given path.
	 */
	default String getString(List<String> path) {
		return (String)getValue(path);
	}

	/**
	 * Gets the String value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type String, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the String value at the given path.
	 */
	default String getString(String path) {
		return (String)getValue(path);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setString(List<String> path, String value) {
		setValue(path, value);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setString(String path, String value) {
		setValue(path, value);
	}

	/**
	 * Gets the List value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type List, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the List value at the given path.
	 */
	default <T> List<T> getList(List<String> path) {
		return (List<T>)getValue(path);
	}

	/**
	 * Gets the List value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type List, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the List value at the given path.
	 */
	default <T> List<T> getList(String path) {
		return (List<T>)getValue(path);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default <T> void setList(List<String> path, List<T> value) {
		setValue(path, value);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default <T> void setList(String path, List<T> value) {
		setValue(path, value);
	}

	/**
	 * Gets the Config value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type Config, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the Config value at the given path.
	 */
	default Config getConfig(List<String> path) {
		return (Config)getValue(path);
	}

	/**
	 * Gets the Config value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type Config, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the Config value at the given path.
	 */
	default Config getConfig(String path) {
		return (Config)getValue(path);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setConfig(List<String> path, Config value) {
		setValue(path, value);
	}

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	default void setConfig(String path, Config value) {
		setValue(path, value);
	}

	/**
	 * Creates a new empty instance of the same type as this configuration.
	 *
	 * @return a new empty config
	 */
	Config createEmptyConfig();

}
