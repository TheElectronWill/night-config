package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.utils.FastStringReader;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Interface for reading configurations.
 *
 * @param <C> the type of config created by the parser
 * @author TheElectronWill
 */
public interface ConfigParser<C extends Config> {
	/**
	 * @return the format supported by this parser
	 */
	ConfigFormat<C> getFormat();

	/**
	 * Parses a configuration.
	 *
	 * @param reader the reader to parse
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	C parse(Reader reader);

	/**
	 * Parses a configuration.
	 *
	 * @param reader      the reader to parse
	 * @param destination the config where to put the data
	 * @param parsingMode how to handle conflicts with the entries that already are in the destination
	 */
	void parse(Reader reader, Config destination, ParsingMode parsingMode);

	/**
	 * Parses a configuration String.
	 *
	 * @param input the input to parse
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	default C parse(String input) {
		return parse(new FastStringReader(input));
	}

	/**
	 * Parses a configuration String.
	 *
	 * @param input       the input to parse
	 * @param destination the config where to put the data
	 * @param parsingMode how to handle conflicts with the entries that already are in the destination
	 * @throws ParsingException if an error occurs
	 */
	default void parse(String input, Config destination, ParsingMode parsingMode) {
		parse(new StringReader(input), destination, parsingMode);
	}

	/**
	 * Parses a configuration with the UTF-8 charset.
	 *
	 * @param input the input to parse
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	default C parse(InputStream input) {
		return parse(input, StandardCharsets.UTF_8);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param input   the input to parse
	 * @param charset the charset to use to decode the input
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	default C parse(InputStream input, Charset charset) {
		return parse(new BufferedReader(new InputStreamReader(input, charset)));
	}

	/**
	 * Parses a configuration with the UTF-8 charset.
	 *
	 * @param input       the input to parse
	 * @param destination the config where to put the data
	 * @param parsingMode how to handle conflicts with the entries that already are in the destination
	 * @throws ParsingException if an error occurs
	 */
	default void parse(InputStream input, Config destination, ParsingMode parsingMode) {
		parse(input, destination, parsingMode, StandardCharsets.UTF_8);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param input       the input to parse
	 * @param destination the config where to put the data
	 * @param parsingMode how to handle conflicts with the entries that already are in the destination
	 * @param charset     the charset to use to decode the input
	 * @throws ParsingException if an error occurs
	 */
	default void parse(InputStream input, Config destination, ParsingMode parsingMode,
			Charset charset) {
		Reader reader = new BufferedReader(new InputStreamReader(input, charset));
		parse(reader, destination, parsingMode);
	}

	/**
	 * Parses a configuration with the UTF-8 charset.
	 *
	 * @param file           the file to parse
	 * @param notFoundAction what to do when the file does not exist
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	default C parse(File file, FileNotFoundAction notFoundAction) {
		return parse(file, notFoundAction, StandardCharsets.UTF_8);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file           the file to parse
	 * @param notFoundAction what to do when the file does not exist
	 * @param charset        the charset of the file's content
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	default C parse(File file, FileNotFoundAction notFoundAction, Charset charset) {
		return parse(file.toPath(), notFoundAction, charset);
	}

	/**
	 * Parses a configuration with the UTF-8 charset.
	 *
	 * @param file           the file to parse
	 * @param destination    the config where to put the data
	 * @param parsingMode    how to handle conflicts with the entries that already are in the destination
	 * @param notFoundAction what to do when the file does not exist
	 * @throws ParsingException if an error occurs
	 */
	default void parse(File file, Config destination, ParsingMode parsingMode,
			FileNotFoundAction notFoundAction) {
		parse(file, destination, parsingMode, notFoundAction, StandardCharsets.UTF_8);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file           the file to parse
	 * @param destination    the config where to put the data
	 * @param parsingMode    how to handle conflicts with the entries that already are in the destination
	 * @param notFoundAction what to do when the file does not exist
	 * @param charset        the charset to use to decode the input
	 * @throws ParsingException if an error occurs
	 */
	default void parse(File file, Config destination, ParsingMode parsingMode,
			FileNotFoundAction notFoundAction, Charset charset) {
		parse(file.toPath(), destination, parsingMode, notFoundAction, charset);
	}

	/**
	 * Parses a configuration with the UTF-8 charset.
	 *
	 * @param file           the nio Path to parse
	 * @param notFoundAction what to do when the file does not exist
	 * @return a Config
	 * @throws ParsingException if an error occurs
	 */
	default C parse(Path file, FileNotFoundAction notFoundAction) {
		return parse(file, notFoundAction, StandardCharsets.UTF_8);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file           the nio Path to parse
	 * @param notFoundAction what to do when the file does not exist
	 * @param charset        the charset to use to decode the input
	 * @return a Config
	 * @throws ParsingException if an error occurs
	 */
	default C parse(Path file, FileNotFoundAction notFoundAction, Charset charset) {
		try {
			if (Files.notExists(file) && !notFoundAction.run(file, getFormat())) {
				return getFormat().createConfig();
			}
			try (InputStream input = Files.newInputStream(file)) {
				return parse(input, charset);
			}
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}

	/**
	 * Parses a configuration with the UTF-8 charset.
	 *
	 * @param file           the nio Path to parse
	 * @param destination    the config where to put the data
	 * @param parsingMode    how to handle conflicts with the entries that already are in the destination
	 * @param notFoundAction what to do when the file does not exist
	 * @throws ParsingException if an error occurs
	 */
	default void parse(Path file, Config destination, ParsingMode parsingMode,
			FileNotFoundAction notFoundAction) {
		parse(file, destination, parsingMode, notFoundAction, StandardCharsets.UTF_8);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file           the nio Path to parse
	 * @param destination    the config where to put the data
	 * @param parsingMode    how to handle conflicts with the entries that already are in the destination
	 * @param notFoundAction what to do when the file does not exist
	 * @param charset        the charset to use to decode the input
	 * @throws ParsingException if an error occurs
	 */
	default void parse(Path file, Config destination, ParsingMode parsingMode,
			FileNotFoundAction notFoundAction, Charset charset) {
		try {
			if (Files.notExists(file) && !notFoundAction.run(file, getFormat())) {
				return;
			}
			try (InputStream input = Files.newInputStream(file)) {
				parse(input, destination, parsingMode, charset);
			}
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}

	/**
	 * Parses a configuration.
	 *
	 * @param url the url to parse
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	default C parse(URL url) {
		URLConnection connection;
		try {
			connection = url.openConnection();
		} catch (IOException e) {
			throw new WritingException("Unable to connect to the URL", e);
		}
		String encoding = connection.getContentEncoding();
		Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
		try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
			return parse(reader);
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}

	/**
	 * Parses a configuration.
	 *
	 * @param url         the url to parse
	 * @param destination the config where to put the data
	 * @param parsingMode how to handle conflicts with the entries that already are in the destination
	 * @throws ParsingException if an error occurs
	 */
	default void parse(URL url, Config destination, ParsingMode parsingMode) {
		URLConnection connection;
		try {
			connection = url.openConnection();
		} catch (IOException e) {
			throw new WritingException("Unable to connect to the URL", e);
		}
		String encoding = connection.getContentEncoding();
		Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
		try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
			parse(reader, destination, parsingMode);
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}
}
