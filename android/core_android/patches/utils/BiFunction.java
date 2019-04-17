package com.electronwill.nightconfig.core.utils;

/** BiFunction interface for android below api24 */
public interface BiFunction<T, U, R> {
	R apply(T t, U u);
}
