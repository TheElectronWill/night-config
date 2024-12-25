package com.electronwill.nightconfig.core.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * A FileWatcher can watch several files asynchronously.
 * <p>
 * New watches are added with the {@link #addWatch(Path, Runnable)} method, which specifies the
 * task to execute when the file is modified.
 * <p>
 * This class is thread-safe.
 *
 * @author TheElectronWill
 */
public final class FileWatcher {
	private static final AtomicInteger instanceCount = new AtomicInteger(0);
	private static volatile FileWatcher DEFAULT_INSTANCE = null; // created on demand

	/**
	 * Gets the default, global instance of FileWatcher.
	 * If the previous global instance has been stopped, create and start a new instance.
	 *
	 * @return the default FileWatcher
	 */
	public static synchronized FileWatcher defaultInstance() {
		if (DEFAULT_INSTANCE == null || !DEFAULT_INSTANCE.running) {// null or stopped FileWatcher
			DEFAULT_INSTANCE = new FileWatcher();
		}
		return DEFAULT_INSTANCE;
	}

	public static class WatchingException extends RuntimeException {
		public WatchingException(String message, Throwable cause) {
			super(message, cause);
		}

		public WatchingException(Throwable cause) {
			super(cause);
		}

		public WatchingException(String message) {
			super(message);
		}
	}

	/** The timeout for {@link WatchService#poll(long, TimeUnit)}.
	 * If there's no filesystem event, commands sent to the watcher thread will be taken into account after the expiration of this timeout.
	 * Therefore, it should not be too high.
	 */
	private static final Duration DEFAULT_SERVICE_POLL_TIMEOUT = Duration.ofMillis(200);

	/** When a file is modified, wait for the debounce time to expire before triggering the change handler.
	 * Any modification resets the countdown, there needs to be a period of "calm" before the handler is triggered.
	 */
	private static final Duration DEFAULT_DEBOUNCE_TIME = Duration.ofMillis(500);

	private final ThreadGroup threadGroup;
	private final AtomicInteger threadCount = new AtomicInteger(0);
	private final ConcurrentMap<FileSystem, FsWatcher> watchers = new ConcurrentHashMap<>();
	private final Consumer<Throwable> exceptionHandler;
	private final Duration debounceTime;
	private final long servicePollTimeoutNanos;
	private final int instanceId;
	private volatile boolean running = true;

	/**
	 * Creates a new FileWatcher with a default exception handler and a default debounce time.
	 *
	 * The default exception handler is simply:
	 * <code>
	 * {@code (e) -> e.printStackTrace(); }
	 * </code>
	 *
	 * The default debounce time is unspecified and may change in the future, but is typically
	 * around 1 second or less.
	 */
	public FileWatcher() {
		this(DEFAULT_DEBOUNCE_TIME);
	}

	/**
	 * Creates a new FileWatcher with the default debounce time and the given exception handler.
	 * <p>
	 * The default debounce time is unspecified and may change in the future, but is typically
	 * around 1 second or less.
	 */
	public FileWatcher(Consumer<Exception> exceptionHandler) {
		// This constructor accepts a Consumer<Exception> for backward-compatibility reasons.
		this(DEFAULT_DEBOUNCE_TIME, t -> {
			if (t instanceof Exception) {
				exceptionHandler.accept((Exception)t);
			} else {
				exceptionHandler.accept(new RuntimeException(t));
			}
		});
	}

	/**
	 * Creates a new FileWatcher with a default exception handler.
	 *
	 * The default exception handler is simply:
	 * <code>
	 * {@code (e) -> e.printStackTrace();}
	 * </code>
	 *
	 * @param debounceTime hwo much time to wait after each modification of a file before triggering the change handler
	 */
	public FileWatcher(Duration debounceTime) {
		this(debounceTime, Throwable::printStackTrace);
	}

	/**
	 * Creates a new FileWatcher with the given exception handler.
	 *
	 * @param debounceTime delay between each call of the file's changeHandler.
	 * @param exceptionHandler called when an exception occurs during the handling of file events
	 */
	public FileWatcher(Duration debounceTime, Consumer<Throwable> exceptionHandler) {
		this(debounceTime, DEFAULT_SERVICE_POLL_TIMEOUT, exceptionHandler);
	}

	// Full constructor that allows to specify the service poll timeout, not part of the public API yet
	// because this is an implementation detail (could be opened in the future if the users need it).
	FileWatcher(Duration debounceTime, Duration servicePollTimeout, Consumer<Throwable> exceptionHandler) {
		this.instanceId = instanceCount.getAndIncrement();
		this.debounceTime = debounceTime;
		this.servicePollTimeoutNanos = servicePollTimeout.toNanos();
		this.exceptionHandler = exceptionHandler;
		this.threadGroup = new ThreadGroup("watchers-" + instanceId);
	}

	/**
	 * Watches a file, if not already watched by this FileWatcher.
	 * The file's parent directory must exist.
	 * <p>
	 * NOTE: This method may return before the handler is set up.
	 * Prefer to use {@link #addWatchFuture(Path, Runnable)}.
	 *
	 * @param file          the file to watch
	 * @param changeHandler the handler to call when the file is modified
	 */
	public void addWatch(File file, Runnable changeHandler) {
		addWatch(file.toPath(), changeHandler);
	}

	/**
	 * Watches a file, if not already watched by this FileWatcher.
	 * The file's parent directory must exist.
	 * <p>
	 * NOTE: This method may return before the handler is set up.
	 * Prefer to use {@link #addWatchFuture(Path, Runnable)}.
	 *
	 * @param file          the file to watch
	 * @param changeHandler the handler to call when the file is modified
	 */
	public void addWatch(Path file, Runnable changeHandler) {
		addOrPutWatch(file, changeHandler, ControlMessageKind.ADD, null);
	}

	/**
	 * Watches a File, if not already watched by this FileWatcher.
	 * The file's parent directory must exist.
	 * <p>
	 * This method returns a {@code CompletableFuture} that is completed when the
	 * handler is registered and ready to be notified of file events.
	 *
	 * <h2>Examples</h2>
	 *
	 * Waiting for the FileWatcher to register one handler:
	 * <pre>
	 * {@code
	 * FileWatcher watcher = FileWatcher.defaultInstance();
	 * CompletableFuture<Void> f = watcher.addWatchFuture(file, handler);
	 * f.join();
	 * }
	 * </pre>
	 *
	 * Watching multiple files and waiting for the handlers to be ready,
	 * with a timeout of 1 second:
	 * <pre>
	 * {@code
	 * FileWatcher watcher = FileWatcher.defaultInstance();
	 * List<CompletableFuture<Void>> futures = new ArrayList<>();
	 * for (Path path : filesToWatch) {
	 *     futures.add(watcher.addWatchFuture(file, () -> {
	 *         // react to file creation or modification
	 *     }));
	 * }
	 * CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get(1, TimeUnit.SECONDS);
	 * }
	 * </pre>
	 *
	 * @param file          the file to watch
	 * @param changeHandler the handler to call when the file is modified
	 * @return a future that completes when the handler is ready
	 */
	public CompletableFuture<Void> addWatchFuture(Path file, Runnable changeHandler) {
		failIfStopped();
		CompletableFuture<Void> future = new CompletableFuture<>();
		try {
			addOrPutWatch(file, changeHandler, ControlMessageKind.ADD, future);
		} catch (Exception ex) {
			future.completeExceptionally(ex);
		}
		return future;
	}

	/**
	 * Watches a file. If the file is already watched by this FileWatcher, its changeHandler is
	 * replaced.
	 * The file's parent directory must exist.
	 * <p>
	 * NOTE: This method may return before the handler is set up.
	 * Prefer to use {@link #setWatchFuture(Path, Runnable)}.
	 *
	 * @param file          the file to watch
	 * @param changeHandler the handler to call when the file is modified
	 */
	public void setWatch(File file, Runnable changeHandler) {
		setWatch(file.toPath(), changeHandler);
	}

	/**
	 * Watches a file. If the file is already watched by this FileWatcher, its changeHandler is
	 * replaced.
	 * The file's parent directory must exist.
	 * <p>
	 * NOTE: This method may return before the handler is set up.
	 * Prefer to use {@link #setWatchFuture(Path, Runnable)}.
	 *
	 * @param file          the file to watch
	 * @param changeHandler the handler to call when the file is modified
	 */
	public void setWatch(Path file, Runnable changeHandler) {
		addOrPutWatch(file, changeHandler, ControlMessageKind.PUT, null);
	}

	public CompletableFuture<Void> setWatchFuture(Path file, Runnable changeHandler) {
		failIfStopped();
		CompletableFuture<Void> future = new CompletableFuture<>();
		try {
			addOrPutWatch(file, changeHandler, ControlMessageKind.PUT, future);
		} catch (Exception ex) {
			future.completeExceptionally(ex);
		}
		return future;
	}

	private void addOrPutWatch(Path file, Runnable changeHandler, ControlMessageKind kind, CompletableFuture<Void> future) {
		failIfStopped();
		try {
			if (Files.exists(file) && Files.readAttributes(file, BasicFileAttributes.class).isDirectory()) {
				throw new IllegalArgumentException(
						"FileWatcher is designed to watch files but this path is a directory, not a file: " + file);
			}
		} catch (IOException ex) {
			throw new WatchingException("Failed to get information about path: " + file, ex);
		}
		CanonicalPath canon = CanonicalPath.from(file);
		FileSystem fs = canon.parentDirectory.getFileSystem();
		try {
			FsWatcher watcher = watchers.computeIfAbsent(fs, k -> {
				// start a new watcher for this filesystem
				try {
					WatchService service = fs.newWatchService();
					FsWatcher w = new FsWatcher(exceptionHandler, debounceTime, servicePollTimeoutNanos, service);
					String threadName = "config-file-watcher-" + instanceId + "-" + threadCount.getAndIncrement();
					Thread t = new Thread(threadGroup, w, threadName);
					t.setDaemon(true);
					t.start();
					return w;
				} catch (IOException ex) {
					throw new WatchingException("Failed to start a new watcher thread for directory " + canon.parentDirectory, ex);
				}
			});
			// tell the watcher thread to watch the file
			watcher.send(ControlMessage.addOrPut(kind, canon, changeHandler, future));
		} catch (Exception ex) {
			throw new WatchingException("Failed to watch path '" + file + "', canonical path '" + canon + "'", ex);
		}
	}

	/**
	 * Stops watching a file.
	 * <p>
	 * NOTE: This method may return before the handler is removed.
	 * Prefer to use {@link #setWatchFuture(Path, Runnable)}.
	 *
	 * @param file the file to stop watching
	 */
	public void removeWatch(File file) {
		removeWatch(file.toPath());
	}

	/**
	 * Stops watching a file.
	 * <p>
	 * NOTE: This method may return before the handler is removed.
	 * Prefer to use {@link #setWatchFuture(Path, Runnable)}.
	 *
	 * @param file the file to stop watching
	 */
	public void removeWatch(Path file) {
		failIfStopped();
		removeWatch(file, null);
	}

	public CompletableFuture<Void> removeWatchFuture(Path file) {
		failIfStopped();
		CompletableFuture<Void> future = new CompletableFuture<>();
		try {
			removeWatch(file, future);
		} catch (Exception ex) {
			future.completeExceptionally(ex);
		}
		return future;
	}

	private void removeWatch(Path file, CompletableFuture<Void> future) {
		CanonicalPath canon = CanonicalPath.from(file);
		FileSystem fs = canon.parentDirectory.getFileSystem();
		FsWatcher watcher = watchers.get(fs);
		if (watcher != null) {
			watcher.send(ControlMessage.remove(canon, future));
		}
	}

	/**
	 * Stops this FileWatcher. The underlying ressources (ie the WatchServices) are closed, and
	 * the file modification handlers won't be called anymore.
	 * <p>
	 * NOTE: This method may return before the FileWatcher is completely stopped.
	 * Prefer to use {@link #stopFuture()}.
	 */
	public void stop() {
		// prevent further use of the FileWatcher
		running = false;

		// stop each watcher thread
		for (FsWatcher watcher : watchers.values()) {
			watcher.send(ControlMessage.poison(null));
		}
		// interrupt each watcher thread so that they handle the "poison" asap
		threadGroup.interrupt();
	}

	public CompletableFuture<Void> stopFuture() {
		// prevent further use of the FileWatcher
		running = false;

		Collection<FsWatcher> allWatchers = watchers.values();
		if (allWatchers.size() == 0) {
			// There is no background watcher thread to stop, we're done.
			return CompletableFuture.completedFuture(null);
		}

		// create the main future
		CompletableFuture<Void> main = new CompletableFuture<>();
		AtomicInteger remainingChildCount = new AtomicInteger(allWatchers.size());

		// stop each watcher thread
		for (FsWatcher watcher : allWatchers) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.handle((ok, err) -> {
				int remaining = remainingChildCount.decrementAndGet();
				if (remaining == 0) {
					// When every watcher thread is stopped, complete the main future.
					if (err == null) {
						main.complete(null);
					} else {
						main.completeExceptionally(err);
					}
				}
				return null;
			});
			watcher.send(ControlMessage.poison(f));
		}
		// interrupt each watcher thread so that they handle the "poison" asap
		threadGroup.interrupt();

		// return the main future
		return main;
	}

	private void failIfStopped() {
		if (!running) {
			throw new IllegalStateException("FileWatcher " + instanceId + " has been stopped and cannot be used anymore.");
		}
	}

	/** A directory watcher for one filesystem.
	 * The watcher runs in its own thread, and is controlled via asynchronous messages of type {@link ControlMessage}.
	 */
	private static final class FsWatcher implements Runnable {
		private final Consumer<Throwable> exceptionHandler;
		private final Duration debounceTime;
		private final long servicePollTimeoutNanos;

		private final WatchService watchService;
		private final Map<Path, WatchedDirectory> watchedDirectories = new HashMap<>();
		private final ConcurrentLinkedQueue<ControlMessage> controlMessages = new ConcurrentLinkedQueue<>();

		FsWatcher(Consumer<Throwable> exceptionHandler, Duration debounceTime, long servicePollTimeoutNanos, WatchService watchService) {
			this.exceptionHandler = exceptionHandler;
			this.debounceTime = debounceTime;
			this.servicePollTimeoutNanos = servicePollTimeoutNanos;
			this.watchService = watchService;
		}

		void send(ControlMessage msg) {
			controlMessages.add(msg);
		}

		/**
		 * Attempts to register a new directory to watch.
		 *
		 * If {@code future} is not null and an exception occurs (for instance if the
		 * directory does not exist), call {@link CompletableFuture#completeExceptionally}
		 * with the exception.
		 *
		 * If {@code future} is null and an exception occurs, call the
		 * {@code exceptionHandler}.
		 *
		 * @param dir    directory to watch
		 * @param future future to notify about failures
		 * @return info about the watched directory
		 */
		private WatchedDirectory watchDirectory(Path dir, CompletableFuture<Void> future) {
			return watchedDirectories.computeIfAbsent(dir, k -> {
				// the file's parent directory isn't monitored yet, register it
				WatchKey key;
				try {
					key = dir.register(watchService, ENTRY_MODIFY, ENTRY_CREATE);
					return new WatchedDirectory(key, new HashMap<>(8));
				} catch (Exception ex) {
					if (future != null) {
						future.completeExceptionally(ex);
					} else {
						exceptionHandler.accept(ex);
					}
					return null;
				}
			});
		}

		@Override
		public void run() {
			// executor (in yet another thread) to schedule debounced actions
			ThreadFactory threadFactory = new NamedDaemonThreadFactory("FileWatcher-", "-thread-");
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, threadFactory);

			// future that initiated the shutdown and needs to be completed with the result or error
			CompletableFuture<Void> shutdownFuture = null;

			mainLoop:
			while (true) {
				// handle control messages coming from other threads (modification of the watch list)
				ControlMessage msg;
				while ((msg = controlMessages.poll()) != null) {
					CanonicalPath path = msg.path;
					CompletableFuture<Void> future = msg.future;
					switch (msg.kind) {
						case ADD: {
							// Combine the handlers if there's already one, otherwise set it
							Path dir = path.parentDirectory;
							Path fileName = path.fileName;
							WatchedDirectory w = watchDirectory(dir, future);
							if (w != null) {
								DebouncedRunnable newHandler;
								DebouncedRunnable existingHandler = w.fileChangeHandlers.get(fileName);
								if (existingHandler != null) {
									newHandler = existingHandler.andThen(msg.handler);
								} else {
									newHandler = new DebouncedRunnable(msg.handler, debounceTime);
								}
								w.fileChangeHandlers.put(fileName, newHandler);
							}
							break;
						}
						case PUT: {
							// Set the handler, replacing any existing handler
							Path dir = path.parentDirectory;
							Path fileName = path.fileName;
							WatchedDirectory w = watchDirectory(dir, future);
							if (w != null) {
								DebouncedRunnable newHandler = new DebouncedRunnable(msg.handler, debounceTime);
								w.fileChangeHandlers.put(fileName, newHandler);
							}
							break;
						}
						case REMOVE: {
							// Stop watching a file
							Path dir = path.parentDirectory;
							Path fileName = path.fileName;
							WatchedDirectory w = watchedDirectories.get(dir);
							if (w != null) {
								w.fileChangeHandlers.remove(fileName);
								if (w.fileChangeHandlers.isEmpty()) {
									// no more file to watch in this directory
									w.key.cancel();
									// this will be done in the event loop below: watchedDirectories.remove(dir);
								}
							}
							break;
						}
						case POISON: {
							// Kill the thread
							shutdownFuture = future;
							break mainLoop;
						}
					}
					if (future != null) {
						future.complete(null);
					}
				}

				// poll the events from the filesystem (monitoring of the files)
				WatchKey key = null;
				try {
					key = watchService.poll(servicePollTimeoutNanos, TimeUnit.NANOSECONDS);
				} catch (InterruptedException e) {
					// loop back to the handling of control messages
					continue;
				}

				if (key != null) {
					// a key has been polled
					Path dir = (Path) key.watchable();
					WatchedDirectory w = watchedDirectories.get(dir);
					for (WatchEvent<?> evt : key.pollEvents()) {
						WatchEvent.Kind<?> kind = evt.kind();
						if (kind == StandardWatchEventKinds.OVERFLOW) {
							// The probability of this happening is very low, and what to do is not obvious, especially from the pov of our library.
							// We could add a specific handler so that we can optionnaly notify the user and reload all files if they want to.
							exceptionHandler.accept(new WatchingException("Got watch event OVERFLOW"));
						} else {
							Path file = (Path)evt.context();
							DebouncedRunnable changeHandler = w.fileChangeHandlers.get(file);
							// The handler is null if the file that has changed is not in the list of monitored files
							// (there exist a sibling file in the same directory that we want to monitor).
							// A WatchService monitors directories, not files, that's why we need to do a check here.
							if (changeHandler != null) {
								try {
									changeHandler.run(executor);
								} catch (Exception ex) {
									exceptionHandler.accept(ex);
									// TODO: change the API to pass more information to the exception handler and the change handler
								}
							}
						}
						if (Thread.interrupted()) {
							continue mainLoop; // early stop
						}
					}
					boolean valid = key.reset();
					if (!valid) {
						// key cancelled explicitely, or WatchService closed, or directory no longer accessible
						// To account for the latter case (dir no longer accessible), we need to remove the dir from our map.
						watchedDirectories.remove(dir);
					}
				}
			}
			try {
				executor.shutdown();
				watchService.close();
				watchedDirectories.clear();
			} catch (Exception e) {
				if (shutdownFuture != null) {
					shutdownFuture.completeExceptionally(e);
				} else {
					exceptionHandler.accept(e);
				}
			}
			if (shutdownFuture != null) {
				shutdownFuture.complete(null);
			}
		}
	}

	private static final class WatchedDirectory {
		private final WatchKey key;
		private final Map<Path, DebouncedRunnable> fileChangeHandlers;

		WatchedDirectory(WatchKey key, Map<Path, DebouncedRunnable> fileChangeHandlers) {
			this.key = Objects.requireNonNull(key);
			this.fileChangeHandlers = Objects.requireNonNull(fileChangeHandlers);
		}
	}

	/** Control message that can be send to a watcher thread. */
	private static final class ControlMessage {
		private final ControlMessageKind kind;
		private final CanonicalPath path; // null for poison
		private final Runnable handler; // null for some poison and remove
		/** Allows to notify the caller when the processing of the message is complete. */
		private final CompletableFuture<Void> future; // optional

		private ControlMessage(ControlMessageKind kind, CanonicalPath path, Runnable handler, CompletableFuture<Void> future) {
			this.path = path;
			this.kind = kind;
			this.handler = handler;
			this.future = future;
		}

		static ControlMessage addOrPut(ControlMessageKind kind, CanonicalPath path, Runnable handler, CompletableFuture<Void> future) {
			if (kind != ControlMessageKind.ADD && kind != ControlMessageKind.PUT) {
				throw new IllegalArgumentException("Unexpected message kind " + kind);
			}
			return new ControlMessage(kind, path, handler, future);
		}

		static ControlMessage remove(CanonicalPath path, CompletableFuture<Void> future) {
			return new ControlMessage(ControlMessageKind.REMOVE, path, null, future);
		}

		static ControlMessage poison(CompletableFuture<Void> future) {
			return new ControlMessage(ControlMessageKind.POISON, null, null, future);
		}

		@Override
		public String toString() {
			return "ControlMessage[kind=" + kind + ", path=" + path + ", handler=" + handler + ", future=" + future + "]";
		}
	}

	private static class CanonicalPath {
		public final Path parentDirectory, fileName;

		private CanonicalPath(Path parentDirectory, Path fileName) {
			this.parentDirectory = parentDirectory;
			this.fileName = fileName;
		}

		public static CanonicalPath from(Path fullFilePath) {
			try {
			// To avoid duplicate entries in the map of dirs, make the path absolute and resolve links and special names like ".."
			// toRealPath() only works if the file exists, so we call `toRealPath` on its parent if it doesn't
			Path dir, fileName;
			try {
				Path realFile = fullFilePath.toRealPath();
				dir = realFile.getParent();
				fileName = realFile.getFileName();
			} catch (NoSuchFileException e) {
				dir = fullFilePath.getParent().toRealPath();
				fileName = fullFilePath.getFileName();
			}
			return new CanonicalPath(dir, fileName);
			} catch (IOException ex) {
				throw new WatchingException("Failed to determine the canonical path of: " + fullFilePath + "\nHint: make sure that all parent directories exist.", ex);
			}
		}

		@Override
		public String toString() {
			return parentDirectory + "/" + fileName;
		}

	}

	private static enum ControlMessageKind {
		PUT, ADD, REMOVE, POISON
	}

	private static class NamedDaemonThreadFactory implements ThreadFactory {
		private static final AtomicInteger FACTORY_NUMBER = new AtomicInteger(1);
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		NamedDaemonThreadFactory(String prefix, String suffix) {
			this.namePrefix = prefix + FACTORY_NUMBER.getAndIncrement() + suffix;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
			t.setDaemon(true);
			t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
}
