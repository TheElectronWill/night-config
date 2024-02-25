package com.electronwill.nightconfig.core.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.AssertionError;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;

public class StampedConfigTest {
    private static StampedConfig newConfig() {
        return new StampedConfig(InMemoryCommentedFormat.defaultInstance(),
                Config.getDefaultMapCreator(false));
    }

    static StampedConfig.Accumulator newAccumulator() {
        return new StampedConfig.Accumulator(InMemoryCommentedFormat.defaultInstance(),
                Config.getDefaultMapCreator(false));
    }

    @Test
    public void basicSanity() {
        CommonTests.testBasicSanity(newConfig());
    }

    @Test
    public void errors() {
        CommonTests.testErrors(newConfig());
    }

    @Test
    public void comments() {
        CommonTests.testComments(newConfig());
    }

    @Test
    public void putAll() {
        CommonTests.testPutAll(newConfig(), newConfig());
        CommonTests.testPutAll(newConfig(), newAccumulator());
        CommonTests.testPutAll(newConfig(), SynchronizedConfigTest.newConfig());
    }

    @Test
    public void removeAll() {
        CommonTests.testRemoveAll(newConfig(), newConfig());
        CommonTests.testRemoveAll(newConfig(), newAccumulator());
        CommonTests.testPutAll(newConfig(), SynchronizedConfigTest.newConfig());
    }

    @Test
    public void putAllComments() {
        CommonTests.testPutAllComments(newConfig(), newConfig());
        CommonTests.testPutAllComments(newConfig(), newAccumulator());
        CommonTests.testPutAll(newConfig(), SynchronizedConfigTest.newConfig());
    }

    @Test
    public void iterators() {
        CommonTests.testIterators(newConfig());
    }

    @Test
    public void concurrentCounters() throws InterruptedException {
        CommonTests.testConcurrentCounters(newConfig());
    }

    @Test
    public void bulk() {
        CommonTests.testBulkOperations(newConfig());
    }

    @Test
    public void accumulator() {
        CommonTests.testBasicSanity(newAccumulator(), false);
        CommonTests.testErrors(newAccumulator());
        CommonTests.testComments(newAccumulator());
        CommonTests.testIterators(newAccumulator());
    }

    @Test
    public void replaceContentByAccumulator() throws InterruptedException {
        CommonTests.testReplaceContent(4, newConfig(), newAccumulator(),
                (a, b) -> a.replaceContentBy(b));
    }

    @Test
    public void replaceContentByConfig() throws InterruptedException {
        CommonTests.testReplaceContent(4, newConfig(), newConfig(),
                (a, b) -> a.replaceContentBy(b));
    }

