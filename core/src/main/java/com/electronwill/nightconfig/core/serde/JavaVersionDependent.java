package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * Utilities whose implementation may change across Java versions, thanks to the multi-release JAR.
 */
final class JavaVersionDependent {
    public static Object deserializeConfigToObject(UnmodifiableConfig value, Class<?> objectClass,
            DeserializerContext ctx) {

        Object instance;
        try {
            Constructor<?> constructor = objectClass.getDeclaredConstructor();
            if (!Modifier.isPublic(constructor.getModifiers())) {
                constructor.setAccessible(true);
            }
            instance = constructor.newInstance();
        } catch (Exception e) {
            throw new DeserializationException("Failed to create an instance of " + objectClass, e);
        }
        ctx.deserializeFields(value, instance);
        return instance;
    }
}
