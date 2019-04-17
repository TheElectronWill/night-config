package com.electronwill.nightconfig.core.utils;

/** Consumer interface for android below api24 */
public interface Consumer<T> {
	void accept(T value);
}
