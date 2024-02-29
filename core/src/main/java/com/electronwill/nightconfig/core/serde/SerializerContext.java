package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;

public final class SerializerContext {
    final ObjectSerializer settings;
    final Supplier<? extends ConfigFormat<?>> formatSupplier;
    final Supplier<? extends CommentedConfig> configSupplier;

    SerializerContext(ObjectSerializer settings,
            Supplier<? extends ConfigFormat<?>> formatSupplier,
            Supplier<? extends CommentedConfig> configSupplier) {
        this.settings = settings;
        this.formatSupplier = formatSupplier;
        this.configSupplier = configSupplier;
    }

    public ConfigFormat<?> configFormat() {
        return formatSupplier.get();
    }

    public CommentedConfig createConfig() {
        return configSupplier.get();
    }

    /** Serializes a single value. */
    public Object serializeValue(Object value) {
        ValueSerializer<Object, ?> serializer = settings.findValueSerializer(value);
        return serializer.serialize(value, this);
    }

    /**
     * Serializes an object as a {@code Config} by transforming its fields into configuration entries in
     * {@code destination}.
     */
    public void serializeFields(Object source, CommentedConfig destination) {
        // loop through the class hierarchy of the source type
        Class<?> cls = source.getClass();
        while (cls != Object.class) {
            for (Field field : cls.getDeclaredFields()) {
                if (preCheck(field)) {
                    // read the fields's value
                    Object value;
                    try {
                        value = field.get(source);
                    } catch (IllegalAccessException e) {
                        throw new SerializationException("Failed to read field: " + field);
                    }

                    // read some annotations
                    List<String> path = settings.getConfigPath(field);
                    String comment = settings.getConfigComment(field);

                    // find the right serializer
                    ValueSerializer<Object, ?> serializer = settings.findValueSerializer(value);

                    // serialize the value and modify the destination
                    try {
                        Object serialized = serializer.serialize(value, this);
                        destination.set(path, serialized);
                        if (comment != null) {
                            destination.setComment(path, comment);
                        }
                    } catch (Exception ex) {
                        throw new SerializationException(
                                "Error during serialization of field " + field
                                        + " with serializer " + serializer,
                                ex);
                    }
                }
            }
            cls = cls.getSuperclass();
        }
    }

    private boolean preCheck(Field field) {
        int mods = field.getModifiers();
        if (Modifier.isStatic(mods)) {
            return false;
        }
        if (Modifier.isTransient(mods) && settings.applyTransientModifier) {
            return false;
        }
        if (Modifier.isFinal(mods)) {
            field.setAccessible(true);
        }
        return true;
    }
}
