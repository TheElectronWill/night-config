package com.electronwill.nightconfig.core.io;

/**
 * A style of newline.
 *
 * @author TheElectronWill
 */
public enum NewlineStyle {
	/**
	 * LF character '\n'
	 */
	UNIX('\n'),
	/**
	 * CRLF sequence "\r\n"
	 */
	WINDOWS('\r', '\n');

	public final char[] chars;

	NewlineStyle(char... chars) {
		this.chars = chars;
	}

	/**
	 * @return the system's newline
	 */
	public static NewlineStyle system() {
		String systemNewline = System.getProperty("line.separator");
		if (systemNewline.equals("\n")) {
			return UNIX;
		} else if (systemNewline.equals("\r\n")) {
			return WINDOWS;
		} else {
			throw new IllegalArgumentException("Unknown system line separator '" + systemNewline + "'. The NewlineStyle enum only supports LF and CRLF.");
		}
	}
}
