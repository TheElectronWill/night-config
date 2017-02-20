package com.electronwill.nightconfig.core.serialization;

import java.util.Arrays;

/**
 * An implementation of {@link CharacterInput} based on an array of characters.
 *
 * @author TheElectronWill
 */
public final class ArrayInput extends AbstractInput {
	private final char[] chars;
	private int index = 0;

	/**
	 * Creates a new ArrayInput based on the underlying array of the specified CharsWrapper. Any
	 * modification to the wrapper is reflected to the input.
	 *
	 * @param chars the CharsWrapper to use as an input
	 */
	public ArrayInput(CharsWrapper chars) {
		this(chars.getChars());
	}

	/**
	 * Creates a new ArrayInput based on the specified array. Any modification to the array is reflected to
	 * the input.
	 *
	 * @param chars the char array to use as an input
	 */
	public ArrayInput(char[] chars) {
		this.chars = chars;
	}

	@Override
	public int directRead() {
		if (index >= chars.length)
			return EOS;
		return chars[index++];
	}

	@Override
	public char directReadChar() throws ParsingException {
		if (index >= chars.length)
			throw new ParsingException("Not enough data available.");
		return chars[index++];
	}

	@Override
	public char[] read(int n) {
		// Overriden method to provide better performance: use arraycopy instead of taking the characters
		// one by one.
		n = Math.min(n, chars.length - index + deque.size());
		final int offset;
		final char[] array = new char[n];
		if (deque.isEmpty()) {
			offset = 0;
		} else {
			offset = Math.min(deque.size(), n);
			for (int i = 0; i < offset; i++) {
				int next = deque.removeFirst();
				if (next == EOS) {
					return Arrays.copyOf(array, i);
				}
				array[i] = (char)next;
			}
		}
		System.arraycopy(chars, index, array, offset, n - offset);
		index += n;
		return array;
	}

	@Override
	public char[] readChars(final int n) {
		if (chars.length - index + deque.size() < n) {
			throw new ParsingException("Not enough data available.");
		}
		final int offset;
		final char[] array = new char[n];
		if (deque.isEmpty()) {
			offset = 0;
		} else {
			offset = Math.min(deque.size(), n);
			for (int i = 0; i < offset; i++) {
				int next = deque.removeFirst();
				if (next == EOS) {
					throw new ParsingException("Not enough data available.");
				}
				array[i] = (char)next;
			}
		}
		System.arraycopy(chars, index, array, offset, n - offset);
		index += n;
		return array;
	}
}
