package com.electronwill.nightconfig.core.serde;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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
    ValueSerializerProvider<?, ?> defaultProvider = NoProvider.INSTANCE;

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
                (valueClass, ctx) -> valueClass != null && Util.canAssign(cls, valueClass)
                        ? (ValueSerializer) serializer
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
        ValueSerializer trivialSer = new StandardSerializers.TrivialSerializer();
        ValueSerializer fieldsSer = new StandardSerializers.FieldsToConfigSerializer();
        ValueSerializer numberToIntSer = (value, ctx) -> ((Number) value).intValue();
        ValueSerializer charToIntSer = (value, ctx) -> (int) (Character) value;

        defaultProvider = (valueClass, ctx) -> {
            ConfigFormat<?> format = ctx.configFormat();
            if (format == null || format.supportsType(valueClass)) {
                return trivialSer;
            } else if (valueClass != null
                    && (Util.isPrimitiveOrWrapper(valueClass) || valueClass == String.class
                            || valueClass.isArray())) {
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

	/**
	 * Serialize transient fields instead of ignoring them.
	 */
    public void serializeTransientFields() {
        this.applyTransientModifier = false;
    }

    /** registers the standard serializers */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void registerStandardSerializers() {
        withDefaultSerializerProvider();

        ValueSerializer mapSer = new StandardSerializers.MapSerializer();
        ValueSerializer collSer = new StandardSerializers.CollectionSerializer();
        ValueSerializer iterSer = new StandardSerializers.IterableSerializer();
        ValueSerializer arraySer = new StandardSerializers.ArraySerializer();
        ValueSerializer enumSer = new StandardSerializers.EnumSerializer();
        ValueSerializer trivialSer = new StandardSerializers.TrivialSerializer();

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
            if (valueClass.isArray()) {
                return arraySer;
            }
            return null;
        });
    }

    /** A provider that provides nothing, {@code provide} always returns null. */
    static final class NoProvider implements ValueSerializerProvider<Object, Object> {
        static final NoProvider INSTANCE = new NoProvider();

        @Override
        public ValueSerializer<Object, Object> provide(Class<?> valueClass, SerializerContext ctx) {
            return null;
        }
    }
}
