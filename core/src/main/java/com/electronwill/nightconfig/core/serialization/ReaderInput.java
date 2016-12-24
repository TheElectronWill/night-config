package com.electronwill.nightconfig.core.serialization;

import java.io.IOException;
import java.io.Reader;

/**
 * @author TheElectronWill
 */
public final class ReaderInput implements CharacterInput {
	private final Reader reader;
	private int next = -1;

	public ReaderInput(Reader reader) {
		this.reader = reader;
	}

	@Override
	public int read() {
		if (next != -1) {
			char c = (char) next;
			next = -1;
			return c;
		}
		try {
			return reader.read();
		} catch (IOException e) {
			throw new ParsingException("Failed to read data", e);
		}
	}

	@Override
	public char[] read(int n) {
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
		final int length = n - offset;
		try {
			int read = reader.read(array, offset, length);
			if (read != length)
				return null;
			return array;
		} catch (IOException e) {
			throw new ParsingException("Failed to read data", e);
		}
	}

	@Override
	public char readChar() {
		if (next != -1) {
			char c = (char) next;
			next = -1;
			return c;
		}
		try {
			int read = reader.read();
			if (read == -1)
				throw new ParsingException("Not enough data available");
			return (char) read;
		} catch (IOException e) {
			throw new ParsingException("Failed to read data", e);
		}
	}

	@Override
	public char[] readChars(int n) {
		char[] read = read(n);
		if (read == null)
			throw new ParsingException("Not enough data available");
		return read;
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
