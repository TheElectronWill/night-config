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
		final List<String> list = StringUtils.split(path, '.');
		return containsValue(list);
	}

	/**
	 * Gets the value at the given path.
	 *
	 * @param path the path.
	 * @return the value at the given path, or {@code null} if no value is associated with this path.
	 */
	Object getValue(List<String> path);

	/**
	 * Gets the value at the given path.
	 *
	 * @param path the path.
	 * @return the value at the given path, or {@code null} if no value is associated with this path.
	 */
	default Object getValue(String path) {
		final List<String> list = StringUtils.split(path, '.');
		return getValue(list);
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
		final List<String> list = StringUtils.split(path, '.');
		setValue(list, value);
	}

	/**
	 * Gets the int value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type int, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the int value at the given path.
	 */
	default int getInt(List<String> path) {
		return (int)getValue(path);
	}

	/**
	 * Gets the int value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type int, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the int value at the given path.
	 */
	default int getInt(String path) {
		return (int)getValue(path);
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
	default long getLong(List<String> path) {
		return (long)getValue(path);
	}

	/**
	 * Gets the long value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type long, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the long value at the given path.
	 */
	default long getLong(String path) {
		return (long)getValue(path);
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
	default float getFloat(List<String> path) {
		return (float)getValue(path);
	}

	/**
	 * Gets the float value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type float, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the float value at the given path.
	 */
	default float getFloat(String path) {
		return (float)getValue(path);
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
	default double getDouble(List<String> path) {
		return (double)getValue(path);
	}

	/**
	 * Gets the double value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type double, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the double value at the given path.
	 */
	default double getDouble(String path) {
		return (double)getValue(path);
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
	default boolean getBoolean(List<String> path) {
		return (boolean)getValue(path);
	}

	/**
	 * Gets the boolean value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type boolean, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the boolean value at the given path.
	 */
	default boolean getBoolean(String path) {
		return (boolean)getValue(path);
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
		final Object value = getValue(path);
		if (value == null)
			return null;
		return (String)value;
	}

	/**
	 * Gets the String value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type String, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the String value at the given path.
	 */
	default String getString(String path) {
		final Object value = getValue(path);
		if (value == null)
			return null;
		return (String)value;
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
		final Object value = getValue(path);
		if (value == null)
			return null;
		return (List<T>)value;
	}

	/**
	 * Gets the List value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type List, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the List value at the given path.
	 */
	default <T> List<T> getList(String path) {
		final Object value = getValue(path);
		if (value == null)
			return null;
		return (List<T>)value;
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
		final Object value = getValue(path);
		if (value == null)
			return null;
		return (Config)value;
	}

	/**
	 * Gets the Config value at the given path. If the given path isn't associated with a value, or if its value
	 * isn't of type Config, this method throws an exception.
	 *
	 * @param path the path.
	 * @return the Config value at the given path.
	 */
	default Config getConfig(String path) {
		final Object value = getValue(path);
		if (value == null)
			return null;
		return (Config)value;
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
