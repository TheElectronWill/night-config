package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * @author TheElectronWill
 */
public interface ConfigFormat<C extends D, D extends Config, W extends UnmodifiableConfig> {
	/**
	 * @return a writer of this config format
	 */
	ConfigWriter<W> createWriter();

	/**
	 * @return a parser of this config format
	 */
	ConfigParser<C, D> createParser();

	/**
	 * @return a config of this format
	 */
	C createConfig();

	/**
	 * Checks if this format supports CommentedConfigs. Note that supporting CommentedConfigs
	 * isn't the same things as allowing the user to write comments in the config files.
	 *
	 * @return {@code true} iff this format supports CommentedConfigs
	 */
	boolean supportsComments();

	/**
	 * Checks if this format supports the given type of value.
	 *
	 * @param type the type to check, may be null in which case this method checks if the format
	 *             supports null values
	 * @return {@code true} iff this format supports the given type
	 */
	default boolean supportsType(Class<?> type) {
		return InMemoryFormat.DEFAULT_PREDICATE.test(type);
	}

	/**
	 * Checks if this format is in memory only and therefore cannot create writers nor parsers.
	 * @return {@code true} iff this format is only in memory.
	 */
	default boolean isInMemory() {
		return false;
	}
}