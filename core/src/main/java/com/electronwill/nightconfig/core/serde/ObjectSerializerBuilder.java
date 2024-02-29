package com.electronwill.nightconfig.core.serde;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * Implements Object to Config serialization.
 */
public final class ObjectSerializerBuilder {
    final IdentityHashMap<Class<?>, ValueSerializer<?, ?>> classBasedSerializers = new IdentityHashMap<>(
            7);

    final List<ValueSerializerProvider<?, ?>> generalProviders = new ArrayList<>();

    /** the last-resort serializer provider, used when no other provider matches */
    ValueSerializerProvider<?, ?> defaultProvider = new NoProvider();

    /** setting: skip transient fields as requested by the modifier */
    boolean applyTransientModifier = true;

    ObjectSerializerBuilder(boolean standards) {
        if (standards) {
            registerStandardSerializers();
        }
    }

    public ObjectSerializer build() {
        return new ObjectSerializer(this);
    }

    public <V, R> void withSerializerForExactClass(Class<V> cls,
            ValueSerializer<? super V, ? extends R> serializer) {
        classBasedSerializers.put(cls, serializer);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <V, R> void withSerializerForClass(Class<V> cls,
            ValueSerializer<? super V, ? extends R> serializer) {
        generalProviders.add(
                valueClass -> cls.isAssignableFrom(valueClass) ? (ValueSerializer) serializer : null);
    }

    public <V, R> void withSerializerProvider(ValueSerializerProvider<V, R> provider) {
        generalProviders.add((ValueSerializerProvider<?, ?>) provider);
    }

    public <V, R> void withDefaultSerializerProvider(ValueSerializerProvider<V, R> provider) {
        defaultProvider = provider;
    }

    public void withDefaultSerializerProvider() {
        defaultProvider = valueClass -> {
            // always succeed: returns the basic "value's fields to config" serializer
            return (value, ctx) -> {
                ConfigFormat<?> format = ctx.configFormat();
                if (format != null && format.supportsType(valueClass)) {
                    // type supported, use as is
                    return value;
                } else if (value != null && Util.isPrimitiveOrWrapper(valueClass)) {
                    // confusing situation, but no conversion is possible anyway
                    return value;
                } else {
                    // type not supported, convert to subconfig with fields
                    CommentedConfig sub = ctx.createConfig();
                    ctx.serializeFields(value, sub);
                    return sub;
                }
            };
        };
    }

    public void ignoreTransientModifier() {
        this.applyTransientModifier = false;
    }

    /** registers the standard serializers */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void registerStandardSerializers() {
        withDefaultSerializerProvider();

        ValueSerializer mapSer = new MapSerializer();
        ValueSerializer collSer = new CollectionSerializer();
        ValueSerializer iterSer = new IterableSerializer();
        ValueSerializer enumSer = new EnumSerializer();

        withSerializerProvider(valueClass -> {
            if (valueClass == null) {
                return (v, ctx) -> {
                    ConfigFormat format = ctx.configFormat();
                    if (format == null || format.supportsType(null)) {
                        return v;
                    } else {
                        throw new DeserializationException("Cannot serialize a null value into a configuration that doesn't support null. You could create a blankBuilder() and replace the standard serializers to serialize null values to something else.");
                    }
                };
            }
            if (Map.class.isAssignableFrom(valueClass)) {
                return mapSer;
            }
            if (Collection.class.isAssignableFrom(valueClass)) {
                return collSer;
            }
            if (Iterable.class.isAssignableFrom(valueClass)) {
                return iterSer;
            }
            if (UnmodifiableConfig.class.isAssignableFrom(valueClass)) {
                return (v, ctx) -> v; // the value is already a config, nothing to serialize
            }
            if (Enum.class.isAssignableFrom(valueClass)) {
                return enumSer;
            }
            return null;
        });
    }

    private static final class MapSerializer implements ValueSerializer<Map<?, ?>, CommentedConfig> {

        @Override
        public CommentedConfig serialize(Map<?, ?> value, SerializerContext ctx) {
            CommentedConfig sub = ctx.createConfig();
            // serialize each entry as a config entry
            for (Entry<?, ?> entry : value.entrySet()) {
                Object key = entry.getKey();
                if (!(key instanceof String)) {
                    String keyTypeString = key == null ? "null" : key.getClass().toString();
                    throw new SerializationException("Map keys must be strings, invalid key type "
                            + keyTypeString + " in value.");
                }
                ctx.serializeFields(entry.getValue(), sub);
            }
            return sub;
        }

    }

    private static final class CollectionSerializer implements ValueSerializer<Collection<?>, List<?>> {

        @Override
        public List<?> serialize(Collection<?> value, SerializerContext ctx) {
            List<Object> res = new ArrayList<>(value.size());
            for (Object v : value) {
                Object serialized = ctx.serializeValue(v);
                res.add(serialized);
            }
            return res;
        }
    }

    private static final class IterableSerializer implements ValueSerializer<Iterable<?>, List<?>> {

        @Override
        public List<?> serialize(Iterable<?> value, SerializerContext ctx) {
            List<Object> res = new ArrayList<>();
            for (Object v : value) {
                Object serialized = ctx.serializeValue(v);
                res.add(serialized);
            }
            return res;
        }
    }

    private static final class EnumSerializer implements ValueSerializer<Enum<?>, String> {

        @Override
        public String serialize(Enum<?> value, SerializerContext ctx) {
            return value.name();
        }
    }

    private static final class TrivialSerializer implements ValueSerializer<Object, Object> {

        @Override
        public Object serialize(Object value, SerializerContext ctx) {
            return value;
        }
    }

    /** A provider that provides nothing, {@code provide} always returns null. */
    private static final class NoProvider implements ValueSerializerProvider<Object, Object> {
        @Override
        public ValueSerializer<Object, Object> provide(Class<?> valueClass) {
            return null;
        }
    }
}
