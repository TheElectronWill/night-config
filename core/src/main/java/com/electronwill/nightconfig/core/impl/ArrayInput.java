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
	public ArrayInput(CharsWrapper chars) {
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
	 * @param chars  the char array to use as an input
	 * @param offset the index to begin at (inclusive index)
	 * @param limit  the limit to stop at (exclusive index)
	 */
	public ArrayInput(char[] chars, int offset, int limit) {
		this.chars = chars;
		this.cursor = offset;
		this.limit = limit;
	}

	@Override
	protected int directRead() {
		if (cursor >= limit) {
			return -1;
		}
		return chars[cursor++];
	}

	@Override
	protected char directReadChar() throws ParsingException {
		if (cursor >= limit) {
			throw ParsingException.notEnoughData();
		}
		return chars[cursor++];
	}

	@Override
	public CharsWrapper read(int n) {
		/* Overriden method to provide better performance: use System.arraycopy instead of
		   taking the characters one by one. */
		final int size = Math.min(n, limit - cursor + deque.size());
		final int offset = Math.min(deque.size(), size);
		final char[] array = new char[size];
		CharsWrapper smaller = consumeDeque(array, offset, false);
		if (smaller != null) {// Less than n characters were read
			return smaller;
		}
		System.arraycopy(chars, cursor, array, offset, size - offset);
		cursor += size;
		return new CharsWrapper(array);
	}

	@Override
	public CharsWrapper readChars(final int n) {
		if (limit - cursor + deque.size() < n) {
			throw ParsingException.notEnoughData();
		}
		final int offset = Math.min(deque.size(), n);
		final char[] array = new char[n];
		consumeDeque(array, offset, true);
		System.arraycopy(chars, cursor, array, offset, n - offset);
		cursor += n;
		return new CharsWrapper(array);
	}
}