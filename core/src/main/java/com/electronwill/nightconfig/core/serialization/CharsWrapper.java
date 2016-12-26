package com.electronwill.nightconfig.core.serialization;

import java.util.Arrays;

/**
 * A simple, efficient implementation of CharSequence. To avoid data copying, its constructor doesn't
 * perfom any copy of the char array.
 *
 * @author TheElectronWill
 */
public final class CharsWrapper implements CharSequence, Cloneable {
	private final char[] chars;

	/**
	 * Creates a new CharsWrapper backed by the given char array. Any modification to the array is
	 * reflected to the CharsWrapper and vice-versa.
	 *
	 * @param chars the char array to use
	 */
	public CharsWrapper(char[] chars) {
		this.chars = chars;
	}

	@Override
	public int length() {
		return chars.length;
	}

	@Override
	public char charAt(int index) {
		return chars[index];
	}

	@Override
	public CharsWrapper subSequence(int start, int end) {
		return new CharsWrapper(Arrays.copyOfRange(chars, start, end));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj instanceof CharsWrapper) {
			CharsWrapper wrapper = (CharsWrapper)obj;
			return Arrays.equals(wrapper.chars, chars);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(chars);
	}

	@Override
	protected CharsWrapper clone() {
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
		String a;
		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			if (ch == c)
				return i;
		}
		return -1;
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

		/**
		 * Builds a CharsWrapper with the content of this builder. The content is NOT copied but directly
		 * used as it is.
		 *
		 * @return a new CharsWrapper with the content of this builder
		 */
		public CharsWrapper build() {
			return new CharsWrapper(data);
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
