package com.electronwill.nightconfig.core.utils;

/** Function interface for android below api24 */
public interface Function<T, R> {
	R apply(T t);
}