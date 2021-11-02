package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.ListSupplier;
import com.electronwill.nightconfig.core.utils.MapSupplier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This class stores global NightConfig settings.
 */
public final class NightConfig {
	private NightConfig() {}

	private static volatile MapSupplier DEFAULT_MAP_SUPPLIER =
		isInsertionOrderPreserved() ? LinkedHashMap::new : HashMap::new;

	private static volatile ListSupplier DEFAULT_LIST_SUPPLIER = ArrayList::new;

	/**
	 * Checks if the newly created configs keep the insertion order of their content.
	 * By default, the insertion order is preserved.
	 *
	 * This can be controlled with the `nightconfig.ordered`
	 * system property or by calling {@link #setInsertionOrderPreserved(boolean)}.
	 * <p>
	 * This setting does not apply to configurations created from a Map, from another Config,
	 * or with a specific map supplier.
	 *
	 * @return true if the new configs preserve the insertion order of their values, false to
	 * give no guarantee about the values ordering.
	 */
	public static boolean isInsertionOrderPreserved() {
		String prop = System.getProperty("nightconfig.preserveInsertionOrder");
		return (prop == null) || prop.equals("true") || prop.equals("1");
	}

	/**
	 * Modifies the behavior of the new configurations with regards to the preservation of the
	 * order of config values.
	 * <p>
	 * This setting does not apply to configurations created from a Map, from another Config,
	 * or with a specific map supplier.
	 *
	 * @param orderPreserved true to make the new configs preserve the insertion order of their
	 *                       values, false to give no guarantee about the values ordering.
	 * @see #isInsertionOrderPreserved()
	 */
	public static void setInsertionOrderPreserved(boolean orderPreserved) {
		System.setProperty("nightconfig.preserveInsertionOrder", orderPreserved ? "true" : "false");
		DEFAULT_MAP_SUPPLIER = orderPreserved ? LinkedHashMap::new : HashMap::new;
	}

	/**
	 * Returns the default map supplier. This method is thread-safe.
	 *
	 * @return the default supplier
	 */
	public static MapSupplier getDefaultMapSupplier() {
		return DEFAULT_MAP_SUPPLIER;
	}

	/**
	 * Sets the default map supplier. This method is thread-safe.
	 *
	 * @param mapSupplier the new supplier
	 */
	public static void setDefaultMapSupplier(MapSupplier mapSupplier) {
		DEFAULT_MAP_SUPPLIER = mapSupplier;
	}

	/**
	 * Returns the default list supplier. This method is thread-safe.
	 *
	 * @return the default supplier
	 */
	public static ListSupplier getDefaultListSupplier() {
		return DEFAULT_LIST_SUPPLIER;
	}

	/**
	 * Sets the default list supplier. This method is thread-safe.
	 *
	 * @param listSupplier the new supplier
	 */
	public static void setDefaultListSupplier(ListSupplier listSupplier) {
		DEFAULT_LIST_SUPPLIER = listSupplier;
	}
}
