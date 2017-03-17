package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import java.io.File;
import java.io.IOException;

/**
 * Interface for configurations that can be read from files and written to files.
 *
 * @author TheElectronWill
 */
public interface FileConfig extends Config {

	/**
	 * Writes the config to a file. The content of the file is overwritten. This method is equivalent to
	 * <pre>writeTo(file, false)</pre>
	 *
	 * @param file the file to write to
	 * @throws IOException if an I/O error occurs
	 */
	default void writeTo(File file) throws IOException {
		writeTo(file, false);
	}

	/**
	 * Writes the config to a file.
	 *
	 * @param file   the file to write to
	 * @param append {@code true} to append the data to the file, {@code false} to overwrite
	 *               the file.
	 * @throws IOException if an I/O error occurs
	 */
	void writeTo(File file, boolean append) throws IOException;

	/**
	 * Reads the config from a file. The content of the config is replaced by the read one. This method is
	 * equivalent to <pre>readFrom(file, false)</pre>
	 *
	 * @param file the file to read
	 * @throws IOException if an I/O error occurs
	 */
	default void readFrom(File file) throws IOException {
		readFrom(file, false);
	}

	/**
	 * Reads the config from a file.
	 *
	 * @param file  the file to read
	 * @param merge {@code true} to merge the current data with the read one, {@code false} to replace the
	 *              current data with the read one.
	 * @throws IOException if an I/O error occurs
	 */
	void readFrom(File file, boolean merge) throws IOException;
}
