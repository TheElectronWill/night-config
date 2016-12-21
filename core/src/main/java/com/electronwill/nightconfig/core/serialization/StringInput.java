package com.electronwill.nightconfig.core.serialization;

/**
 * @author TheElectronWill
 */
public final class StringInput implements CharacterInput {
	private final String str;
	private int index = 0;

	public StringInput(String str) {
		this.str = str;
	}

	@Override
	public int read() throws IndexOutOfBoundsException {
		if (index >= str.length())
			return -1;
		return readChar();
	}

	@Override
	public char[] read(int n) throws IndexOutOfBoundsException {
		if (index + n >= str.length())
			return null;
		return readChars(n);
	}

	@Override
	public char readChar() throws IndexOutOfBoundsException {
		return str.charAt(index++);
	}

	@Override
	public char[] readChars(int n) throws IndexOutOfBoundsException {
		char[] array = new char[n];
		CharSequence s;
		str.getChars(index, index + n, array, 0);
		return array;
	}
}
