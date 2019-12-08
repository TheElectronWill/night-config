package com.electronwill.nightconfig.core.impl;

import java.util.Arrays;
import java.util.Objects;

/**
 * An augmented array of chars, designed to avoid data copying and maximize performance.
 * <p>
 * <b>The {@code append} methods are fast but unsafe: they may modify the underlying char array
 * outside of the initial bounds (offset and limit) of the Charray.</b>
 *
 * @author TheElectronWill
 */
public final class Charray implements CharSequence, Cloneable, Appendable {
	static final int DEFAULT_CAPACITY = 64;

	char[] chars;
	int offset, limit;

	/** Creates a new empty Charrayof default capacity. */
	public Charray() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Creates a new empty Charray backed by a char array of the given length.
	 * @param initialCapacity the initial length of the char array
	 */
	public Charray(int initialCapacity) {
		this.chars = new char[initialCapacity];
	}

	/**
	 * Wraps an array in a Charray. Any modification to the array is
	 * reflected in the Charray and vice-versa.
	 */
	public Charray(char... chars) {
		this(chars, 0, chars.length);
	}

	/**
	 * Wraps a portion of an array in a Charray. Any modification to the array is
	 * reflected in the Charray and vice-versa.
	 *
	 * @param chars the char array to use
	 * @param start the index (in the array) of the first character to use
	 * @param end   the index +1 (in the array) of the last character to use
	 */
	public Charray(char[] chars, int start, int end) {
		if (chars == null)
			throw new NullPointerException("The given char array is null");
		if (end < 0)
			throw new IllegalArgumentException("Invalid end index " + end);
		if (end < start) {
			throw new IllegalArgumentException(
				String.format("start (%d) is greater than end (%d)", start, end)
			);
		}
		this.chars = Objects.requireNonNull(chars, "chars must not be null");
		this.offset = start;
		this.limit = end;
	}

	/**
	 * Copies a String into a Charray.
	 *
	 * @param str the String to copy
	 */
	public Charray(String str) {
		this(str, 0, str.length());
	}

	/**
	 * Copies a String into a Charray.
	 *
	 * @param str   the String to copy
	 * @param start index of the first character to copy from str
	 * @param end   index after the last character to copy from str
	 */
	public Charray(String str, int start, int end) {
		limit = end - start;
		chars = new char[limit];
		str.getChars(start, end, chars, 0);
	}

	/**
	 * Copies a CharSequence into a Charray.
	 *
	 * @param csq the sequence to copy
	 */
	public Charray(CharSequence csq) {
		this(csq, 0, csq.length());
	}

	/**
	 * Copies a CharSequence into a Charray.
	 *
	 * @param csq   the sequence to copy
	 * @param start index of the first character to copy from csq
	 * @param end   index after the last character to copy from csq
	 */
	public Charray(CharSequence csq, int start, int end) {
		limit = end - start;
		chars = new char[limit];
		for (int i = 0; i < limit; i++) {
			chars[i] = csq.charAt(start + i);
		}
	}

	public boolean isEmpty() {
		return limit == offset;
	}

	@Override
	public int length() {
		return limit - offset;
	}

	@Override
	public char charAt(int index) {
		return chars[offset + index];
	}

	/**
	 * @param index the character's index (the first character is at index 0)
	 * @return the character at the specified index
	 */
	public char get(int index) {
		return chars[offset + index];
	}

	/**
	 * Replaces a character.
	 *
	 * @param index the character's index (the first character is at index 0)
	 * @param c     the value to set
	 */
	public void set(int index, char c) {
		chars[offset + index] = c;
	}

	/**
	 * Replaces all occurences in this Wrapper of a character by another one.
	 *
	 * @param c           the character to replace
	 * @param replacement the replacement to use
	 */
	public void replaceAll(char c, char replacement) {
		for (int i = offset; i < limit; i++) {
			if (chars[i] == c) {
				chars[i] = replacement;
			}
		}
	}

	/** @return the index at which the additional data can be written */
	private int growUnsafe(int add) {
		if (limit + add <= chars.length) {
			// array big enough and offset ok => append at the end and increase the limit
			// (appending WILL erase the data after limit)
			int oldLimit = limit;
			limit = oldLimit + add;
			return oldLimit;
		} else {
			final int len = length();
			final int needed = len + add;
			if (chars.length < needed) {
				// array too small => create a bigger one
				// (appending WILL NOT erase the original array)
				int newCapacity = Math.max(needed, chars.length * 2);
				char[] newArray = new char[newCapacity];
				System.arraycopy(chars, offset, newArray, 0, len);
				chars = newArray;
			} else {
				// array big enough but offset too big => move the existing data to the beginning
				// (appending WILL erase the data before offset)
				System.arraycopy(chars, offset, chars, 0, len);
			}
			offset = 0;
			limit = needed;
			return len;
		}
	}

