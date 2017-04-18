package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Interface for writing configurations.
 *
 * @author TheElectronWill
 */
public interface ConfigWriter<T extends UnmodifiableConfig> {
	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param writer the writer to write it to
	 * @throws WritingException if an error occurs
	 */
	void write(T config, Writer writer);

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param output the output to write it to
	 * @throws WritingException if an error occurs
	 */
	default void write(T config, OutputStream output) {
		Writer writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
		write(config, writer);
	}

	/**
	 * Writes a configuration. The content of the file is overwritten. This method is equivalent to
	 * <pre>write(config, file, false)</pre>
	 *
	 * @param config the config to write
	 * @param file   the file to write it to
	 * @throws WritingException if an error occurs
	 */
	default void write(T config, File file) {
		write(config, file, false);
	}

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param file   the file to write it to
	 * @param append {@code true} to write to the end of the file, {@code false} to
	 *               write to the beginning (which overwrites the file)
	 * @throws WritingException if an error occurs
	 */
	default void write(T config, File file, boolean append) {
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file, append),
									   StandardCharsets.UTF_8))) {
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
	default void write(T config, URL url) {
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
	default String writeToString(T config) {
		CharsWrapper.Builder builder = new CharsWrapper.Builder(64);
		write(config, builder);
		return builder.toString();
	}
}