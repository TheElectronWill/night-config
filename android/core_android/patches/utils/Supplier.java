package com.electronwill.nightconfig.core.utils;

/** Supplier interface for android below api 24 */
public interface Supplier<T> {
	T get();
}
