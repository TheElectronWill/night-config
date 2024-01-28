package com.electronwill.nightconfig.core.io;

/**
 * How to write a config to a file.
 * 
 * @author TheElectronWill
 */
public enum WritingMode {
	/**
	 * Replaces the existing file.
	 * If the file does not exist, create it.
	 */
	REPLACE,

	/**
	 * Writes to a temporary file and, when finished, atomically replaces the existing file by the temporary one.
	 * This mode ensures that the existing file is not partially erased, which would corrupt it.
	 * <p>
	 * If the file does not exist, create it.
	 */
	REPLACE_ATOMIC,

	/**
	 * Appends the config to the end of the file, if it exists.
	 * If the file does not exist, create it.
	 */
	APPEND;
}