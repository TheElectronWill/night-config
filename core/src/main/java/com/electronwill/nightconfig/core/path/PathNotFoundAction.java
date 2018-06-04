package com.electronwill.nightconfig.core.path;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Defines the action to perform when the file is not found.
 *
 * @author TheElectronWill
 */
@FunctionalInterface
public interface PathNotFoundAction {
	/**
	 * Performs the action.
	 *
	 * @return {@code true} to parse the path, {@code false} to stop after the action's execution
	 * (thus making the config empty)
	 *
	 * @throws IOException if an IO error occurs
	 */
	boolean run(Path path) throws IOException;

	// --- Static members ---

	PathNotFoundAction CREATE_EMPTY = path -> {
		Files.createFile(path);
		return false;
	};
	PathNotFoundAction READ_NOTHING = f -> false;
	PathNotFoundAction THROW_ERROR = f -> {
		throw new NoSuchFileException(f.toString());
	};

	/**
	 * Action: copies the data at the given url.
	 *
	 * @param url the data url
	 * @return a PathNotFoundAction that copies the url's data if the file is not found
	 */
	static PathNotFoundAction copyData(URL url) {
		return f -> {
			Files.copy(url.openStream(), f);
			return true;
		};
	}

	/**
	 * Action: copies the specified file.
	 *
	 * @param file the data url
	 * @return a PathNotFoundAction that copies the file's data if the file is not found
	 */
	static PathNotFoundAction copyData(Path file) {
		// copyResource(new FIS(file)) isn't used here to avoid dealing with the exception
		// declared by the FIS constructor
		return f -> {
			Files.copy(file, f);
			return true;
		};
	}

	/**
	 * Action: copies the stream's data.
	 *
	 * @param data the stream containing the data
	 * @return a PathNotFoundAction that copies the stream's data if the file is not found
	 */
	static PathNotFoundAction copyData(InputStream data) {
		return f -> {
			Files.copy(data, f);
			return true;
		};
	}

	/**
	 * Action: copies the inner resource.
	 *
	 * @param resourcePath the resource's path
	 * @return a PathNotFoundAction that copies the url's data if the file is not found
	 *
	 * @see Class#getResource(String)
	 */
	static PathNotFoundAction copyResource(String resourcePath) {
		return copyData(PathNotFoundAction.class.getResource(resourcePath));
	}
}