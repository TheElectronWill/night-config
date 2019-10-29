package com.electronwill.nightconfig.core;

public final class StandardAttributes {
	private StandardAttributes() {}

	/** Entry's value */
	public static final AttributeType<Object> VALUE = () -> "value";

	/** Entry's comment */
	public static final AttributeType<String> COMMENT = () -> "comment";

	/** Number's base/radix, for instance 16 will save the value in hexadecimal (if possible) */
	public static final AttributeType<Integer> NUMBER_BASE = () -> "numberBase";

	/**
	 * Forces a config entry to be serialized as multiple lines. For instance, if applied to
	 * a list, forces each element to be written on a separate line (if supported by the format).
	 * <p>
	 * If neither {@code FORCE_MULTILINE} nor {@link #FORCE_SINGLELINE} is specified, the
	 * {@code ConfigWriter} is free to decide.
	 */
	public static final AttributeType<Boolean> FORCE_MULTILINE = () -> "forceMultiline";

	/**
	 * Forces a config entry to be serialized as a single line. This is the opposite of
	 * {@link #FORCE_MULTILINE}.
	 * <p>
	 * If neither {@link #FORCE_MULTILINE} nor {@code FORCE_SINGLELINE} is specified, the
	 * {@code ConfigWriter} is free to decide.
	 */
	public static final AttributeType<Boolean> FORCE_SINGLELINE = () -> "forceMultiline";
}