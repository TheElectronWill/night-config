package com.electronwill.nightconfig.core;

import java.util.HashSet;
import java.util.List;

/**
 * A SimpleConfig is a MapConfig that supports only the following "basic" types: int, long, float, double,
 * boolean, String, List, Config.
 *
 * @author TheElectronWill
 */
public class SimpleConfig extends MapConfig {

	private static final HashSet<Class<?>> SUPPORTED_TYPES = new HashSet<>();

	static {
		SUPPORTED_TYPES.add(int.class);
		SUPPORTED_TYPES.add(Integer.class);
		SUPPORTED_TYPES.add(long.class);
		SUPPORTED_TYPES.add(Long.class);
		SUPPORTED_TYPES.add(float.class);
		SUPPORTED_TYPES.add(Float.class);
		SUPPORTED_TYPES.add(double.class);
		SUPPORTED_TYPES.add(Double.class);
		SUPPORTED_TYPES.add(boolean.class);
		SUPPORTED_TYPES.add(Boolean.class);
		SUPPORTED_TYPES.add(String.class);
		SUPPORTED_TYPES.add(List.class);
		SUPPORTED_TYPES.add(Config.class);
	}

	/**
	 * Returns {@code true} if and only if {@code type} is the class of: int, long, float, double, boolean,
	 * (or a wrapper of these primitive types), List, Config (or an implementation of these interfaces).
	 */
	@Override
	public boolean supportsType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type)
				|| List.class.isAssignableFrom(type)
				|| Config.class.isAssignableFrom(type);
	}
}
