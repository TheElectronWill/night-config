package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.utils.IntDeque;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract base class for CharacterInputs.
 *
 * @author TheElectronWill
 */
public abstract class AbstractInput implements CharacterInput {
	/**
	 * Contains the peeked characters that haven't been read (by the read methods) yet.
	 */
	protected final IntDeque deque = new IntDeque();
	private final AtomicLong line = new AtomicLong();
	private final AtomicLong column = new AtomicLong();

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

	protected void advance(int r) {
		if (r == '\n')
			advanceLine();
		else advanceColumn();
	}

	protected void advanceLine() {
		line.incrementAndGet();
		column.set(0);
	}

	protected void advanceColumn() {
		column.incrementAndGet();
	}

	@Override
	public long line() {
		return line.get();
	}

	@Override
	public long column() {
		return column.get();
	}

	@Override
	public int read() {
		int r;
		if (!deque.isEmpty()) {
			r = deque.removeFirst();
		} else {
			r = directRead();
		}
		advance(r);
		return r;
	}

	@Override
	public char readChar() {
		char r;
		if (!deque.isEmpty()) {
			int next = deque.removeFirst();
			if (next == -1) {
				throw ParsingException.notEnoughData();
			}
			r = (char)next;
		} else {
			r = directReadChar();
		}
		advance(r);
		return r;
	}

	@Override
	public int peek() {
		if (deque.isEmpty()) {
			int read = directRead();
			deque.addLast(read);
			return read;
		}
		return deque.getFirst();
	}

	@Override
	public int peek(int n) {
		final int diff = n - deque.size();
		if (diff >= 0) {
			for (int i = 0; i <= diff; i++) {
				int read = directRead();
				deque.addLast(read);
				if (read == -1) {
					return -1;//it's useless to continue reading of the EOS has been reached
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
		deque.clear();
	}

	@Override
	public void pushBack(char c) {
		deque.addFirst(c);
	}

	@Override
	public CharsWrapper readUntil(char[] stop) {
		CharsWrapper.Builder builder = new CharsWrapper.Builder(10);
		int c = read();
		while (c != -1 && !Utils.arrayContains(stop, (char)c)) {
			builder.append((char)c);
			c = read();
		}
		deque.addFirst(c);//remember this char for later
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
		deque.addFirst(c);//remember this char for later
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
			int next = deque.removeFirst();
			if (next == -1) {
				if (mustReadAll) {
					throw ParsingException.notEnoughData();
				}
				return new CharsWrapper(array, 0, i);
			}
			array[i] = (char)next;
		}
		return null;
	}
}