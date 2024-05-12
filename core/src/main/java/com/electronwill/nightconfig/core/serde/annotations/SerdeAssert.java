package com.electronwill.nightconfig.core.serde.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Throw an exception if the annotated field does not match the given condition.
 *
 * <h2>Example: rejecting null fields</h2>
 *
 * <pre><code>
 * import com.electronwill.nightconfig.core.serde.annotations.SerdeAssert;
 * import com.electronwill.nightconfig.core.serde.annotations.SerdeAssert.AssertThat;
 *
 * class MyObject {
 * 		{@code @SerdeAssert(AssertThat.NOT_NULL)}
 * 		String nonNull;
 * }
 * </code></pre>
 *
 * <h2>When is the assertion applied?</h2>
 * Assertions defined with {@code @SerdeAssert} are always applied on the
 * value of the java field. If the field's value is replaced by a default
 * value, the assertions are applied on the default value.
 *
 * <ul>
 * <li>When deserializing: after the deserialization of the value from the
 * config, after the resolution of the default value (if any).</li>
 * <li>When serializing: before the serialization of the value to the
 * config, after the resolution of the default value (if any).</li>
 * </ul>
 */
@Repeatable(SerdeAssertsContainer.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerdeAssert {
	/**
	 * One or multiple assertions to check.
	 * <p>
	 * If multiple assertions are specified, they are combined with a logical AND,
	 * that is,
	 * they must all be true for the assertion to succeed.
	 * <p>
	 * If set to {@code CUSTOM}, you must provide the {@link #customCheck()}
	 * parameter,
	 * and you may provide the {@link #customClass()} parameter.
	 */
	AssertThat[] value();

	/**
	 * The class where to find the custom assertion predicate.
	 * <p>
	 * By default, it is set to {@code Object.class}, which is treated as a special
	 * value.
	 * It means that the actual class to use is the one of the object that is
	 * currently being (de)serialized.
	 *
	 * @see #customCheck()
	 * @return the class that defines the assertion predicate
	 */
	Class<?> customClass()

	default Object.class;

	/**
	 * The name of the field or method that defines the predicate to apply
	 * in order to test whether the field that we are (de)serializing
	 * satisfies a condition. The predicate is applied on the field's value.
	 *
	 * <h2>Constraints on methods</h2>
	 * The predicate method must take exactly one parameter of type {@code T}, where
	 * {@code T} is the type of the field to (de)serialize.
	 * If {@link #customClass()} is set to its non-default value, the method must be
	 * static.
	 *
	 * <h2>Constraints on fields</h2>
	 * The predicate field must be of type {@code java.util.function.Predicate<T>},
	 * where {@code T} is the type of the field to (de)serialize.
	 * In most cases, the predicate field should be declared with the
	 * {@code transient} keyword, to prevent it from being (de)serialized.
	 *
	 * @return the name of the assertion predicate
	 * @see java.util.function.Predicate
	 */
	String customCheck()

	default "";

	/**
	 * Controls whether the assert value applies when Serializing and/or
	 * Deserializing.
	 *
	 * @return the phase during which the assert condition should be checked
	 */
	SerdePhase phase() default SerdePhase.BOTH;

	public static enum AssertThat {
		/**
		 * Throw an exception if the field is null.
		 */
		NOT_NULL,
		/**
		 * Throw an exception if the field is empty.
		 * <p>
		 * Determining whether an object is "empty" or not is done in a "logical" way
		 * for common Java objects.
		 *
		 * For instance, a {@code CharSequence} is empty is its {@code length()} is
		 * zero, a {@code Collection} is empty if calling {@code isEmpty()} returns
		 * true, etc.
		 *
		 * As a last-resort try to implement the "is empty" check, reflection is used to
		 * find and call the method {@code boolean isEmpty()} on the value.
		 */
		NOT_EMPTY,
		/**
		 * Throw an exception if the field's value does not satisfy a custom condition,
		 * defined by {@link SerdeAssert#customCheck()}.
		 */
		CUSTOM,
	}
}
