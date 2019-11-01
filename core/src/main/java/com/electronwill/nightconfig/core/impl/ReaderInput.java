package com.electronwill.nightconfig.core.impl;

import com.electronwill.nightconfig.core.io.ParsingException;

import java.io.IOException;
import java.io.Reader;

/**
 * An implementation of {@link CharacterInput} based on a {@link Reader}.
 *
 * @author TheElectronWill
 */
public final class ReaderInput extends AbstractInput {
	private final Reader reader;

	public ReaderInput(Reader reader) {
		this.reader = reader;
	}

	@Override
	protected int directRead() {
		try {
			return reader.read();
		} catch (IOException e) {
			throw ParsingException.readFailed(e);
		}
	}

	@Override
	public Charray readAtMost(int n) {
		final int dequeSize = deque.size();
		final char[] dst = new char[n];
		int actualN;
		if (dequeSize == 0) {
			actualN = readOnce(dst, 0, n);
		} else if (dequeSize <= n) {
			deque.consumeAllNonEmptyQueue(dst);
			actualN = dequeSize + readOnce(dst, dequeSize, n - dequeSize);
		} else { // dequeSize > n
			deque.consumeQueue(dst, 0, n);
			actualN = dequeSize;
		}
		return new Charray(dst, 0, actualN);
	}

	@Override
	public Charray readExactly(int n) {
		final int dequeSize = deque.size();
		final char[] dst = new char[n];
		int actualN;
		if (dequeSize == 0) {
			actualN = readLoop(dst, 0, n);
		} else if (dequeSize <= n) {
			deque.consumeAllNonEmptyQueue(dst);
			actualN = dequeSize + readLoop(dst, dequeSize, n - dequeSize);
		} else { // dequeSize > n
			deque.consumeQueue(dst, 0, n);
			actualN = dequeSize;
		}
		if (actualN != n) {
			throw ParsingException.notEnoughData();
		}
		return new Charray(dst);
	}

	/**
	 * Reads multiple characters from the reader.
	 *
	 * @param dst    where to put the chars
	 * @param dstPos where to start
	 * @param maxLen how many chars to read (at most)
	 * @return the number of chars actually read
	 */
	private int readOnce(char[] dst, int dstPos, int maxLen) {
		try {
			return reader.read(dst, dstPos, maxLen);
		} catch (IOException ex) {
			throw ParsingException.readFailed(ex);
		}
	}

	/**
	 * Best effort to read {@code maxLen} characters from the Reader, by waiting if needed.
	 *
	 * @param dst    where to put the chars
	 * @param dstPos where to start
	 * @param maxLen how many chars to read (at most)
	 * @return the number of chars actually read
	 */
	private int readLoop(char[] dst, int dstPos, int maxLen) {
		final int end = dstPos + maxLen;
		int pos = dstPos;
		while (pos < end) {
			int read = readOnce(dst, pos, end - pos);
			if (read == -1)
				break;
			pos += read;
		}
		return pos - dstPos;
	}
}