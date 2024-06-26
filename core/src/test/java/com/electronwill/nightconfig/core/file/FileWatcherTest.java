package com.electronwill.nightconfig.core.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.Isolated;

// other threads can slow down the threads in this test, which will make the "awaits" time out
@Isolated
@Execution(SAME_THREAD)
public class FileWatcherTest {

	@TempDir
	static Path tmp;

	static final Consumer<Throwable> onWatcherException = e -> {
		throw new RuntimeException(e); // fail the test
	};

	@Test
	public void singleFile() throws Exception {
		// no debouncing, no waiting on underlying filesystem watcher (handles new
		// messages immediately)
		FileWatcher watcher = new FileWatcher(Duration.ZERO, Duration.ZERO, onWatcherException);

		// ---- watch new file
		Path file = tmp.resolve("fileNotifications.txt"); // does not exist yet
		AtomicReference<CountDownLatch> ref = new AtomicReference<>(new CountDownLatch(1));
		watcher.addWatch(file, () -> ref.get().countDown());
		// wait a little bit to ensure that the wacher is all set up
		// (there is a background thread that needs to handle the commands sent by
		// addWatch)
		Thread.sleep(10);

		Files.createFile(file); // creates the file (1st notif)
		assertTrue(ref.get().await(100, TimeUnit.MILLISECONDS), "creation not detected");

		// reset count and write
		ref.set(new CountDownLatch(1));
		Files.write(file, Arrays.asList("something something")); // writes (2nd notif)
		assertTrue(ref.get().await(100, TimeUnit.MILLISECONDS), "write not detected");

		// reset count and write again
		ref.set(new CountDownLatch(1));
		Files.write(file, Arrays.asList("something else")); // writes (3rd notif)
		assertTrue(ref.get().await(100, TimeUnit.MILLISECONDS), "write not detected");

		ref.set(null); // if the handler is called again, which it should not, fail the test

		// ---- watch existing file
		file = tmp.resolve("fileNotifications-2.txt");
		ref.set(new CountDownLatch(1));
		Files.createFile(file);
		watcher.addWatch(file, () -> ref.get().countDown());
		Thread.sleep(10);

		Files.write(file, Arrays.asList("test2"));
		assertTrue(ref.get().await(100, TimeUnit.MILLISECONDS));

		// ---- change the watcher
		CountDownLatch newLatch = new CountDownLatch(1);
		watcher.setWatch(file, () -> newLatch.countDown());
		Thread.sleep(10);
		Files.write(file, Arrays.asList(":)"));
		assertTrue(ref.get().await(100, TimeUnit.MILLISECONDS));

		// ---- stop watching
		ref.set(null);
		watcher.removeWatch(file);
		Thread.sleep(10);
		Files.write(file, Arrays.asList("..."));
		Thread.sleep(50);

		// ---- shutdown the watcher
		watcher.stop();
	}

	/** Watches multiple files in multiple directories. */
	@Test
	public void multipleFiles() throws Exception {
		int nDirs = 10;
		int nFiles = 10;
		FileWatcher watcher = new FileWatcher(Duration.ZERO, Duration.ZERO, onWatcherException);
		CountDownLatch latch = new CountDownLatch(nDirs * nFiles);
		// watch many files
		for (int i = 0; i < nDirs; i++) {
			Path dir = tmp.resolve("sub-" + i);
			Files.createDirectory(dir);
			for (int j = 0; j < nFiles; j++) {
				Path file = dir.resolve("multipleFilesNotifications-" + j);
				watcher.addWatch(file, latch::countDown);
			}
		}
		// wait for the watchers to activate (should be quick)
		Thread.sleep(10);
		// generate an event on all the files
		for (int i = 0; i < nDirs; i++) {
			for (int j = 0; j < nFiles; j++) {
				Path dir = tmp.resolve("sub-" + i);
				Path file = dir.resolve("multipleFilesNotifications-" + j);
				writeAndSync(file, Arrays.asList("test"));
				assertTrue(Files.exists(file));
			}
		}
		// check that all the handlers have been called
		assertTrue(latch.await(200, TimeUnit.MILLISECONDS));

		// stop watching
		watcher.stop();
	}

