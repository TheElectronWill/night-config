package com.electronwill.nightconfig.core.utils;

@FunctionalInterface
public interface ExFunction<T, R, E extends Exception> {

	R apply(T t) throws E;
}
