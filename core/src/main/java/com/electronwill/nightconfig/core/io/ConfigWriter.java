package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
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
public interface ConfigWriter<T extends Config> {
	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param writer the writer to write it to
	 */
	void writeConfig(T config, Writer writer) throws IOException;

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param output the output to write it to
	 */
	default void writeConfig(T config, OutputStream output) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
		writeConfig(config, writer);
	}

	/**
	 * Writes a configuration. The content of the file is overwritten. This method is equivalent to
	 * <pre>writeConfig(config, file, false)</pre>
	 *
	 * @param config the config to write
	 * @param file   the file to write it to
	 */
	default void writeConfig(T config, File file) throws IOException {
		writeConfig(config, file, false);
	}

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param file   the file to write it to
	 */
	default void writeConfig(T config, File file, boolean append) throws IOException {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(file, append), StandardCharsets.UTF_8))) {
			writeConfig(config, writer);
		}
	}

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param url    the url to write it to
	 */
	default void writeConfig(T config, URL url) throws IOException {
		URLConnection connection = url.openConnection();
		String encoding = connection.getContentEncoding();
		Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
			connection.getOutputStream(), charset))) {
			writeConfig(config, writer);
		}
	}
}