	@Override
	public Charray append(CharSequence csq) {
		if (csq == null)
			return append("null", 0, 4);
		return append(csq, 0, csq.length());
	}

	@Override
	public Charray append(CharSequence csq, int start, int end) {
		final int l = end-start;
		if (l < 0)
			throw new IndexOutOfBoundsException(
				String.format("start (%d) is greater than end (%d)", start, end)
			);
		if (l > 0) {
			int dst = growUnsafe(l);
			for (int i = 0; i < l; i++) {
				chars[dst + i] = csq.charAt(i);
			}
		}
		return this;
	}

	@Override
	public Charray append(char c) {
		int dst = growUnsafe(1);
		chars[dst] = c;
		return this;
	}

	public Charray append(String str) {
		if (str == null)
			return append("null", 0, 4);
		return append(str, 0, str.length());
	}

	public Charray append(String str, int start, int end) {
		int dst = growUnsafe(end - start);
		str.getChars(start, end, chars, dst);
		return this;
	}

	public Charray append(Object o) {
		return append(String.valueOf(o));
	}

	public Charray append(Charray cha) {
		int len = cha.length();
		int dst = growUnsafe(len);
		System.arraycopy(cha.chars, cha.offset, chars, dst, len);
		return this;
	}

	public Charray append(Charray cha, int start, int end) {
		return append(cha.sub(start, end));
	}

	public Charray append(char[] array) {
		return append(array, 0, array.length);
	}

	public Charray append(char[] array, int start, int end) {
		final int l = end-start;
		if (l < 0)
			throw new IndexOutOfBoundsException(
				String.format("start (%d) is greater than end (%d)", start, end)
			);
		if (l > 0) {
			int dst = growUnsafe(l);
			System.arraycopy(array, start, chars, dst, l);
		}
		return this;
	}


	/**
	 * Checks if this Charray contains the specified character.
	 *
	 * @param c the character to look for
	 * @return true if it contains the character, false if it does not
	 */
	public boolean contains(char c) {
		return indexOf(c) != -1;
	}

	/**
	 * Returns the index within this Charray of the first occurrence of the specified
	 * character. Returns -1 if this Charray doesn't contain the character.
	 *
	 * @param c the character to look for
	 * @return the index of the first occurence of {@code c}, or {@code -1} if not found.
	 */
	public int indexOf(char c) {
		for (int i = offset; i < limit; i++) {
			if (chars[i] == c) {
				return i - offset;
			}
		}
		return -1;
	}

	/**
	 * Returns the index within this Charray of the first occurrence of one of the specified
	 * characters. Returns -1 if this Charray doesn't contain any of these characters.
	 *
	 * @param ch the characters to look for
	 * @return the index of the first occurence of a character of {@code ch}, or {@code -1} if not
	 * found.
	 */
	public int indexOfFirst(char... ch) {
		for (int i = offset; i < limit; i++) {
			if (Utils.arrayContains(ch, chars[i])) {
				return i - offset;
			}
		}
		return -1;
	}

	/**
	 * Compares this Charray to a CharSequence, ignoring case considerations.
	 *
	 * @param csq the CharSequence to compare with this Charray
	 * @return true if cs isn't null and contains the same characters as this Charray, ignoring
	 * case considerations.
	 *
	 * @see String#equalsIgnoreCase(String)
	 */
	public boolean equalsIgnoreCase(CharSequence csq) {
		if (csq == this)
			return true;
		if (csq == null || csq.length() != length())
			return false;

		for (int i = 0; i < limit; i++) {
			char c1 = chars[offset + i];
			char c2 = csq.charAt(i);
			if (c1 != c2 && Character.toLowerCase(c1) != Character.toLowerCase(c2))
				return false;
		}
		return true;
	}

	/**
	 * Compares this Charray to a CharSequence.
	 *
	 * @param csq the CharSequence to compare with this Charray
	 * @return true if cs isn't null and contains the same characters as this Charray
	 *
	 * @see String#contentEquals(CharSequence)
	 */
	public boolean contentEquals(CharSequence csq) {
		final int l = length();
		if (csq == null || csq.length() != l) {
			return false;
		}
		for (int i = 0; i < l; i++) {
			if (chars[offset + i] != csq.charAt(i))
				return false;
		}
		return true;
	}

