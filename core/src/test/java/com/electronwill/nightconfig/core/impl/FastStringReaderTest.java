package com.electronwill.nightconfig.core.impl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import static org.junit.jupiter.api.Assertions.*;

class FastStringReaderTest {

	@Test
	void testEmpty() throws IOException {
		Reader emptyReader = new FastStringReader("");
		assertTrue(emptyReader.ready());
		assertTrue(emptyReader.markSupported());
		assertEquals(-1, emptyReader.read());
		char[] dst = new char[10];
		assertEquals(-1, emptyReader.read(dst));
		CharBuffer buff = CharBuffer.allocate(10);
		assertEquals(-1, emptyReader.read(buff));
	}

	@Test
	void testRead() throws IOException {
		Reader reader = new FastStringReader("ScalaScalaScala");
		assertTrue(reader.ready());
		assertEquals('S', reader.read());
		char[] dst = new char[4];
		assertEquals(4, reader.read(dst));
		assertArrayEquals(new char[]{'a', 'l', 'a'}, dst);

		reader.mark(15);
		CharBuffer buff = CharBuffer.allocate(15);
		assertEquals(10, reader.read(buff));
		assertEquals(5, buff.remaining());
		buff.flip();
		assertEquals("ScalaScala", buff.toString());
	}
}