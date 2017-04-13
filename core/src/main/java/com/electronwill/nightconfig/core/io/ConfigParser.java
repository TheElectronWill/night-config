package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.FastStringReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Interface for reading configurations.
 *
 * @param <C> the type of config created by the parser
 * @param <D> the type of config that can be populated by the parser
 * @author TheElectronWill
 */
public interface ConfigParser<C extends D, D extends Config> {
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
	void parse(Reader reader, D destination);

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
	default void parse(String input, D destination) {
		parse(new StringReader(input), destination);
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
	default void parse(InputStream input, D destination) {
		Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		parse(reader, destination);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file the file to parse
	 * @return a Config
	 *
	 * @throws ParsingException if an error occurs
	 */
	default C parse(File file) {
		try (Reader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			return parse(reader);
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file        the file to parse
	 * @param destination the config where to put the data
	 * @throws ParsingException if an error occurs
	 */
	default void parse(File file, D destination) {
		try (Reader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			parse(reader, destination);
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
	 * @throws ParsingException if an error occurs
	 */
	default void parse(URL url, D destination) {
		URLConnection connection;
		try {
			connection = url.openConnection();
		} catch (IOException e) {
			throw new WritingException("Unable to connect to the URL", e);
		}
		String encoding = connection.getContentEncoding();
		Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
		try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
			parse(reader, destination);
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}
}