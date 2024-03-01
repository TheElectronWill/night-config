package com.electronwill.nightconfig.core.serde;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
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
                (valueClass, ctx) -> Util.canAssign(cls, valueClass) ? (ValueSerializer) serializer
                        : null);
    }

    public <V, R> void withSerializerProvider(ValueSerializerProvider<V, R> provider) {
        generalProviders.add((ValueSerializerProvider<?, ?>) provider);
    }

    public <V, R> void withDefaultSerializerProvider(ValueSerializerProvider<V, R> provider) {
        defaultProvider = provider;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void withDefaultSerializerProvider() {
        ValueSerializer trivialSer = new TrivialSerializer();
        ValueSerializer fieldsSer = new FieldsSerializer();
        ValueSerializer numberToIntSer = (value, ctx) -> ((Number) value).intValue();
        ValueSerializer charToIntSer = (value, ctx) -> (int) (Character) value;

        defaultProvider = (valueClass, ctx) -> {
            ConfigFormat<?> format = ctx.configFormat();
            if (format == null || format.supportsType(valueClass)) {
                return trivialSer;
            } else if (Util.isPrimitiveOrWrapper(valueClass) || valueClass == String.class) {
                // Cannot access the fields of the value!
                // try to convert to int, if supported
                if (format.supportsType(int.class)) {
                    if (Util.canAssign(int.class, valueClass)) {
                        if (valueClass == Character.class || valueClass == char.class) {
                            return charToIntSer;
                        } else {
                            return numberToIntSer;
                        }
                    }
                }
                // no possible conversion, fail
                return null;
            } else {
                // type not supported, convert to subconfig with fields
                return fieldsSer;
            }
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
        ValueSerializer trivialSer = new TrivialSerializer();

        withSerializerProvider((valueClass, ctx) -> {
            if (valueClass == null) {
                ConfigFormat<?> format = ctx.configFormat();
                if (format == null || format.supportsType(null)) {
                    return trivialSer;
                } else {
                    return null;
                }
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
                return trivialSer; // the value is already a config, nothing to serialize
            }
            if (Enum.class.isAssignableFrom(valueClass)) {
                return enumSer;
            }
            return null;
        });
    }

    /**
     * Serializes a map to a config by converting each entry of the map into an entry of the config,
     * converting the entry's value.
     */
    private static final class MapSerializer implements ValueSerializer<Map<?, ?>, Config> {

        @Override
        public Config serialize(Map<?, ?> value, SerializerContext ctx) {
            Config res = ctx.createConfig();

            // serialize each entry as a config entry
            for (Entry<?, ?> entry : value.entrySet()) {
                // get the path
                Object key = entry.getKey();
                if (!(key instanceof String)) {
                    String keyTypeString = key == null ? "null" : key.getClass().toString();
                    throw new SerializationException("Map keys must be strings, invalid key type "
                            + keyTypeString + " in value.");
                }
                List<String> path = Collections.singletonList((String) key);

                // convert the value
                Object serialized = ctx.serializeValue(entry.getValue());

                // add the value to the config
                res.set(path, serialized);
            }
            return res;
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

    /** Converts an object to a config by turning each field into an entry. */
    private static final class FieldsSerializer implements ValueSerializer<Object, Config> {

        @Override
        public Config serialize(Object value, SerializerContext ctx) {
            Config sub = ctx.createConfig();
            ctx.serializeFields(value, sub);
            return sub;
        }
    }

    /** A provider that provides nothing, {@code provide} always returns null. */
    private static final class NoProvider implements ValueSerializerProvider<Object, Object> {
        @Override
        public ValueSerializer<Object, Object> provide(Class<?> valueClass, SerializerContext ctx) {
            return null;
        }
    }
}
