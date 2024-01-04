package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.utils.WriterSupplier;

import java.io.File;
import java.io.Writer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A configuration format, that can parse, create and write some types of configurations.
 *
 * @param <C> the type of configurations created by this format
 * @author TheElectronWill
 */
public interface ConfigFormat<C extends Config> {
	/**
	 * @return a writer of this config format
	 */
	ConfigWriter createWriter();

	/**
	 * @return a parser of this config format
	 */
	ConfigParser<C> createParser();

	/**
	 * @return a config of this format
	 */
	default C createConfig() {
		return createConfig(Config.getDefaultMapCreator(false));
	}

	/**
	 * Creates a config of this format. The returned config is guaranteed to be thread-safe.
	 *
	 * @return a concurrent config of this format
	 */
	default C createConcurrentConfig() {
		return createConfig(Config.getDefaultMapCreator(true));
	}

	/**
	 * Creates a config that uses the given map supplier for all its levels (top
	 * level and subconfigs).
	 *
	 * @param mapCreator the map supplier for the config
	 * @return a config of this format with the given map creator
	 */
	C createConfig(Supplier<Map<String, Object>> mapCreator);

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
	 *
	 * @return {@code true} iff this format is only in memory.
	 */
	default boolean isInMemory() {
		return false;
	}

	/**
	 * Initializes an empty configuration file so that it can be parsed to an empty configuration.
	 * Does nothing by default.
	 *
	 * @param f the existing file to initialize
	 */
	default void initEmptyFile(Path f) throws IOException {
		initEmptyFile(() -> Files.newBufferedWriter(f));
	}

	/**
	 * Initializes an empty configuration file so that it can be parsed to an empty configuration.
	 * Does nothing by default.
	 *
	 * @param f the existing file to initialize
	 */
	default void initEmptyFile(File f) throws IOException {
		initEmptyFile(f.toPath());
	}

	/**
	 * Initializes an empty configuration file so that it can be parsed to an empty configuration.
	 * Does nothing by default.
	 *
	 * @param ws an objet that provides a Writer to the file (the Writer will be closed by this method)
	 */
	default void initEmptyFile(WriterSupplier ws) throws IOException {
		initEmptyFile(ws.get());
	}

	/**
	 * Initializes an empty configuration file so that it can be parsed to an empty configuration.
	 * Does nothing by default.
	 *
	 * @param writer a Writer to use to write to the file (will not be closed)
	 */
	default void initEmptyFile(Writer writer) throws IOException {}
}