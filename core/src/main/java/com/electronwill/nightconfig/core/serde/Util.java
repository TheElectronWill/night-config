package com.electronwill.nightconfig.core.serde;

import java.util.IdentityHashMap;

final class Util {
    /**
     * A better version of {@link Class#isAssignableFrom(Class)} that works with widening primitive conversions
     * and conversions between primitive types and wrapper types.
     * 
     * @param fieldType the type of the field
     * @param valueType the type of the value
     * @return true if a field of this type can receive a value of that type
     */
    public static boolean canAssign(Class<?> fieldType, Class<?> valueType) {
        if (valueType == null) {
            return !fieldType.isPrimitive();
        }
        if (fieldType.isPrimitive() || valueType.isPrimitive()) {
            TypeAndOrder a = fieldType.isPrimitive() ? PRIMITIVE_TO_WRAPPER.get(fieldType)
                    : WRAPPER_TO_PRIMITIVE.get(fieldType);
            TypeAndOrder b = valueType.isPrimitive() ? PRIMITIVE_TO_WRAPPER.get(valueType)
                    : WRAPPER_TO_PRIMITIVE.get(valueType);
            return a != null && b != null && a.canAssignValue(b);
        }
        return fieldType.isAssignableFrom(valueType);
    }

    public static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() || WRAPPER_TO_PRIMITIVE.get(type) != null;
    }

    private static final IdentityHashMap<Class<?>, TypeAndOrder> PRIMITIVE_TO_WRAPPER = new IdentityHashMap<>();
    private static final IdentityHashMap<Class<?>, TypeAndOrder> WRAPPER_TO_PRIMITIVE = new IdentityHashMap<>();

    static void addPrimitiveAndWrapper(Class<?> primitiveType, Class<?> wrapperType) {
        PRIMITIVE_TO_WRAPPER.put(primitiveType,
                new TypeAndOrder(PRIMITIVE_TO_WRAPPER.size(), wrapperType));
        WRAPPER_TO_PRIMITIVE.put(wrapperType,
                new TypeAndOrder(WRAPPER_TO_PRIMITIVE.size(), primitiveType));
    }

    static {
        addPrimitiveAndWrapper(Boolean.TYPE, Boolean.class);
        addPrimitiveAndWrapper(Byte.TYPE, Byte.class);
        addPrimitiveAndWrapper(Short.TYPE, Short.class);
        addPrimitiveAndWrapper(Character.TYPE, Character.class);
        addPrimitiveAndWrapper(Integer.TYPE, Integer.class);
        addPrimitiveAndWrapper(Long.TYPE, Long.class);
        addPrimitiveAndWrapper(Float.TYPE, Float.class);
        addPrimitiveAndWrapper(Double.TYPE, Double.class);
    }

    private static final class TypeAndOrder {
        final int order;
        final Class<?> type;

        TypeAndOrder(int order, Class<?> type) {
            this.order = order;
            this.type = type;
        }

        boolean canAssignValue(TypeAndOrder valueType) {
            // no widening conversion for boolean
            if (this.order == 0) {
                return valueType.order == 0;
            } else if (valueType.order == 0) {
                return false;
            }
            // widening conversions for numbers: int <- short, float <- int, ...
            return this.order >= valueType.order;
        }

        @Override
        public String toString() {
            return "TypeAndOrder [order=" + order + ", type=" + type + "]";
        }

    }
}
