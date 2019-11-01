package com.electronwill.nightconfig.core.impl;

import com.electronwill.nightconfig.core.io.ParsingException;

/**
 * Interface for sources of characters.
 *
 * @author TheElectronWill
 */
public interface CharacterInput {
	/**
	 * Gets the number of the line of the last character read. Peeks don't count.
	 * The numbers start at one.
	 *
	 * @return the current line.
	 */
	int line();

	/**
	 * Gets the number of the column of the last character read. Peeks don't count.
	 * The numbers start at one.
	 *
	 * @return the current column.
	 */
	int column();

	/**
	 * Reads the next character.
	 *
	 * @return the next char, or -1 if there is no more available data
	 */
	int read();

	/**
	 * Reads at most n characters.
	 *
	 * @param n the number of characters to parse
	 * @return an array containing at most n characters, not null
	 */
	Charray readAtMost(int n);

	/**
	 * Returns the next n characters. If there isn't enough available data, throws an exception.
	 *
	 * @param n the number of characters to parse
	 * @return an array containing the next n characters, not null
	 *
	 * @throws ParsingException if there is no more available data
	 */
	Charray readExactly(int n);

	/**
	 * Reads until a character that is not in {@code toSkip} is found.
	 *
	 * @param toSkip the characters to skip
	 * @return the next character that is not in {@code toSkip}, -1 if there is no more data
	 */
	default int readSkipping(Charray toSkip) {
		int c;
		do {
			c = read();
		} while (toSkip.contains((char)c));
		return c;
	}

	/**
	 * Reads the next characters, skipping spaces and tabs.
	 * @return the next character that is not a space nor a tab, -1 if there is no more data
	 */
	default int readNonSpace() {
		int c;
		do {
			c = read();
		} while (c == ' ' || c == '\t');
		return c;
	}

	/**
	 * Reads the next "solid" character, ie the next char {@code ch > '\u005Cu0020'}.
	 * This excludes (among others), {@code ' ', '\t', '\r', '\n'}.
	 *
	 * @return the next character, -1 if there is no more data
	 */
	default int readSolid() {
		int c;
		do {
			c = read();
		} while (c <= ' ');
		return c;
	}

	/**
	 * Reads until a character in {@code stop} is encountered.
	 *
	 * @param stop the characters to stop at
	 * @return a Charray containing all the characters read before the stop, excluding it.
	 */
	default Charray readUntil(Charray stop) {
		return readUntil(stop, Charray.DEFAULT_CAPACITY);
	}

	/**
	 * Reads until a character in {@code stop} is encountered.
	 *
	 * @param stop the characters to stop at
	 * @param sizeHint hint for the size of the Charray
	 * @return a Charray containing all the characters read before the stop, excluding it.
	 */
	default Charray readUntil(Charray stop, int sizeHint) {
		Charray builder = new Charray(sizeHint);
		int c;
		while ((c = read()) != -1) {
			char ch = (char)c;
			if (stop.contains(ch)) {
				pushBack(ch);
				break;
			} else {
				builder.append(ch);
			}
		}
		return builder;
	}

	/**
	 * Returns the next character, without moving the reading position forward. After a call to
	 * {@code peek()}, the method {@link #read()} will return the exact same character.
	 * <p>
	 * This method behaves exactly like {@code peek(0)}
	 *
	 * @return the next character, or -1 if there is no more available data
	 */
	default int peek() {
		return peek(0);
	}

	/**
	 * Returns the next (n+1)th character, without moving the reading position forward.
	 * The next character is given by peek(0), the one after it by peel(1), and so on.
	 *
	 * @param n the position to peek
	 * @return the next (n+1)th character, or -1 if there is no such character
	 */
	int peek(int n);

	/**
	 * Skips all the characters that have been peeked and not read yet.
	 */
	void skipPeeks();

	/**
	 * Reads and returns all the characters that have been peeked and not read yet.
	 */
	Charray readPeeks();

	/**
	 * Pushes a character back to the input, so that it will be returned by the next reading
	 * operation.
	 *
	 * @param c the character to push back
	 */
	void pushBack(char c);
}