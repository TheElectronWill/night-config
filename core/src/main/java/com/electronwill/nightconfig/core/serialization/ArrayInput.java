package com.electronwill.nightconfig.core.serialization;

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
		if (index >= chars.length)
	protected int directRead() {
			return EOS;
		return chars[index++];
	}

	@Override
		if (index >= chars.length)
	protected char directReadChar() throws ParsingException {
			throw ParsingException.notEnoughData();
		return chars[index++];
	}

	@Override
	public CharsWrapper read(int n) {
		// Overriden method to provide better performance: use arraycopy instead of taking the characters
		// one by one.
		n = Math.min(n, chars.length - index + deque.size());
		final int offset = Math.min(deque.size(), n);
		final char[] array = new char[n];
		for (int i = 0; i < offset; i++) {
			int next = deque.removeFirst();
			if (next == EOS) {
				return new CharsWrapper(array, 0, i);
			}
			array[i] = (char)next;
		}
		System.arraycopy(chars, index, array, offset, n - offset);
		index += n;
		return new CharsWrapper(array);
	}

	@Override
	public CharsWrapper readChars(final int n) {
		if (chars.length - index + deque.size() < n) {
			throw ParsingException.notEnoughData();
		}
		final int offset = Math.min(deque.size(), n);
		final char[] array = new char[n];
		for (int i = 0; i < offset; i++) {
			int next = deque.removeFirst();
			if (next == EOS) {
				throw ParsingException.notEnoughData();
			}
			array[i] = (char)next;
		}
		System.arraycopy(chars, index, array, offset, n - offset);
		index += n;
		return new CharsWrapper(array);
	}
}
