package com.electronwill.nightconfig.core.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig.CommentNode;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.IncompatibleIntermediaryLevelException;
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

    public static void testBasicSanity(ConcurrentCommentedConfig config) {
        testBasicSanity(config, true);
    }

    public static void testBasicSanity(Config config, boolean checkSubconfigClass) {
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
            assertFalse(config.contains(Arrays.asList("sub", "sub")));
            assertFalse(config.contains(Arrays.asList("config")));
        }
        if (checkSubconfigClass) {
            assertInstanceOf(config.getClass(), config.get("sub"));
            assertInstanceOf(config.getClass(), config.get("sub.config"));
            assertInstanceOf(config.getClass(), config.remove("sub"));
        }

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

    public static void testErrors(CommentedConfig config) {
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
        assertThrows(IncompatibleIntermediaryLevelException.class, () -> {
            config.set("a", "test");
            config.set("a.b.c", "wrong");
        });

        // invalid subconfig
        assertThrows(IncompatibleIntermediaryLevelException.class, () -> {
            config.set("a", "test");
            config.setComment("a", "test-comment");
            config.setComment("a.b.c", "wrong");
        });
    }

    public static void testIterators(CommentedConfig config) {
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
        // System.err.println(keys);

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

    public static void testComments(CommentedConfig config) {
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

    public static void testPutAll(Config a, Config b) {
        // fill B
        assertTrue(b.isEmpty());
        for (var key : testKeys) {
            var path = Collections.singletonList(key);
            b.set(path, testValues[0]);

            var listPath = Collections.singletonList("list-"+key);
            var sub = b.createSubConfig();
            sub.set(path, testValues[0]);
            b.set(listPath, Arrays.asList(testValues[0], sub));

            var nested1 = Arrays.asList("sub", key);
            b.set(nested1, testValues[1]);

            var nested2 = Arrays.asList("deeply","nested", key);
            b.set(nested2, testValues[2]);
        }

        // A.putAll(B)
        assertTrue(a.isEmpty());
        a.putAll(b);
        assertEquals(b.size(), a.size());

        // check A
        for (var key : testKeys) {
            var path = Collections.singletonList(key);
            assertTrue(a.contains(path));
            assertSame(testValues[0], a.get(path));

            var listPath = Collections.singletonList("list-"+key);
            assertTrue(a.contains(listPath));
            assertInstanceOf(List.class, a.get(listPath));
            var configInList = a.<List<Object>>get(listPath).get(1);
            assertInstanceOf(Config.class, configInList);
            assertEquals(testValues[0], ((Config)configInList).get(path));

            var nested1 = Arrays.asList("sub", key);
            assertTrue(a.contains(nested1));
            assertSame(testValues[1], a.get(nested1));

            var nested2 = Arrays.asList("deeply","nested", key);
            assertTrue(a.contains(nested2));
            assertSame(testValues[2], a.get(nested2));
        }
    }

    public static void testRemoveAll(Config a, Config b) {
        // fill A with some entries that will be removed, some that will not
        assertTrue(a.isEmpty());
        a.set(Collections.singletonList(testKeys[0]), "to-remove");
        a.set(Collections.singletonList(testKeys[1]), "to-remove");
        a.set(Arrays.asList("sub", testKeys[0]), "to-remove");
        a.set(Arrays.asList("sub", testKeys[1]), "to-remove");
        a.set(Arrays.asList("deeply", "nested", testKeys[0]), "to-remove");
        a.set(Arrays.asList("deeply", "nested", testKeys[1]), "to-remove");
        a.set("key-to-keep", "do-not-remove");
        a.set("other key to keep", "keep me!");
        a.set(Arrays.asList("sub", "I will be removed too, actually"), "Goodbye, world!");

        // fill B
        assertTrue(b.isEmpty());
        for (var key : testKeys) {
            var path = Collections.singletonList(key);
            b.set(path, testValues[0]);

            var nested1 = Arrays.asList("sub", key);
            b.set(nested1, testValues[2]);

            var nested2 = Arrays.asList("deeply","nested", key);
            b.set(nested2, testValues[2]);
        }

        // A.removeAll(B)
        var initialSize = a.size();
        a.removeAll(b);
        assertTrue(a.size() < initialSize);

        // check removed entries
        for (var key : testKeys) {
            var path = Collections.singletonList(key);
            assertFalse(a.contains(path));
            assertNull(a.get(path));

            var nested1 = Arrays.asList("sub", key);
            assertFalse(a.contains(nested1));
            assertNull(a.get(nested1));

            var nested2 = Arrays.asList("deeply","nested", key);
            assertFalse(a.contains(nested2));
            assertNull(a.get(nested2));
        }
        // check kept entries
        assertTrue(a.contains("key-to-keep"));
        assertEquals("do-not-remove", a.get("key-to-keep"));
        assertTrue(a.contains("other key to keep"));
        assertFalse(a.contains(Arrays.asList("sub", "keep me please")));
    }

    public static void testPutAllComments(CommentedConfig a, CommentedConfig b) {
        // fill B
        assertTrue(b.isEmpty());
        for (var key : testKeys) {
            var path = Collections.singletonList(key);
            b.set(path, testValues[0]);
            b.setComment(path, "simple comment");

            var nested1 = Arrays.asList("sub", key);
            b.set(nested1, testValues[1]);
            b.setComment(nested1, "sub comment");
        }

        // A.putAllComments(B)
        assertTrue(a.isEmpty());
        a.putAllComments(b);
        assertTrue(a.isEmpty());

        // Because we didn't call putAll, only the top-level comments are copied, no subconfig is created!
        for (var key : testKeys) {
            var path = Collections.singletonList(key);
            assertTrue(a.containsComment(path));
            assertEquals("simple comment", a.getComment(path));

            var nested1 = Arrays.asList("sub", key);
            assertFalse(a.containsComment(nested1));
            assertNull( a.getComment(nested1));
        }

        // Create the subconfigs
        for (var key : testKeys) {
            var nested1 = Arrays.asList("sub", key);
            a.set(nested1, "arbitrary value");
        }
        assertFalse(a.isEmpty());
        a.putAllComments(b);
        assertFalse(a.isEmpty());

        // Now, all the entries exist, all the comments will be copied.
        for (var key : testKeys) {
            var path = Collections.singletonList(key);
            assertTrue(a.containsComment(path));
            assertEquals("simple comment", a.getComment(path));

            var nested1 = Arrays.asList("sub", key);
            assertTrue(a.containsComment(nested1));
            assertEquals("sub comment", a.getComment(nested1));
        }

        // Test putAllComments(Map<String, CommentNode>)
        a.clear();
        b.clear();
        var comments = new HashMap<String, CommentNode>();
        var commentsNested = new HashMap<String, CommentNode>();
        commentsNested.put("nested", new CommentNode("nestedComm", null));
        comments.put("a", new CommentNode("aComm", commentsNested));
        a.putAllComments(comments);
        assertEquals("aComm", a.getComment("a"));
        assertNull( a.getComment("a.nested"));
    }

    public static void testBulkOperations(ConcurrentCommentedConfig config) {
        config.set("a", "val-a");
        config.set("sub.nested.deep.more", "amazing");
        assertEquals("val-a", config.get("a"));
        assertEquals("amazing", config.get("sub.nested.deep.more"));
        config.bulkCommentedRead(view -> {
            var valueA = view.get("a");
            var valueB = view.get("sub.nested.deep.more");
            assertEquals("val-a", valueA, "unexpected value: " + valueA + ",\nwhole view: " + view);
            assertEquals("amazing", valueB, "unexpected value: " + valueB + ",\nwhole view: " + view);
            assertEquals(config.size(), view.size());
            assertEquals(config.size(), view.entrySet().size());
            for (var entry : view.entrySet()) {
                var key = entry.getKey();
                assertTrue(key.equals("a") || key.equals("sub"));
                if (key == "a") {
                    assertEquals("val-a", entry.getValue());
                    assertEquals("val-a", entry.getRawValue());
                } else {
                    assertInstanceOf(Config.class, entry.getValue());
                }
            }
            assertTrue(view.entrySet().iterator().hasNext());
        });
        config.clear();
        config.clearComments();
        config.bulkUpdate(view -> {
            testBasicSanity(view, false);
        });
        config.bulkCommentedUpdate(view -> {
            view.clear();
            view.clearComments();
            testBasicSanity(view, false);
            view.clear();
            view.clearComments();
            testComments(view);
        });
    }

    /**
     * From multiple threads, check that the integrity of the config is respected, i.e.
     * that we only see either the old version or the new version, not a mix of the two.
     *
     * @param keys
     * @param oldValues
     * @param newValues
     */
    public static List<Future<?>> checkConcurrentConfigIntegrity(ExecutorService executor, int nThreads, ConcurrentConfig config, List<String> keys, List<?> oldValues, List<?> newValues) {
        var reverseKeys = reversed(keys);
        var reverseOldValues = reversed(oldValues);
        var reverseNewValues = reversed(newValues);
        var futures = new ArrayList<Future<?>>();
        for (int i = 0; i < nThreads/2; i++) {
            var fut1 = executor.submit(() -> {
                var isOld = true;
                while (isOld) {
                    // It is important to use bulkRead, otherwise the call to `replaceContentBy` could occur between two `get`
                    var currentValues = config.bulkRead(view -> {
                        return keys.stream().map(k -> view.get(k)).collect(Collectors.toList());
                    });
                    isOld = currentValues.equals(oldValues);
                    var isNew = currentValues.equals(newValues);
                    assertTrue(isOld || isNew, "Corrupted config values: " + currentValues + "\n full config (may have changed): " + config);
                    Thread.yield();
                }
            });
            var fut2 = executor.submit(() -> {
                var isOld = true;
                while (isOld) {
                    var currentValues = config.bulkRead(view -> {
                        return reverseKeys.stream().map(k -> view.get(k)).collect(Collectors.toList());
                    });
                    isOld = currentValues.equals(reverseOldValues);
                    var isNew = currentValues.equals(reverseNewValues);
                    assertTrue(isOld || isNew, "Corrupted config values (r): " + currentValues + "\n full config (may have changed): " + config);
                    Thread.yield();
                }
            });
            futures.add(fut1);
            futures.add(fut2);
        }
        return futures;
    }

    private static <T> List<T> reversed(List<T> list) {
        var c = new ArrayList<>(list);
        Collections.reverse(c);
        return c;
    }

    public static <A extends ConcurrentCommentedConfig, B extends Config> void testReplaceContent(
        int nThreads, A config, B replacement, BiConsumer<A,B> replaceContentBy) {

        assertTrue(config.isEmpty());
        assertTrue(replacement.isEmpty());

        var executor = Executors.newFixedThreadPool(nThreads);
        var keys = Arrays.asList("a", "b", "c", "e", "sub.a", "sub.b", "sub.nested.a",
                "sub.nested.b");
        var oldValues = keys.stream().map(k -> "old value of " + k).collect(Collectors.toList());
        var newValues = keys.stream().map(k -> {
            var value = "new value of " + k;
            if (k.equals("c")) {
                // write an array of configs to test this specific case
                var sub = replacement.createSubConfig();
                sub.set(k + "-sub", value);
                return Arrays.asList(sub);
            } else {
                return value;
            }
        }).collect(Collectors.toList());

        // fill config
        for (int i = 0; i < keys.size(); i++) {
            var key = keys.get(i);
            var val = oldValues.get(i);
            config.set(key, val);
        }

        // From multiple threads, check the integrity of the config: either full old version, or full new version.
        var futures = checkConcurrentConfigIntegrity(executor, nThreads, config, keys, oldValues,
                newValues);

        // fill replacement config/accumulator/whatever
        for (int i = 0; i < keys.size(); i++) {
            var key = keys.get(i);
            var val = newValues.get(i);
            replacement.set(key, val);
        }

        // replace the config's content, the tasks submitted to the executor should stop when they see the new content
        replaceContentBy.accept(config, replacement);

        // wait for the tasks to finish
        for (var fut : futures) {
            assertDoesNotThrow(() -> fut.get(1, TimeUnit.SECONDS));
        }
        executor.shutdown();
    }

}
