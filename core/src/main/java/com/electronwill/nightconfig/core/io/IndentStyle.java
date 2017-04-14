package com.electronwill.nightconfig.core.io;

/**
 * A style of indent.
 *
 * @author TheElectronWill
 */
public enum IndentStyle {
	/**
	 * 1 indent = 1 tab character \t
	 */
	TABS('\t'),
	/**
	 * 1 indent = 2 spaces
	 */
	SPACES_2(' ', ' '),
	SPACES_4(' ', ' ', ' ', ' '),
	SPACES_8(' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ');

	public final char[] chars;

	IndentStyle(char... chars) {
		this.chars = chars;
	}
}
