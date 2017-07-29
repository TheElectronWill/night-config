package com.electronwill.nightconfig.core.io;

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
public interface FileNotFoundAction {
	FileNotFoundAction CREATE_EMPTY = f -> {
		f.createNewFile();
		return false;
	};
	FileNotFoundAction READ_NOTHING = f -> false;
	FileNotFoundAction THROW_ERROR = f -> {
		throw new NoSuchFileException(f.getAbsolutePath());
	};

	static FileNotFoundAction copyResource(URL resource) {
		return f -> {
			Files.copy(resource.openStream(), f.toPath());
			return true;
		};
	}

	static FileNotFoundAction copyResource(File resource) {
		// copyResource(new FIS(resource)) isn't used here to avoid dealing with the exception
		// declared by the FIS constructor
		return f -> {
			Files.copy(new FileInputStream(resource), f.toPath());
			return true;
		};
	}

	static FileNotFoundAction copyResource(InputStream resource) {
		return f -> {
			Files.copy(resource, f.toPath());
			return true;
		};
	}

	static FileNotFoundAction copyInnerResource(String resource) {
		return copyResource(
				FileNotFoundAction.class.getClassLoader().getResourceAsStream(resource));
	}

	/**
	 * Performs the action.
	 *
	 * @return {@code true} to parse the file, {@code false} to stop after the action's execution
	 * (thus making the config empty)
	 *
	 * @throws IOException if an IO error occurs
	 */
	boolean run(File file) throws IOException;
}