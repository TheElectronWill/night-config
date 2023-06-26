package com.electronwill.nightconfig.core.conversion;


@FunctionalInterface
public interface DefaultValueFactory<T> {

	T defaultValue();
}
