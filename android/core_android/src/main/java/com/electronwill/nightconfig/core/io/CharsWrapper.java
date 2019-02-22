package com.electronwill.nightconfig.core.io;

import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A simple, efficient implementation of CharSequence, designed to avoid data copying and to
 * maximize performance.
 *
 * @author TheElectronWill
 */
public final class CharsWrapper implements CharSequence, Cloneable, Iterable<Character> {
	final char[] chars;
	final int offset, limit;

	/**
	 * Creates a new CharsWrapper backed by the given char array. Any modification to the array is
	 * reflected in the CharsWrapper and vice-versa.
	 *
	 * @param chars the char array to use
	 */
	public CharsWrapper(char... chars) {
		this(chars, 0, chars.length);
	}

	/**
	 * Creates a new CharsWrapper backed by the given char array. Any modification to the array is
	 * reflected in the CharsWrapper and vice-versa.
	 *
	 * @param chars  the char array to use
	 * @param offset the index (in the array) of the first character to use
	 * @param limit  the index +1 (in the array) of the last character to use
	 */
	public CharsWrapper(char[] chars, int offset, int limit) {
		if (limit < offset) {
			throw new IllegalArgumentException("limit must be bigger than offset");
		}
		this.chars = Objects.requireNonNull(chars, "chars must not be null");
		this.offset = offset;
		this.limit = limit;
	}

	/**
	 * Creates a new CharsWrapper containing the same characters as the specified String. The data
	 * is copied and the new CharsWrapper is completely independent.
	 *
	 * @param str the String to copy
	 */
	public CharsWrapper(String str) {
		this(str, 0, str.length());
	}

	/**
	 * Creates a new CharsWrapper containing the same characters as the specified String. The data
	 * is copied and the new CharsWrapper is completely independent.
	 *
	 * @param str   the String to copy
	 * @param begin index of the first character to copy from str
	 * @param end   index after the last character to copy from str
	 */
	public CharsWrapper(String str, int begin, int end) {
		offset = 0;
		limit = end - begin;
		chars = new char[limit];
		str.getChars(begin, end, chars, 0);
	}

	/**
	 * Creates a new CharsWrapper containing the same characters as the specified CharSequence. The
	 * data is copied and the new CharsWrapper is completely independent.
	 *
	 * @param csq the sequence to copy
	 */
	public CharsWrapper(CharSequence csq) {
		this(csq, 0, csq.length());
	}

	/**
	 * Creates a new CharsWrapper containing the same characters as the specified CharSequence. The
	 * data is copied and the new CharsWrapper is completely independent.
	 *
	 * @param csq   the sequence to copy
	 * @param begin index of the first character to copy from csq
	 * @param end   index after the last character to copy from csq
	 */
	public CharsWrapper(CharSequence csq, int begin, int end) {
		offset = 0;
		limit = end - begin;
		chars = new char[limit];
		for (int i = begin; i < end; i++) {
			chars[i - begin] = csq.charAt(i);
		}
	}

	/**
	 * Checks if this CharsWrapper is empty, ie if its length is zero.
	 *
	 * @return true if it's empty, false otherwise
	 */
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
	 * Sets the value of a character.
	 *
	 * @param index the character's index (the first character is at index 0)
	 * @param ch    the character value to set
	 */
	public void set(int index, char ch) {
		chars[offset + index] = ch;
	}

	/**
	 * Replaces all occurences in this Wrapper of a character by another one.
	 *
	 * @param ch          the character to replace
	 * @param replacement the replacement to use
	 */
	public void replaceAll(char ch, char replacement) {
		for (int i = offset; i < limit; i++) {
			if (chars[i] == ch) {
				chars[i] = replacement;
			}
		}
	}

	/**
	 * Checks if this CharsWrapper contains the specified character.
	 *
	 * @param c the character to look for
	 * @return true if it contains the character, false if it does not
	 */
	public boolean contains(char c) {
		return indexOf(c) != -1;
	}

