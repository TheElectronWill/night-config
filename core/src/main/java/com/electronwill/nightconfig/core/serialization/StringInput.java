package com.electronwill.nightconfig.core.serialization;

/**
 * @author TheElectronWill
 */
public final class StringInput implements CharacterInput {
	private final String str;
	private int index = 0;
	private int next = -1;

	public StringInput(String str) {
		this.str = str;
	}

	@Override
	public int read() throws IndexOutOfBoundsException {
		if (next != -1) {
			char c = (char) next;
			next = -1;
			return c;
		}
		if (index >= str.length())
			return -1;
		return readChar();
	}

	@Override
	public char[] read(int n) throws IndexOutOfBoundsException {
		int plus = (next == -1) ? 0 : 1;
		if (index + n >= str.length() + plus)
			return null;
		return readChars(n);
	}

	@Override
	public char readChar() throws IndexOutOfBoundsException {
		if (next != -1) {
			char c = (char) next;
			next = -1;
			return c;
		}
		return str.charAt(index++);
	}

	@Override
	public char[] readChars(int n) throws IndexOutOfBoundsException {
		final char[] array;
		final int offset;
		if (next != -1) {
			offset = 1;
			array = new char[n + 1];
			array[0] = (char) next;
			next = -1;
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
}