	/**
	 * Watches files in some directories, and remove one of the dirs after a while.
	 */
	@Test
	public void dirNoLongerAccessible() throws Exception {
		FileWatcher watcher = new FileWatcher(Duration.ZERO, Duration.ZERO, onWatcherException);

		// create the directories
		Path dir1 = tmp.resolve("dir1");
		Path dir2 = tmp.resolve("dir2");
		Files.createDirectory(dir1);
		Files.createDirectory(dir2);

		// watch the files
		Path file1 = dir1.resolve("file1");
		Path file2 = dir2.resolve("file2");
		AtomicInteger notifCount1 = new AtomicInteger(0);
		AtomicInteger notifCount2 = new AtomicInteger(0);
		watcher.addWatch(file1, () -> notifCount1.incrementAndGet());
		watcher.addWatch(file2, () -> notifCount2.incrementAndGet());

		// wait for the watchers to activate (should be quick)
		Thread.sleep(10);

		// generate events on the files
		writeAndSync(file1, Arrays.asList("1"));
		writeAndSync(file2, Arrays.asList("2"));
		Thread.sleep(20); // wait for the event handler to be called
		int midCount1 = notifCount1.get();
		int midCount2 = notifCount2.get();

		// remove one directory
		Files.delete(file1);
		Files.delete(dir1);

		// generate events on the remaining files
		writeAndSync(file2, Arrays.asList("22"));
		for (int tries = 0; tries < 10 && notifCount2.get() == midCount2; tries++) {
			Thread.sleep(10);
		}
		int finalCount1 = notifCount1.get();
		int finalCount2 = notifCount2.get();
		assertEquals(midCount1, finalCount1);
		assertNotEquals(midCount2, finalCount2);
		assertTrue(finalCount2 > midCount2);
	}

	@Test
	public void multipleThreads() throws Exception {
		int n = 100;
		Path tmpFile = tmp.resolve("multipleThreads.txt");
		FileWatcher watcher = new FileWatcher(Duration.ofMillis(100), Duration.ZERO, onWatcherException);
		CountDownLatch latch = new CountDownLatch(n);
		List<Thread> threads = new ArrayList<>(n);
		// watch the file, from different threads
		for (int i = 0; i < n; i++) {
			Thread adder = new Thread(() -> {
				watcher.addWatch(tmpFile, latch::countDown);
			});
			threads.add(adder);
			adder.start();
		}
		// wait for the watchers to activate (should be quick)
		for (Thread adder : threads) {
			adder.join(50);
		}
		// write to the file (generates an event)
		writeAndSync(tmpFile, Arrays.asList("test"));

		// check that all the handlers have been called
		latch.await(200, TimeUnit.MILLISECONDS);

		// stop watching
		watcher.stop();
	}

	@Test
	public void rejectWhenStopped() throws Exception {
		FileWatcher watcher = new FileWatcher();
		watcher.stop();
		assertThrows(IllegalStateException.class, () -> {
			watcher.addWatch(tmp.resolve("whatever"), () -> {
				throw new RuntimeException("I should not be called!");
			});
		});
	}

	@Test
	public void debouncing() throws Exception {
		int n = 100;
		Path file = tmp.resolve("debouncing");
		Duration debounceTime = Duration.ofMillis(100);
		Duration debounceAndTolerance = debounceTime.plusMillis(11); // tolerate some delay on top of the debounce time
		FileWatcher watcher = new FileWatcher(debounceTime, onWatcherException);

		// watch the file
		AtomicInteger callCounter = new AtomicInteger(0);
		watcher.addWatch(file, () -> callCounter.getAndIncrement());

		// generate plenty of events, they should be debounced to only one
		Files.createFile(file);
		for (int i = 0; i < n; i++) {
			writeAndSync(file, Arrays.asList("a" + i));
		}
		assertEquals(0, callCounter.get());
		Thread.sleep(debounceAndTolerance.toMillis());
		assertEquals(1, callCounter.get());

		// modify the file again
		for (int j = 0; j < n; j++) {
			writeAndSync(file, Arrays.asList("b" + j, "second line"));
		}
		Thread.sleep(debounceAndTolerance.toMillis());
		assertEquals(2, callCounter.get());

		watcher.stop();
	}

	@Test
	public void debouncingInternals() throws Exception {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		Duration debounceDuration = Duration.ofMillis(10);
		Duration debounceAndTolerance = debounceDuration.plusMillis(11);

		AtomicInteger callCounter = new AtomicInteger(0);
		DebouncedRunnable r = new DebouncedRunnable(callCounter::getAndIncrement, debounceDuration);

		// call the DebouncedRunnable once and check that it's debounced
		r.run(executor);
		assertEquals(0, callCounter.get());
		Thread.sleep(debounceAndTolerance.toMillis());
		assertEquals(1, callCounter.get());

		// call it many times and check
		int n = 100;
		for (int i = 0; i < n; i++) {
			r.run(executor);
		}
		assertEquals(1, callCounter.get());
		Thread.sleep(debounceAndTolerance.toMillis());
		assertEquals(2, callCounter.get());
	}

	private void writeAndSync(Path file, List<String> lines) throws IOException {
		try (FileChannel chan = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
			for (String line : lines) {
				chan.write(ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8)));
			}
			chan.force(true);
		}
	}

	@Test
	public void badDirWatch() throws Exception {
		Path dir = Files.createDirectory(tmp.resolve("I am a directory"));
		FileWatcher watcher = new FileWatcher();
		assertThrows(IllegalArgumentException.class, () -> {
			watcher.addWatch(dir, () -> fail("should not happen"));
		});
	}
}
