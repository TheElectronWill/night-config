package com.electronwill.nightconfig.core.impl;

import com.electronwill.nightconfig.core.io.WritingException;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes the characters in a {@link Writer}.
 *
 * @author TheElectronWill
 */
public final class WriterOutput implements CharacterOutput {
	private final Writer writer;

	public WriterOutput(Writer writer) {
		this.writer = writer;
	}

	@Override
	public void write(char c) {
		try {
			writer.write(c);
		} catch (IOException e) {
			throw new WritingException(e);
		}
	}

	@Override
	public void write(char[] chars, int offset, int length) {
		try {
			writer.write(chars, offset, length);
		} catch (IOException e) {
			throw new WritingException(e);
		}
	}

	@Override
	public void write(String s, int offset, int length) {
		try {
			writer.write(s, offset, length);
		} catch (IOException e) {
			throw new WritingException(e);
		}
	}
}