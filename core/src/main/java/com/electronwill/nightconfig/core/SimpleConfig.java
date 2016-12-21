package com.electronwill.nightconfig.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	private final SupportStrategy supportStrategy;

	public SimpleConfig() {
		this(new SimpleSupportStrategy(BASIC_TYPES, BASIC_EXTENSIBLE_TYPES));
	}

	public SimpleConfig(SupportStrategy supportStrategy) {
		this.supportStrategy = supportStrategy;
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return supportStrategy.supportsType(type);
	}

	@Override
	public SimpleConfig createEmptyConfig() {
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

	public static final class SupportEverythingStrategy implements SupportStrategy {
		@Override
		public boolean supportsType(Class<?> type) {
			return true;
		}
	}

	@FunctionalInterface
	public static interface SupportStrategy {
		boolean supportsType(Class<?> type);
	}
}
