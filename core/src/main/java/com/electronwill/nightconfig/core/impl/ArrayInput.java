package com.electronwill.nightconfig.core.impl;

import com.electronwill.nightconfig.core.io.ParsingException;

/**
 * An implementation of {@link CharacterInput} based on an array of characters.
 *
 * @author TheElectronWill
 */
public final class ArrayInput extends AbstractInput {
	private final char[] chars;
	private final int limit;
	private int cursor;

	/**
	 * Creates a new ArrayInput based on the underlying array of the specified CharsWrapper. Any
	 * modification to the wrapper is reflected in the input.
	 *
	 * @param chars the CharsWrapper to use as an input
	 */
	public ArrayInput(Charray chars) {
		this(chars.chars, chars.offset, chars.limit);
	}

	/**
	 * Creates a new ArrayInput based on the specified array. Any modification to the array is
	 * reflected in the input.
	 *
	 * @param chars the char array to use as an input
	 */
	public ArrayInput(char[] chars) {
		this(chars, 0, chars.length);
	}

	/**
	 * Creates a new ArrayInput based on the specified array. Any modification to the array is
	 * reflected in the input.
	 *
	 * @param chars the char array to use as an input
	 * @param start the index to start at (inclusive index)
	 * @param end   the index to stop at (exclusive index)
	 */
	public ArrayInput(char[] chars, int start, int end) {
		this.chars = chars;
		this.cursor = start;
		this.limit = end;
	}

	@Override
	protected int directRead() {
		if (cursor >= limit) {
			return -1;
		}
		return chars[cursor++];
	}

	@Override
	public Charray readAtMost(int n) {
		final int dequeSize = deque.size();
		final int available = limit - cursor + dequeSize;
		final int toRead = Math.min(available, n);
		return readCharray(toRead, dequeSize);
	}

	@Override
	public Charray readExactly(final int n) {
		final int dequeSize = deque.size();
		if (limit - cursor + dequeSize < n) {
			throw ParsingException.notEnoughData();
		}
		return readCharray(n, dequeSize);
	}

	private Charray readCharray(int n, int dequeSize) {
		char[] dst = new char[n];
		if (dequeSize == 0) {
			System.arraycopy(chars, cursor, dst, 0, n);
		} else if (dequeSize <= n) {
			deque.consumeAllNonEmptyQueue(dst);
			System.arraycopy(chars, cursor, dst, dequeSize, n - dequeSize);
		} else { // dequeSize > n
			deque.consumeQueue(dst, 0, n);
		}
		cursor += n;
		return new Charray(dst);
	}
}
