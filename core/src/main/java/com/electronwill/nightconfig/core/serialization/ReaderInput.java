package com.electronwill.nightconfig.core.serialization;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import static javax.swing.TransferHandler.NONE;

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
	public int directRead() {
		try {
			return reader.read();
		} catch (IOException e) {
			throw new ParsingException("Failed to read data", e);
		}
	}

	@Override
	public char directReadChar() throws ParsingException {
		int read;
		try {
			read = reader.read();
		} catch (IOException e) {
			throw new ParsingException("Failed to read data", e);
		}
		if (read == -1) {
			throw new ParsingException("Not enough data available.");
		}
		return (char)read;
	}

	@Override
	public char[] read(int n) {
		// Overriden method to provide better performance: use read(char[], ...) instead of taking the
		// characters one by one.
		char[] array = new char[n];
		if (deque.isEmpty()) {
			int nRead;
			try {
				nRead = reader.read(array);
			} catch (IOException e) {
				throw new ParsingException("Failed to read data", e);
			}
			if (nRead != n) return Arrays.copyOf(array, nRead);
			return array;
		} else {
			int offset = Math.min(deque.size(), n);
			for (int i = 0; i < offset; i++) {
				int next = deque.removeFirst();
				if (next == EOS) {
					return Arrays.copyOf(array, i);
				}
				array[i] = (char)next;
			}
			int length = n - offset;
			int nRead;
			try {
				nRead = reader.read(array, offset, length);
			} catch (IOException e) {
				throw new ParsingException("Failed to read data", e);
			}
			if (nRead != length) return Arrays.copyOf(array, offset + nRead);
			return array;
		}
	}

	@Override
	public char[] readChars(int n) {
		char[] array = new char[n];
		if (deque.isEmpty()) {
			int nRead;
			try {
				nRead = reader.read(array);
			} catch (IOException e) {
				throw new ParsingException("Failed to read data", e);
			}
			if (nRead != n) return Arrays.copyOf(array, nRead);
			return array;
		} else {
			int offset = Math.min(deque.size(), n);
			for (int i = 0; i < offset; i++) {
				int next = deque.removeFirst();
				if (next == EOS) {
					throw new ParsingException("Not enough data available.");
				}
				array[i] = (char)next;
			}
			int length = n - offset;
			int nRead;
			try {
				nRead = reader.read(array, offset, length);
			} catch (IOException e) {
				throw new ParsingException("Failed to read data", e);
			}
			if (nRead != length) {
				throw new ParsingException("Not enough data available.");
			}
			return array;
		}
	}
}