	/**
	 * Compares this Charray to an array of characters.
	 *
	 * @param array the array to compare with this Charray
	 * @return true if the array isn't null and contains the same characters as this Charray
	 */
	public boolean contentEquals(char[] array) {
		final int l = length();
		if (array == null || array.length != l) {
			return false;
		}
		for (int i = 0; i < l; i++) {
			if (chars[offset + i] != array[i])
				return false;
		}
		return true;
	}

	/**
	 * Compares two Charrays.
	 *
	 * @param cha the other Charray
	 * @return true if their content are equal
	 */
	public boolean contentEquals(Charray cha) {
		if (cha == null || cha.length() != length()) {
			return false;
		}
		final int diff = cha.offset-offset;
		for (int i = offset; i < limit; i++) {
			if (chars[i] != cha.chars[diff + i])
				return false;
		}
		return true;
	}

	/**
	 * Checks if this Charray starts with the same characters as the given CharSequence.
	 *
	 * @param cs the sequence to compare to the beginning of this Charray
	 * @return true if the first characters of this wrapper are the same as the given sequence
	 */
	public boolean startsWith(CharSequence cs) {
		if (cs == null) {
			return false;
		}
		final int l = cs.length();
		if (l > length()) {
			return false;
		}
		for (int i = 0; i < l; i++) {
			if (chars[offset + i] != cs.charAt(i))
				return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method copies the data so the returned Charray doesn't share its array with this
	 * Charray and is completely independant.
	 */
	@Override
	public Charray subSequence(int start, int end) {
		return new Charray(Arrays.copyOfRange(chars, start + offset, end + offset));
	}

	/**
	 * Creates a view of a part of this Charray. Any modification to the view is reflected in
	 * the original Charray and vice-versa.
	 *
	 * @param start the start index, inclusive
	 * @param end   the end index, exclusive
	 * @return a new Charray that is a view of a part of this Charray
	 */
	public Charray sub(int start, int end) {
		return new Charray(chars, start + offset, end + offset);
	}

	/**
	 * Creates a view of a part of this Charray. Any modification to the view is reflected in
	 * the original Charray and vice-versa.
	 *
	 * @param start the start index, inclusive
	 * @return a new Charray that is a view of a part of this Charray
	 */
	public Charray sub(int start) {
		return new Charray(chars, start + offset, limit);
	}

	/**
	 * Trims this Charray, that is, modifies its bounds to remove any leading and trailing
	 * whitespace (any char <= {@code '\u005Cu0020'} is considered to be a whitespace).
	 *
	 * @see String#trim()
 	 */
	public void trim() {
		while (offset < limit && chars[offset] <= ' ') {
			offset++;
		}
		while (limit > offset && chars[limit-1] <= ' ') {
			limit--;
		}
	}

	/**
	 * Creates a trimmed view of this Charray, with any leading and trailing whitespace
	 * removed. Any modification to the view is reflected in the original Charray and
	 * vice-versa.
	 *
	 * @return a new Charray that is a trimmed view of this Charray
	 * @see String#trim()
	 */
	public Charray trimmed() {
		int offset = this.offset, limit = this.limit;
		while (offset < limit && chars[offset] <= ' ') {
			offset++;
		}
		while (limit > offset && chars[limit-1] <= ' ') {
			limit--;
		}
		return new Charray(chars, offset, limit);
	}

	/**
	 * Creates a new input backed by this Charray. The Charray is not modified by the use of the
	 * input, so this method may be called multiple times. If the Charray is modified then the
	 * input reflects these modifications.
	 *
	 * @return a new input that returns the characters of this charray
	 */
	public CharacterInput asInput() {
		return new ArrayInput(this);
	}

	@Override
	public String toString() {
		return new String(chars, offset, length());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Charray) && contentEquals((Charray)obj);
	}

	/**
	 * Calculates the hash code of this Charray.
	 * <h1>Relation to String's hash code</h1>
	 * The hash code calculated by this method is guaranteed to return the same thing as
	 * {@code wrapper.toString().hashCode()}. That is, if a String and a Charray contain
	 * exactly the same characters then they will have the same hash code.
	 *
	 * @return a hash code for the current content of this Charray
	 *
	 * @see String#hashCode()
	 */
	@Override
	public int hashCode() {
		int h = 0;
		for (int i = offset; i < limit; i++) {
			h = 31 * h + chars[i];
		}
		return h;
	}

	/**
	 * Creates and returns a copy of this Charray. The underlying char array is copied and used
	 * to create a new instance of Charray.
	 *
	 * @return a copy of this Charray
	 */
	@Override
	public Charray clone() {
		return new Charray(Arrays.copyOfRange(chars, offset, limit));
	}
}
