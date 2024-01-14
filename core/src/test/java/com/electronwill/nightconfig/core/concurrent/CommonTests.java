package com.electronwill.nightconfig.core.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.electronwill.nightconfig.core.Config.Entry;

class CommonTests {

    static String[] testKeys = {
            "", "a", "true", "false", "0", "null", "3.1415926535", "!?-*/€\\²"
    };
    static Set<String> setTestKeys = Set.of(testKeys);
    static Set<String> setTestComments = setTestKeys.stream().map(k -> k + "-comment").collect(Collectors.toSet());

    static Object[] testValues = {
            new Object(), "", "some string", 0, Integer.MIN_VALUE, Integer.MAX_VALUE,
            0.0, Double.MIN_VALUE, Double.MAX_VALUE, null,
            Arrays.asList("heterogeneous", true, false, null, 123, 123.456)
    };

    public static void testBasicSanity(ConcurrentConfig config) {
        assertTrue(config.isEmpty());
        // insert values into the config
        for (var key : testKeys) {
            for (var value : testValues) {
                var path = Collections.singletonList(key);
                config.set(path, value);
                assertSame(value, config.get(path), "wrong value for key " + key);
                assertTrue(config.contains(path));
                assertFalse(config.add(path, value)); // false because a value already exist
            }
            if (!key.contains(".")) {
                assertNotNull(config.set(key, testValues[0])); // returns the previous value
                assertSame(testValues[0], config.get(key));
                assertTrue(config.contains(key));
                assertFalse(config.add(key, testValues[0]));
            }
        }
        // check the keys
        {
            var entries = config.entrySet();
            Set<String> keys = entries.stream().map(e -> e.getKey()).collect(Collectors.toSet());
            assertEquals(setTestKeys, keys);
            assertEquals(testKeys.length, entries.size());
            assertEquals(testKeys.length, config.size());
        }

        // remove some values, check again
        var someKeysToRemove = Arrays.copyOfRange(testKeys, 0, testKeys.length / 2);
        var expectedSize = testKeys.length;
        for (var key : someKeysToRemove) {
            var path = Collections.singletonList(key);
            assertNotNull(config.remove(path)); // should return the value
            assertFalse(config.contains(path));
            assertNull(config.get(path));
            expectedSize--;
            assertEquals(expectedSize, config.size());
        }
        var setSomeKeysStillThere = new HashSet<>(setTestKeys);
        setSomeKeysStillThere.removeAll(List.of(someKeysToRemove));
        {
            var entries = config.entrySet();
            Set<String> keys = entries.stream().map(e -> e.getKey()).collect(Collectors.toSet());
            assertEquals(setSomeKeysStillThere, keys);
            assertEquals(someKeysToRemove.length, entries.size());
            assertEquals(someKeysToRemove.length, config.size());
        }

        // add some subconfigs automatically
        for (var key : testKeys) {
            var value = testValues[0];
            var path = Arrays.asList("sub", "config", key);
            assertTrue(config.add(path, value));
            assertSame(value, config.get(path));
            assertTrue(config.contains(path));
            assertTrue(config.contains(Arrays.asList("sub", "config")));
            assertTrue(config.contains(Arrays.asList("sub")));
        }
        assertInstanceOf(config.getClass(), config.get("sub"));
        assertInstanceOf(config.getClass(), config.get("sub.config"));
        assertInstanceOf(config.getClass(), config.remove("sub"));

        // add some subconfigs manually
        {
            var path = Collections.singletonList("subconfig");
            var subconfig = config.createSubConfig();
            assertNull(config.set(path, subconfig));
            assertSame(subconfig, config.get(path));
            assertSame(subconfig, config.set(path, config.createSubConfig()));
        }

        // clear the config
        config.clear();
        assertEquals(0, config.size());
        assertEquals(0, config.entrySet().size());
        assertTrue(config.isEmpty());
        assertTrue(config.entrySet().isEmpty());
        for (var key : testKeys) {
            assertNull(config.get(Collections.singletonList(key)));
        }

        // more subconfigs test
        assertNull(config.set("a.b.c", "test"));
        assertEquals("test", config.set("a.b.c", 123));
        assertEquals(123, config.<Integer>get("a.b.c"));
        assertTrue(config.contains("a.b.c"));
        assertEquals(123, config.<Integer>remove("a.b.c"));
    }

    public static void testErrors(ConcurrentCommentedConfig config) {
        // empty path for values
        assertThrows(Exception.class, () -> {
            config.set(Collections.emptyList(), "???");
        });
        assertThrows(Exception.class, () -> {
            config.get(Collections.emptyList());
        });
        assertThrows(Exception.class, () -> {
            config.remove(Collections.emptyList());
        });
        assertThrows(Exception.class, () -> {
            config.add(Collections.emptyList(), "???");
        });
        assertThrows(Exception.class, () -> {
            config.contains(Collections.emptyList());
        });

        // empty path for comments
        assertThrows(Exception.class, () -> {
            config.setComment(Collections.emptyList(), "???");
        });
        assertThrows(Exception.class, () -> {
            config.getComment(Collections.emptyList());
        });
        assertThrows(Exception.class, () -> {
            config.removeComment(Collections.emptyList());
        });
        assertThrows(Exception.class, () -> {
            config.containsComment(Collections.emptyList());
        });

        // invalid subconfig
        assertThrows(IllegalArgumentException.class, () -> {
            config.set("a", "test");
            config.set("a.b.c", "wrong");
        });

        // invalid subconfig
        assertThrows(IllegalArgumentException.class, () -> {
            config.set("a", "test");
            config.setComment("a", "test-comment");
            config.setComment("a.b.c", "wrong");
        });
    }

