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
	 * @throws ParsingException if an error occurs
	 */
	default void parse(String input, Config destination, ParsingMode parsingMode) {
		parse(new StringReader(input), destination, parsingMode);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param input the input to parse
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	default C parse(InputStream input) {
		Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		return parse(reader);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param input       the input to parse
	 * @param destination the config where to put the data
	 * @throws ParsingException if an error occurs
	 */
	default void parse(InputStream input, Config destination, ParsingMode parsingMode) {
		Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		parse(reader, destination, parsingMode);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file the file to parse
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	default C parse(File file, FileNotFoundAction nefAction, Charset charset) {
		try {
			if (!file.exists() && !nefAction.run(file)) {
				return getFormat().createConfig();
			}
			try (Reader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), charset))) {
				return parse(reader);
			}
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}

	/**
	 * Parses a configuration with the UTF-8 charset.
	 *
	 * @param file the file to parse
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	default C parse(File file, FileNotFoundAction nefAction) {
		return parse(file, nefAction, StandardCharsets.UTF_8);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file        the file to parse
	 * @param destination the config where to put the data
	 * @throws ParsingException if an error occurs
	 */
	default void parse(File file, Config destination, ParsingMode parsingMode,
					   FileNotFoundAction nefAction, Charset charset) {
		try {
			if (!file.exists() && !nefAction.run(file)) {
				return;
			}
			try (Reader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), charset))) {
				parse(reader, destination, parsingMode);
			}
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}

	/**
	 * Parses a configuration with the UTF-8 charset.
	 *
	 * @param file        the file to parse
	 * @param destination the config where to put the data
	 * @throws ParsingException if an error occurs
	 */
	default void parse(File file, Config destination, ParsingMode parsingMode,
					   FileNotFoundAction nefAction) {
		parse(file, destination, parsingMode, nefAction, StandardCharsets.UTF_8);
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