	/**
	 * Returns the index within this CharsWrapper of the first occurrence of the specified
	 * character. Returns -1 if this CharsWrapper doesn't contain the character.
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
	 * Returns the index within this CharsWrapper of the first occurrence of one of the specified
	 * characters. Returns -1 if this CharsWrapper doesn't contain any of these characters.
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

	@Override
	public boolean equals(Object obj) {
		if (obj == this) { return true; }
		if (!(obj instanceof CharsWrapper)) { return false; }

		final CharsWrapper other = (CharsWrapper)obj;
		final int l = other.length();
		if (length() != l) {
			return false;
		}
		for (int i = 0; i < l; i++) {
			char c = chars[offset + i];
			char co = other.chars[other.offset + i];
			if (c != co) { return false; }
		}
		return true;
	}

	/**
	 * Compares this CharsWrapper to a CharSequence, ignoring case considerations.
	 *
	 * @param cs the CharSequence to compare with this CharsWrapper
	 * @return true if cs isn't null and contains the same characters as this CharsWrapper, ignoring
	 * case considerations.
	 *
	 * @see String#equalsIgnoreCase(String)
	 */
	public boolean equalsIgnoreCase(CharSequence cs) {
		if (cs == this) { return true; }
		if (cs == null || cs.length() != length()) { return false; }

		for (int i = 0; i < limit; i++) {
			char u1 = Character.toUpperCase(chars[offset + i]);
			char u2 = Character.toUpperCase(cs.charAt(i));
			if (u1 != u2) { return false; }
		}
		return true;
	}

