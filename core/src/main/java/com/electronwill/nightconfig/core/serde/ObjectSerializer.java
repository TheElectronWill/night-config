package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.AdvancedPath;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.utils.StringUtils;

/**
 * Implements Object to Config serialization.
 */
public final class ObjectSerializer {
    /**
     * Creates a new {@link ObjectSerializerBuilder} with some standard serializers already registered.
     * 
     * @return a new builder
     */
    public static ObjectSerializerBuilder builder() {
        return new ObjectSerializerBuilder(true);
    }

    /**
     * Creates a new {@link ObjectSerializerBuilder} with some standard serializers already registered.
     * 
     * @return a new builder
     */
    public static ObjectSerializerBuilder blankBuilder() {
        return new ObjectSerializerBuilder(false);
    }

    private final IdentityHashMap<Class<?>, ValueSerializer<?, ?>> classBasedSerializers;
    private final List<ValueSerializerProvider<?, ?>> generalProviders;

    /** the last-resort serializer provider, used when no other provider matches */
    private final ValueSerializerProvider<?, ?> defaultProvider;

    /** setting: skip transient fields as requested by the modifier */
    final boolean applyTransientModifier;

    ObjectSerializer(ObjectSerializerBuilder builder) {
        this.classBasedSerializers = builder.classBasedSerializers;
        this.generalProviders = builder.generalProviders;
        this.defaultProvider = builder.defaultProvider;
        this.applyTransientModifier = builder.applyTransientModifier;
        assert classBasedSerializers != null && generalProviders != null && defaultProvider != null;
    }

    /**
     * Serializes a single value.
     * The resulting object can be a Config, a List, or a plain value that would be suitable for insertion in a
     * configuration supplied by {@code configSupplier}.
     * <p>
     * To ensure that you serialize the <em>fields</em> of a Java object into a {@code Config}, use
     * {@link #serializeFields(Object, Supplier)}.
     *
     * @return the object, converted to a config value
     */
    public Object serialize(Object value, Supplier<? extends Config> configSupplier) {
        SerializerContext ctx = new SerializerContext(
                this,
                () -> configSupplier.get().configFormat(),
                configSupplier);
        return ctx.serializeValue(value);
    }

    /**
     * Serializes an object as a {@code Config} or type {@code C} by transforming its fields into configuration
     * entries. A new configuration is created with the {@code configSupplier}.
     * <p>
     * The serialization depends on the registered {@link ValueSerializer value serializers}.
     *
     * @see ObjectSerializerBuilder#withSerializerForClass(Class, ValueSerializer)
     * @see ObjectSerializerBuilder#withSerializerForExactClass(Class, ValueSerializer)
     * @see ObjectSerializerBuilder#withSerializerProvider(ValueSerializerProvider)
     *
     * @return the new configuration
     */
    public <C extends Config> C serializeFields(Object source, Supplier<C> configSupplier) {
        C dest = configSupplier.get();
        serializeFields(source, dest);
        return dest;
    }

    /**
     * Serializes an object as a {@code Config} by transforming its fields into configuration entries.
     * The entries are inserted in {@code destination}.
     * <p>
     * The serialization depends on the registered {@link ValueSerializer value serializers}.
     *
     * @param source      object to serialize
     * @param destination configuration to store the result in
     * 
     * @see ObjectSerializerBuilder#withSerializerForClass(Class, ValueSerializer)
     * @see ObjectSerializerBuilder#withSerializerForExactClass(Class, ValueSerializer)
     * @see ObjectSerializerBuilder#withSerializerProvider(ValueSerializerProvider)
     * 
     * @return the new configuration
     */
    public void serializeFields(Object source, Config destination) {
        // the destination exists, convert the fields recursively
        SerializerContext ctx = new SerializerContext(
                this,
                () -> destination.configFormat(),
                () -> destination.createSubConfig());
        ctx.serializeFields(source, destination);
    }

    /**
     * Finds a suitable converter for this value.
     *
     * @throws SerializationException if no converter is found
     */
    @SuppressWarnings("unchecked")
    <T, R> ValueSerializer<T, R> findValueSerializer(Object value, SerializerContext ctx) { // todo
        Class<?> valueClass = value == null ? null : value.getClass();
        ValueSerializer<?, ?> maybeSe = null;
        for (ValueSerializerProvider<?, ?> provider : generalProviders) {
            maybeSe = provider.provide(valueClass, ctx);
            if (maybeSe != null) {
                return (ValueSerializer<T, R>) maybeSe;
            }
        }
        maybeSe = classBasedSerializers.get(valueClass);
        if (maybeSe != null) {
            return (ValueSerializer<T, R>) maybeSe;
        }
        maybeSe = defaultProvider.provide(valueClass, ctx);
        if (maybeSe != null) {
            return (ValueSerializer<T, R>) maybeSe;
        }
        throw SerializationException.noSerializerFound(value, valueClass, ctx);
    }

    List<String> getConfigPath(Field field) {
        Path path = field.getDeclaredAnnotation(Path.class);
        if (path != null) {
            return StringUtils.split(path.value(), '.');
        }
        AdvancedPath advancedPath = field.getDeclaredAnnotation(AdvancedPath.class);
        if (advancedPath != null) {
            return Arrays.asList(advancedPath.value());
        }
        return Collections.singletonList(field.getName());
    }

    String getConfigComment(Field field) {
        return null; // TODO annotation for comments
    }
}
