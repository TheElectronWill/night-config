package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.impl.CharacterInput;
import com.electronwill.nightconfig.core.utils.ExFunction;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.function.BiFunction;

final class IOUtils {
	private IOUtils() {}

	static void consumeUTF8BOM(CharacterInput input) {
		int read = input.read();
		if (read != -1 && read != '\uFEFF')
			input.pushBack((char)read);
	}

	static <R, S extends Closeable> R useURL(
			URL url,
			Charset cs,
			ExFunction<URLConnection, S, IOException> streamProvider,
			BiFunction<S, Charset, R> f,
			BiFunction<String, Throwable, RuntimeException> error) {

		// Connects to the URL
		URLConnection connection;
		try {
			connection = url.openConnection();
		} catch (IOException e) {
			throw error.apply("Unable to connect to the URL", e);
		}

		// Detects the encoding if it's not already set
		Charset charset = cs;
		if (charset == null) {
			String encoding = connection.getContentEncoding();
			try {
				charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
			} catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
				throw error.apply("Invalid or unsupported encoding: " + encoding, e);
			}
		}

		// Opens a stream and applies the function on it
		try (S stream = streamProvider.apply(connection)) {
			return f.apply(stream, charset);
		} catch (IOException e) {
			throw error.apply("An I/O error occured", e);
		}
	}
}
