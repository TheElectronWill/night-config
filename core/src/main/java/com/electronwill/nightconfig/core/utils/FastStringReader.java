package com.electronwill.nightconfig.core.utils;

import java.io.Reader;
import java.util.Objects;

/**
 * A fast, unsynchronized, simple Reader that reads the content of a String.
 * @author TheElectronWill
 */
public final class FastStringReader extends Reader {
	private final String str;
	private int cursor = 0, mark;

	public FastStringReader(String str) {
		this.str = Objects.requireNonNull(str, "The string must not be null.");
	}

	@Override
	public int read() {
		return str.charAt(cursor++);
	}

	@Override
	public int read(char[] cbuf, int off, int len) {
		str.getChars(off, off + len, cbuf, 0);
		cursor += len;
		return len;
	}

	@Override
	public long skip(long n) {
		int skip = (int)n;
		cursor += skip;
		return skip;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readAheadLimit) {
		mark = cursor;
	}

	@Override
	public void reset() {
		cursor = mark;
	}

	@Override
	public boolean ready() {
		return true;
	}

	@Override
	public void close() {}
}
