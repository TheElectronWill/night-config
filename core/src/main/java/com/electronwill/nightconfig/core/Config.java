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
	 * @return the number of top-level elements in this config.
	 */
	int size();

	/**
	 * Returns a map view of this config. Any change to the map is reflected in the config and vice-versa.
	 *
	 * @return a map view of this config.
	 */
	Map<String, Object> asMap();

	/**
	 * Checks if the given type is supported by this config. Please note that an implementation of the
	 * Config interface is <b>not</b> required to check the type of the values that you add to it.
	 * Actually, the default behavior is to allow any value. If an implementation behaves otherwise it must
	 * document it.
	 *
	 * @param type the type's class.
	 * @return {@code true} if it is supported, {@code false} otherwise.
	 */
	default boolean supportsType(Class<?> type) {
		return true;
	}

	/**
	 * Checks if the given path is associated with a value.
	 *
	 * @param path the path.
	 * @return {@code true} if the value exist, {@code false} if it doesn't.
	 */
	boolean containsValue(String path);

	/**
	 * Gets the value at the given path.
	 *
	 * @param path the path.
	 * @return the value at the given path, or {@code null} if no value is associated with this path.
	 */
	Object getValue(String path);

	/**
	 * Sets the value at the given path.
	 *
	 * @param path  the path.
	 * @param value the value to set.
	 */
	void setValue(String path, Object value);

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
	default long getLong(String path) {
		return (long)getValue(path);
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
	default boolean getBoolean(String path) {
		return (boolean)getValue(path);
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
	default String getString(String path) {
		return (String)getValue(path);
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
	default <T> List<T> getList(String path) {
		return (List<T>)getValue(path);
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
	default Config getConfig(String path) {
		return (Config)getValue(path);
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
}
