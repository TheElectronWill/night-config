package com.electronwill.nightconfig.core.utils;

/** Consumer interface for android */
public interface Consumer<T> {
	void accept(T value);
}
