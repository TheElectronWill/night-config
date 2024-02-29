package com.electronwill.nightconfig.core.serde;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;

public final class TestObjects {
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

    static class Maps {
        Map<String, String> simpleMap = M1;
        Map<String, Map<String, Integer>> nestedMap = M2;

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
            SERIALIZED.set("simpleMap.key1", "value1");
            SERIALIZED.set("simpleMap.key2", "value2");
            SERIALIZED.set("simpleMap.key3", null);
            SERIALIZED.set("nestedMap.key.i0", 0);
            SERIALIZED.set("nestedMap.key.i123", 123);
            SERIALIZED.set("nestedMap.duplicate.i0", 123);
            SERIALIZED.set("nestedMap.duplicate.i123", 123);
            SERIALIZED.set("nestedMap.null", null);
        }
    }

    static class Transient<T, U extends T> {
        transient Object dontSerializeMe;
        transient T dontSerializeMeT;
        transient U dontSerializeMeU;

        static final Config SERIALIZED = Config.inMemory(); // empty!
    }

}
