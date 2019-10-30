package com.electronwill.nightconfig.core.impl;

import com.electronwill.nightconfig.core.io.ParsingException;

/**
 * Abstract base class for CharacterInputs.
 *
 * @author TheElectronWill
 */
public abstract class AbstractInput implements CharacterInput {
	/**
	 * Contains the peeked characters that haven't been read (by the read methods) yet.
	 */
	protected final CharDeque deque = new CharDeque();
	protected int currentLine = 1, currentColumn = 1;

	/**
	 * Tries to parse the next character without taking care of the peek deque.
	 *
	 * @return the next character, or -1 if the EOS has been reached
	 */
	protected abstract int directRead();

	/**
	 * Tries to parse the next character without taking care of the peek deque.
	 *
	 * @return the next character
	 *
	 * @throws ParsingException if the EOS has been reached
	 */
	protected abstract char directReadChar();

	@Override
	public int line() {
		return currentLine;
	}

	@Override
	public int column() {
		return currentColumn;
	}

	private void updatePosition(char read) {
		if (read == '\n') {
			currentLine += 1;
			currentColumn = 1;
		} else {
			currentColumn += 1;
		}
	}

	private void rollbackPosition(char pushedBack) {
		if (pushedBack == '\n') {
			currentLine -= 1;
			currentColumn = 1; // ???
		} else {
			currentColumn -= 1;
		}
	}

	@Override
	public int read() {
		if (!deque.isEmpty()) {
			char next = deque.removeFirst();
			updatePosition(next);
			return next;
		}
		return directRead();
	}

	@Override
	public char readChar() {
		char next = deque.isEmpty() ? directReadChar() : deque.removeFirst();
		updatePosition(next);
		return next;
	}

	@Override
	public int peek() {
		if (deque.isEmpty()) {
			int read = directRead();
			if (read != -1)
				deque.addLast((char)read);
			return read;
		}
		return deque.getFirst();
	}

	@Override
	public int peek(int n) {
		final int toRead = n - deque.size();
		if (toRead >= 0) {
			for (int i = 0; i <= toRead; i++) {
				int read = directRead();
				if (read == -1) {
					return -1; // it's useless to continue reading if the EOS has been reached
				} else {
					deque.addLast((char)read);
				}
			}
		}
		return deque.get(n);
	}

	@Override
	public char peekChar() {
		int c = peek();
		if (c == -1) {
			throw ParsingException.notEnoughData();
		}
		return (char)c;
	}

	@Override
	public char peekChar(int n) {
		int c = peek(n);
		if (c == -1) {
			throw ParsingException.notEnoughData();
		}
		return (char)c;
	}

	@Override
	public void skipPeeks() {
		while (!deque.isEmpty()) {
			updatePosition(deque.removeLast());
			// TODO optimize by using the internal structure of the queue
		}
	}

	@Override
	public void pushBack(char c) {
		deque.addFirst(c);
		rollbackPosition(c);
	}

	@Override
	public CharsWrapper readUntil(char[] stop) {
		CharsWrapper.Builder builder = new CharsWrapper.Builder(10);
		int c = read();
		while (c != -1 && !Utils.arrayContains(stop, (char)c)) {
			builder.append((char)c);
			c = read();
		}
		if (c != -1)
			deque.addFirst((char)c); // remember this char for later
		return builder.build();
	}

	@Override
	public CharsWrapper readCharsUntil(char[] stop) {
		CharsWrapper.Builder builder = new CharsWrapper.Builder(10);
		char c = readChar();
		while (!Utils.arrayContains(stop, c)) {
			builder.append(c);
			c = readChar();
		}
		deque.addFirst(c); // remember this char for later
		return builder.build();
	}

	/**
	 * Consumes the chars of the deque and put them in an array.
	 *
	 * @param array       the destination array
	 * @param offset      the beginning index in the array
	 * @param mustReadAll {@code true} to throw an exception if the array can't be fulled,
	 *                    {@code false} to return a CharsWrapper containing the read characters.
	 * @return a CharsWrapper containing the read characters if the array can't be fulled, or null
	 */
	protected CharsWrapper consumeDeque(char[] array, int offset, boolean mustReadAll) {
		for (int i = 0; i < offset; i++) {
			if (deque.isEmpty()) {
				if (mustReadAll)
					throw ParsingException.notEnoughData();
				return new CharsWrapper(array, 0, i);
			} else {
				char next = deque.removeFirst();
				array[i] = next;
			}
		}
		return null;
	}
}