package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
	 */
	C parseConfig(Reader reader);

	/**
	 * Parses a configuration.
	 *
	 * @param reader      the reader to parse
	 * @param destination the config where to put the data
	 */
	void parseConfig(Reader reader, D destination);

	/**
	 * Parses a configuration.
	 *
	 * @param input the input to parse
	 * @return a Config
	 */
	default C parseConfig(InputStream input) throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		return parseConfig(reader);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param input       the input to parse
	 * @param destination the config where to put the data
	 */
	default void parseConfig(InputStream input, D destination) throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		parseConfig(reader, destination);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file the file to parse
	 * @return a Config
	 */
	default C parseConfig(File file) throws IOException {
		try (Reader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			return parseConfig(reader);
		}
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file        the file to parse
	 * @param destination the config where to put the data
	 */
	default void parseConfig(File file, D destination) throws IOException {
		try (Reader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			parseConfig(reader, destination);
		}
	}

	/**
	 * Parses a configuration.
	 *
	 * @param url the url to parse
	 * @return a Config
	 */
	default C parseConfig(URL url) throws IOException {
		URLConnection connection = url.openConnection();
		String encoding = connection.getContentEncoding();
		Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
		try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
			return parseConfig(reader);
		}
	}

	/**
	 * Parses a configuration.
	 *
	 * @param url         the url to parse
	 * @param destination the config where to put the data
	 */
	default void parseConfig(URL url, D destination) throws IOException {
		URLConnection connection = url.openConnection();
		String encoding = connection.getContentEncoding();
		Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
		try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
			parseConfig(reader, destination);
		}
	}
}