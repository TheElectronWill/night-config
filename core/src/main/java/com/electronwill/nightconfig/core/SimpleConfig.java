package com.electronwill.nightconfig.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple configuration that allows the user to specify a support strategy.
 */
public final class SimpleConfig extends MapConfig {

	private static final Set<Class<?>> BASIC_TYPES = new HashSet<>();
	private static final Set<Class<?>> BASIC_EXTENSIBLE_TYPES = new HashSet<>();

	static {
		BASIC_TYPES.add(Integer.class);
		BASIC_TYPES.add(Long.class);
		BASIC_TYPES.add(Float.class);
		BASIC_TYPES.add(Double.class);
		BASIC_TYPES.add(Boolean.class);
		BASIC_TYPES.add(String.class);
		BASIC_TYPES.add(List.class);
		BASIC_TYPES.add(Config.class);

		BASIC_EXTENSIBLE_TYPES.add(List.class);
		BASIC_EXTENSIBLE_TYPES.add(Config.class);
	}

	public static final SupportStrategy STRATEGY_SUPPORT_ALL = type -> true;
	public static final SupportStrategy STRATEGY_SUPPORT_BASIC = new SimpleSupportStrategy(BASIC_TYPES, BASIC_EXTENSIBLE_TYPES);

	private final SupportStrategy supportStrategy;

	public SimpleConfig() {
		this(STRATEGY_SUPPORT_BASIC);
	}

	public SimpleConfig(SupportStrategy supportStrategy) {
		this.supportStrategy = supportStrategy;
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return supportStrategy.supportsType(type);
	}

	@Override
	protected SimpleConfig createSubConfig() {
		return new SimpleConfig(supportStrategy);
	}

	public static final class SimpleSupportStrategy implements SupportStrategy {
		private final Set<Class<?>> supportedTypes, extensibleSupportedTypes;

		public SimpleSupportStrategy(Set<Class<?>> supportedTypes, Set<Class<?>> extensibleSupportedTypes) {
			this.supportedTypes = supportedTypes;
			this.extensibleSupportedTypes = extensibleSupportedTypes;
		}

		@Override
		public boolean supportsType(Class<?> type) {
			if (supportedTypes.contains(type))
				return true;
			for (Class<?> supportedType : extensibleSupportedTypes) {
				if (supportedType.isAssignableFrom(type))
					return true;
			}
			return false;
		}
	}

	@FunctionalInterface
	public static interface SupportStrategy {
		boolean supportsType(Class<?> type);
	}
}
