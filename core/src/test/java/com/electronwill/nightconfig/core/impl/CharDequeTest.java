package com.electronwill.nightconfig.core.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author TheElectronWill
 */
public class CharDequeTest {
	private static CharDeque deque;

	@BeforeEach
	public void setUp() {
		deque = new CharDeque();
	}

	@Test
	public void testCompact() {
		final char first = 'a', last = 'z';
		final int n = last - first;
		//ADD, CLEAR AND COMPACT
		for (char c = first; c <= last; c++) {
			deque.addLast(c);
		}
		deque.clear(); // Removes all the elements
		assertTrue(deque.isEmpty());
		deque.compact(); // Compacts the deque
		assertTrue(deque.isEmpty());

		//ADD, REMOVE AND COMPACT
		for (char c = first; c <= last; c++) {
			deque.addLast(c);
			System.out.println(c + " => " + deque.size());
		}

		deque.removeFirst(); // Removes 1 element
		assertFalse(deque.isEmpty(), "The dequeue should NOT be empty!");
		assertEquals(n, deque.size());

		deque.compact(); // Compacts the deque
		assertFalse(deque.isEmpty(), "The dequeue should NOT be empty!");
		assertEquals(n, deque.size());

		deque.addLast('€');
		assertFalse(deque.isEmpty(), "The dequeue should NOT be empty!");
		assertEquals(n + 1, deque.size());
	}

	@Test
	public void testQueue() {
		final int n = 4;
		//ADD
		for (int i = 0; i <= n; i++) {
			deque.addLast((char)('a'+i));
		}
		//SIZE
		assertFalse(deque.isEmpty(), "The dequeue should NOT be empty!");
		assertEquals(n + 1, deque.size());

		//GETFIRST
		int first = deque.getFirst();
		System.out.println("got first " + first);
		assertEquals(0, first);

		//GETLAST
		int last = deque.getLast();
		System.out.println("got last " + last);
		assertEquals(n, last);

		//GET
		for (int i = 0; i <= n; i++) {
			int e = deque.get(i);
			System.out.println("got " + e);
			assertEquals(i, e);
		}
		//REMOVE
		for (int i = 0; i <= n; i++) {
			int e = deque.removeFirst();
			System.out.println("removed " + e);
			assertEquals(i, e);
		}
	}

	@Test
	public void testStack() {
		final int n = 4;
		//ADD
		for (int i = 0; i <= n; i++) {
			deque.addFirst((char)('a'+i));
		}
		//SIZE
		assertFalse(deque.isEmpty(), "The dequeue should NOT be empty!");
		assertEquals(n + 1, deque.size());

		//GETFIRST
		int first = deque.getFirst();
		System.out.println("got first " + first);
		assertEquals(n, first);

		//GETLAST
		int last = deque.getLast();
		System.out.println("got last " + last);
		assertEquals(0, last);

		//GET
		for (int i = 0; i < n; i++) {
			int e = deque.get(i);
			System.out.println("got " + e);
			assertEquals(n-i, e);
		}
		//REMOVE
		for (int i = 0; i < n; i++) {
			int e = deque.removeFirst();
			System.out.println("removed " + e);
			assertEquals(n-i, e);
		}
	}
}