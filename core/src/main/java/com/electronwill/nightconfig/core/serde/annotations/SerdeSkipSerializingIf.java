package com.electronwill.nightconfig.core.serde.annotations;

/**
 * todo: document
 */
public @interface SerdeSkipSerializingIf {
	SkipIf value();

	Class<?> customClass() default Object.class;

	String customCheck() default "";

	public static enum SkipIf {
		IS_NULL,
		IS_EMPTY,
		CUSTOM,
	}
}
