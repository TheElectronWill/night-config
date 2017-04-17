package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.Config;
import java.util.List;
import java.util.Set;

/**
 * @author TheElectronWill
 */
public abstract class ConfigWrapper<C extends Config> extends UnmodifiableConfigWrapper<C>
		implements Config {

	protected ConfigWrapper(C config) {
		super(config);
	}

	@Override
	public Set<? extends Config.Entry> entrySet() {
		return config.entrySet();
	}

	@Override
	public <T> T setValue(List<String> path, Object value) {
		return config.setValue(path, value);
	}

	@Override
	public <T> T removeValue(List<String> path) {
		return config.removeValue(path);
	}

	@Override
	public void clear() {
		config.clear();
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return config.supportsType(type);
	}
}