	/**
	 * Compares this CharsWrapper to a CharSequence.
	 *
	 * @param cs the CharSequence to compare with this CharsWrapper
	 * @return true if cs isn't null and contains the same characters as this CharsWrapper
	 *
	 * @see String#contentEquals(CharSequence)
	 */
	public boolean contentEquals(CharSequence cs) {
		final int l = length();
		if (cs == null || cs.length() != l) {
			return false;
		}
		for (int i = 0; i < l; i++) {
			if (chars[offset + i] != cs.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares this CharsWrapper to an array of characters.
	 *
	 * @param array the array to compare with this CharsWrapper
	 * @return true if the array isn't null and contains the same characters as this CharsWrapper
	 */
	public boolean contentEquals(char[] array) {
		final int l = length();
		if (array == null || array.length != l) {
			return false;
		}
		for (int i = 0; i < l; i++) {
			if (chars[offset + i] != array[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if this CharsWrapper starts with the same characters as the given CharSequence.
	 *
	 * @param cs the sequence to compare to the beginning of this CharsWrapper
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
			if (chars[offset + i] != cs.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method copies the data so the returned CharsWrapper doesn't share its array with this
	 * CharsWrapper and is completely independant.
	 */
	@Override
	public CharsWrapper subSequence(int start, int end) {
		return new CharsWrapper(Arrays.copyOfRange(chars, start + offset, end + offset));
	}

	/**
	 * Creates a view of a part of this CharsWrapper. Any modification to the view is reflected in
	 * the original CharsWrapper and vice-versa.
	 *
	 * @param start the start index, inclusive
	 * @param end   the end index, exclusive
	 * @return a new CharsWrapper that is a view of a part of this CharsWrapper
	 */
	public CharsWrapper subView(int start, int end) {
		return new CharsWrapper(chars, start + offset, end + offset);
	}

	/**
	 * Creates a view of a part of this CharsWrapper. Any modification to the view is reflected in
	 * the original CharsWrapper and vice-versa.
	 *
	 * @param start the start index, inclusive
	 * @return a new CharsWrapper that is a view of a part of this CharsWrapper
	 */
	public CharsWrapper subView(int start) {
		return new CharsWrapper(chars, start + offset, limit);
	}

	/**
	 * Creates a trimmed view of this CharsWrapper, with any leading and trailing whitespace
	 * removed. Any modification to the view is reflected in the original CharsWrapper and
	 * vice-versa.
	 *
	 * @return a new CharsWrapper that is a trimmed view of this CharsWrapper
	 *
	 * @see String#trim()
	 */
	public CharsWrapper trimmedView() {
		int offset = this.offset, limit = this.limit;
		while (offset < limit && chars[offset] <= ' ') {
			offset++;
		}
		while (limit > offset && chars[limit-1] <= ' ') {
			limit--;
		}
		return new CharsWrapper(chars, offset, limit);
	}

	@Override
	public String toString() {
		return new String(chars, offset, length());
	}

	/**
	 * Calculates the hash code of this CharsWrapper.
	 * <h1>Relation to String's hash code</h1>
	 * The hash code calculated by this method is guaranteed to return the same thing as
	 * {@code wrapper.toString().hashCode()}. That is, if a String and a CharsWrapper contain
	 * exactly the same characters then they will have the same hash code.
	 *
	 * @return a hash code for the current content of this CharsWrapper
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
	 * Creates and returns a copy of this CharsWrapper. The underlying char array is copied and used
	 * to create a new instance of CharsWrapper.
	 *
	 * @return a copy of this CharsWrapper
	 */
	@Override
	public CharsWrapper clone() {
		return new CharsWrapper(Arrays.copyOf(chars, chars.length));
	}

	@Override
	public Iterator<Character> iterator() {
		return new Iterator<Character>() {
			private int index = offset;

			@Override
			public boolean hasNext() {
				return index < limit;
			}

			@Override
			public Character next() {
				if (index >= limit) {
					throw new NoSuchElementException("Index beyond limit: " + index);
				}
				return chars[index++];
			}
		};
	}

	/**
	 * Builder class for constructing CharsWrappers.
	 */
	public static final class Builder extends Writer implements CharacterOutput {
		private static final char[] NULL = {'n', 'u', 'l', 'l'};
		private char[] data;
		private int cursor = 0;

		/**
		 * Creates a new CharsWrapper's builder with the specified initial capacity. If the
		 * specified capacity is less than 2 then 2 will be used instead.
		 *
		 * @param initialCapacity the initial capacity
		 */
		public Builder(int initialCapacity) {
			this.data = new char[Math.min(2, initialCapacity)];
		}

		/**
		 * Ensures that {@code data} is large enough to contain {@code capacity} characters.
		 *
		 * @param capacity the minimum capacity to ensure
		 */
		private void ensureCapacity(int capacity) {
			if (data.length < capacity) {
				int newCapacity = Math.max(capacity, data.length * 2);
				data = Arrays.copyOf(data, newCapacity);
			}
		}

		@Override
		public Builder append(char c) {
			write(c);
			return this;
		}

		@Override
		public Builder append(CharSequence csq) {
			if (csq == null) {
				return append(NULL);
			} else if (csq instanceof String) {//faster method for Strings, they're more likely to be used
				return append((String)csq);
			}
			return append(csq, 0, csq.length());
		}

		@Override
		public Builder append(CharSequence csq, int start, int end) {
			if (csq == null) {
				return append(NULL, start, end);
			} else if (csq instanceof String) {//faster method for Strings, they're more likely to be used
				return append((String)csq, start, end);
			}
			final int length = end - start;
			final int newCursor = cursor + length;
			ensureCapacity(newCursor);
			for (int i = start; i < end; i++) {
				data[cursor + i] = csq.charAt(i);
			}
			cursor = newCursor;
			return this;
		}

		/**
		 * Appends a char array to this builder.
		 *
		 * @param chars the array to append, not null
		 * @return this builder
		 */
		public Builder append(char... chars) {
			write(chars);
			return this;
		}

		/**
		 * Appends a portion of a char array to this builder.
		 *
		 * @param chars the array to append, not null
		 * @param begin the index to start at
		 * @param end   the index to stop at (exclusive)
		 * @return this builder
		 */
		public Builder append(char[] chars, int begin, int end) {
			final int length = end - begin;
			write(chars, begin, length);
			return this;
		}

		/**
		 * Appends a String to this builder.
		 *
		 * @param str the String to append, not null
		 * @return this builder
		 */
		public Builder append(String str) {
			write(str);//optimized writing
			return this;
		}

		/**
		 * Appends a String to this builder.
		 *
		 * @param str   the String to append, not null
		 * @param begin the index to start at
		 * @param end   the index to stop at (exclusive)
		 * @return this builder
		 */
		public Builder append(String str, int begin, int end) {
			final int length = end - begin;
			write(str, begin, length);//optimized writing
			return this;
		}

		/**
		 * Appends a CharsWrapper to this builder.
		 *
		 * @param cw the wrapper to append, not null
		 * @return this builder
		 */
		public Builder append(CharsWrapper cw) {
			write(cw);//optimized writing
			return this;
		}

		/**
		 * Appends the string representation of an object to this builder. This is equivalent to
		 * {@code append(String.valueOf(o))}.
		 *
		 * @param o the object to append, may be null
		 * @return this builder
		 */
		public Builder append(Object o) {
			if (o == null) {
				return append(NULL);
			}
			return append(o.toString());
		}

		/**
		 * Appends multiple objects to this builder. This is equivalent to calling {@code
		 * append(String.valueOf(o))} in a loop.
		 *
		 * @param objects the objects to append, may be null
		 * @return this builder
		 */
		public Builder append(Object... objects) {
			for (Object o : objects) {
				append(o);
			}
			return this;
		}

		@Override
		public void flush() {}

		@Override
		public void close() {}

		@Override
		public void write(int c) {
			write((char)c);
		}

		@Override
		public void write(char c) {
			final int newCursor = cursor + 1;
			ensureCapacity(newCursor);
			data[cursor] = c;
			cursor = newCursor;
		}

		@Override
		public void write(char... cbuf) {
			CharacterOutput.super.write(cbuf);
		}

		@Override
		public void write(char[] chars, int offset, int length) {
			final int newCursor = cursor + length;
			ensureCapacity(newCursor);
			System.arraycopy(chars, offset, data, cursor, length);
			cursor = newCursor;
		}

		@Override
		public void write(String str) {
			CharacterOutput.super.write(str);
		}

		@Override
		public void write(String s, int offset, int length) {
			final int end = offset + length;
			final int newCursor = cursor + length;
			ensureCapacity(newCursor);
			s.getChars(offset, end, data, cursor);
			cursor = newCursor;
		}

		@Override
		public void write(CharsWrapper cw) {
			CharacterOutput.super.write(cw);
		}

		/**
		 * Gets the length (number of characters) of this builder.
		 *
		 * @return the length of this builder
		 */
		public int length() {
			return cursor;
		}

		/**
		 * Gets the underlying array of this builder. Please note that its size may not be equal to
		 * the length of the builder.
		 *
		 * @return the array containing the characters of this builder.
		 */
		public char[] getChars() {
			return data;
		}

		/**
		 * @param index the character's index (the first character is at index 0)
		 * @return the character at the specified index
		 */
		public char get(int index) {
			return data[index];
		}

		/**
		 * Sets the value of a character.
		 *
		 * @param index the character's index (the first character is at index 0)
		 * @param ch    the character value to set
		 */
		public void set(int index, char ch) {
			if (index >= cursor) {
				throw new IndexOutOfBoundsException(
						"Index must not be larger than the builder's length");
			}
			data[index] = ch;
		}

		/**
		 * Compacts this builder, minimizing its size in memory.
		 */
		public void compact() {
			if (cursor != data.length) {
				data = Arrays.copyOf(data, cursor);
			}
		}

		/**
		 * Builds a CharsWrapper with the content of this builder. The builder's content is directly
		 * used to create a new CharsWrapper.
		 *
		 * @return a new CharsWrapper with the content of this builder
		 */
		public CharsWrapper build() {
			return build(0);
		}

		/**
		 * Builds a CharsWrapper with the content of this builder. The builder's content is directly
		 * used to create a new CharsWrapper.
		 *
		 * @param start index of the 1st character to use
		 * @return a new CharsWrapper with the content of this builder
		 */
		public CharsWrapper build(int start) {
			return new CharsWrapper(data, start,
									cursor);//directly use this, no need to bound check here
		}

		/**
		 * Builds a CharsWrapper with the content of this builder. The builder's content is directly
		 * used to create a new CharsWrapper.
		 *
		 * @param start index of the 1st character to use
		 * @param end   index after the last character to use
		 * @return a new CharsWrapper with the content of this builder
		 */
		public CharsWrapper build(int start, int end) {
			if (end > cursor) {
				throw new IndexOutOfBoundsException(
						"Specified end index is larger than the builder's length!");
			}
			return new CharsWrapper(data, start, end);
		}

		/**
		 * Builds a CharsWrapper with <b>a copy of</b> the content of this builder.
		 *
		 * @return a new CharsWrapper with a copy of the content of this builder
		 */
		public CharsWrapper copyAndBuild() {
			return build(0);
		}

		/**
		 * Builds a CharsWrapper with <b>a copy of</b> the content of this builder.
		 *
		 * @param start index of the 1st character to use
		 * @return a new CharsWrapper with a copy of the content of this builder
		 */
		public CharsWrapper copyAndBuild(int start) {
			return new CharsWrapper(Arrays.copyOfRange(data, start,
													   cursor));//directly use this, no need to bound check here
		}

		/**
		 * Builds a CharsWrapper with <b>a copy of</b> the content of this builder.
		 *
		 * @param start index of the 1st character to use
		 * @param end   index after the last character to use
		 * @return a new CharsWrapper with a copy of the content of this builder
		 */
		public CharsWrapper copyAndBuild(int start, int end) {
			if (end > cursor) {
				throw new IndexOutOfBoundsException(
						"Specified end index is larger than the builder's length!");
			}
			return new CharsWrapper(Arrays.copyOfRange(data, start, end));
		}

		@Override
		public String toString() {
			return toString(0);
		}

		public String toString(int start) {
			return new String(data, start, cursor - start);
		}

		public String toString(int start, int end) {
			if (end > cursor) {
				throw new IndexOutOfBoundsException(
						"Specified end index is larger than the builder's length!");
			}
			return new String(data, start, end - start);
		}
	}
}