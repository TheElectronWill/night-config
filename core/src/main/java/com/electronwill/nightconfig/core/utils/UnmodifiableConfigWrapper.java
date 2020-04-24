package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.AttributeType;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

import java.util.Map;
import java.util.Objects;

/**
 * @author TheElectronWill
 */
public abstract class UnmodifiableConfigWrapper<C extends UnmodifiableConfig>
		implements UnmodifiableConfig {
	protected final C config;

	protected UnmodifiableConfigWrapper(C config) {
		this.config = Objects.requireNonNull(config, "The wrapped config must not be null");
	}

	@Override
	public UnmodifiableConfig.Entry getEntry(String[] path) {
		return config.getEntry(path);
	}

	@Override
	public boolean has(AttributeType<?> attribute, String[] path) {
		return config.has(attribute, path);
	}

	@Override
	public boolean contains(String[] path) {
		return config.contains(path);
	}

	@Override
	public Map<String, Object> valueMap() {
		return config.valueMap();
	}

	@Override
	public Iterable<? extends Entry> entries() {
		return config.entries();
	}

	@Override
	public int size() {
		return config.size();
	}

	@Override
	public boolean isEmpty() {
		return config.isEmpty();
	}

	@Override
	public boolean equals(Object obj) {
		return config.equals(obj);
	}

	@Override
	public int hashCode() {
		return config.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '(' + config + ')';
	}
}
