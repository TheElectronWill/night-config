package com.electronwill.nightconfig.core.impl;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CharrayTest {
	final Charray a = new Charray('a');

	@Test
	void testConstructors() {
		Charray[] c = new Charray[7], ref = new Charray[7];
		c[0] = new Charray('a', 'b');
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
		assertFalse(a.isEmpty());
		assertTrue(new Charray("").isEmpty());
	}

	@Test
	void testLength() {
		int result = a.length();
		assertEquals(1, result);
	}

	@Test
	void testCharAt() {
		char result = a.charAt(0);
		assertEquals('a', result);
	}

	@Test
	void testGet() {
		char result = a.get(0);
		assertEquals('a', result);
	}

	@Test
	void testSet() {
		a.set(0, 'z');
		assertEquals('z', a.get(0));
		a.set(0, 'a');
		assertEquals('a', a.get(0));
	}

	@Test
	void testReplaceAll() {
		Charray ch2 = a.clone();
		ch2.replaceAll('a', 'b');
		assertEquals("b", ch2.toString());
		assertEquals("a", a.toString());

		Charray aaaba = new Charray("aaaba");
		aaaba.replaceAll('a', 'z');
		assertEquals("zzzbz", aaaba.toString());
	}

	@Test
	void testContains() {
		boolean result = a.contains('a');
		assertTrue(result);
	}

	@Test
	void testIndexOf() {
		int result = a.indexOf('a');
		assertEquals(0, result);

		Charray abcdefgh = new Charray("abcdefgh");
		result = abcdefgh.indexOf('h');
		assertEquals(abcdefgh.length()-1, result);
		for (char c: Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')) {
			assertEquals(c, abcdefgh.get(abcdefgh.indexOf(c)));
		}
		assertEquals(-1, abcdefgh.indexOf('z'));
	}

	@Test
	void testIndexOfFirst() {
		assertEquals(0, a.indexOfFirst('a'));
		assertEquals(2, new Charray("aacbaabb").indexOfFirst('b', 'c'));
	}

	@Test
	void testEquals() {
		boolean result = a.equals(new Charray("a"));
		assertTrue(result);
	}

	@Test
	void testEqualsIgnoreCase() {
		boolean result = a.equalsIgnoreCase("a");
		assertTrue(result);
	}

	@Test
	void testContentEquals() {
		assertTrue(a.contentEquals("a"));
		assertTrue(a.contentEquals(new char[] { 'a' }));
	}

	@Test
	void testStartsWith() {
		assertTrue(a.startsWith("a"));
		Charray cha = new Charray("abcdefg");
		assertTrue(cha.startsWith(a));
		assertTrue(cha.startsWith("abcdefg"));
		assertTrue(cha.startsWith(""));
		assertTrue(new Charray().startsWith(""));
	}

	@Test
	void testSubSequence() {
		Charray result = a.subSequence(0, 1);
		assertEquals(a, result);

		Charray cha = new Charray("abcdefg");
		Charray sub = cha.subSequence(2, 4);
		assertEquals("cd", sub.toString());
		assertEquals('c', sub.get(0));
		assertEquals('d', sub.get(1));

		sub.set(0, '!');
		assertEquals('!', sub.get(0));
		assertEquals('c', cha.get(2));

		cha.set(3, '?');
		assertEquals('?', cha.get(3));
		assertEquals('c', sub.get(1));
	}

	@Test
	void testSubView() {
		assertEquals(a, a.sub(0));
		assertEquals(a, a.sub(0, 1));

		Charray cha = new Charray("abcdefg");
		Charray sub = cha.sub(2, 4);
		assertEquals("cd", sub.toString());
		assertEquals('c', sub.get(0));
		assertEquals('d', sub.get(1));

		sub.set(0, '!');
		assertEquals('!', sub.get(0));
		assertEquals('!', cha.get(2));

		cha.set(3, '?');
		assertEquals('?', cha.get(3));
		assertEquals('?', sub.get(1));
	}

	@Test
	void testTrim() {
		testTrim("", "");
		testTrim("  		", "");
		testTrim(" 1234 ", "1234");
		testTrim("       a", "a");
		testTrim("a       ", "a");
		testTrim("a      b", "a      b");

		Charray cha = new Charray(" abab ");
		cha.trimmed().replaceAll('a', 'b');
		assertEquals(" bbbb ", cha.toString());
		cha.trim();
		assertEquals("bbbb", cha.toString());
	}

	private void testTrim(String init, String expected) {
		Charray cha = new Charray(init);
		assertEquals(expected, cha.trimmed().toString());
		cha.trim();
		assertEquals(expected, cha.toString());
		assertEquals(cha, cha.trimmed());
	}

	@Test
	void testToString() {
		String result = a.toString();
		assertEquals("a", result);
		assertEquals(1, result.length());

		assertEquals("abcdefg", new Charray("abcdefg").toString());
		assertEquals("abcd", new Charray('a', 'b', 'c', 'd').toString());
		assertEquals("", new Charray().toString());
		assertEquals("", new Charray(new char[10], 0, 0).toString());
	}

	@Test
	void testClone() {
		Charray result = a.clone();
		assertEquals(a, result);
	}

	@Test
	void testHashCode() {
		assertEquals("".hashCode(), new Charray().hashCode());
		assertEquals("a".hashCode(), a.hashCode());
		Charray cha = new Charray("abcdefg");
		assertEquals("abcdefg".hashCode(), cha.hashCode());
	}

	@Test
	void testAppend() {
		Charray java = new Charray("java");
		Charray scala = new Charray("scala");
		Charray appended = java.append(scala);
		assertSame(java, appended);
		assertEquals(9, appended.length());
		assertEquals("javascala", appended.toString());
		assertEquals("scala", scala.toString());
		appended.set(4, ' ');
		appended.set(5, 'v');
		assertEquals("java vala", appended.toString());
		assertEquals("scala", scala.toString());

		appended = scala.append('3');
		assertSame(scala, appended);
		assertEquals(6, scala.length());
		assertEquals("scala3", scala.toString());
		appended = scala.append(" is dotty");
		assertSame(scala, appended);
		assertEquals("scala3 is dotty", scala.toString());
	}
}