    public static void testIterators(ConcurrentConfig config) {
        config.set("key1", "value1");
        config.set("key2", "value2");
        config.set("key3", "value3");
        
        var entries = config.entrySet();
        // test forEach
        entries.forEach(e -> {
            assertNotNull(e.getKey());
            assertNotNull(e.getValue());
            assertNotNull(e.getRawValue());
            e.setValue("new-" + e.getValue());
        });

        // test Iterator
        for (Entry e : entries) {
            assertNotNull(e.getKey());
            assertNotNull(e.getValue());
            assertNotNull(e.getRawValue());
            assertTrue(((String)e.getValue()).startsWith("new-"));
            e.setValue("new2-" + e.getValue());
        }

        // test Spliterator
        var keys = entries.stream().map(e -> e.getKey()).collect(Collectors.toList());
        assertEquals(config.size(), keys.size());
        System.err.println(keys);

        // test Iterator.remove
        var iter = entries.iterator();
        while (iter.hasNext()) {
            Entry e = iter.next();
            assertNotNull(e.getKey());
            assertNotNull(e.getValue());
            assertNotNull(e.getRawValue());
            assertTrue(((String)e.getValue()).startsWith("new2-"));
            iter.remove();
        }
        assertTrue(config.isEmpty());

        // test Iterator errors
        config.set("key", "value");
        assertThrows(IllegalStateException.class, () -> {
            var it = entries.iterator();
            it.next();
            it.remove();
            it.remove();
        });
        assertThrows(Exception.class, () -> {
            var it = entries.iterator();
            for (int i = 0; i < 1000; i++) {
                it.next();
            }
        });
    }

    public static void testConcurrentCounters(ConcurrentConfig config) throws InterruptedException {
        int nThreads = Runtime.getRuntime().availableProcessors();
        var executor = Executors.newFixedThreadPool(nThreads);
        var counters = new ConcurrentHashMap<List<String>, AtomicInteger>(nThreads);
        for (int i = 0; i < nThreads; i++) {
            var counter = new AtomicInteger(0);
            var path = Collections.singletonList(String.valueOf(i));
            counters.put(path, counter);
            config.set(path, 0);
            executor.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    var newValue = counter.incrementAndGet();
                    var previousValue = newValue - 1;
                    assertEquals(previousValue, config.<Integer>set(path, newValue));
                }
            });
        }
        Thread.sleep(5_000); // wait for 5 seconds
        executor.shutdownNow();
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);

        // check the counters
        counters.forEach((path, counter) -> {
            assertEquals(counter.get(), config.<Integer>get(path));
        }); 
    }

    public static void testComments(ConcurrentCommentedConfig config) {
        assertTrue(config.isEmpty());

        // insert and remove comments into the config
        for (var key : testKeys) {
            var path = Collections.singletonList(key);
            var comment = key + "-comment";
            config.set(path, "value");
            config.setComment(path, comment);
            assertEquals(comment, config.getComment(path));
            assertTrue(config.containsComment(path));
            assertEquals(comment, config.removeComment(path));
            assertNull(config.getComment(path));
            config.setComment(path, comment);
            assertEquals(comment, config.getComment(path));
        }

        // check the comments
        {
            var entries = config.entrySet();
            Set<String> comments = entries.stream().map(e -> e.getComment()).collect(Collectors.toSet());
            assertEquals(setTestComments, comments);
        }

        // clear comments
        config.clearComments();
        {
            var entries = config.entrySet();
            Set<String> comments = entries.stream().map(e -> e.getComment()).collect(Collectors.toSet());
            assertTrue(comments.stream().allMatch(c -> c == null));

            for (var key : testKeys) {
                var path = Collections.singletonList(key);
                assertFalse(config.containsComment(path));
            }
        }

        // insert some more
        for (var key : testKeys) {
            var path = Collections.singletonList(key);
            config.setComment(path, "abcd");
        }
        config.setComment("z.z.c.d.e.f.g", "\n\n\n\n");
        assertEquals("\n\n\n\n", config.getComment("z.z.c.d.e.f.g"));
        assertEquals("\n\n\n\n", config.removeComment("z.z.c.d.e.f.g"));
        assertNull(config.setComment("z.z.c.d.e.f.g", "comment!"));
        assertEquals("comment!", config.setComment("z.z.c.d.e.f.g", "new-comment"));

        // clear again
        config.clearComments();
        for (var key : testKeys) {
            var path = Collections.singletonList(key);
            assertFalse(config.containsComment(path));
        }
        assertNull(config.getComment("z.z.c.d.e.f.g"));
        assertFalse(config.containsComment("z.z.c.d.e.f.g"));
    }
}
