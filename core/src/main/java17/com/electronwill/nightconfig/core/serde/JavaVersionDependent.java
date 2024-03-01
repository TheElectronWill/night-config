package com.electronwill.nightconfig.core.serde;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * Java 17 version of some utilities.
 */
final class JavaVersionDependent {
    public static Object deserializeConfigToObject(UnmodifiableConfig value, Class<?> objectClass,
            DeserializerContext ctx) {

        if (objectClass.isRecord()) {
            // The object is a record, its constructor matches the record's fields in their order.
            // We will use this information to create the record, matching the record's components with the config's
            // entries.
            var components = objectClass.getRecordComponents();
            var constructor = getCanonicalRecordConstructor(objectClass, components);
            var componentValues = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                RecordComponent comp = components[i];
                Object configValue = value.getRaw(Collections.singletonList(comp.getName()));
                if (configValue == null) {
                    // missing component!
                    // find all the missing components to emit a more helpful error message
                    List<String> missingComponents = Arrays.stream(components).map(c -> c.getName())
                            .filter(c -> !value.contains(c)).collect(Collectors.toList());
                    var missingComponentsStr = String.join(", ", missingComponents);
                    throw new DeserializationException(
                            "Could not deserialize this configuration to a record of type " + objectClass
                                    + " because the following components (entries) are missing: "
                                    + missingComponentsStr);
                }
                if (configValue == NULL_OBJECT) {
                    // component of value null
                    configValue = null;
                }
                componentValues[i] = configValue;
            }
            try {
                return constructor.newInstance(componentValues);
            } catch (Exception e) {
                throw new DeserializationException(
                        "Failed to create an instance of record " + objectClass, e);
            }
        } else {
            Object instance;
            try {
                var constructor = objectClass.getDeclaredConstructor();
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

    private static Constructor<?> getCanonicalRecordConstructor(Class<?> cls,
            RecordComponent[] components) {
        Class<?>[] paramTypes = Arrays.stream(components)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
        try {
            return cls.getDeclaredConstructor(paramTypes);
        } catch (Exception e) {
            throw new DeserializationException(
                    "Failed to get the canonical constructor of record " + cls, e);
        }
    }
}
