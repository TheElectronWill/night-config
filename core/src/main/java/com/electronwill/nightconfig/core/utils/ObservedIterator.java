package com.electronwill.nightconfig.core.utils;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author TheElectronWill
 */
public final class ObservedIterator<E> extends AbstractObserved implements Iterator<E> {
	private final Iterator<E> iterator;

	public ObservedIterator(Iterator<E> iterator, Runnable callback) {
		super(callback);
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public E next() {
		return iterator.next();
	}

	@Override
	public void remove() {
		iterator.remove();
		callback.run();
	}

	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		iterator.forEachRemaining(action);
	}

	@Override
	public boolean equals(Object obj) {
		return iterator.equals(obj);
	}

	@Override
	public int hashCode() {
		return iterator.hashCode();
	}
}