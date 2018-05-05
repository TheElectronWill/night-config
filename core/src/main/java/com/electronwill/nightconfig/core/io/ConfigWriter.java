package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
	 * @param writer the writer to write it to
	 * @throws WritingException if an error occurs
	 */
	void write(UnmodifiableConfig config, Writer writer);

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param output the output to write it to
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, OutputStream output, Charset charset) {
		Writer writer = new BufferedWriter(new OutputStreamWriter(output, charset));
		write(config, writer);
	}

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param output the output to write it to
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, OutputStream output) {
		write(config, output, StandardCharsets.UTF_8);
	}

	/**
	 * Writes a configuration. The content of the file is overwritten. This method is equivalent to
	 * <pre>write(config, file, false)</pre>
	 *
	 * @param config the config to write
	 * @param file   the file to write it to
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, File file, WritingMode writingMode) {
		write(config, file, writingMode, StandardCharsets.UTF_8);
	}

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param file   the file to write it to
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, File file, WritingMode writingMode, Charset charset) {
		boolean append = (writingMode == WritingMode.APPEND);
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file, append), charset))) {
			write(config, writer);
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param url    the url to write it to
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, URL url) {
		URLConnection connection;
		try {
			connection = url.openConnection();
		} catch (IOException e) {
			throw new WritingException("Unable to connect to the URL", e);
		}
		String encoding = connection.getContentEncoding();
		Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(connection.getOutputStream(), charset))) {
			write(config, writer);
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
	}

	/**
	 * Writes a configuration to a String.
	 *
	 * @param config the config to write
	 * @return a new String
	 *
	 * @throws WritingException if an error occurs
	 */
	default String writeToString(UnmodifiableConfig config) {
		CharsWrapper.Builder builder = new CharsWrapper.Builder(64);
		write(config, builder);
		return builder.toString();
	}
}