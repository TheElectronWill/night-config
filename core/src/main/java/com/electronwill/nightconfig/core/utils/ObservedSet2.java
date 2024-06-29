package com.electronwill.nightconfig.core.utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
public final class ObservedSet2<K, KImpl extends K> extends AbstractObserved implements Set<K> {
	private final Set<KImpl> set;
	private final Function<K, KImpl> kToImpl;

	public ObservedSet2(Runnable callback, Set<KImpl> set, Function<K, KImpl> kToImpl) {
		super(callback);
		this.set = set;
		this.kToImpl = kToImpl;
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<K> iterator() {
		return new TransformingIterator<>(set.iterator(), k -> k);
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	@Override
	public boolean add(K k) {
		boolean result = set.add(kToImpl.apply(k));
		callback.run();
		return result;
	}

	@Override
	public boolean remove(Object o) {
		boolean result = set.remove(o);
		callback.run();
		return result;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends K> c) {
		List<KImpl> kImpls = new ArrayList<>(c.size());
		for (K k : c) {
			kImpls.add(kToImpl.apply(k));
		}
		boolean result = set.addAll(kImpls);
		callback.run();
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = set.retainAll(c);
		callback.run();
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = set.removeAll(c);
		callback.run();
		return result;
	}

	@Override
	public void clear() {
		set.clear();
		callback.run();
	}

	@Override
	public boolean removeIf(Predicate<? super K> filter) {
		boolean removed = set.removeIf(filter);
		if (removed) { callback.run(); }
		return removed;
	}

	@Override
	public boolean equals(Object obj) {
		return set.equals(obj);
	}

	@Override
	public int hashCode() {
		return set.hashCode();
	}
}