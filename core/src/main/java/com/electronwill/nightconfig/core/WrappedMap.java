package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.MapSupplier;

import java.util.Map;

/**
 * A {@link java.util.Map} wrapped in a {@link Config}.
 */
public final class WrappedMap extends AbstractConfig {
	private final Map<String, Object> values;

	public WrappedMap(Map<String, Object> values, MapSupplier mapSupplier) {
		super(mapSupplier);
		this.values = values;
	}

	@Override
	public AbstractConfig createSubConfig() {
		return new WrappedMap(mapSupplier.get(), mapSupplier);
	}

	@Override
	public AbstractConfig clone() {
		WrappedMap copy = new WrappedMap(mapSupplier.get(), mapSupplier);
		copy.putAll(this);
		return copy;
	}
}
