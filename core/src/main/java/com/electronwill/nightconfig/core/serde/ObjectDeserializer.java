package com.electronwill.nightconfig.core.serde;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * Deserializes configs ({@link UnmodifiableConfig}, {@link Config}, etc.) to Java objects.
 *
 * <h2>Example</h2>
 *
 * Given a class like this:
 * <pre><code>
 * class Position {
 *     private final int x, y, z;
 *
 *     public Position(int x, int y, int z) {
 *         this.x=x; this.y=y; this.z=z;
 *     }
 * }
 * </code></pre>
 *
 * And a configuration like this:
 * <pre><code>
 * Config conf = Config.inMemory();
 * conf.set("x", 12);
 * conf.set("y", -20);
 * conf.set("z", 42);
 * </code></pre>
 *
 * You can deserialize the Config to an instance of Position with:
 * <pre><code>
 * Position deserialized = new ObjectDeserializer.standard().deserializeFields(conf, Position::new);
 * // result: Position(12, -20, 42)
 * </code></pre>
 *
 * <p>
 * Use {@link #builder()} or {@link #blankBuilder()} to precisely configure
 * the deserialization process.
 */
public final class ObjectDeserializer extends AbstractObjectDeserializer {
	// This is the Java 8 version of ObjectDeserializer.
	// Remember to make this version in sync with the version for Java 17+ (methods and comments).

	/**
	 * Creates a new {@link ObjectDeserializerBuilder} with some standard deserializers already registered.
	 *
	 * @return a new builder
	 */
	public static ObjectDeserializerBuilder builder() {
		return new ObjectDeserializerBuilder(true);
	}

	/**
	 * Creates a new {@link ObjectDeserializerBuilder} without the standard deserializers already registered.
	 *
	 * @return a new builder
	 */
	public static ObjectDeserializerBuilder blankBuilder() {
		return new ObjectDeserializerBuilder(false);
	}

	/**
	 * Creates a new {@link ObjectDeserializer} with the standard deserializers.
	 * <p>
	 * This is equivalent to {@code ObjectDeserializer.builder().build()}.
	 */
	public static ObjectDeserializer standard() {
		return builder().build();
	}

	ObjectDeserializer(ObjectDeserializerBuilder builder) {
		super(builder);
	}

	// NOTE: it would make no sense to provide a method deserialize(Object) -> Object, because
	// the trivial deserialization can always be applied when there is no constraint on the result.
	// ObjectSerializer.serialize does exist, however, because there is a constraint on the type of
	// values that the configuration can contain, through its ConfigFormat.

	/**
	 * Deserializes a value into an instance of the collection {@code C<V>}.
	 *
	 * @param <C>             type of the collection
	 * @param <V>             type of the values in the collection
	 * @param configValue     config value to deserialize
	 * @param collectionClass class of the collection
	 * @param valueClass      class of the values in the collection
	 * @return the deserialized collection
	 */
	public <C extends Collection<V>, V> C deserializeToCollection(Object configValue,
			Class<C> collectionClass, Class<V> valueClass) {
		return super.deserializeToCollection(configValue, collectionClass, valueClass);
	}

	/**
	 * Deserializes a value into an instance of the map {@code M<String, V>}.
	 *
	 * @param <M>         type of the map
	 * @param <V>         type of the values in the map
	 * @param configValue config value to deserialize
	 * @param mapClass    class of the map
	 * @param valueClass  class of the values in the collection
	 * @return the deserialized map
	 */
	public <M extends Map<String, V>, V> M deserializeToMap(Object configValue, Class<M> mapClass,
			Class<V> valueClass) {
		return super.deserializeToMap(configValue, mapClass, valueClass);
	}

	/**
	 * Deserializes a {@code Config} as an object by transforming the config entries into fields.
	 * The fields of the {@code destination} are modified through reflection.
	 *
	 * @param source      config to deserialize
	 * @param destination object to store the result in
	 */
	public void deserializeFields(UnmodifiableConfig source, Object destination) {
		super.deserializeFields(source, destination);
	}

	/**
	 * Deserializes a {@code Config} as an object of type {@code R} by transforming the config entries
	 * into fields. A new instance of the object is created, and its fields are modified through reflection.
	 *
	 * @param <R>                 type of the resulting object
	 * @param source              config to deserialize
	 * @param destinationSupplier supplier of the resulting object
	 * @return the deserialized object
	 */
	public <R> R deserializeFields(UnmodifiableConfig source,
			Supplier<? extends R> destinationSupplier) {
		return super.deserializeFields(source, destinationSupplier);
	}
}
