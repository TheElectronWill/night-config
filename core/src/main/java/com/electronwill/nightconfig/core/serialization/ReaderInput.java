package com.electronwill.nightconfig.core.serialization;

import java.io.IOException;
import java.io.Reader;

/**
 * @author TheElectronWill
 */
public final class ReaderInput implements CharacterInput {
	private final Reader reader;

	public ReaderInput(Reader reader) {
		this.reader = reader;
	}

	@Override
	public int read() {
		try {
			return reader.read();
		} catch (IOException e) {
			throw new ParsingException("Failed to read data", e);
		}
	}

	@Override
	public char[] read(int n) {
		final char[] array = new char[n];
		try {
			int read = reader.read(array);
			if (read == -1)
				return null;
			return array;
		} catch (IOException e) {
			throw new ParsingException("Failed to read data", e);
		}
	}

	@Override
	public char readChar() {
		try {
			int read = reader.read();
			if (read == -1)
				throw new ParsingException("Not enough data available");
			return (char)read;
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
}
