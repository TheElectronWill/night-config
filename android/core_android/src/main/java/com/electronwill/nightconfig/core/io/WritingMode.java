package com.electronwill.nightconfig.core.io;

/**
 * @author TheElectronWill
 */
public enum WritingMode {
	/**
	 * Replaces the existing file.
	 */
	REPLACE,

	/**
	 * Appends the config at the end of the file.
	 */
	APPEND;
}