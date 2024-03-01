package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.electronwill.nightconfig.core.Config;

final class StandardSerializers {
    /**
     * Serializes a {@code Map<String, V>} to a {@code Config} by converting each entry of the map into an entry of the config,
     * converting the entry's value.
     */
    static final class MapSerializer implements ValueSerializer<Map<?, ?>, Config> {

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

    /**
     * Serializes a {@code Collection<V>} to a {@code List<R>}.
     */
    static final class CollectionSerializer implements ValueSerializer<Collection<?>, List<?>> {

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

    /**
     * Serializes an {@code Iterable<V>} to a {@code List<R>}
     */
    static final class IterableSerializer implements ValueSerializer<Iterable<?>, List<?>> {

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

    /**
     * Serializes an {@code Array<V>} to a {@code List<R>}.
     */
    static final class ArraySerializer implements ValueSerializer<Object, List<?>> {

        @Override
        public List<?> serialize(Object arrayValue, SerializerContext ctx) {
            int size = Array.getLength(arrayValue);

            List<Object> res = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                Object element = Array.get(arrayValue, i);
                Object serialized = ctx.serializeValue(element);
                res.add(serialized);
            }
            return res;
        }
    }

    /**
     * Serializes an {@code Enum} to a {@code String}.
     */
    static final class EnumSerializer implements ValueSerializer<Enum<?>, String> {

        @Override
        public String serialize(Enum<?> value, SerializerContext ctx) {
            return value.name();
        }
    }

    /**
     * The trivial serializer: serialize(value) == value.
     */
    static final class TrivialSerializer implements ValueSerializer<Object, Object> {

        @Override
        public Object serialize(Object value, SerializerContext ctx) {
            return value;
        }
    }

    /** 
     * Converts an object to a config by turning each field into an entry.
     */
    static final class FieldsToConfigSerializer implements ValueSerializer<Object, Config> {

        @Override
        public Config serialize(Object value, SerializerContext ctx) {
            Config sub = ctx.createConfig();
            ctx.serializeFields(value, sub);
            return sub;
        }
    }
}
