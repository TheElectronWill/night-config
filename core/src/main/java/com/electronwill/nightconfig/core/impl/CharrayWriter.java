package com.electronwill.nightconfig.core.impl;

import java.io.Writer;

/**
 * Like {@link java.io.StringWriter} but with a {@link Charray}.
 * Also, a CharrayWriter implements {@link CharacterOutput}.
 * <p>
 * CharrayWriter exists to provide a Writer and CharacterOutput interface on top of a Charray.
 * You don't need it to construct a Charray, String or CharSequence bit by bit.
 * Use {@link Charray} directly instead.
 * </p>
 */
public final class CharrayWriter extends Writer implements CharacterOutput {
	private final Charray cha;

	public CharrayWriter() {
		this(new Charray());
	}

	public CharrayWriter(int initialCapacity) {
		this(new Charray(initialCapacity));
	}

	public CharrayWriter(Charray cha) {
		this.cha = cha;
	}

	/** @return the Charray that contains all the data written to this CharrayWriter. */
	public Charray getCharray() {
		return cha;
	}

	/** @return the underlying Charray as a String. */
	@Override
	public String toString() {
		return cha.toString();
	}

	/**
	 * Flushing a CharrayWriter has no effect.
	 */
	@Override
	public void flush() {}

	/**
	 * Closing a CharrayWriter has no effect. It can still be used after a call to this method.
	 */
	@Override
	public void close() {}

	@Override
	public void write(char c) {
		cha.append(c);
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		cha.append(cbuf, off, off + len);
	}

	@Override
	public void write(int c) {
		write((char)c);
	}

	@Override
	public void write(char[] cbuf) {
		cha.append(cbuf);
	}

	@Override
	public void write(String str) {
		cha.append(str);
	}

	@Override
	public void write(String str, int off, int len) {
		cha.append(str, off, off + len);
	}

	@Override
	public void write(Charray cha) {
		cha.append(cha);
	}

	@Override
	public void write(Charray cha, int offset, int length) {
		cha.append(cha, offset, length);
	}

	@Override
	public CharrayWriter append(CharSequence csq) {
		cha.append(csq);
		return this;
	}

	@Override
	public CharrayWriter append(CharSequence csq, int start, int end) {
		cha.append(csq, start, end);
		return this;
	}

	@Override
	public CharrayWriter append(char c) {
		cha.append(c);
		return this;
	}
}
