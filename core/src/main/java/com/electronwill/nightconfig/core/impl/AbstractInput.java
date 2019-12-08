package com.electronwill.nightconfig.core.impl;

/**
 * Abstract base class for CharacterInputs.
 *
 * @author TheElectronWill
 */
public abstract class AbstractInput implements CharacterInput {
	/**
	 * Contains the peeked characters that haven't been read (by the read() methods) yet.
	 */
	protected final CharDeque deque = new CharDeque();

	/**
	 * Tracks the current position.
	 */
	protected int currentLine = 1, currentColumn = 0, previousLastColumn = -1;

	/**
	 * Tries to parse the next character without taking care of the peek deque.
	 *
	 * @return the next character, or -1 if the EOS has been reached
	 */
	protected abstract int directRead();

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
			previousLastColumn = currentColumn;
			currentColumn = 0;
		} else {
			currentColumn += 1;
		}
	}

	private void rollbackPosition(char pushedBack) {
		if (pushedBack == '\n') {
			currentLine -= 1;
			currentColumn = previousLastColumn;
			previousLastColumn = -1;
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
	public int peekAfter(int n) {
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
	public Charray peekExactly(int n) {
		Charray read = readExactly(n);
		deque.addFirst(read);
		return read;
	}

	@Override
	public Charray peekAtMost(int n) {
		Charray read = readAtMost(n);
		deque.addFirst(read);
		return read;
	}

	@Override
	public void skipPeeks() {
		// Read the deque from the head and take each peeked char into account
		final char[] data = deque.data;
		final int mask = deque.mask;
		final int head = deque.head;
		final int end = head + deque.size();
		for (int i = head; i < end; i++) {
			updatePosition(data[i & mask]);
		}
		// Mark the deque as empty
		deque.tail = head;
	}

	@Override
	public Charray readPeeks() {
		return new Charray(deque.consumeAllQueue());
	}

	@Override
	public void pushBack(char c) {
		deque.addFirst(c);
		rollbackPosition(c);
	}
}
