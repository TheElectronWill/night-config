package com.electronwill.nightconfig.core.io;

import static java.nio.file.StandardOpenOption.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

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
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(output, charset))) {
			write(config, writer);
		} catch (IOException e) {
			throw new WritingException("An I/O error occured", e);
		}
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
	 * 
	 * <pre>
	 * write(config, file, false)
	 * </pre>
	 *
	 * @param config the config to write
	 * @param file   the nio Path to write it to
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, Path file, WritingMode writingMode) {
		write(config, file, writingMode, StandardCharsets.UTF_8);
	}

	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param file   the nio Path to write it to
	 * @throws WritingException if an error occurs
	 */
	default void write(UnmodifiableConfig config, Path file, WritingMode writingMode, Charset charset) {
		if (writingMode == WritingMode.REPLACE_ATOMIC) {
			// write to another file, then atomically move it
			Path tmp = file.resolveSibling(file.getFileName().toString() + ".new.tmp");
			try (OutputStream output = Files.newOutputStream(tmp, WRITE, CREATE, TRUNCATE_EXISTING)) {
				write(config, output, charset);
				Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				// can fail in some conditions (OS and filesystem-dependent)
				String msg = String.format(
						"Failed to atomically move the config from '%s' to '%s': WritingMode.REPLACE_ATOMIC is not supported for this path, use WritingMode.REPLACE instead.\n%s",
						tmp.toString(), file.toString(),
						"Note: you may see *.new.tmp files after this error, they contain the \"new version\" of your configurations and can be safely removed."
								+ "If you want, you can manually copy their content into your regular configuration files (replacing the old config).");
				throw new WritingException(msg, e);
			} catch (IOException e) {
				// regular IO exception
				String msg = String.format("Failed to write (%s) the config to: %s",
						writingMode.toString(), file.toString());
				throw new WritingException(msg, e);
			}
		} else {
			// write to the file directly
			StandardOpenOption lastOption = (writingMode == WritingMode.APPEND) ? APPEND : TRUNCATE_EXISTING;
			try (OutputStream output = Files.newOutputStream(file, WRITE, CREATE, lastOption)) {
				write(config, output, charset);
			} catch (IOException e) {
				String msg = String.format("Failed to write (%s) the config to: %s",
						writingMode.toString(), file.toString());
				throw new WritingException(msg, e);
			}
		}
	}

	/**
	 * Writes a configuration. The content of the file is overwritten. This method is equivalent to
	 * 
	 * <pre>
	 * write(config, file, false)
	 * </pre>
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
	default void write(UnmodifiableConfig config, File file, WritingMode writingMode,
			Charset charset) {
		write(config, file.toPath(), writingMode, charset);
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
		try (OutputStream output = connection.getOutputStream()) {
			write(config, output, charset);
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