package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.check.UpdateChecker;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.utils.WriterSupplier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A configuration format that can parse and write configurations.
 *
 * @author TheElectronWill
 */
public interface ConfigFormat {
	/** @return a new ConfigWriter for this format */
	ConfigWriter writer();

	/** @return a new ConfigParser for this format */
	ConfigParser parser();

	/**
	 * @return a ConfigChecker that checks whether the config's values are supported by this
	 * configuration format.
	 */
	UpdateChecker checker();

	/**
	 * @return true if this format handles the given attribute.
	 */
	boolean supportsAttribute(AttributeType<?> attribute);

	/** @return true if this format supports config comments. */
	boolean supportsComments();

	/** @return true if this format supports the given value. */
	boolean supportsValue(Object value);

	/**
	 * @param type the type to check, may be null to check if the format supports null values
	 * @return true if this format supports the given type of value.
	 */
	boolean supportsType(Class<?> type);

	/**
	 * Initializes an empty configuration file so that it can be parsed to an empty configuration.
	 *
	 * @param f the existing file to initialize
	 */
	default void initEmptyFile(Path f) throws IOException {
		initEmptyFile(() -> Files.newBufferedWriter(f));
	}

	/**
	 * Initializes an empty configuration file so that it can be parsed to an empty configuration.
	 *
	 * @param f the existing file to initialize
	 */
	default void initEmptyFile(File f) throws IOException {
		initEmptyFile(f.toPath());
	}

	/**
	 * Initializes an empty configuration file so that it can be parsed to an empty configuration.
	 *
	 * @param ws an objet that provides a Writer to the file.
	 */
	void initEmptyFile(WriterSupplier ws) throws IOException;
}