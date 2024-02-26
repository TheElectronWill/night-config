package com.electronwill.nightconfig.core.serde;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * Implements Object to Config serialization.
 */
public final class ObjectSerializerBuilder {
    /** map of entries (Class<T> of value) -> (ValueSerializer<T> where V is a config value) */
    final IdentityHashMap<Class<?>, ValueSerializer<?>> exactClassSerializers = new IdentityHashMap<>(
            4);

    /** list of functions (Class<T> of value) -> (ValueSerializer based on the value, or null) */
    final List<Function<Class<?>, ValueSerializer<?>>> generalSerializers = new ArrayList<>();

    /** the last-resort serializer, used when no other serializer matches */
    ValueSerializer<Object> defaultSerializer;

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

    public <T> void withSerializerForExactClass(Class<T> cls, ValueSerializer<? super T> serializer) {
        exactClassSerializers.put(cls, serializer);
    }

    public <T> void withSerializerForClass(Class<T> cls, ValueSerializer<? super T> serializer) {
        generalSerializers.add(valueClass -> cls.isAssignableFrom(valueClass) ? serializer : null);
    }

    @SuppressWarnings("unchecked")
    public <T> void withSerializerProvider(Function<Class<T>, ValueSerializer<? super T>> provider) {
        generalSerializers.add((Function<Class<?>, ValueSerializer<?>>) (Function<?, ?>) provider);
    }

    public void withDefaultSerializer(ValueSerializer<Object> serializer) {
        defaultSerializer = serializer;
    }

    public void withDefaultSerializer() {
        // recurse if the type is not supported by the config's format
        defaultSerializer = (value, ctx) -> {
            ConfigFormat<?> format = ctx.configFormat();
            if (format != null && format.supportsType(value == null ? null : value.getClass())) {
                return value;
            } else {
                CommentedConfig sub = ctx.createConfig();
                ctx.serializeFields(value, sub);
                return sub;
            }
        };
    }

    public void ignoreTransientModifier() {
        this.applyTransientModifier = false;
    }

    /** registers the standard serializers */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void registerStandardSerializers() {
        withDefaultSerializer();

        ValueSerializer mapSer = new MapSerializer();
        ValueSerializer collSer = new CollectionSerializer();
        ValueSerializer iterSer = new IterableSerializer();
        ValueSerializer enumSer = new EnumSerializer();

        withSerializerProvider(cls -> {
            if (Map.class.isAssignableFrom(cls)) {
                return mapSer;
            }
            if (Collection.class.isAssignableFrom(cls)) {
                return collSer;
            }
            if (Iterable.class.isAssignableFrom(cls)) {
                return iterSer;
            }
            if (UnmodifiableConfig.class.isAssignableFrom(cls)) {
                return (v, ctx) -> v; // the value is already a config, nothing to serialize
            }
            if (Enum.class.isAssignableFrom(cls)) {
                return enumSer;
            }
            return null;
        });
    }

    private static final class MapSerializer implements ValueSerializer<Map<?, ?>> {

        @Override
        public Object serialize(Map<?, ?> value, SerializerContext ctx) {
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

    private static final class CollectionSerializer implements ValueSerializer<Collection<?>> {

        @Override
        public Object serialize(Collection<?> value, SerializerContext ctx) {
            List<Object> res = new ArrayList<>(value.size());
            for (Object v : value) {
                Object serialized = ctx.serializeValue(v);
                res.add(serialized);
            }
            return res;
        }
    }

    private static final class IterableSerializer implements ValueSerializer<Iterable<?>> {

        @Override
        public Object serialize(Iterable<?> value, SerializerContext ctx) {
            List<Object> res = new ArrayList<>();
            for (Object v : value) {
                Object serialized = ctx.serializeValue(v);
                res.add(serialized);
            }
            return res;
        }
    }

    private static final class EnumSerializer implements ValueSerializer<Enum<?>> {

        @Override
        public Object serialize(Enum<?> value, SerializerContext ctx) {
            return value.name();
        }
    }
}
