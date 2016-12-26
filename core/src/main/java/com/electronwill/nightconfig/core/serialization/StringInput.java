package com.electronwill.nightconfig.core.serialization;

/**
 * An implementation of {@link CharacterInput} based on a {@link String}.
 *
 * @author TheElectronWill
 */
public final class StringInput implements CharacterInput {
	private final String str;
	private int index = 0;
	private int next = NONE;
	private static final int EOS = -1, NONE = -2;

	public StringInput(String str) {
		this.str = str;
	}

	@Override
	public int read() {
		if (next != NONE) {
			char c = (char)next;
			next = NONE;
			return c;
		}
		if (index >= str.length())
			return EOS;
		return readChar();
	}

	@Override
	public char[] read(int n) {
		int plus = (next == NONE) ? 0 : 1;
		if (index + n >= str.length() + plus)
			return null;
		return readChars(n);
	}

	@Override
	public int seek() {
		if (next == NONE) {
			next = read();
		}
		return next;
	}

	@Override
	public char readChar() throws IndexOutOfBoundsException {
		if (next != NONE) {
			if (next == EOS)
				throw new ParsingException("Not enough data available");
			char c = (char)next;
			next = NONE;
			return c;
		}
		return str.charAt(index++);
	}

	@Override
	public char[] readChars(int n) throws IndexOutOfBoundsException {
		final char[] array;
		final int offset;
		if (next != NONE) {
			if (next == EOS)
				throw new ParsingException("Not enough data available");
			offset = 1;
			array = new char[n + 1];
			array[0] = (char)next;
			next = NONE;
		} else {
			offset = 0;
			array = new char[n];
		}
		str.getChars(index, index + n, array, offset);
		return array;
	}

	@Override
	public CharsWrapper readCharUntil(char[] stop) {
		CharsWrapper.Builder builder = new CharsWrapper.Builder(10);
		char c = readChar();
		while (!Utils.arrayContains(stop, c)) {
			builder.append(c);
			c = readChar();
		}
		next = c;//remember this char for later
		return builder.build();
	}

	@Override
	public char seekChar() {
		int n = seek();
		if (n == EOS)
			throw new ParsingException("Not enough data available");
		return (char)n;
	}
}
