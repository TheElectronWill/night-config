package com.electronwill.nightconfig.core.serialization;

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
	 * Writes this config to a file.
	 *
	 * @param file the file to write to
	 * @throws IOException if an I/O error occurs
	 */
	void writeTo(File file) throws IOException;

	/**
	 * Reads this config from a file.
	 *
	 * @param file the file to read
	 * @throws IOException if an I/O error occurs
	 */
	void readFrom(File file) throws IOException;
}
