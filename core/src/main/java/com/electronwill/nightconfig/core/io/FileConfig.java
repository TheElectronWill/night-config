package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import java.io.File;

/**
 * Interface for configurations that can be parsed from files and written to files.
 *
 * @author TheElectronWill
 */
public interface FileConfig extends Config {

	/**
	 * Writes the config to a file. The content of the file is overwritten. This method is
	 * equivalent to <pre>write(file, false)</pre>
	 *
	 * @param file the file to write to
	 * @throws WritingException if an I/O error occurs
	 */
	default void write(File file) {
		write(file, false);
	}

	/**
	 * Writes the config to a file.
	 *
	 * @param file   the file to write to
	 * @param append {@code true} to append the data to the file, {@code false} to overwrite the file.
	 * @throws WritingException if an I/O error occurs
	 */
	void write(File file, boolean append);

	/**
	 * Parses the config from a file. The content of the config is replaced by the parse one. This
	 * method is equivalent to <pre>parse(file, false)</pre>
	 *
	 * @param file the file to parse
	 * @throws ParsingException if an I/O error occurs
	 */
	default void parse(File file) {
		parse(file, false);
	}

	/**
	 * Parses the config from a file.
	 *
	 * @param file  the file to parse
	 * @param merge {@code true} to merge the current data with the parse one, {@code false} to
	 *              replace the current data with the parse one.
	 * @throws ParsingException if an I/O error occurs
	 */
	void parse(File file, boolean merge);
}