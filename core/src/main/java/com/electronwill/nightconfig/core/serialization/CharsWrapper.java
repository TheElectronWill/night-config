package com.electronwill.nightconfig.core.serialization;

import java.util.Arrays;

/**
 * @author TheElectronWill
 */
public final class CharsWrapper implements CharSequence, Cloneable {
	private final char[] chars;

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

	public boolean contains(char c) {
		return indexOf(c) != -1;
	}

	public int indexOf(char c) {
		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			if (ch == c)
				return i;
		}
		return -1;
	}

	char[] getChars() {
		return chars;
	}

	public static class Builder implements CharacterOutput {
		private char[] data;
		private int cursor = 0;

		public Builder(int initialCapacity) {
			this.data = new char[initialCapacity];
		}

		private void ensureCapacity(int capacity) {
			if (data.length < capacity) {
				int newCapacity = Math.max(capacity, data.length * 2);
				data = Arrays.copyOf(data, newCapacity);
			}
		}

		public Builder append(char c) {
			ensureCapacity(cursor + 1);
			data[cursor] = c;
			cursor++;
			return this;
		}

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

		public Builder append(char[] chars) {
			return append(chars, 0, chars.length);
		}

		public Builder append(char[] chars, int start, int length) {
			ensureCapacity(cursor + length);
			System.arraycopy(chars, start, data, cursor, length);
			cursor += length;
			return this;
		}

		public Builder append(Object o) {
			return append(String.valueOf(o));
		}

		public Builder append(Object... objects) {
			for (Object o : objects) {
				append(o);
			}
			return this;
		}

		public CharsWrapper build() {
			return new CharsWrapper(data);
		}

		@Override
		@Deprecated
		public String toString() {
			return build().toString();
		}

		@Override
		public void write(char c) {
			append(c);
		}

		@Override
		public void write(char[] chars) {
			append(chars);
		}

		@Override
		public void write(String s) {
			append(s);
		}
	}
}
