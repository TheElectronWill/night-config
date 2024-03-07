package com.electronwill.nightconfig.core.serde;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeDefault;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeDefault.WhenValue;

public final class SerdeTest {
    static class Primitives {
        long l = Long.MAX_VALUE;
        int i = Integer.MAX_VALUE;
        short s = Short.MAX_VALUE;
        char c = Character.MAX_VALUE;
        byte b = Byte.MAX_VALUE;
        boolean bool = true;
        float f = Float.MAX_VALUE;
        double d = Double.MAX_VALUE;

        static final Config SERIALIZED;
        static {
            SERIALIZED = Config.inMemory();
            SERIALIZED.set("l", Long.MAX_VALUE);
            SERIALIZED.set("i", Integer.MAX_VALUE);
            SERIALIZED.set("s", Short.MAX_VALUE);
            SERIALIZED.set("c", Character.MAX_VALUE);
            SERIALIZED.set("b", Byte.MAX_VALUE);
            SERIALIZED.set("bool", true);
            SERIALIZED.set("f", Float.MAX_VALUE);
            SERIALIZED.set("d", Double.MAX_VALUE);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Primitives other = (Primitives) obj;
            if (l != other.l)
                return false;
            if (i != other.i)
                return false;
            if (s != other.s)
                return false;
            if (c != other.c)
                return false;
            if (b != other.b)
                return false;
            if (bool != other.bool)
                return false;
            if (Float.floatToIntBits(f) != Float.floatToIntBits(other.f))
                return false;
            if (Double.doubleToLongBits(d) != Double.doubleToLongBits(other.d))
                return false;
            return true;
        }
    }

    @Test
    public void testPrimitives() throws Exception {
        var de = ObjectDeserializer.builder().build();
        var deserialized = de.deserializeFields(Primitives.SERIALIZED, Primitives::new);
        assertEquals(new Primitives(), deserialized);

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new Primitives(), Config::inMemory);
        assertEquals(Primitives.SERIALIZED, serialized);

