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

	/**
	 * Supports anything: supportsType(type) returns {@code true} for all type.
	 */
	public static final SupportStrategy STRATEGY_SUPPORT_ALL = type -> true;

	/**
	 * Supports the following types:
	 * <ul>
	 * <li>Integer, Long, Float and Double
	 * <li>Boolean
	 * <li>String
	 * <li>List and all its implementations
	 * <li>Config and all its implementations
	 * </ul>
	 */
	public static final SupportStrategy STRATEGY_SUPPORT_BASIC = new SimpleSupportStrategy(
			BASIC_TYPES, BASIC_EXTENSIBLE_TYPES);

	private final SupportStrategy supportStrategy;

	/**
	 * Creates a new SimpleConfig that uses the basic SupportStrategy.
	 * <p>
	 * It supports the following types:
	 * <ul>
	 * <li>Integer, Long, Float and Double
	 * <li>Boolean
	 * <li>String
	 * <li>List and all its implementations
	 * <li>Config and all its implementations
	 * </ul>
	 *
	 * @see SimpleConfig#STRATEGY_SUPPORT_BASIC
	 */
	public SimpleConfig() {
		this(STRATEGY_SUPPORT_BASIC);
	}

	/**
	 * Creates a new SimpleConfig that uses the specified SupportStrategy.
	 *
	 * @param supportStrategy the SupportStrategy that decides which classes of values are
	 *                        supported by the config
	 * @see SimpleConfig#STRATEGY_SUPPORT_BASIC
	 * @see SimpleConfig#STRATEGY_SUPPORT_ALL
	 */
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

	/**
	 * An implementation of SupportStrategy based on two sets: one for the types that are
	 * supported strictly, one for the types that are supported with their subtypes.
	 */
	public static final class SimpleSupportStrategy implements SupportStrategy {
		private final Set<Class<?>> supportedTypes, extensibleSupportedTypes;

		/**
		 * Creates a new SimpleSupportStrategy.
		 *
		 * @param supportedTypes           the set of the types that are stricly supported (their
		 *                                 subtypes aren't supported)
		 * @param extensibleSupportedTypes the set of the types that are supported with their
		 *                                 subtypes.
		 */
		public SimpleSupportStrategy(Set<Class<?>> supportedTypes,
									 Set<Class<?>> extensibleSupportedTypes) {
			this.supportedTypes = supportedTypes;
			this.extensibleSupportedTypes = extensibleSupportedTypes;
		}

		@Override
		public boolean supportsType(Class<?> type) {
			if (supportedTypes.contains(type)) { return true; }
			for (Class<?> supportedType : extensibleSupportedTypes) {
				if (supportedType.isAssignableFrom(type)) { return true; }
			}
			return false;
		}
	}

	/**
	 * Checks whether a type is supported (by the config) or not.
	 */
	@FunctionalInterface
	public static interface SupportStrategy {
		boolean supportsType(Class<?> type);
	}
}