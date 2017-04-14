package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.io.CharacterOutput;

/**
 * @author TheElectronWill
 */
public enum KeyValueSeparatorStyle {
	/**
	 * The : character.
	 */
	COLON(':', ' '),
	/**
	 * The = character.
	 */
	EQUALS(' ', '=', ' ');

	public final char[] chars;

	KeyValueSeparatorStyle(char... chars) {
		this.chars = chars;
	}
}
