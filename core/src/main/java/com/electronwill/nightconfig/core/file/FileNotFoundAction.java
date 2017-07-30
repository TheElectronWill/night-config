package com.electronwill.nightconfig.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/**
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
	boolean run(File file) throws IOException;

	// --- Static members ---

	FileNotFoundAction CREATE_EMPTY = f -> {
		f.createNewFile();
		return false;
	};
	FileNotFoundAction READ_NOTHING = f -> false;
	FileNotFoundAction THROW_ERROR = f -> {
		throw new NoSuchFileException(f.getAbsolutePath());
	};

	static FileNotFoundAction copyData(URL url) {
		return f -> {
			Files.copy(url.openStream(), f.toPath());
			return true;
		};
	}

	static FileNotFoundAction copyData(File file) {
		// copyResource(new FIS(file)) isn't used here to avoid dealing with the exception
		// declared by the FIS constructor
		return f -> {
			Files.copy(new FileInputStream(file), f.toPath());
			return true;
		};
	}

	static FileNotFoundAction copyData(InputStream data) {
		return f -> {
			Files.copy(data, f.toPath());
			return true;
		};
	}

	static FileNotFoundAction copyResource(String resourcePath) {
		return copyData(
				FileNotFoundAction.class.getClassLoader().getResourceAsStream(resourcePath));
	}
}