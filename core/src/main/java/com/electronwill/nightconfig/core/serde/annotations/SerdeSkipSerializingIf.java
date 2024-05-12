package com.electronwill.nightconfig.core.serde.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Don't serialize the annotated field if some condition is true.
 *
 * <h2>Example: skipping null fields</h2>
 *
 * <pre><code>
 * class MyObject {
 *     {@code @SerdeSkipSerializingIf(SkipSerIf.IS_NULL)}
 *     String name;
 * }
 * </code></pre>
 *
 * <h2>Example: skipping empty lists</h2>
 *
 * <pre><code>
 * class MyObject {
 *     {@code @SerdeSkipSerializingIf(SkipSerIf.IS_EMPTY)}
 *     {@code List<String>} servers;
 * }
 * </code></pre>
 *
 * <h2>Advanced example: skipping a field based on a custom condition</h2>
 *
 * <pre><code>
 * class MyObject {
 *     {@code @SerdeSkipSerializingIf(value = SkipSerIf.CUSTOM, customCheck="skipName")}
 *     String name;
 *
 *     private boolean skipName(String name) {
 *         return name == null || name.equals("skip me");
 *     }
 * }
 * </code></pre>
 *
 * <h2>Advanced example: skipping a field based on a custom condition defined
 * in another class</h2>
 *
 * <pre><code>
 * class MyObject {
 *     {@code @SerdeSkipSerializingIf(value = SkipSerIf.CUSTOM, customClass=SkipChecker.class, customCheck="skipName")}
 *     String name;
 *
 *     {@code @SerdeSkipSerializingIf(value = SkipSerIf.CUSTOM, customClass=SkipChecker.class, customCheck="skipId")}
 *     int id;
 * }
 *
 * class SkipChecker {
 *     // The predicate can be defined by a `Predicate` field too!
 *     static final {@code Predicate<String> skipName = name -> name == null || name.equals("skip me")};
 *
 *     // Note that skip predicates defined in another class must be static.
 *     static boolean skipId(int id) {
 *         return id == -1; // skip if id is -1
 *     }
 * }
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SerdeSkipSerializingIf {
	/**
	 * The type of skip predicate: either a predefined condition, or {@code CUSTOM}.
	 * <p>
	 * If set to {@code CUSTOM}, you must provide the {@link #customCheck()}
	 * parameter, and you may provide the {@link #customClass()} parameter.
	 */
	SkipSerIf[] value();

	/**
	 * The class where to find the custom skip predicate.
	 * <p>
	 * By default, it is set to {@code Object.class}, which is treated as a special
	 * value.
	 * It means that the actual class to use is the one of the object that is
	 * currently being (de)serialized.
	 *
	 * @see #customCheck()
	 * @return the class that defines the skip predicate
	 */
	Class<?> customClass() default Object.class;

	/**
	 * The name of the field or method that defines the predicate to apply
	 * in order to test whether the field that we are serializing should
	 * be skipped. The predicate is applied on the field's value.
	 *
	 * <h2>Constraints on methods</h2>
	 * The predicate method must take exactly one parameter of type {@code T}, where
	 * {@code T} is the type of the field to serialize.
	 * If {@link #customClass()} is set to its non-default value, the method must be
	 * static.
	 *
	 * <h2>Constraints on fields</h2>
	 * The predicate field must be of type {@code java.util.function.Predicate<T>},
	 * where {@code T} is the type of the field to serialize.
	 * In most cases, the predicate field should be declared with the
	 * {@code transient} keyword, to prevent it from being (de)serialized.
	 *
	 * @return the name of the skip predicate
	 * @see java.util.function.Predicate
	 */
	String customCheck() default "";

	public static enum SkipSerIf {
		/**
		 * Skip the field if it's null.
		 */
		IS_NULL,
		/**
		 * Skip the field if it's empty.
		 * <p>
		 * Determining whether an object is "empty" or not is done in a "logical" way
		 * for common Java objects.
		 * For instance, a {@code CharSequence} is empty is its {@code length()} is
		 * zero, a {@code Collection} is empty if calling {@code isEmpty()} returns
		 * true, etc.
		 *
		 * As a last-resort try to implement the "is empty" check, reflection is used to
		 * find and call the method {@code boolean isEmpty()} on the value.
		 */
		IS_EMPTY,
		/**
		 * Skip the field if its value satisfies a custom condition,
		 * defined by {@link SerdeSkipSerializingIf#customCheck()}.
		 */
		CUSTOM,
	}
}
