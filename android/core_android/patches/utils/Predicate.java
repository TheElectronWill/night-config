package com.electronwill.nightconfig.core.utils;

/** Predicate interface for android below api24*/
public interface Predicate<T> {
	boolean test(T t);
}
