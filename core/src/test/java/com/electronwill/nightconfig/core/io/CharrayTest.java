package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.impl.Charray;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class CharrayTest {
	Charray charray = new Charray(new char[] { 'a' }, 0, 1);

	@Test
	void testConstructors() {
		Charray[] c = new Charray[7], ref = new Charray[7];
		c[0] = new Charray('a', 'b');
		c[1] = new Charray(new char[]{'a', 'b'});
		c[2] = new Charray(new char[]{'a', 'b'}, 0, 2);
		c[3] = new Charray("ab");
		c[4] = new Charray("ab", 0, 2);
		c[5] = new Charray((CharSequence)"ab");
		c[6] = new Charray((CharSequence)"ab", 0, 2);
		Arrays.fill(ref, c[0]);
		assertArrayEquals(ref, c); // everything should be equal
	}

	@Test
	void testIsEmpty() {
		boolean result = charray.isEmpty();
		assertFalse(result);

		assertTrue(new Charray("").isEmpty());
	}

	@Test
	void testLength() {
		int result = charray.length();
		assertEquals(1, result);
	}

	@Test
	void testCharAt() {
		char result = charray.charAt(0);
		assertEquals('a', result);
	}

	@Test
	void testGet() {
		char result = charray.get(0);
		assertEquals('a', result);
	}

	@Test
	void testSet() {
		charray.set(0, 'a');
	}

	@Test
	void testReplaceAll() {
		Charray ch2 = charray.clone();
		ch2.replaceAll('a', 'b');
		assertEquals("b", ch2.toString());
		assertEquals("a", charray.toString());
	}

	@Test
	void testContains() {
		boolean result = charray.contains('a');
		assertTrue(result);
	}

	@Test
	void testIndexOf() {
		int result = charray.indexOf('a');
		assertEquals(0, result);

		Charray abcdefgh = new Charray("abcdefgh");
		result = abcdefgh.indexOf('h');
		assertEquals(abcdefgh.length()-1, result);
		assertEquals('a', abcdefgh.get(abcdefgh.indexOf('a')));
		assertEquals('e', abcdefgh.get(abcdefgh.indexOf('e')));
		assertEquals('h', abcdefgh.get(abcdefgh.indexOf('h')));
		assertEquals(-1, abcdefgh.indexOf('z'));
	}

	@Test
	void testIndexOfFirst() {
		int result = charray.indexOfFirst('a');
		assertEquals(0, result);
	}

	@Test
	void testEquals() {
		boolean result = charray.equals(new Charray("a"));
		assertTrue(result);
	}

	@Test
	void testEqualsIgnoreCase() {
		boolean result = charray.equalsIgnoreCase("a");
		assertTrue(result);
	}

	@Test
	void testContentEquals() {
		boolean result = charray.contentEquals("a");
		assertTrue(result);
	}

	@Test
	void testContentEquals2() {
		boolean result = charray.contentEquals(new char[] { 'a' });
		assertEquals(true, result);
	}

	@Test
	void testStartsWith() {
		boolean result = charray.startsWith(new Charray(new char[] { 'a' }, 0, 0));
		assertEquals(true, result);
	}

	@Test
	void testSubSequence() {
		Charray result = charray.subSequence(0, 1);
		assertEquals(charray, result);
	}

	@Test
	void testSubView() {
		Charray result = charray.sub(0, 1);
		assertEquals(new Charray(charray), result);

		result = new Charray("abcdefg").sub(2, 4);
		assertEquals(new Charray("cd"), result);
	}

	@Test
	void testSubView2() {
		Charray result = charray.sub(0);
		assertEquals(charray, result);
	}

	@Test
	void testTrimmedView() {
		Charray trimmed = new Charray("  		").trimmed();
		assertEquals("", trimmed.toString());

		trimmed = new Charray(" 1234 ").trimmed();
		assertEquals("1234", trimmed.toString());

		trimmed = new Charray("			a").trimmed();
		assertEquals("a", trimmed.toString());

		trimmed = new Charray("a			").trimmed();
		assertEquals("a", trimmed.toString());

		trimmed = new Charray("a			b").trimmed();
		assertEquals("a			b", trimmed.toString());
	}

	@Test
	void testToString() {
		String result = charray.toString();
		assertEquals("a", result);
	}

	@Test
	void testClone() {
		Charray result = charray.clone();
		assertEquals(charray, result);
	}
}
