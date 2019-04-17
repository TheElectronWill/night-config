package com.electronwill.nightconfig.core.utils;

/** BiConsumer interface for android below api24 */
public interface BiConsumer<T, U> {
	void accept(T t, U u);
}
