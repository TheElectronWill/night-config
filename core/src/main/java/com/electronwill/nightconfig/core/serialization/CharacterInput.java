package com.electronwill.nightconfig.core.serialization;

/**
 * Interface for sources of characters.
 * <p>
 * The readXXX() and seek() methods do not throw any exception when the end of the available data
 * is reached, but return special values.
 * <p>
 * The readCharXXX() and seekChar() methods do throw a RuntimeException when the end of the available
 * data is reached.
 * </p>
 *
 * @author TheElectronWill
 */
public interface CharacterInput {
	/**
	 * Reads the next character.
	 *
	 * @return the next char, or -1 if there is no more available data
	 */
	int read();

	/**
	 * Reads the next characters, skipping some characters. Returns the next character that is not in the
	 * given array.
	 *
	 * @param toSkip the characters to skip
	 * @return the next character that is not in {@code toSkip}, or -1 if there is no more available data
	 */
	default int readAndSkip(char[] toSkip) {
		int c;
		do {
			c = read();
		} while (c != -1 && Utils.arrayContains(toSkip, (char)c));
		return c;
	}

	/**
	 * Reads the next n characters. If there isn't n available characters, this method returns -1.
	 *
	 * @param n the number of characters to read
	 * @return an array containing the next n characters, or null if there is no more available data
	 */
	char[] read(int n);

	/**
	 * Returns the next character, without moving the reading position forward. After a call to
	 * {@code seek()}, the method {@link #read()} will return the exact same character.
	 *
	 * @return the next character, or -1 if there is no more available data
	 */
	int seek();

	/**
	 * Reads the next character, throwing an exception if there is no more available data.
	 *
	 * @return the next character
	 * @throws ParsingException if there is no more available data
	 */
	char readChar();

	/**
	 * Reads the next characters, skipping some characters. Returns the next character that is not in the
	 * given array. This method throws an exception if there is no more available data.
	 *
	 * @param toSkip the characters to skip
	 * @return the next character that is not in {@code toSkip}, or -1 if there is no more available data
	 * @throws ParsingException if there is no more available data
	 */
	default char readCharAndSkip(char[] toSkip) {
		char c;
		do {
			c = readChar();
		} while (Utils.arrayContains(toSkip, c));
		return c;
	}

	/**
	 * Reads the next n characters. If there isn't n available characters, this method throws an exception.
	 *
	 * @param n the number of characters to read
	 * @return an array containing the next n characters, not null
	 * @throws ParsingException if there is no more available data
	 */
	char[] readChars(int n);

	/**
	 * Reads all the characters until a character contained in {@code stop} is reached, and returns the
	 * {@link CharsWrapper} that contains all the characters before the stop.
	 *
	 * @param stop the characters to stop at
	 * @return a CharsWrapper that contains all the characters read before the stop
	 * @throws ParsingException if there is no more available data
	 */
	CharsWrapper readCharUntil(char[] stop);

	/**
	 * Returns the next character, without moving the reading position forward. After a call to
	 * {@code seek()}, the method {@link #read()} will return the exact same character.
	 * This method throws an exception if there is no more available data.
	 *
	 * @return the next character
	 * @throws ParsingException if there is no more available data
	 */
	char seekChar();
}
