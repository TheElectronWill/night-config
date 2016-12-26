package com.electronwill.nightconfig.core.serialization;

import java.io.IOException;
import java.io.Writer;

/**
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
			throw new SerializationException("Failed to write data", e);
		}
	}

	@Override
	public void write(char[] chars, int offset, int length) {
		try {
			writer.write(chars, offset, length);
		} catch (IOException e) {
			throw new SerializationException("Failed to write data", e);
		}
	}

	@Override
	public void write(String s) {
		try {
			writer.write(s);
		} catch (IOException e) {
			throw new SerializationException("Failed to write data", e);
		}
	}
}
