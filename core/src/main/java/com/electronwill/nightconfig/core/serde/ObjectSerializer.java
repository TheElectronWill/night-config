package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.AdvancedPath;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.utils.FakeCommentedConfig;
import com.electronwill.nightconfig.core.utils.StringUtils;

/**
 * Implements Object to Config serialization.
 */
public final class ObjectSerializer {
    public static ObjectSerializerBuilder builder() {
        return new ObjectSerializerBuilder(true);
    }

    public static ObjectSerializerBuilder blankBuilder() {
        return new ObjectSerializerBuilder(false);
    }

    /** map of entries (Class<T> of value) -> (ValueSerializer<T> where V is a config value) */
    private final IdentityHashMap<Class<?>, ValueSerializer<?>> classBasedSerializers;

    /** list of functions (Class<T> of value) -> (ValueSerializer based on the value, or null) */
    private final List<Function<Class<?>, ValueSerializer<?>>> generalSerializers;

    /** the last-resort serializer, used when no other serializer matches */
    private final ValueSerializer<Object> defaultSerializer;

    /** setting: skip transient fields as requested by the modifier */
    final boolean applyTransientModifier;

    ObjectSerializer(ObjectSerializerBuilder builder) {
        this.classBasedSerializers = builder.exactClassSerializers;
        this.generalSerializers = builder.generalSerializers;
        this.defaultSerializer = builder.defaultSerializer;
        this.applyTransientModifier = builder.applyTransientModifier;
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
    public Object serialize(Object value, Supplier<? extends CommentedConfig> configSupplier) {
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
     * @see ObjectSerializerBuilder#withSerializerProvider(Function)
     *
     * @return the new configuration
     */
    @SuppressWarnings("unchecked")
    public <C extends Config> C serializeFields(Object source, Supplier<C> configSupplier) {
        C dest = configSupplier.get();
        if (dest instanceof CommentedConfig) {
            serializeFields(source, (CommentedConfig) dest);
            return dest;
        } else {
            FakeCommentedConfig cc = new FakeCommentedConfig(dest);
            serializeFields(source, cc);
            return (C) cc.unwrap();
        }
    }

    /**
     * Serializes an object as a {@code Config} by transforming its fields into configuration entries.
     * The entries are inserted in {@code destination}.
     * <p>
     * The serialization depends on the registered {@link ValueSerializer value serializers}.
     *
     * @param source object to serialize
     * @param destination configuration to store the result in
     * 
     * @see ObjectSerializerBuilder#withSerializerForClass(Class, ValueSerializer)
     * @see ObjectSerializerBuilder#withSerializerForExactClass(Class, ValueSerializer)
     * @see ObjectSerializerBuilder#withSerializerProvider(Function)
     *  
     * @return the new configuration
     */
    public void serializeFields(Object source, CommentedConfig destination) {
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
    <T> ValueSerializer<T> findValueSerializer(Object value) {
        Class<?> cls = value == null ? null : value.getClass();
        ValueSerializer<?> c = null;
        for (Function<Class<?>, ValueSerializer<?>> f : generalSerializers) {
            c = f.apply(cls);
            if (c != null) {
                return (ValueSerializer<T>) c;
            }
        }
        c = classBasedSerializers.get(cls);
        if (c == null) {
            c = defaultSerializer;
            if (c == null) {
                String ofTypeStr = cls == null ? "" : " of type " + cls;
                throw new SerializationException(
                        "No suitable serializer found for value" + ofTypeStr + ":" + value);
            }
        }
        return (ValueSerializer<T>) c;
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
