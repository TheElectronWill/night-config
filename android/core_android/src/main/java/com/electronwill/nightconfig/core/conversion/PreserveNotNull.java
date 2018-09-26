package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * On a field: indicates that, when converting a config to a java object, the value of the field
 * must not be overriden by a null value from the config. Only non-null config values can replace
 * the field's value.
 * <p>
 * On a class declaration: applies the effect of this annotation to all the fields.
 *
 * @author TheElectronWill
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface PreserveNotNull {}