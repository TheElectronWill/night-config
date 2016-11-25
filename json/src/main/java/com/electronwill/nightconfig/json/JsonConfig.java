package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import java.util.HashSet;
import java.util.List;

/**
 * @author TheElectronWill
 */
public class JsonConfig extends MapConfig {

	private static final HashSet<Class<?>> SUPPORTED_TYPES = new HashSet<>();
	static {
		SUPPORTED_TYPES.add(int.class);
		SUPPORTED_TYPES.add(Integer.class);
		SUPPORTED_TYPES.add(long.class);
		SUPPORTED_TYPES.add(Long.class);
		SUPPORTED_TYPES.add(double.class);
		SUPPORTED_TYPES.add(Double.class);
		SUPPORTED_TYPES.add(boolean.class);
		SUPPORTED_TYPES.add(Boolean.class);
		SUPPORTED_TYPES.add(String.class);
		SUPPORTED_TYPES.add(List.class);
		SUPPORTED_TYPES.add(Config.class);
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type);
	}

}
