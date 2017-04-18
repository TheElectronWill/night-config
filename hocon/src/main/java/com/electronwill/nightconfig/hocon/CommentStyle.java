package com.electronwill.nightconfig.hocon;

/**
 * A style of HOCON comment.
 *
 * @author TheElectronWill
 */
public enum CommentStyle {
	/**
	 * # prefix
	 */
	HASH('#'),
	/**
	 * // prefix
	 */
	SLASH('/', '/');

	public final char[] chars;

	CommentStyle(char... chars) {
		this.chars = chars;
	}
}
