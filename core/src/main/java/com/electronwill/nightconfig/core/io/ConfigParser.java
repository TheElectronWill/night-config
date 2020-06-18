package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.MapConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.impl.CharacterInput;
import com.electronwill.nightconfig.core.impl.ReaderInput;
import com.electronwill.nightconfig.core.impl.FastStringReader;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts text data to {@link Config} objects.
 *
 * @author TheElectronWill
 */
public interface ConfigParser {
	// --- BASES ---

	/** @return the format recognized by this parser. */
	ConfigFormat getFormat();

	/**
	 * Parses data and puts the result in an existing configuration.
	 *
	 * @param input data source
	 * @param dst existing config
	 * @param mode how to deal with existing value, see {@link ParsingMode} docs.
	 * @throws ParsingException if an error occurs
	 */
	void parse(CharacterInput input, Config dst, ParsingMode mode);

	/**
	 * Parses data and puts the result in a new configuration.
	 *
	 * @param input data source
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(CharacterInput input) {
		Config cfg = new MapConfig();
		parse(input, cfg, ParsingMode.REPLACE);
		return cfg;
	}

	// --- PARSING TO A NEW CONFIG ---

	/**
	 * Parses data and puts the result in a new configuration.
	 *
	 * @param reader data source
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(Reader reader) {
		return parse(new ReaderInput(reader));
	}

	/**
	 * Parses data and puts the result in a new configuration.
	 *
	 * @param input data source
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(String input) {
		return parse(new FastStringReader(input));
	}

	/**
	 * Parses data and puts the result in a new configuration.
	 *
	 * @param input data source
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(InputStream input, Charset cs) {
		CharacterInput ci = new ReaderInput(new BufferedReader(new InputStreamReader(input, cs)));
		if (cs == StandardCharsets.UTF_8)
			IOUtils.consumeUTF8BOM(ci);
		return parse(ci);
	}

	/**
	 * Parses data and puts the result in a new configuration.
	 *
	 * @param input data source, <b>UTF-8 encoded</b>
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(InputStream input) {
		return parse(input, StandardCharsets.UTF_8);
	}

	/**
	 * Parses data and puts the result in a new configuration.
	 *
	 * @param path data source
	 * @param cs encoding
	 * @param notFoundAction how to deal with missing file
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(Path path, Charset cs, FileNotFoundAction notFoundAction) {
		try {
			if (Files.notExists(path) && !notFoundAction.run(path, getFormat())) {
				return new MapConfig();
			}
			try (InputStream input = Files.newInputStream(path)) {
				return parse(input, cs);
			}
		} catch (IOException e) {
			throw new ParsingException("An I/O error occured", e);
		}
	}

	/**
	 * Parses data and puts the result in a new configuration.
	 *
	 * @param file data source, <b>UTF-8 encoded</b>
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(Path file, FileNotFoundAction notFoundAction) {
		return parse(file, StandardCharsets.UTF_8, notFoundAction);
	}

	/**
	 * Parses data and puts the result in a new configuration.
	 *
	 * @param file data source
	 * @param cs encoding
	 * @param notFoundAction how to deal with missing file
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(File file, Charset cs, FileNotFoundAction notFoundAction) {
		return parse(file.toPath(), cs, notFoundAction);
	}

	/**
	 * Parses data and puts the result in a new configuration.
	 *
	 * @param file data source, <b>UTF-8 encoded</b>
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(File file, FileNotFoundAction notFoundAction) {
		return parse(file, StandardCharsets.UTF_8, notFoundAction);
	}

	/**
	 * Downloads data, parses it and puts the results in a new configuration.
	 * The text encoding is set by a parameter, server's information is ignored.
	 *
	 * @param url data url
	 * @param cs data encoding
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(URL url, Charset cs) {
		return IOUtils.useURL(url, cs, URLConnection::getInputStream, this::parse, ParsingException::new);
	}

	/**
	 * Downloads data, parses it and puts the result in a new configuration.
	 * The text encoding is selected according to the server's information.
	 * If the server doesn't specify an encoding, UTF-8 is used.
	 *
	 * @param url data url
	 * @return a new config
	 * @throws ParsingException if an error occurs
	 */
	default Config parse(URL url) {
		return parse(url, null);
	}

	// --- PARSING TO AN EXISTING CONFIG ---

	/**
	 * Parses data and puts the result in an existing configuration.
	 *
	 * @param reader data source
	 * @param dst existing config
	 * @param mode how to deal with existing entries, see {@link ParsingMode} docs.
	 * @throws ParsingException if an error occurs
	 */
	default void parse(Reader reader, Config dst, ParsingMode mode) {
		parse(new ReaderInput(reader), dst, mode);
	}

