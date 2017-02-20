package com.electronwill.nightconfig.core.serialization;

import java.util.NoSuchElementException;

/**
 * A deque of integers that increases its capacity as necessary. Since it's a deque, it can be used as a
 * FIFO (First-In-First-Out) queue and as a LIFO (Last-In-First-Out) stack.
 *
 * @author TheElectronWill
 * @see java.util.Deque
 */
public final class IntDeque {
	/**
	 * The array that contains the data. It is used as a circular buffer.
	 */
	private int[] data;

	/**
	 * The position of the first element.
	 */
	private int head = 0;

	/**
	 * The position of the last element + 1.
	 */
	private int tail = 0;

	/**
	 * Bitmask to calculate (x MODULO data.length) faster, by doing (x AND mask).
	 * It works only if data.length is a power of two.
	 */
	private int mask;

	/**
	 * Creates a new IntDeque with an initial capacity of 4.
	 */
	public IntDeque() {
		this(4);
	}

	/**
	 * Creates a new IntDeque with the specified initial capacity. The capacity must be positive and non-zero.
	 *
	 * @param initialCapacity the initial capacity, strictly positive
	 */
	public IntDeque(int initialCapacity) {
		if (initialCapacity <= 0)
			throw new IllegalArgumentException("The capacity must be positive and non-zero.");
		if (!isPowerOfTwo(initialCapacity))
			initialCapacity = nextPowerOfTwo(initialCapacity);

		data = new int[initialCapacity];
		mask = initialCapacity - 1;
	}

	private boolean isPowerOfTwo(int n) {
		return (n & -n) == n;//clever check based on how the numbers are represented
	}

	/**
	 * @param n
	 * @return the smallest power of two that is strictly greater than n
	 */
	private int nextPowerOfTwo(int n) {
		return Integer.highestOneBit(n) << 1;
	}

	/**
	 * Clears this deque. After a call of this method, the size of the deque will be zero.
	 */
	public void clear() {
		head = 0;
		tail = 0;
	}

	/**
	 * @return true if the deque is empty, false if it's not
	 */
	public boolean isEmpty() {
		return tail == head;
	}

	/**
	 * @return the size of the deque
	 */
	public int size() {
		if (tail >= head)
			return tail - head;
		return data.length - head + tail;
	}

	/**
	 * Compacts this deque, minimizing its size in memory.
	 */
	public void compact() {
		if (tail == head) {//deque empty
			data = new int[1];//the capacity must be non-zero
			head = 0;
			tail = 0;
			mask = 0;
			return;
		}

		final int size = size();
		int newCapacity = size + 1;//+1 because the array is never kept full
		if (!isPowerOfTwo(newCapacity))
			newCapacity = nextPowerOfTwo(newCapacity);
		final int[] newData = new int[newCapacity];
		if (tail > head) {
			System.arraycopy(data, head, newData, 0, tail - head);
		} else {
			int lenght1 = data.length - head;//length of the part from the head to the end of the array
			System.arraycopy(data, head, newData, 0, lenght1);//head to end
			System.arraycopy(data, 0, newData, lenght1, tail);//start to tail
		}
		head = 0;
		tail = size;
		data = newData;
		mask = newData.length - 1;
	}

	/**
	 * Increases the deque's capacity.
	 */
	private void grow() {
		final int newSize = data.length << 1;//double the size
		if (newSize < 0)//overflow!
			throw new IllegalStateException("IntDeque too big");

		final int[] newData = new int[newSize];//double the size
		final int lenght1 = data.length - head;//length of the part from the head to the end of the array
		System.arraycopy(data, head, newData, 0, lenght1);//head to end
		System.arraycopy(data, 0, newData, lenght1, tail);//start to tail

		head = 0;
		tail = data.length;
		data = newData;
		mask = newData.length - 1;
	}

	/**
	 * Inserts an element before the head of this deque. The deque increases its capacity if necessary.
	 *
	 * @param element the element to add
	 */
	public void addFirst(int element) {
		head = (head - 1) & mask;
		data[head] = element;
		if (head == tail)//deque full
			grow();
	}

	/**
	 * Inserts an element at the tail of this deque. The deque increases its capacity if necessary.
	 *
	 * @param element the element to add
	 */
	public void addLast(int element) {
		data[tail] = element;
		tail = (tail + 1) & mask;
		if (tail == head)//deque full
			grow();
	}

	/**
	 * Gets the element at the specified index of this deque, without removing it.
	 * <p>
	 * The index is relative to the head: the first element is at index 0, the next element is at index 1,
	 * etc.
	 * </p>
	 *
	 * @param index the index of the element, relative to the head
	 * @return the element at the specified index
	 *
	 * @throws NoSuchElementException if the deque contains less than {@code index+1} elements
	 */
	public int get(int index) {
		if (index >= size())
			throw new NoSuchElementException("No element at index " + index);
		return data[(head + index) & mask];
	}

	/**
	 * Gets the first element (head) of this deque without removing it.
	 *
	 * @return the first element of this deque
	 *
	 * @throws NoSuchElementException if the deque is empty
	 */
	public int getFirst() {
		if (tail == head)
			throw new NoSuchElementException("Empty deque");
		return data[head];
	}

	/**
	 * Gets the last element of this deque without removing it.
	 *
	 * @return the last element of this deque
	 *
	 * @throws NoSuchElementException if the deque is empty
	 */
	public int getLast() {
		if (tail == head)
			throw new NoSuchElementException("Empty deque");
		return data[(tail - 1) & mask];
	}

	/**
	 * Retrieves and removes the first element (head) of this deque.
	 *
	 * @return the first element of this deque
	 *
	 * @throws NoSuchElementException if the deque is empty
	 */
	public int removeFirst() {
		if (tail == head)
			throw new NoSuchElementException("Empty deque");
		int element = data[head];
		head = (head + 1) & mask;
		return element;
	}

	/**
	 * Retrieves and removes the last element of this deque.
	 *
	 * @return the last element of this deque
	 *
	 * @throws NoSuchElementException if the deque is empty
	 */
	public int removeLast() {
		if (tail == head)
			throw new NoSuchElementException("Empty deque");
		tail = (tail - 1) & mask;
		return data[tail];
	}
}
