package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.impl.CharacterOutput;
import com.electronwill.nightconfig.core.impl.CharrayWriter;
import com.electronwill.nightconfig.core.impl.WriterOutput;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static java.nio.file.StandardOpenOption.*;

/**
 * Interface for writing configurations.
 *
 * @author TheElectronWill
 */
public interface ConfigWriter {
	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param output where to write
	 * @throws WritingException if an error occurs
	 */
	void write(UnmodifiableConfig config, CharacterOutput output);

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param writer where to write
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, Writer writer) {
		write(config, new WriterOutput(writer));
	}

	/**
	 * Writes a configuration using the specified encoding.
	 *
	 * @param config the config to write
	 * @param output where to write
	 * @param cs text encoding
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, OutputStream output, Charset cs) {
		Writer writer = new BufferedWriter(new OutputStreamWriter(output, cs));
		write(config, writer);
		try {
			writer.flush();
		} catch (IOException e) {
			throw new WritingException("Failed to flush the writer", e);
		}
	}

	/**
	 * Writes a configuration <b>in UTF-8</b>.
	 *
	 * @param config the config to write
	 * @param output where to write
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, OutputStream output) {
		write(config, output, StandardCharsets.UTF_8);
	}

	/**
	 * Writes a configuration using the specified encoding.
	 *
	 * @param config the config to write
	 * @param file where to write
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, Path file, Charset cs, WritingMode mode) {
		StandardOpenOption[] options;
		if (mode == WritingMode.APPEND) {
			options = new StandardOpenOption[] { WRITE, CREATE, APPEND };
		} else {
			options = new StandardOpenOption[] { WRITE, CREATE, TRUNCATE_EXISTING };
		}
		try (OutputStream output = Files.newOutputStream(file, options)) {
			write(config, output, cs);
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}

	/**
	 * Writes a configuration <b>in UTF-8</b>.
	 *
	 * @param config the config to write
	 * @param file where to write
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, Path file, WritingMode mode) {
		write(config, file, StandardCharsets.UTF_8, mode);
	}

	/**
	 * Writes a configuration using the specified encoding.
	 *
	 * @param config the config to write
	 * @param file where to write
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, File file, Charset cs, WritingMode mode) {
		write(config, file.toPath(), cs, mode);
	}

	/**
	 * Writes a configuration <b>in UTF-8</b>.
	 *
	 * @param config the config to write
	 * @param file where to write
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, File file, WritingMode mode) {
		write(config, file, StandardCharsets.UTF_8, mode);
	}

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param url the url to write it to
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, URL url) {
		IOUtils.useURL(url, null, URLConnection::getOutputStream, (outputStream, charset) -> {
			write(config, outputStream, charset);
			return null;
		}, WritingException::new);
	}

	/**
	 * Writes a configuration to a String.
	 *
	 * @param config the config to write
	 * @return a new String
	 * @throws WritingException if an error occurs
	 */
	default String writeToString(UnmodifiableConfig config) {
		CharrayWriter writer = new CharrayWriter();
		write(config, (CharacterOutput)writer);
		return writer.toString();
	}
}