    @Test
    @Timeout(value = 250, unit = TimeUnit.MILLISECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void illegalBulkOperations() {
        var config = newConfig();
        config.set("a.b", "value of a.b");
        config.set("key", "value of key");

        var error = AssertionError.class;
        var exception = IllegalStateException.class;

        // bulkRead
        assertThrows(error, () -> {
            config.bulkRead(view -> {
                config.get("a.b"); // bad! rejected by assertion, accepted if assertions are disabled because multiple concurrent reads are allowed
            });
        }, "bulkRead(view -> config.get(\"a.b\", ...)) should be rejected by assertion");
        assertThrows(exception, () -> {
            config.bulkRead(view -> {
                config.set("c", "..."); // 100% wrong, rejected!
            });
        }, "bulkRead(view -> config.set(\"c\", ...)) should be rejected");

        assertThrows(error, () -> {
            config.bulkRead(view -> {
                config.set("a.b", "..."); // currently accepted too, because we first read "a" and then modify it to add entry "b"
            });
        }, "bulkRead(view -> config.set(\"a.b\", ...)) should be rejected by assertion");

        assertThrows(error, () -> {
            config.bulkRead(view -> {
                config.remove("a.b");
            });
        }, "bulkRead(view -> config.remove(\"a.b\", ...)) should be rejected by assertions");

        assertThrows(exception, () -> {
            config.bulkRead(view -> {
                config.remove("c");
            });
        }, "bulkRead(view -> config.remove(\"c\", ...)) should be rejected");

        assertThrows(exception, () -> {
            config.bulkRead(view -> {
                config.clear();
            });
        }, "bulkRead(view -> config.clear()) should be rejected");

        // bulkCommentedRead
        assertThrows(exception, () -> {
            config.bulkCommentedRead(view -> {
                config.set("c", "..."); // wrong
            });
        }, "bulkCommentedRead(view -> config.set(\"c\", ...)) should be rejected");

        assertThrows(error, () -> {
            config.bulkCommentedRead(view -> {
                config.set("a.b", "..."); // wrong
            });
        }, "bulkCommentedRead(view -> config.set(\"a.b\", ...)) should be rejected by assertions");

        assertThrows(exception, () -> {
            config.bulkCommentedRead(view -> {
                config.setComment("c", "..."); // wrong
            });
        }, "bulkCommentedRead(view -> config.setComment(\"c\", ...)) should be rejected");

        assertThrows(error, () -> {
            config.bulkCommentedRead(view -> {
                config.setComment("a.b", "..."); // wrong
            });
        }, "bulkCommentedRead(view -> config.setComment(\"a.b\", ...)) should be rejected by assertions");

        assertThrows(error, () -> {
            config.bulkCommentedRead(view -> {
                config.remove("a.b"); // wrong
            });
        }, "bulkCommentedRead(view -> config.remove(\"a.b\", ...)) should be rejected by assertions");

        assertThrows(exception, () -> {
            config.bulkCommentedRead(view -> {
                config.clearComments(); // wrong
            });
        }, "bulkCommentedRead(view -> config.clearComments()) should be rejected");

        assertThrows(exception, () -> {
            config.bulkCommentedRead(view -> {
                config.clear(); // wrong
            });
        }, "bulkCommentedRead(view -> config.clear()) should be rejected");

        // bulkUpdate
        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.get("a.b"); // wrong
            });
        });

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.set("x", "value"); // wrong
            });
        });

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.getRaw("x"); // wrong
            });
        });

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.add("x", "value"); // wrong
            });
        });

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.addAll(view); // wrong
            });
        });
        System.out.println("221");

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.putAll(view); // wrong
            });
        });
        System.out.println("228");

        assertThrows(exception, () -> {
            config.bulkCommentedUpdate(view -> {
                config.putAllComments(view); // wrong
            });
        });
        System.out.println("235");

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.removeAll(view); // wrong
            });
        });
        System.out.println("242");

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                view.addAll(config); // wrong
            });
        });
        System.out.println("249");

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                view.putAll(config); // wrong
            });
        });
        System.out.println("256");

        assertThrows(exception, () -> {
            config.bulkCommentedUpdate(view -> {
                view.putAllComments(config); // wrong
            });
        });
        System.out.println("263");

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                view.removeAll(config); // wrong
            });
        });
        System.out.println("270");

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.remove("a.b"); // wrong
            });
        });
        System.out.println("277");

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.clear(); // wrong
            });
        });
        System.out.println("284");

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.clearComments(); // wrong
            });
        });
        System.out.println("291");

        assertThrows(exception, () -> {
            config.bulkUpdate(view -> {
                config.removeAll(view); // wrong
            });
        });
        System.out.println("298");
    }

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void illegalIterationOperations() {
        var config = newConfig();
        config.set("a.b", "value of a.b");
        config.set("key", "value of key");

        var error = AssertionError.class;
        var exception = IllegalStateException.class;

        // during a bulk operation, iteration on the config (not the view)
        assertThrows(exception, () -> {
            config.bulkRead(view -> {
                config.entrySet().forEach(System.out::println);
            });
        });
        assertThrows(exception, () -> {
            config.bulkRead(view -> {
                config.entrySet().iterator().forEachRemaining(System.out::println);
            });
        });

        assertThrows(exception, () -> {
            config.bulkRead(view -> {
                config.entrySet().forEach(entry -> {
                    if (entry.equals(config.get("a.b"))) {
                        fail("this should never happen: illegal state should have been thrown earlier");
                    }
                });
            });
        });

        // For now, this works because:
        // 1. the iterator is a snapshot of the config at the moment of its creation
        // 2. config.remove() does not check the config thread state if the lock can be optimistically acquired
        // BUT you should probably NOT do this in your code.
        assertDoesNotThrow(() -> {
            var it = config.entrySet().iterator();
            if (it.hasNext()) {
                var entry = it.next();
                config.remove(entry.getKey()); // discouraged: use it.remove() instead!
            }
        });

        // this is not ok
        assertThrows(exception, () -> {
            config.entrySet().forEach(e -> {
                config.entrySet().iterator().forEachRemaining(o -> fail());
            });
        });
        assertThrows(exception, () -> {
            config.entrySet().forEach(e -> {
                config.bulkRead(view -> {
                    fail();
                });
            });
        });
        assertThrows(exception, () -> {
            config.entrySet().forEach(e -> {
                config.bulkUpdate(view -> {
                    fail();
                });
            });
        });
        assertThrows(exception, () -> {
            config.entrySet().forEach(e -> {
                config.bulkCommentedRead(view -> {
                    fail();
                });
            });
        });
        assertThrows(exception, () -> {
            config.entrySet().forEach(e -> {
                config.bulkCommentedUpdate(view -> {
                    fail();
                });
            });
        });
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void multithreadDeadlockPrevention() throws InterruptedException {
        var config = newConfig();
        config.set("a.b", "value of a.b");
        config.set("key", "value of key");

        var runFlag = new AtomicBoolean(true);
        int nThreads = Runtime.getRuntime().availableProcessors();
        var executor = Executors.newFixedThreadPool(nThreads);

        var nGoodThreads = Math.max(2, nThreads - 2);
        var nBadThreads = 2;

        var goodFutures = new ArrayList<Future<?>>();
        var badFutures = new ArrayList<Future<?>>();

        for (int i = 0; i < nGoodThreads; i++) {
            var threadNumber = i;
            var future = executor.submit(() -> {
                while (runFlag.get()) {
                    assertDoesNotThrow(() -> {
                        config.get("a.b.c");
                        config.set("key", "new value from thread " + threadNumber);
                    });
                }
            });
            goodFutures.add(future);
        }
        for (int i = 0; i < nBadThreads; i++) {
            var future = executor.submit(() -> {
                while (runFlag.get()) {
                    assertThrows(IllegalStateException.class, () -> {
                        config.bulkRead(view -> {
                            config.bulkUpdate(view2 -> {
                                config.entrySet().forEach(e -> fail());
                            });
                        });
                    });
                }
            });
            badFutures.add(future);
        }
        Thread.sleep(500); // wait for 0.5 seconds
        runFlag.set(false);
        executor.shutdownNow();
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);

        for (var f : goodFutures) {
            try {
                assertNull(f.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                fail("\"good\" future failed ! " + e);
            }
        }

        for (var f : goodFutures) {
            try {
                assertNull(f.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                fail("\"bad\" future failed ! " + e);
            }
        }

        // check the config
        assertEquals("value of a.b", config.get("a.b"));
        assertTrue(config.<String>get("key").startsWith("new value from thread"),
                "wrong value: key = " + config.get("key"));
    }
}