	/**
	 * Parses data and puts the result in an existing configuration.
	 *
	 * @param input data source
	 * @param dst existing config
	 * @param mode how to deal with existing entries, see {@link ParsingMode} docs.
	 * @throws ParsingException if an error occurs
	 */
	default void parse(String input, Config dst, ParsingMode mode) {
		parse(new FastStringReader(input), dst, mode);
	}

	/**
	 * Parses data and puts the result in an existing configuration.
	 *
	 * @param input data source
	 * @param cs data encoding
	 * @param dst existing config
	 * @param mode how to deal with existing entries, see {@link ParsingMode} docs.
	 * @throws ParsingException if an error occurs
	 */
	default void parse(InputStream input, Charset cs, Config dst, ParsingMode mode) {
		CharacterInput ci = new ReaderInput(new BufferedReader(new InputStreamReader(input, cs)));
		if (cs == StandardCharsets.UTF_8)
			IOUtils.consumeUTF8BOM(ci);
		parse(ci, dst, mode);
	}

	/**
	 * Parses data and puts the result in an existing configuration.
	 *
	 * @param input data source, <b>UTF-8 encoded</b>
	 * @param dst existing config
	 * @param mode how to deal with existing entries, see {@link ParsingMode} docs.
	 * @throws ParsingException if an error occurs
	 */
	default void parse(InputStream input, Config dst, ParsingMode mode) {
		parse(input, StandardCharsets.UTF_8, dst, mode);
	}

	/**
	 * Parses data and puts the result in an existing configuration.
	 *
	 * @param path data source
	 * @param cs data encoding
	 * @param dst existing config
	 * @param mode how to deal with existing entries, see {@link ParsingMode} docs.
	 * @param notFoundAction how to deal with missing file
	 * @throws ParsingException if an error occurs
	 */
	default void parse(Path path, Charset cs, Config dst, ParsingMode mode, FileNotFoundAction notFoundAction) {
		try {
			if (Files.notExists(path) && !notFoundAction.run(path, getFormat())) {
				return; // nothing to parse
			}
			try (InputStream input = Files.newInputStream(path)) {
				parse(input, cs, dst, mode);
			}
		} catch (IOException e) {
			throw new ParsingException("An I/O error occured", e);
		}
	}

	/**
	 * Parses data and puts the result in an existing configuration.
	 *
	 * @param path data source, <b>UTF-8 encoded</b>
	 * @param dst existing config
	 * @param mode how to deal with existing entries, see {@link ParsingMode} docs.
	 * @param notFoundAction how to deal with missing file
	 * @throws ParsingException if an error occurs
	 */
	default void parse(Path path, Config dst, ParsingMode mode, FileNotFoundAction notFoundAction) {
		parse(path, StandardCharsets.UTF_8, dst, mode, notFoundAction);
	}

	/**
	 * Parses data and puts the result in an existing configuration.
	 *
	 * @param file data source
	 * @param cs file encoding
	 * @param dst existing config
	 * @param mode how to deal with existing entries, see {@link ParsingMode} docs.
	 * @param notFoundAction how to deal with missing file
	 * @throws ParsingException if an error occurs
	 */
	default void parse(File file, Charset cs, Config dst, ParsingMode mode, FileNotFoundAction notFoundAction) {
		parse(file.toPath(), cs, dst, mode, notFoundAction);
	}

	/**
	 * Parses data and puts the result in an existing configuration.
	 *
	 * @param file data source, <b>UTF-8 encoded</b>
	 * @param dst existing config
	 * @param mode how to deal with existing entries, see {@link ParsingMode} docs.
	 * @param notFoundAction how to deal with missing file
	 * @throws ParsingException if an error occurs
	 */
	default void parse(File file, Config dst, ParsingMode mode, FileNotFoundAction notFoundAction) {
		parse(file.toPath(), dst, mode, notFoundAction);
	}

	/**
	 * Downloads data, parses it and puts the results in an existing configuration.
	 * The text encoding is set by a parameter, server's information is ignored.
	 *
	 * @param url data url
	 * @param cs data encoding
	 * @param dst existing config
	 * @param mode how to deal with existing entries, see {@link ParsingMode} docs.
	 * @throws ParsingException if an error occurs
	 */
	default void parse(URL url, Charset cs, Config dst, ParsingMode mode) {
		IOUtils.useURL(url, cs, URLConnection::getInputStream, (inputStream, charset) -> {
			parse(inputStream, charset, dst, mode);
			return null;
		}, ParsingException::new);
	}

	/**
	 * Downloads data, parses it and puts the result in an existing configuration.
	 * The text encoding is selected according to the server's information.
	 * If the server doesn't specify an encoding, UTF-8 is used.
	 *
	 * @param url data url
	 * @param dst existing config
	 * @param mode how to deal with existing entries, see {@link ParsingMode} docs.
	 * @throws ParsingException if an error occurs
	 */
	default void parse(URL url, Config dst, ParsingMode mode) {
		parse(url, null, dst, mode);
	}
}
