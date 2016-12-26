package com.electronwill.nightconfig.core.serialization;

import java.io.IOException;
import java.io.Reader;

/**
 * An implementation of {@link CharacterInput} based on a {@link Reader}.
 *
 * @author TheElectronWill
 */
public final class ReaderInput implements CharacterInput {
	private final Reader reader;
	private int next = NONE;
	private static final int EOS = -1, NONE = -2;

	public ReaderInput(Reader reader) {
		this.reader = reader;
	}

	@Override
	public int read() {
		if (next != NONE) {
			int n = next;
			next = NONE;
			return n;
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
		if (next != NONE) {
			if (next == EOS)
				return null;
			offset = 1;
			array = new char[n + 1];
			array[0] = (char)next;
			next = NONE;
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
	public int seek() {
		if (next == NONE) {
			try {
				next = reader.read();
			} catch (IOException e) {
				throw new ParsingException("Failed to read data", e);
			}
		}
		return next;
	}

	@Override
	public char readChar() {
		int read = read();
		if (read == EOS)
			throw new ParsingException("Not enough data available");
		return (char)read;
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

	@Override
	public char seekChar() {
		int n = seek();
		if (n == EOS)
			throw new ParsingException("Not enough data available");
		return (char)n;
	}
}
