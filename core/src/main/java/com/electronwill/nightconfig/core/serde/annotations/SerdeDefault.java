package com.electronwill.nightconfig.core.serde.annotations;

import java.lang.annotation.*;

import com.electronwill.nightconfig.core.Config;

/**
 * Defines a default value to use when serializing, deserializing, or both.
 * <p>
 * By default, the default value is used when the field to deserialize has
 * no corresponding entry in the {@link Config}.
 *
 * <h2>Example: providing the default value with a method</h2>
 *
 * <pre>
 * <code>
 * class MyObject {
 *     // When deserializing, if the config entry "servers" is missing, the
 *     // field will be assigned to the default value, as returned by defaultServers().
 *     // If the config value is null, the field will be assigned to null.
 *     {@code @SerdeDefault(provider = "defaultServers")}
 *     {@code List<String>} servers;
 *
 *     {@code List<String>} defaultServers() {
 *         return Arrays.asList("example.org");
 *     }
 * }
 * </code>
 * </pre>
 *
 * <h2>Example: providing the default value with a field</h2>
 *
 * <pre>
 * <code>
 * class MyObject {
 *     // When deserializing, if the config entry "servers" is missing, the
 *     // field will be assigned to the default value, as returned by defaultServersSupplier.get().
 *     // If the config value is null, the field will be assigned to null.
 *     {@code @SerdeDefault(provider = "defaultServersSupplier")}
 *     {@code List<String>} servers;
 *
 *     // Note the "transient" annotation, which excludes this field from (de)serialization.
 *     private transient {@code Supplier<List<String>> defaultServersSupplier = () -> Arrays.asList("example.org")};
 * }
 * </code>
 * </pre>
 *
 * <h2>Advanced example: separing serialization and deserialization</h2>
 *
 * <pre>
 * <code>
 * import java.util.Arrays;
 * import java.util.Collections;
 * import static com.electronwill.nightconfig.core.serde.annotations.SerdeDefault.WhenValue.*;
 * import static com.electronwill.nightconfig.core.serde.annotations.SerdeDefault.SerdePhase.*;
 *
 * class MyObject {
 *     // When deserializing, if the config entry "servers" is missing, null or empty,
 *     // the field will be assigned to the value returned by
 *     // DefaultProviders.defaultServersWhenDeserializing().
 *     //
 *     // When serializing, if the object's field is null, the config entry will be set to the
 *     // value returned by DefaultsProviders.defaultServersWhenSerializing().
 *     {@code @SerdeDefault}(cls = DefaultProviders.class, provider = "defaultServersWhenDeserializing", phase = DESERIALIZING, whenValue = {IS_MISSING, IS_NULL, IS_EMPTY})
 *     {@code @SerdeDefault}(cls = DefaultProviders.class, provider = "defaultServersWhenSerializing", phase = SERIALIZING, whenValue = {IS_NULL})
 *     {@code List<String>} servers;
 * }
 *
 * class DefaultProviders {
 *     // Here, the default provider is not in the class of the object to deserialize,
 *     // therefore it needs to be static.
 *     static {@code List<String>} defaultServersWhenDeserializing() {
 *         return Arrays.asList("example.org");
 *     }
 *
 *     static {@code List<String>} defaultServersWhenSerializing() {
 *         return Collections.emptyList();
 *     }
 * }
 * </code>
 * </pre>
 */
@Repeatable(SerdeDefaultsContainer.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerdeDefault {
	/**
	 * The class that defines the method that provides the default value.
	 * <p>
	 * By default, it is set to {@code Object.class}, which is treated as a special
	 * value.
	 * It means that the actual class to use is the one of the object that is
	 * currently being (de)serialized.
	 *
	 * @return the class that defines the default value provider
	 */
	Class<?> cls() default Object.class;
	// we can't use null here, so we use Object.class to signal that the actual
	// class is the one that we're (de)serializing

	/**
	 * The name of the method or field that provides the default value.
	 *
	 * <h2>Constraints on methods</h2>
	 * The provider method must take no parameter.
	 * If {@link #cls()} is set to its non-default value, the method must be static.
	 *
	 * <h2>Constraints on fields</h2>
	 * The provider field must be of type {@code java.util.function.Supplier<T>}
	 * where {@code T} is the type of the field to (de)serialize.
	 * In most cases, the provider field should be declared with the
	 * {@code transient}
	 * keyword, to prevent it from being (de)serialized.
	 *
	 * @return the name of the default value provider
	 * @see java.util.function.Supplier
	 */
	String provider();

	/**
	 * Controls whether the default value applies when Serializing and/or
	 * Deserializing.
	 *
	 * @return the phase during which the default value should be applied
	 */
	SerdePhase phase() default SerdePhase.BOTH;

	/**
	 * Controls when to use the default value, based on the actual value.
	 * <p>
	 * When serializing, the "actual value" is the value of the field.
	 * When deserializing, the "actual value" is the value of the config entry.
	 *
	 * @return when to use the default value
	 */
	WhenValue[] whenValue() default { WhenValue.IS_MISSING };

	/**
	 * A condition that defines when to use the default value during (de)serialization.
	 * <p>
	 * The default value is used if the condition is true.
	 */
	public static enum WhenValue {
		/**
		 * When deserializing a field, call the default value provider if the value is
		 * missing from the config.
		 * <p>
		 * This does nothing when serializing, because a field cannot be missing.
		 */
		IS_MISSING,
		/**
		 * When deserializing a field, set it to its default value if the value from the
		 * config is null.
		 * <p>
		 * When serializing a field, serialize the default value instead of the field's
		 * value if the latter is null.
		 */
		IS_NULL,
		/**
		 * When deserializing a field, set it to its default value if the value from the
		 * config is empty.
		 * <p>
		 * When serializing a field, serialize the default value instead of the field's
		 * value if the latter is empty.
		 * <p>
		 * Determining whether an object is "empty" or not is done in a "logical" way
		 * for common Java objects.
		 * For instance, a {@code CharSequence} is empty is its {@code length()} is
		 * zero, a {@code Collection} is empty if calling {@code isEmpty()} returns
		 * true, etc.
		 * As a last-resort try to implement the "is empty" check, reflection is used to
		 * find and call the method {@code boolean isEmpty()} on the value.
		 */
		IS_EMPTY,
	}
}
