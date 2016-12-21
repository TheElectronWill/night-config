package com.electronwill.nightconfig.core.serialization;

/**
 * @author TheElectronWill
 */
public interface CharacterOutput {
	void write(char c);

	void write(char[] chars);

	void write(String s);
}
