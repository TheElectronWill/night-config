package com.electronwill.nightconfig.core.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterInputTest {

	final Charray spaces = new Charray(' ', '\t', '\n');

	@Test
	void test() {
		Charray cha = new Charray();
		cha.append("night-config core\t\n");
		cha.append("aaa: value\n");
		cha.append("bBBBbb :value // maybe some comment\n");
		cha.append("\t tab\n");
		cha.append("\n");
		cha.append("ddd: end\n");
		testInput(new ArrayInput(cha));
		testInput(new ReaderInput(new FastStringReader(cha.toString())));
	}

	private void testInput(CharacterInput input) {
		assertEquals(1, input.line());
		assertEquals(0, input.column());
		assertEquals('n', input.read());
		assertEquals(1, input.line());
		assertEquals(1, input.column());

		assertEquals("ight-config", input.readUntilAny(spaces).toString());
		assertEquals(1, input.line());
		assertEquals(' ', input.read());
		assertEquals('c', input.peek());
		assertEquals('c', input.peekAfter(0));
		assertEquals('o', input.peekAfter(1));
		assertEquals('r', input.peekAfter(2));
		assertEquals('e', input.peekAfter(3));
		input.skipPeeks();

		input.skipWhitespaces();
		assertEquals(2, input.line());
		assertEquals(0, input.column());
		assertEquals('a', input.peek());
		assertEquals("aaa", input.readWhileRange('a', 'a').toString());
		assertEquals(": value\n", input.readUntilRange('b', 'b').toString());
		assertEquals(3, input.line());
		assertEquals(0, input.column());
		assertEquals('b', input.peek());
		assertEquals(3, input.skipAtMost(3));
		assertEquals("bbb :value // maybe some comment", input.readUntilAny(spaces).toString());
		assertEquals('\n', input.peek());
		input.skipWhitespaces();
		assertEquals("tab", input.readExactly(3).toString());
		input.skipWhitespaces();
		assertEquals(6, input.line());
		assertEquals('d', input.read());
		assertEquals(6, input.line());
		assertEquals(1, input.column());
		assertEquals("dd: end\n", input.readAtMost(10).toString());
		assertEquals(7, input.line());
		assertEquals(0, input.column());
		input.pushBack('\n');
		assertEquals(6, input.line());
		System.out.println("column after pushBack: " + input.column());
		assertEquals('\n', input.read());
		assertEquals(-1, input.read());
		assertEquals("", input.readAtMost(100).toString());
		assertEquals("", input.readUntilAny(spaces).toString());
		assertEquals("", input.readWhileAny(spaces).toString());
		assertEquals("", input.readWhileRange(0, 'b').toString());
		assertEquals("", input.readUntilRange(0, 'b').toString());
	}
}