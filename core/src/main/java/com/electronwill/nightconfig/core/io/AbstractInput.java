package com.electronwill.nightconfig.core.io;

/**
 * Abstract base class for CharacterInputs.
 *
 * @author TheElectronWill
 */
public abstract class AbstractInput implements CharacterInput {
	protected static final int EOS = -1;

	protected final IntDeque deque = new IntDeque();

	/**
	 * Tries to parse the next character without taking care of the peek deque.
	 *
	 * @return the next character, or -1 if the EOS has been reached
	 */
	protected abstract int directRead();

	/**
	 * Tries to parse the next character without taking care of the peek deque.
	 *
	 * @return the next character
	 *
	 * @throws ParsingException if the EOS has been reached
	 */
	protected abstract char directReadChar();

	@Override
	public int read() {
		if (!deque.isEmpty()) {
			return deque.removeFirst();
		}
		return directRead();
	}

	@Override
	public char readChar() {
		if (!deque.isEmpty()) {
			int next = deque.removeFirst();
			if (next == EOS) {
				throw ParsingException.notEnoughData();
			}
			return (char)next;
		}
		return directReadChar();
	}

	@Override
	public int peek() {
		if (deque.isEmpty()) {
			int read = directRead();
			deque.addLast(read);
			return read;
		}
		return deque.getFirst();
	}

	@Override
	public int peek(int n) {
		final int diff = n - deque.size();
		if (diff >= 0) {
			for (int i = 0; i <= diff; i++) {
				int read = directRead();
				deque.addLast(read);
				if (read == EOS) {
					return EOS;//it's useless to continue reading of the EOS has been reached
				}
			}
		}
		return deque.get(n);
	}

	@Override
	public char peekChar() {
		int c = peek();
		if (c == EOS) {
			throw ParsingException.notEnoughData();
		}
		return (char)c;
	}

	@Override
	public char peekChar(int n) {
		int c = peek(n);
		if (c == EOS) {
			throw ParsingException.notEnoughData();
		}
		return (char)c;
	}

	@Override
	public void skipPeeks() {
		deque.clear();
	}

	@Override
	public void pushBack(char c) {
		deque.addFirst(c);
	}

	@Override
	public CharsWrapper readUntil(char[] stop) {
		CharsWrapper.Builder builder = new CharsWrapper.Builder(10);
		int c = read();
		while (c != -1 && !Utils.arrayContains(stop, (char)c)) {
			builder.append((char)c);
			c = read();
		}
		deque.addFirst(c);//remember this char for later
		return builder.build();
	}

	@Override
	public CharsWrapper readCharsUntil(char[] stop) {
		CharsWrapper.Builder builder = new CharsWrapper.Builder(10);
		char c = readChar();
		while (!Utils.arrayContains(stop, c)) {
			builder.append(c);
			c = readChar();
		}
		deque.addFirst(c);//remember this char for later
		return builder.build();
	}
}