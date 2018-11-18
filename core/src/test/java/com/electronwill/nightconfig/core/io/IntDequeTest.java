package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.utils.IntDeque;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author TheElectronWill
 */
public class IntDequeTest {
	private static IntDeque deque;

	@BeforeEach
	public void setUp() {
		deque = new IntDeque();
	}

	@Test
	public void testCompact() {
		final int n = 10;
		//ADD, CLEAR AND COMPACT
		for (int i = 0; i <= n; i++) {
			deque.addLast(i);
		}
		deque.clear(); // Removes all the elements
		assertTrue(deque.isEmpty());
		deque.compact(); // Compacts the deque
		assertTrue(deque.isEmpty());

		//ADD, REMOVE AND COMPACT
		for (int i = 0; i <= n; i++) {
			deque.addLast(i);
			System.out.println(i + " => " + deque.size());
		}

		deque.removeFirst(); // Removes 1 element
		assertFalse(deque.isEmpty(), "The dequeue should NOT be empty!");
		assertEquals(n, deque.size());

		deque.compact(); // Compacts the deque
		assertFalse(deque.isEmpty(), "The dequeue should NOT be empty!");
		assertEquals(n, deque.size());

		deque.addLast(1234567890);
		assertFalse(deque.isEmpty(), "The dequeue should NOT be empty!");
		assertEquals(n + 1, deque.size());
	}

	@Test
	public void testQueue() {
		final int n = 4;
		//ADD
		for (int i = 0; i <= n; i++) {
			deque.addLast(i);
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
			deque.addFirst(i);
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