package com.electronwill.nightconfig.core.serialization;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A simple, efficient implementation of CharSequence. To avoid data copying, its constructor doesn't
 * perfom any copy of the char array.
 *
 * @author TheElectronWill
 */
public final class CharsWrapper implements CharSequence, Cloneable, Iterable<Character> {
	private final char[] chars;
	private final int offset, limit;

	/**
	 * Creates a new CharsWrapper backed by the given char array. Any modification to the array is
	 * reflected to the CharsWrapper and vice-versa.
	 *
	 * @param chars the char array to use
	 */
	public CharsWrapper(char[] chars) {
		this(chars, 0, chars.length);
	}

	/**
	 * Creates a new CharsWrapper backed by the given char array. Any modification to the array is
	 * reflected to the CharsWrapper and vice-versa.
	 *
	 * @param chars  the char array to use
	 * @param offset the index (in the array) of the first character of the Wrapper
	 * @param limit  the index +1 (in the array) of the last character of the Wrapper
	 */
	public CharsWrapper(char[] chars, int offset, int limit) {
		this.chars = chars;
		this.offset = offset;
		this.limit = limit;
	}

	/**
	 * Creates a new CharsWrapper containing the same characters as the specified CharSequence. The data is
	 * copied and the new CharsWrapper is completely independent.
	 *
	 * @param csq the sequence to copy
	 */
	public CharsWrapper(CharSequence csq) {
		offset = 0;
		limit = csq.length();
		chars = new char[limit];
		for (int i = 0; i < limit; i++) {
			chars[i] = csq.charAt(i);
		}
	}

	/**
	 * Returns the underlying char array that contains the CharsWrapper's characters. Any modification to
	 * the array is reflected to the CharsWrapper and vice-versa.
	 *
	 * @return the underlying char array
	 */
	char[] getChars() {
		return chars;
	}

	int getOffset() {
		return offset;
	}

	int getLimit() {
		return limit;
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
	 * {@inheritDoc}
	 * <p>
	 * This method copies the data so the returned CharsWrapper doesn't share its array with this CharsWrapper
	 * and is completely independant.
	 * </p>
	 */
	@Override
	public CharsWrapper subSequence(int start, int end) {
		return new CharsWrapper(Arrays.copyOfRange(chars, start, end));
	}

	/**
	 * Creates a view of a part of this CharsWrapper. Any modification to the view is reflected to the
	 * original CharsWrapper and vice-versa.
	 *
	 * @param start the start index, inclusive
	 * @param end   the end index, exclusive
	 * @return a new CharsWrapper that is a view of a part of this CharsWrapper
	 */
	public CharsWrapper subView(int start, int end) {
		return new CharsWrapper(chars, start, end);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof CharsWrapper)) return false;

		CharsWrapper other = (CharsWrapper)obj;
		final int l = other.length();
		if (length() != l) {
			return false;
		}

