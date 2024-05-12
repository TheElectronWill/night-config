package com.electronwill.nightconfig.core.serde.annotations;

import java.lang.annotation.*;

/**
 * Sets the key to use when serializing/deserializing an element, instead of its
 * Java name.
 * <p>
 * Example:
 *
 * <pre>
 * <code>
 *class MyObject {
 *    {@code @SerdeKey("uuid")}
 *    String objectUniqueId;
 *
 *    String withoutAnnotation;
 *}
 * </code>
 * </pre>
 *
 * Serialization to json:
 *
 * <pre>
 * <code>
 *{
 *    "uuid" : "…",
 *    "withoutAnnotation": "…"
 *}
 * </code>
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerdeKey {
	String value();
}
