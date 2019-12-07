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
	 * @param n how many characters to read
	 * @return a Charray containing at most n characters, not null
	 */
	Charray readAtMost(int n);

	/**
	 * Reads exactly n characters. If there isn't enough available data, throws an exception.
	 *
	 * @param n how many characters to read
	 * @return a Charray containing the next n characters, not null
	 * @throws ParsingException if there is no more available data
	 */
	Charray readExactly(int n);

	default int skip(int a) {
		int c;
		while ((c = read()) == a);
		return c;
	}

	default int skipAny(int a, int b) {
		int c;
		do { c = read(); } while (c == a || c == b);
		return c;
	}

	default int skipAny(Charray chars) {
		int c;
		while (chars.contains((char)(c = read())));
		return c;
	}

	default int skipRange(int min, int max) {
		int c;
		while ((c = read()) >= min && c <= max);
		return c;
	}

	default int skipNotRange(int min, int max) {
		int c;
		while ((c = read()) < min || c > max);
		return c;
	}

	default int skipWhitespace() {
		return skipRange(0, ' ');
	}

	default Charray readWhileRange(int min, int max) {
		Charray dst = new Charray();
		readWhileRange(min, max, dst);
		return dst;
	}

	default void readWhileRange(int min, int max, Charray dst) {
		int c;
		while ((c = read()) != -1) {
			if (c < min || c > max) {
				pushBack((char)c);
				return;
			} else {
				dst.append((char)c);
			}
		}
	}

	default Charray readWhileAny(Charray chars) {
		Charray dst = new Charray();
		reawWhileAny(chars, dst);
		return dst;
	}

	default void reawWhileAny(Charray chars, Charray dst) {
		int c;
		while (chars.contains((char)(c = read())))
			dst.append((char)c);
	}

	default Charray readUntilAny(Charray chars) {
		Charray dst = new Charray();
		readUntilAny(chars, dst);
		return dst;
	}
	default void readUntilAny(Charray chars, Charray dst) {
		int c;
		while ((c = read()) != -1 && !chars.contains((char)c))
			dst.append((char)c);
	}

	default Charray readUntilRange(char min, char max) {
		Charray dst = new Charray();
		readUntilRange(min, max, dst);
		return dst;
	}

	default void readUntilRange(int min, int max, Charray dst) {
		int c;
		while ((c = read()) != -1) {
			if (c < min || c > max) {
				dst.append((char)c);
			} else {
				pushBack((char)c);
				return;
			}
		}
	}

	/**
	 * Returns the next character, without moving the reading position forward. After a call to
	 * {@code peek()}, the method {@link #read()} will return the exact same character.
	 * <p>
	 * This method behaves exactly like {@code #peekAfter(0)}
	 *
	 * @return the next character, or -1 if there is no more available data
	 * @see #peekAfter(int)
	 */
	default int peek() {
		return peekAfter(0);
	}

	/**
	 * Returns the next (n+1)th character, without moving the reading position forward.
	 * The next character is given by {@code peek(0)} (or just {@code peek()}, the one just after
	 * it by {@code peek(1)}, and so on.
	 *
	 * @param n the position to peek
	 * @return the next (n+1)th character, or -1 if there is no such character
	 */
	int peekAfter(int n);

	/**
	 * Returns the next n characters, without moving the reading position forward. After a call to
	 * {@code peekExactly(n)}, the method {@code #readExactly(n)} will return the same characters.
	 *
	 * @param n how many characters to read
	 */
	Charray peekExactly(int n);

	/**
	 * Returns the next characters, at most n, without moving the reading position forward. After
	 * a call to {@code peekAtMost(n)}, {@code #readAtMost(n)} will return the same characters.
	 *
	 * @param n how many characters to read
	 */
	Charray peekAtMost(int n);

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