		for (int i = 0; i < l; i++) {
			char c = chars[offset + i];
			char co = chars[other.offset + i];
			if (c != co) return false;
		}
		return true;
	}

	/**
	 * Compares this CharsWrapper to a CharSequence, ignoring case considerations.
	 *
	 * @param cs the CharSequence to compare with this CharsWrapper
	 * @return true if cs isn't null and contains the same characters as this CharsWrapper, ignoring
	 * case considerations.
	 * @see String#equalsIgnoreCase(String)
	 */
	public boolean equalsIgnoreCase(CharSequence cs) {
		if (cs == this) return true;
		if (cs == null || cs.length() != length()) return false;

		for (int i = 0; i < limit; i++) {
			char u1 = Character.toUpperCase(chars[offset + i]);
			char u2 = Character.toUpperCase(cs.charAt(i));
			if (u1 != u2) return false;
		}
		return true;
	}

	/**
	 * Compares this CharsWrapper to a CharSequence.
	 *
	 * @param cs the CharSequence to compare with this CharsWrapper
	 * @return true if cs isn't null and contains the same characters as this CharsWrapper
	 * @see String#contentEquals(CharSequence)
	 */
	public boolean contentEquals(CharSequence cs) {
		if (cs == this) return true;
		if (cs == null || cs.length() != length()) return false;

		for (int i = offset; i < limit; i++) {
			if (chars[i] != cs.charAt(i))
				return false;
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
		if (array == null || array.length != l) return false;

		for (int i = 0; i < l; i++) {
			if (chars[i + offset] != array[i])
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 1;
		for (int i = offset; i < limit; i++) {
			hashCode = 31 * hashCode + chars[offset + i];
		}
		return hashCode;
	}

	/**
	 * Creates and returns a copy of this CharsWrapper. The underlying char array is copied and used to
	 * create a new instance of CharsWrapper.
	 *
	 * @return a copy of this CharsWrapper
	 */
	@Override
	public CharsWrapper clone() {
		return new CharsWrapper(Arrays.copyOf(chars, chars.length));
	}

	@Override
	public String toString() {
		return new String(chars);
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
	 * Returns the index within this CharsWrapper of the first occurrence of the specified character.
	 * Returns -1 if this CharsWrapper doesn't contains the character.
	 *
	 * @param c the character to look for
	 * @return the index of the first occurence of {@code c}, or {@code -1} if not found.
	 */
	public int indexOf(char c) {
		for (int i = offset; i < limit; i++) {
			char ch = chars[i];
			if (ch == c) return i;
		}
		return -1;
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
				return chars[index++];
			}
		};
	}

	/**
	 * Builder class for constructing CharsWrappers.
	 */
	public static class Builder implements CharacterOutput {
		private char[] data;
		private int cursor = 0;

		/**
		 * Creates a new CharsWrapper's builder with the specified initial capacity.
		 *
		 * @param initialCapacity the initial capacity
		 */
		public Builder(int initialCapacity) {
			this.data = new char[initialCapacity];
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

		/**
		 * Appends a character to this builder.
		 *
		 * @param c the character to append
		 * @return this builder
		 */
		public Builder append(char c) {
			write(c);
			return this;
		}

		/**
		 * Appends a CharSequence to this builder.
		 *
		 * @param sequence the sequence to append
		 * @return this builder
		 */
		public Builder append(CharSequence sequence) {
			final int length = sequence.length();//caches the length for better performance.
			// The sequence must not change between this point and the end of the loop!
			ensureCapacity(cursor + length);
			for (int i = 0; i < length; i++) {
				data[cursor + i] = sequence.charAt(i);
			}
			cursor += length;
			return this;
		}

		/**
		 * Appends a char array to this builder.
		 *
		 * @param chars the array to append
		 * @return this builder
		 */
		public Builder append(char[] chars) {
			return append(chars, 0, chars.length);
		}

		/**
		 * Appends a portion of a char array to this builder.
		 *
		 * @param chars  the array to append
		 * @param start  the index to start at
		 * @param length the number of characters to append
		 * @return this builder
		 */
		public Builder append(char[] chars, int start, int length) {
			write(chars, start, length);
			return this;
		}

		/**
		 * Appends the string representation of an object to this builder. This is equivalent to {@code
		 * append(String.valueOf(o))}.
		 *
		 * @param o the object to append
		 * @return this builder
		 */
		public Builder append(Object o) {
			return append(String.valueOf(o));
		}

		/**
		 * Appends multiple objects to this builder. This is equivalent to calling {@code append(String
		 * .valueOf (o))} in a loop.
		 *
		 * @param objects the objects to append
		 * @return this builder
		 */
		public Builder append(Object... objects) {
			for (Object o : objects) {
				append(o);
			}
			return this;
		}

		public int length() {
			return cursor;
		}

		public char[] getChars() {
			return data;
		}

		/**
		 * Compacts this builder, minimizing its size in memory.
		 */
		public void compact() {
			if (cursor != data.length)
				data = Arrays.copyOf(data, cursor);
		}

		/**
		 * Builds a CharsWrapper with the content of this builder. The builder's content is directly used
		 * to create a new CharsWrapper.
		 *
		 * @return a new CharsWrapper with the content of this builder
		 */
		public CharsWrapper build() {
			return new CharsWrapper(data, 0, cursor);
		}

		/**
		 * Builds a CharsWrapper with <b>a copy of</b> the content of this builder.
		 *
		 * @return a new CharsWrapper with a copy of the content of this builder
		 */
		public CharsWrapper copyAndBuild() {
			return new CharsWrapper(Arrays.copyOfRange(data, 0, cursor));
		}

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated use {@link #build()} instead
		 */
		@Override
		@Deprecated
		public String toString() {
			return build().toString();
		}

		@Override
		public void write(char c) {
			ensureCapacity(cursor + 1);
			data[cursor] = c;
			cursor++;
		}

		@Override
		public void write(char[] chars, int offset, int length) {
			ensureCapacity(cursor + length);
			System.arraycopy(chars, offset, data, cursor, length);
			cursor += length;
		}

		@Override
		public void write(String s) {
			append(s);
		}
	}
}
