package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.impl.CharsWrapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class CharsWrapperTest {
	CharsWrapper charsWrapper = new CharsWrapper(new char[] { 'a' }, 0, 1);

	@Test
	void testConstructors() {
		CharsWrapper[] c = new CharsWrapper[7], ref = new CharsWrapper[7];
		c[0] = new CharsWrapper('a', 'b');
		c[1] = new CharsWrapper(new char[]{'a', 'b'});
		c[2] = new CharsWrapper(new char[]{'a', 'b'}, 0, 2);
		c[3] = new CharsWrapper("ab");
		c[4] = new CharsWrapper("ab", 0, 2);
		c[5] = new CharsWrapper((CharSequence)"ab");
		c[6] = new CharsWrapper((CharSequence)"ab", 0, 2);
		Arrays.fill(ref, c[0]);
		assertArrayEquals(ref, c); // everything should be equal
	}

	@Test
	void testIsEmpty() {
		boolean result = charsWrapper.isEmpty();
		assertFalse(result);

		assertTrue(new CharsWrapper("").isEmpty());
	}

	@Test
	void testLength() {
		int result = charsWrapper.length();
		assertEquals(1, result);
	}

	@Test
	void testCharAt() {
		char result = charsWrapper.charAt(0);
		assertEquals('a', result);
	}

	@Test
	void testGet() {
		char result = charsWrapper.get(0);
		assertEquals('a', result);
	}

	@Test
	void testSet() {
		charsWrapper.set(0, 'a');
	}

	@Test
	void testReplaceAll() {
		CharsWrapper ch2 = charsWrapper.clone();
		ch2.replaceAll('a', 'b');
		assertEquals("b", ch2.toString());
		assertEquals("a", charsWrapper.toString());
	}

	@Test
	void testContains() {
		boolean result = charsWrapper.contains('a');
		assertTrue(result);
	}

	@Test
	void testIndexOf() {
		int result = charsWrapper.indexOf('a');
		assertEquals(0, result);

		CharsWrapper abcdefgh = new CharsWrapper("abcdefgh");
		result = abcdefgh.indexOf('h');
		assertEquals(abcdefgh.length()-1, result);
		assertEquals('a', abcdefgh.get(abcdefgh.indexOf('a')));
		assertEquals('e', abcdefgh.get(abcdefgh.indexOf('e')));
		assertEquals('h', abcdefgh.get(abcdefgh.indexOf('h')));
		assertEquals(-1, abcdefgh.indexOf('z'));
	}

	@Test
	void testIndexOfFirst() {
		int result = charsWrapper.indexOfFirst('a');
		assertEquals(0, result);
	}

	@Test
	void testEquals() {
		boolean result = charsWrapper.equals(new CharsWrapper("a"));
		assertTrue(result);
	}

	@Test
	void testEqualsIgnoreCase() {
		boolean result = charsWrapper.equalsIgnoreCase("a");
		assertTrue(result);
	}

	@Test
	void testContentEquals() {
		boolean result = charsWrapper.contentEquals("a");
		assertTrue(result);
	}

	@Test
	void testContentEquals2() {
		boolean result = charsWrapper.contentEquals(new char[] { 'a' });
		assertEquals(true, result);
	}

	@Test
	void testStartsWith() {
		boolean result = charsWrapper.startsWith(new CharsWrapper(new char[] { 'a' }, 0, 0));
		assertEquals(true, result);
	}

	@Test
	void testSubSequence() {
		CharsWrapper result = charsWrapper.subSequence(0, 1);
		assertEquals(charsWrapper, result);
	}

	@Test
	void testSubView() {
		CharsWrapper result = charsWrapper.subView(0, 1);
		assertEquals(new CharsWrapper(charsWrapper), result);

		result = new CharsWrapper("abcdefg").subView(2, 4);
		assertEquals(new CharsWrapper("cd"), result);
	}

	@Test
	void testSubView2() {
		CharsWrapper result = charsWrapper.subView(0);
		assertEquals(charsWrapper, result);
	}

	@Test
	void testTrimmedView() {
		CharsWrapper trimmed = new CharsWrapper("  		").trimmedView();
		assertEquals("", trimmed.toString());

		trimmed = new CharsWrapper(" 1234 ").trimmedView();
		assertEquals("1234", trimmed.toString());

		trimmed = new CharsWrapper("			a").trimmedView();
		assertEquals("a", trimmed.toString());

		trimmed = new CharsWrapper("a			").trimmedView();
		assertEquals("a", trimmed.toString());

		trimmed = new CharsWrapper("a			b").trimmedView();
		assertEquals("a			b", trimmed.toString());
	}

	@Test
	void testToString() {
		String result = charsWrapper.toString();
		assertEquals("a", result);
	}

	@Test
	void testClone() {
		CharsWrapper result = charsWrapper.clone();
		assertEquals(charsWrapper, result);
	}

	@Test
	void testIterator() {
		Iterator<Character> it = charsWrapper.iterator();
		assertTrue(it.hasNext());
		assertEquals('a', (char)it.next());
		assertFalse(it.hasNext());
	}
}
