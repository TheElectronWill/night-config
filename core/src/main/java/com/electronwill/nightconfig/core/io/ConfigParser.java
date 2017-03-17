package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Interface for reading configurations.
 *
 * @author TheElectronWill
 */
public interface ConfigParser<T extends Config> {
	/**
	 * Parses a configuration.
	 *
	 * @param reader the reader to read the data from
	 * @return a config containing the read data
	 */
	T parseConfig(Reader reader);

	/**
	 * Parses a configuration.
	 *
	 * @param reader      the reader to read the data from
	 * @param destination the config to put the data to
	 */
	void parseConfig(Reader reader, T destination);

	/**
	 * Parses a configuration.
	 *
	 * @param input the input to read the data from
	 * @return a config containing the read data
	 */
	default T parseConfig(InputStream input) throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		return parseConfig(reader);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param input       the input to read the data from
	 * @param destination the config to put the data to
	 */
	default void parseConfig(InputStream input, T destination) throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		parseConfig(reader, destination);
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file the file to read the data from
	 * @return a config containing the read data
	 */
	default T parseConfig(File file) throws IOException {
		try (Reader reader = new BufferedReader(new InputStreamReader(
			new FileInputStream(file), StandardCharsets.UTF_8))) {
			return parseConfig(reader);
		}
	}

	/**
	 * Parses a configuration.
	 *
	 * @param file        the file to read the data from
	 * @param destination the config to put the data to
	 */
	default void parseConfig(File file, T destination) throws IOException {
		try (Reader reader = new BufferedReader(new InputStreamReader(
			new FileInputStream(file), StandardCharsets.UTF_8))) {
			parseConfig(reader, destination);
		}
	}

	/**
	 * Parses a configuration.
	 *
	 * @param url the url to read the data from
	 * @return a config containing the read data
	 */
	default T parseConfig(URL url) throws IOException {
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
	 * @param url         the url to read the data from
	 * @param destination the config to put the data to
	 */
	default void parseConfig(URL url, T destination) throws IOException {
		URLConnection connection = url.openConnection();
		String encoding = connection.getContentEncoding();
		Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
		try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
			parseConfig(reader, destination);
		}
	}
}