        var serialized2 = ser.serialize(new Primitives(), CommentedConfig::inMemory);
        assertEquals(Primitives.SERIALIZED, serialized2);
    }

    @Test
    public void testPrimitivesWithExoticConfigFormats() throws Exception {
        // Here we use a ConfigFormat that does not support byte, short and char, but that support int.
        // We expect the Serializer to convert the values to int.
        Predicate<Class<?>> onlyIntOrFloat = (cls) -> cls == Boolean.class || cls == boolean.class
                || cls == Integer.class || cls == int.class || cls == Long.class || cls == long.class
                || cls == Float.class || cls == float.class || cls == Double.class
                || cls == double.class;

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new Primitives(),
                () -> Config.of(InMemoryFormat.withSupport(onlyIntOrFloat)));

        var expectedResult = Config.copy(Primitives.SERIALIZED);
        expectedResult.set("s", (int) (Short) Primitives.SERIALIZED.get("s"));
        expectedResult.set("b", (int) (Byte) Primitives.SERIALIZED.get("b"));
        expectedResult.set("c", (int) (Character) Primitives.SERIALIZED.get("c"));
        assertEquals(expectedResult, serialized);

        // Here we use a ConfigFormat that supports everything.
        ser = ObjectSerializer.builder().build();
        serialized = ser.serializeFields(new Primitives(), Config::inMemoryUniversal);
        assertEquals(Primitives.SERIALIZED, serialized);
    }

    static class Simple {
        String myString = "abcdefg";
        String nullString;

        List<String> listOfStrings = Arrays.asList("a", "b", "c");
        List<Object> heterogeneousList = Arrays.asList("str", false, 0.0);
        CommentedConfig subConfig = CommentedConfig.fake(Primitives.SERIALIZED);

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Simple other = (Simple) obj;
            if (myString == null) {
                if (other.myString != null)
                    return false;
            } else if (!myString.equals(other.myString))
                return false;
            if (nullString == null) {
                if (other.nullString != null)
                    return false;
            } else if (!nullString.equals(other.nullString))
                return false;
            if (listOfStrings == null) {
                if (other.listOfStrings != null)
                    return false;
            } else if (!listOfStrings.equals(other.listOfStrings))
                return false;
            if (heterogeneousList == null) {
                if (other.heterogeneousList != null)
                    return false;
            } else if (!heterogeneousList.equals(other.heterogeneousList))
                return false;
            if (subConfig == null) {
                if (other.subConfig != null)
                    return false;
            } else if (!subConfig.equals(other.subConfig))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Simple [myString=" + myString + ", nullString=" + nullString + ", listOfStrings="
                    + listOfStrings + ", heterogeneousList=" + heterogeneousList + ", subConfig="
                    + subConfig + "]";
        }

        static final Config SERIALIZED;
        static {
            SERIALIZED = Config.inMemory();
            SERIALIZED.set("myString", "abcdefg");
            SERIALIZED.set("nullString", null);
            SERIALIZED.set("listOfStrings", Arrays.asList("a", "b", "c"));
            SERIALIZED.set("heterogeneousList", Arrays.asList("str", false, 0.0));
            SERIALIZED.set("subConfig", CommentedConfig.fake(Primitives.SERIALIZED));
        }
    }

    @Test
    public void testSimple() throws Exception {
        var de = ObjectDeserializer.builder().build();
        var deserialized = de.deserializeFields(Simple.SERIALIZED, Simple::new);
        assertEquals(new Simple(), deserialized);

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new Simple(), Config::inMemory);
        assertEquals(Simple.SERIALIZED, serialized);
        var serialized2 = ser.serialize(new Simple(), CommentedConfig::inMemory);
        assertEquals(Simple.SERIALIZED, serialized2);
    }

    static class NestedObjects {
        List<List<Simple>> inList = L;
        Map<String, Map<String, Simple>> inMap = M;
        NestedObjects inSelf;

        private NestedObjects() {
            this(true);
        }

        private NestedObjects(boolean nest) {
            if (nest) {
                inSelf = new NestedObjects(false);
            } else {
                inSelf = null;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NestedObjects other = (NestedObjects) obj;
            if (inList == null) {
                if (other.inList != null)
                    return false;
            } else if (!inList.equals(other.inList))
                return false;
            if (inMap == null) {
                if (other.inMap != null)
                    return false;
            } else if (!inMap.equals(other.inMap))
                return false;
            if (inSelf == null) {
                if (other.inSelf != null)
                    return false;
            } else if (!inSelf.equals(other.inSelf))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "NestedObjects [\n\tinList=" + inList + ",\n\tinMap=" + inMap + ",\n\tinSelf=" + inSelf + "\n]";
        }

        static final List<List<Simple>> L;
        static final Map<String, Map<String, Simple>> M;
        static final Config SERIALIZED;
        static {
            L = List.of(
                List.of(new Simple(), new Simple()),
                Collections.singletonList(null),
                List.of()
            );
            M = Map.of(
                "map", Map.of("sub", new Simple()),
                "empty", Map.of()
            );

            var serializedL = List.of(
                List.of(Simple.SERIALIZED, Simple.SERIALIZED),
                Collections.singletonList(null),
                List.of()
            );
            var serializedM = Config.inMemory();
            var sub = Config.inMemory();
            sub.set("sub", Simple.SERIALIZED);
            serializedM.set("map", sub);
            serializedM.set("empty", Config.inMemory());

            SERIALIZED = Config.inMemory();
            SERIALIZED.set("inList", serializedL);
            SERIALIZED.set("inMap", serializedM);

            var nested = Config.copy(SERIALIZED);
            nested.set("inSelf", null);
            SERIALIZED.set("inSelf", nested);
        }
    }

    @Test
    public void testNestedObjects() throws Exception {
        var de = ObjectDeserializer.builder().build();
        var deserialized = de.deserializeFields(NestedObjects.SERIALIZED, NestedObjects::new);
        assertEquals(new NestedObjects(), deserialized);

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new NestedObjects(), Config::inMemory);
        assertEquals(NestedObjects.SERIALIZED, serialized);
        var serialized2 = ser.serialize(new NestedObjects(), CommentedConfig::inMemory);
        assertEquals(NestedObjects.SERIALIZED, serialized2);
    }

    static class SimpleMaps {
        Map<String, String> simpleMap = M1;

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleMaps other = (SimpleMaps) obj;
            if (simpleMap == null) {
                if (other.simpleMap != null)
                    return false;
            } else if (!simpleMap.equals(other.simpleMap))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Maps [simpleMap=" + simpleMap + "]";
        }

        static final Map<String, String> M1;
        static final Config SERIALIZED;
        static {
            M1 = new HashMap<>();
            M1.put("key1", "value1");
            M1.put("key2", "value2");
            M1.put("key3", null);

            SERIALIZED = Config.inMemory();
            SERIALIZED.set("simpleMap.key1", "value1");
            SERIALIZED.set("simpleMap.key2", "value2");
            SERIALIZED.set("simpleMap.key3", null);
        }
    }

    @Test
    public void testSimpleMaps() throws Exception {
        var de = ObjectDeserializer.builder().build();
        var deserialized = de.deserializeFields(SimpleMaps.SERIALIZED, SimpleMaps::new);
        assertEquals(new SimpleMaps(), deserialized);

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new SimpleMaps(), Config::inMemory);
        assertEquals(SimpleMaps.SERIALIZED, serialized);
        var serialized2 = ser.serialize(new SimpleMaps(), CommentedConfig::inMemory);
        assertEquals(SimpleMaps.SERIALIZED, serialized2);
    }

    static class Maps {
        SimpleMaps object = new SimpleMaps();
        Map<String, String> simpleMap = M1;
        Map<String, Map<String, Integer>> nestedMap = M2;

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Maps other = (Maps) obj;
            if (!Objects.equals(object, other.object) && Objects.equals(simpleMap, other.simpleMap))
                return false;
            for (Map.Entry<String, Map<String, Integer>> e : nestedMap.entrySet()) {
                Object otherValue = other.nestedMap.get(e.getKey());
                if (!Objects.equals(e.getValue(), otherValue)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "Maps [object=" + object + ", simpleMap=" + simpleMap + ", nestedMap=" + nestedMap + "]";
        }

        static final Map<String, String> M1;
        static final Map<String, Map<String, Integer>> M2;
        static final Config SERIALIZED;
        static {
            M1 = new HashMap<>();
            M1.put("key1", "value1");
            M1.put("key2", "value2");
            M1.put("key3", null);

            M2 = new HashMap<>();
            Map<String, Integer> sub = new HashMap<>();
            sub.put("i0", 0);
            sub.put("i123", 123);
            M2.put("key", sub);
            M2.put("duplicate", sub);
            M2.put("null", null);

            SERIALIZED = Config.inMemory();
            SERIALIZED.set("object", SimpleMaps.SERIALIZED);
            SERIALIZED.set("simpleMap.key1", "value1");
            SERIALIZED.set("simpleMap.key2", "value2");
            SERIALIZED.set("simpleMap.key3", null);
            SERIALIZED.set("nestedMap.key.i0", 0);
            SERIALIZED.set("nestedMap.key.i123", 123);
            SERIALIZED.set("nestedMap.duplicate.i0", 0);
            SERIALIZED.set("nestedMap.duplicate.i123", 123);
            SERIALIZED.set("nestedMap.null", null);
        }
    }

    @Test
    public void testMaps() throws Exception {
        var de = ObjectDeserializer.builder().build();
        var deserialized = de.deserializeFields(Maps.SERIALIZED, Maps::new);
        assertEquals(new Maps(), deserialized);

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new Maps(), Config::inMemory);
        assertEquals(Maps.SERIALIZED, serialized);
        var serialized2 = ser.serialize(new Maps(), CommentedConfig::inMemory);
        assertEquals(Maps.SERIALIZED, serialized2);
    }

    static class Transient<T, U extends T> {
        transient Object dontSerializeMe;
        transient T dontSerializeMeT;
        transient U dontSerializeMeU;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Transient;
        }

        @Override
        public String toString() {
            return "Transient(...)";
        }

        static final Config SERIALIZED = Config.inMemory(); // empty!
    }

    @Test
    public void testTransient() throws Exception {
        var de = ObjectDeserializer.builder().build();
        var deserialized = de.deserializeFields(Transient.SERIALIZED, Transient::new);
        assertEquals(new Transient<CharSequence, String>(), deserialized);

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new Transient<CharSequence, String>(), Config::inMemory);
        assertEquals(Transient.SERIALIZED, serialized);
        var serialized2 = ser.serialize(new Transient<CharSequence, String>(),
                CommentedConfig::inMemory);
        assertEquals(Transient.SERIALIZED, serialized2);
    }

    static class ArrayValues<T extends CharSequence> {
        String[] str = { "a", "b", "c" };
        Simple[] nested = { new Simple() };

        int[][][] iii = { { { 1, 2 }, { 10, 20 } }, { { -1, -2 } }, { { 0 }, { 0 } } };
        T[][] seqs = null;

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ArrayValues))
                return false;
            ArrayValues<?> other = (ArrayValues<?>) obj;
            return Arrays.deepEquals(str, other.str) &&
                    Arrays.equals(nested, other.nested) &&
                    Arrays.deepEquals(iii, other.iii) &&
                    Arrays.deepEquals(seqs, other.seqs);
        }

        @Override
        public String toString() {
            return "ArrayValues [str=" + Arrays.deepToString(str) + ", nested="
                    + Arrays.deepToString(nested)
                    + ", iii=" + Arrays.deepToString(iii) + ", seqs=" + Arrays.deepToString(seqs) + "]";
            // return "ArrayValues [nested = " + Arrays.deepToString(nested) + "]";
        }

        static final Config SERIALIZED = Config.inMemory();
        static {
            SERIALIZED.set("str", List.of("a", "b", "c"));
            SERIALIZED.set("nested", List.of(Simple.SERIALIZED));
            SERIALIZED.set("iii", List.of(
                    List.of(List.of(1, 2), List.of(10, 20)),
                    List.of(List.of(-1, -2)),
                    List.of(List.of(0), List.of(0))));
            SERIALIZED.set("seqs", null);
        }

    }

    @Test
    public void testArrayValues() throws Exception {
        var de = ObjectDeserializer.builder().build();
        var deserialized = de.deserializeFields(ArrayValues.SERIALIZED, ArrayValues::new);
        assertEquals(new ArrayValues<String>(), deserialized);

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new ArrayValues<String>(), Config::inMemory);
        assertEquals(ArrayValues.SERIALIZED, serialized);
        var serialized2 = ser.serialize(new ArrayValues<String>(),
                CommentedConfig::inMemory);
        assertEquals(ArrayValues.SERIALIZED, serialized2);
    }

	static class SimpleAnnotations {
		@SerdeComment("This is uid")
		@SerdeKey("uid")
		String myUniqueId = "0000-1234-uid";

		@SerdeComment("This is myUniqueId")
		@SerdeKey("myUniqueId") // yes that's confusing, don't do it at home! it's for testing purposes only
		int uid = 42;

		@SerdeComment("Here's a comment with multiple lines.")
		@SerdeComment("See, a second line is right there!")
		Simple nested = new Simple();

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof SimpleAnnotations)) {
				return false;
			}
			SimpleAnnotations o = (SimpleAnnotations)obj;
			return Objects.equals(myUniqueId, o.myUniqueId) && Objects.equals(uid, o.uid) && Objects.equals(nested, o.nested);
		}

        @Override
		public String toString() {
			return "SimpleAnnotations [myUniqueId=" + myUniqueId + ", uid=" + uid + ", nested=" + nested + "]";
		}

		static final CommentedConfig SERIALIZED;
        static {
            SERIALIZED = CommentedConfig.inMemory();
            SERIALIZED.set("uid", "0000-1234-uid");
            SERIALIZED.set("myUniqueId", 42);
            SERIALIZED.set("nested", Simple.SERIALIZED);
            SERIALIZED.setComment("uid", "This is uid");
            SERIALIZED.setComment("myUniqueId", "This is myUniqueId");
            SERIALIZED.setComment("nested", "Here's a comment with multiple lines.\nSee, a second line is right there!");
        }
	}

    @Test
    public void testSimpleAnnotations() throws Exception {
        var de = ObjectDeserializer.builder().build();
        var deserialized = de.deserializeFields(SimpleAnnotations.SERIALIZED, SimpleAnnotations::new);
        assertEquals(new SimpleAnnotations(), deserialized);

        var ser = ObjectSerializer.builder().build();
        var serialized = ser.serializeFields(new SimpleAnnotations(), Config::inMemory);
        assertEquals(SimpleAnnotations.SERIALIZED, serialized);
        var serialized2 = ser.serialize(new SimpleAnnotations(), CommentedConfig::inMemory);
        assertEquals(SimpleAnnotations.SERIALIZED, serialized2);
    }

}
