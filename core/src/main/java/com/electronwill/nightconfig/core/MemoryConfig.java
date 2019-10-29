package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.MapSupplier;

import java.util.HashMap;

/**
 * Basic in-memory configuration.
 */
public final class MemoryConfig extends AbstractConfig {

	public MemoryConfig() {
		this(NightConfig.getDefaultMapSupplier());
	}

	public MemoryConfig(MapSupplier mapSupplier) {
		super(mapSupplier);
	}

	/**
	 * Creates a SimpleConfig by copying a config.
	 */
	public MemoryConfig(UnmodifiableConfig config) {
		this(config, getMapSupplier(config));
	}

	/**
	 * Creates a SimpleConfig by copying a config.
	 */
	MemoryConfig(UnmodifiableConfig config, MapSupplier mapSupplier) {
		super(mapSupplier);
		putAll(config);
	}

	@Override
	public MemoryConfig createSubConfig() {
		return new MemoryConfig(mapSupplier);
	}

	@Override
	public MemoryConfig clone() {
		return new MemoryConfig(this, mapSupplier);
	}

	private static MapSupplier getMapSupplier(UnmodifiableConfig config) {
		if (config instanceof AbstractConfig) {
			return ((AbstractConfig)config).mapSupplier;
		}
		return HashMap::new;
	}
}