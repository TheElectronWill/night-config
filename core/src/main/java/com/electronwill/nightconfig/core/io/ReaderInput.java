package com.electronwill.nightconfig.core.io;

import java.io.IOException;
import java.io.Reader;

/**
 * An implementation of {@link CharacterInput} based on a {@link Reader}.
 *
 * @author TheElectronWill
 */
public final class ReaderInput extends AbstractInput {
	private final Reader reader;

	public ReaderInput(Reader reader) {
		this.reader = reader;
	}

	@Override
	protected int directRead() {
		try {
			return reader.read();
		} catch (IOException e) {
			throw ParsingException.readFailed(e);
		}
	}

	@Override
	protected char directReadChar() throws ParsingException {
		int read;
		try {
			read = reader.read();
		} catch (IOException e) {
			throw ParsingException.readFailed(e);
		}
		if (read == -1) {
			throw ParsingException.notEnoughData();
		}
		return (char)read;
	}

	@Override
	public CharsWrapper read(int n) {
		/* Overriden method to provide better performance: use parse(char[], ...) instead of
		   taking the characters one by one. */
		final char[] array = new char[n];
		final int offset = Math.min(deque.size(), n);
		CharsWrapper smaller = consumeDeque(array, offset, false);
		if (smaller != null) {// Less than n characters were read
			return smaller;
		}
		int nRead;
		try {
			nRead = reader.read(array, offset, n - offset);
		} catch (IOException e) {
			throw ParsingException.readFailed(e);
		}
		return new CharsWrapper(array, 0, offset + nRead);
	}

	@Override
	public CharsWrapper readChars(int n) {
		final char[] array = new char[n];
		final int offset = Math.min(deque.size(), n);
		consumeDeque(array, offset, true);
		int length = n - offset;
		int nRead;
		try {
			nRead = reader.read(array, offset, length);
		} catch (IOException e) {
			throw ParsingException.readFailed(e);
		}
		if (nRead != length) {
			throw ParsingException.notEnoughData();
		}
		return new CharsWrapper(array);
	}
}