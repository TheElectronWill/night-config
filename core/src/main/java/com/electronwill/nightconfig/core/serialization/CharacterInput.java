package com.electronwill.nightconfig.core.serialization;

import java.util.Arrays;

/**
 * Interface for sources of characters.
 * <p>
 * The readXXX() and peek() methods do not throw any exception when the end of the available data
 * is reached, but return special non-null values.
 * <p>
 * The readCharXXX() and peekChar() methods do throw a RuntimeException when the end of the available
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
	 * Reads the next character, throwing an exception if there is no more available data.
	 *
	 * @return the next character
	 * @throws ParsingException if there is no more available data
	 */
	char readChar();

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
	 * Reads the next characters, skipping some characters. Returns the next character that is not in the
	 * given array. This method throws an exception if there is no more available data.
	 *
	 * @param toSkip the characters to skip
	 * @return the next character that is not in {@code toSkip}
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
	 * Reads the next n characters, if possible. If there are less than n available characters, return all
	 * the remaining characters.
	 *
	 * @param n the number of characters to read
	 * @return an array containing at most n characters, not null
	 */
	default char[] read(int n) {
		char[] chars = new char[n];
		for (int i = 0; i < n; i++) {
			int next = read();
			if (next == -1)//EOS
				return Arrays.copyOf(chars, i);//return a smaller array
			chars[i] = (char)next;
		}
		return chars;
	}

	/**
	 * Reads the next n characters. If there isn't n available characters, this method throws an exception.
	 *
	 * @param n the number of characters to read
	 * @return an array containing the next n characters, not null
	 * @throws ParsingException if there is no more available data
	 */
	default char[] readChars(int n) {
		char[] chars = new char[n];
		for (int i = 0; i < n; i++) {
			int next = read();
			if (next == -1)//EOS
				throw new ParsingException("Not enough data available.");
			chars[i] = (char)next;
		}
		return chars;
	}

	/**
	 * Reads all the character until a character containde in {@code stop} is reached or there is no more
	 * available data, and returns the {@link CharsWrapper} that contains all the characters before the
	 * stop (or the end of the data).
	 *
	 * @param stop the characters to stop at
	 * @return a CharsWrapper that contains all the characters read before the stop (or the end of the
	 * data), not null
	 */
	CharsWrapper readUntil(char[] stop);

	/**
	 * Reads all the characters until a character contained in {@code stop} is reached, and returns the
	 * {@link CharsWrapper} that contains all the characters before the stop.
	 *
	 * @param stop the characters to stop at
	 * @return a CharsWrapper that contains all the characters read before the stop
	 * @throws ParsingException if the end of the data is reached before a stop character
	 */
	CharsWrapper readCharsUntil(char[] stop);

	/**
	 * Returns the next character, without moving the reading position forward. After a call to
	 * {@code peek()}, the method {@link #read()} will return the exact same character.
	 * <p>
	 * This method behaves exactly like {@code peek(0)}
	 * </p>
	 *
	 * @return the next character, or -1 if there is no more available data
	 */
	int peek();

	/**
	 * Returns the next (n+1)th character, without moving the reading position forward.
	 * The next character is n=0, then it's n=1 and so on.
	 *
	 * @param n the position to peek
	 * @return the next (n+1)th character
	 * @throws ParsingException if there is no (n+1)th character
	 */
	int peek(int n);

	/**
	 * Returns the next character, without moving the reading position forward. After a call to
	 * {@code peek()}, the method {@link #read()} will return the exact same character.
	 * <p>
	 * This method behaves exactly like {@code peekChar(0)}
	 * </p><p>
	 * This method throws an exception if there is no more available data.
	 * </p>
	 *
	 * @return the next character
	 * @throws ParsingException if there is no more available data
	 */
	char peekChar();

	/**
	 * Returns the next (n+1)th character, without moving the reading position forward.
	 * The next character is n=0, then it's n=1 and so on.
	 * <p>
	 * This method throws an exception if there is no more available data.
	 * </p>
	 *
	 * @param n the position to peek
	 * @return the next (n+1)th character
	 * @throws ParsingException if there is no (n+1)th character
	 */
	char peekChar(int n);

	/**
	 * Skips all the character that have been peeked and not read yead.
	 */
	void skipPeeks();
}
