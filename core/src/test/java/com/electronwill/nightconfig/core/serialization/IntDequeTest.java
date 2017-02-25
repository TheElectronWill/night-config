package com.electronwill.nightconfig.core.serialization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
public class IntDequeTest {
	private IntDeque deque;

	@BeforeAll
	public void setUp() throws Exception {
		deque = new IntDeque();
	}

	@Test
	public void testCompact() {
		final int n = 10;
		//ADD, CLEAR AND COMPACT
		for (int i = 0; i <= n; i++) {
			deque.addLast(i);
		}
		deque.clear();//Remove all the elements
		assert deque.isEmpty() : "The deque should be empty!";
		deque.compact();//Compacts the deque
		assert deque.isEmpty() : "The deque should still be empty!";

		//ADD, REMOVE AND COMPACT
		for (int i = 0; i <= n; i++) {
			deque.addLast(i);
			System.out.println(i + " => " + deque.size());
		}

		deque.removeFirst();//Remove 1 element
		assert !deque.isEmpty() : "The deque should NOT be empty!";
		assert deque.size() == n : "Wrong size: " + deque.size();

		deque.compact();//Compact the deque
		assert !deque.isEmpty() : "The deque should still NOT be empty!";
		assert deque.size() == n : "Wrong size: " + deque.size();

		deque.addLast(1234567890);
		assert deque.size() == n + 1 : "Wrong size: " + deque.size();
	}

	@Test
	public void testQueue() {
		final int n = 4;
		//ADD
		for (int i = 0; i <= n; i++) {
			deque.addLast(i);
		}
		//SIZE
		assert !deque.isEmpty() : "The deque should not be empty!";
		assert deque.size() == n + 1 : "Invalid size " + deque.size();

		//GETFIRST
		int first = deque.getFirst();
		System.out.println("got first " + first);
		assert first == 0;

		//GETLAST
		int last = deque.getLast();
		System.out.println("got last " + last);
		assert last == n;

		//GET
		for (int i = 0; i <= n; i++) {
			int e = deque.get(i);
			System.out.println("got " + e);
			assert e == i;
		}
		//REMOVE
		for (int i = 0; i <= n; i++) {
			int e = deque.removeFirst();
			System.out.println("removed " + e);
			assert e == i;
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
		assert !deque.isEmpty() : "The deque should not be empty!";
		assert deque.size() == n + 1 : "Invalid size " + deque.size();

		//GETFIRST
		int first = deque.getFirst();
		System.out.println("got first " + first);
		assert first == n;

		//GETLAST
		int last = deque.getLast();
		System.out.println("got last " + last);
		assert last == 0;

		//GET
		for (int i = 0; i < n; i++) {
			int e = deque.get(i);
			System.out.println("got " + e);
			assert e == n - i;
		}
		//REMOVE
		for (int i = 0; i < n; i++) {
			int e = deque.removeFirst();
			System.out.println("removed " + e);
			assert e == n - i;
		}

	}

}