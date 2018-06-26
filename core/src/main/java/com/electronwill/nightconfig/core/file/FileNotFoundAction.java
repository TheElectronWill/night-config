package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.ConfigFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/**
 * Defines the action to perform when the file is not found.
 *
 * @author TheElectronWill
 */
@FunctionalInterface
public interface FileNotFoundAction {
	/**
	 * Performs the action.
	 *
	 * @return {@code true} to parse the file, {@code false} to stop after the action's execution
	 * (thus making the config empty)
	 *
	 * @throws IOException if an IO error occurs
	 */
	boolean run(File file, ConfigFormat<?> configFormat) throws IOException;

	// --- Static members ---

	FileNotFoundAction CREATE_EMPTY = (f,c) -> {
		f.createNewFile();
		c.initEmptyFile(f);
		return false;
	};
	FileNotFoundAction READ_NOTHING = (f,c) -> false;
	FileNotFoundAction THROW_ERROR = (f,c) -> {
		throw new NoSuchFileException(f.getAbsolutePath());
	};

	/**
	 * Action: copies the data at the given url.
	 *
	 * @param url the data url
	 * @return a FileNotFoundAction that copies the url's data if the file is not found
	 */
	static FileNotFoundAction copyData(URL url) {
		return (f,c) -> {
			Files.copy(url.openStream(), f.toPath());
			return true;
		};
	}

	/**
	 * Action: copies the specified file.
	 *
	 * @param file the data url
	 * @return a FileNotFoundAction that copies the file's data if the file is not found
	 */
	static FileNotFoundAction copyData(File file) {
		// copyResource(new FIS(file)) isn't used here to avoid dealing with the exception
		// declared by the FIS constructor
		return (f,c) -> {
			Files.copy(new FileInputStream(file), f.toPath());
			return true;
		};
	}

	/**
	 * Action: copies the stream's data.
	 *
	 * @param data the stream containing the data
	 * @return a FileNotFoundAction that copies the stream's data if the file is not found
	 */
	static FileNotFoundAction copyData(InputStream data) {
		return (f,c) -> {
			Files.copy(data, f.toPath());
			return true;
		};
	}

	/**
	 * Action: copies the inner resource.
	 *
	 * @param resourcePath the resource's path
	 * @return a FileNotFoundAction that copies the url's data if the file is not found
	 *
	 * @see Class#getResource(String)
	 */
	static FileNotFoundAction copyResource(String resourcePath) {
		return copyData(FileNotFoundAction.class.getResource(resourcePath));
	}
}