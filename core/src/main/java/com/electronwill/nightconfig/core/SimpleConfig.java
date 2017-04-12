package com.electronwill.nightconfig.core;

import java.util.List;
import java.util.function.Predicate;

/**
 * A simple configuration that allows the user to specify which types of value it supports.
 */
public final class SimpleConfig extends AbstractConfig {
	public static final Predicate<Class<?>> BASIC_SUPPORT_PREDICATE = type -> type.isPrimitive()
																		  || type == Integer.class
																		  || type == Long.class
																		  || type == Float.class
																		  || type == Double.class
																		  || type == Boolean.class
																		  || type == String.class
																		  || List.class.isAssignableFrom(type)
																		  || Config.class.isAssignableFrom(type);

	private final Predicate<Class<?>> supportPredicate;

	/**
	 * Creates a new SimpleConfig that supports the following types:
	 * <ul>
	 * <li>Integer, Long, Float and Double
	 * <li>Boolean
	 * <li>String
	 * <li>List and all its implementations
	 * <li>Config and all its implementations
	 * </ul>
	 */
	public SimpleConfig() {
		this.supportPredicate = BASIC_SUPPORT_PREDICATE;
	}

	/**
	 * Creates a new SimpleConfig that uses the specified Predicate to determines which types it
	 * supports.
	 *
	 * @param supportPredicate the Predicate that returns true when the class it's given is
	 *                         supported by the config
	 */
	public SimpleConfig(Predicate<Class<?>> supportPredicate) {
		this.supportPredicate = supportPredicate;
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return supportPredicate.test(type);
	}

	@Override
	protected SimpleConfig createSubConfig() {
		return new SimpleConfig(supportPredicate);
	}
}