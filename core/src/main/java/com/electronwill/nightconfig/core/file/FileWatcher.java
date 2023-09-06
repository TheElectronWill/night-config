package com.electronwill.nightconfig.core.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
	private static volatile FileWatcher DEFAULT_INSTANCE;

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
	private static final long SERVICE_POLL_TIMEOUT_NANOS = Duration.ofMillis(200).toNanos();

	/** When a file is modified, wait for the debounce time to expire before triggering the change handler.
	 * Any modification resets the countdown, there needs to be a period of "calm" before the handler is triggered.
	 */
	private static final Duration DEFAULT_DEBOUNCE_TIME = Duration.ofMillis(500);

	private final ThreadGroup threadGroup;
	private final AtomicInteger threadCount = new AtomicInteger(0);
	private final ConcurrentMap<FileSystem, FsWatcher> watchers = new ConcurrentHashMap<>();
	private final Consumer<Throwable> exceptionHandler;
	private final Duration debounceTime;
	private final int instanceId;
	private volatile boolean running = true;

	/**
	 * Creates a new FileWatcher with a default exception handler and a default debounce time.
	 *
	 * The default exception handler is simply:
	 * <code>
	 * (e) -> e.printStackTrace();
	 * </code>
	 *
	 * The default debounce time is unspecified and may change in the future, but is typically
	 * around 1 second or less.
	 */
	public FileWatcher() {
		this(DEFAULT_DEBOUNCE_TIME);
	}

	/**
	 * Creates a new FileWatcher with a default exception handler.
	 *
	 * The default exception handler is simply:
	 * <code>
	 * (e) -> e.printStackTrace();
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
	 * @param throttleDelay delay between each call of the file's changeHandler.
	 * @param exceptionHandler called when an exception occurs during the handling of file events
	 */
	public FileWatcher(Duration throttleDelay, Consumer<Throwable> exceptionHandler) {
		this.instanceId = instanceCount.getAndIncrement();
		this.debounceTime = throttleDelay;
		this.exceptionHandler = exceptionHandler;
		this.threadGroup = new ThreadGroup("watchers-" + instanceId);
	}

	/**
	 * Watches a file, if not already watched by this FileWatcher.
	 * The file's parent directory must exist.
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
	 *
	 * @param file          the file to watch
	 * @param changeHandler the handler to call when the file is modified
	 */
	public void addWatch(Path file, Runnable changeHandler) {
		addOrPutWatch(file, changeHandler, ControlMessageKind.ADD);
	}

	/**
	 * Watches a file. If the file is already watched by this FileWatcher, its changeHandler is
	 * replaced.
	 * The file's parent directory must exist.
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
	 *
	 * @param file          the file to watch
	 * @param changeHandler the handler to call when the file is modified
	 */
	public void setWatch(Path file, Runnable changeHandler) {
		addOrPutWatch(file, changeHandler, ControlMessageKind.PUT);
	}

	private void addOrPutWatch(Path file, Runnable changeHandler, ControlMessageKind kind) {
		ensureRunning();
		CanonicalPath canon = CanonicalPath.from(file);
		FileSystem fs = canon.parentDirectory.getFileSystem();
		try {
			FsWatcher watcher = watchers.computeIfAbsent(fs, k -> {
				// start a new watcher for this filesystem
				try {
					WatchService service = fs.newWatchService();
					FsWatcher w = new FsWatcher(exceptionHandler, debounceTime, service);
					threadGroup.activeCount();
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
			watcher.send(ControlMessage.addOrPut(kind, canon, changeHandler));
		} catch (Exception ex) {
			throw new WatchingException("Failed to watch path '" + file + "', canonical path '" + canon + "'", ex);
		}
	}

	/**
	 * Stops watching a file.
	 *
	 * @param file the file to stop watching
	 */
	public void removeWatch(File file) {
		removeWatch(file.toPath());
	}

	/**
	 * Stops watching a file.
	 *
	 * @param file the file to stop watching
	 */
	public void removeWatch(Path file) {
		ensureRunning();
		CanonicalPath canon = CanonicalPath.from(file);
		FileSystem fs = canon.parentDirectory.getFileSystem();
		FsWatcher watcher = watchers.get(fs);
		if (watcher != null) {
			watcher.send(ControlMessage.remove(canon));
		}
	}

	/**
	 * Stops this FileWatcher. The underlying ressources (ie the WatchServices) are closed, and
	 * the file modification handlers won't be called anymore.
	 */
	public void stop() {
		// prevent further use of the FileWatcher
		running = false;

		// stop each watcher thread
		for (FsWatcher watcher : watchers.values()) {
			watcher.send(ControlMessage.poison());
		}
		// interrupt each watcher thread so that they handle the "poison" asap
		threadGroup.interrupt();
	}

	private void ensureRunning() {
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

		private final WatchService watchService;
		private final Map<Path, WatchedDirectory> watchedDirectories = new HashMap<>();
		private final ConcurrentLinkedQueue<ControlMessage> controlMessages = new ConcurrentLinkedQueue<>();

		FsWatcher(Consumer<Throwable> exceptionHandler, Duration debounceTime, WatchService watchService) {
			this.exceptionHandler = exceptionHandler;
			this.debounceTime = debounceTime;
			this.watchService = watchService;
		}

		void send(ControlMessage msg) {
			controlMessages.add(msg);
		}

		private WatchedDirectory watchDirectory(Path dir) {
			return watchedDirectories.computeIfAbsent(dir, k -> {
				// the file's parent directory isn't monitored yet, register it
				WatchKey key;
				try {
					key = dir.register(watchService, ENTRY_MODIFY, ENTRY_CREATE);
					return new WatchedDirectory(key, new HashMap<>(8));
				} catch (IOException e) {
					exceptionHandler.accept(e);
					return null;
				}
			});
		}

		@Override
		public void run() {
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

			mainLoop:
			while (true) {
				// handle control messages coming from other threads (modification of the watch list)
				ControlMessage msg;
				while ((msg = controlMessages.poll()) != null) {
					Path dir = msg.parentDirectory;
					Path fileName = msg.fileName;
					switch (msg.kind) {
						case ADD: {
							// Combine the handlers if there's already one, otherwise set it
							WatchedDirectory w = watchDirectory(dir);
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
							WatchedDirectory w = watchDirectory(dir);
							if (w != null) {
								DebouncedRunnable newHandler = new DebouncedRunnable(msg.handler, debounceTime);
								w.fileChangeHandlers.put(fileName, newHandler);
							}
							break;
						}
						case REMOVE: {
							// Stop watching a file
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
							break mainLoop;
						}
						default: {
							break;
						}
					}
				}

				// poll the events from the filesystem (monitoring of the files)
				WatchKey key = null;
				try {
					key = watchService.poll(SERVICE_POLL_TIMEOUT_NANOS, TimeUnit.NANOSECONDS);
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
						break;
					}
				}
			}
			try {
				executor.shutdown();
				watchService.close();
				watchedDirectories.clear();
			} catch (IOException e) {
				exceptionHandler.accept(e);
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

	private static final class DebouncedRunnable {
		private final Runnable runnable;
		private final long debounceTimeNanos;
		private ScheduledFuture<?> scheduledTask;

		private DebouncedRunnable(Runnable runnable, long debounceTimeNanos, ScheduledFuture<?> scheduledTask) {
			this.runnable = runnable;
			this.debounceTimeNanos = debounceTimeNanos;
			this.scheduledTask = scheduledTask;
		}

		public DebouncedRunnable(Runnable runnable, Duration debounceTime) {
			this.runnable = runnable;
			this.debounceTimeNanos = debounceTime.toNanos();
		}

		/**
		 * Runs the underlying {@link Runnable} after the debounce time has elapsed,
		 * if {@code run} is not called again before its execution.
		 */
		public void run(ScheduledExecutorService executor) {
			if (scheduledTask != null) {
				// cancel the previously scheduled execution (it's ok it it has already been completed or cancelled)
				scheduledTask.cancel(false);
			}
			// schedule the new execution after the debouncing delay
			scheduledTask = executor.schedule(runnable, debounceTimeNanos, TimeUnit.NANOSECONDS);
		}

		/** Combine this runnable with another one, while keeping the debouncing state. */
		public DebouncedRunnable andThen(Runnable then) {
			Runnable combined = () -> {
				runnable.run();
				then.run();
			};
			return new DebouncedRunnable(combined, debounceTimeNanos, scheduledTask);
		}
	}

	/** Control message that can be send to a watcher thread. */
	private static final class ControlMessage {
		private final ControlMessageKind kind;
		private final Path parentDirectory, fileName; // null for poison
		private final Runnable handler; // null for some poison and remove

		private ControlMessage(ControlMessageKind kind, CanonicalPath path, Runnable handler) {
			this.parentDirectory = path.parentDirectory;
			this.fileName = path.fileName;
			this.kind = kind;
			this.handler = handler;
		}

		static ControlMessage addOrPut(ControlMessageKind kind, CanonicalPath path, Runnable handler) {
			if (kind != ControlMessageKind.ADD && kind != ControlMessageKind.PUT) {
				throw new IllegalArgumentException("Unexpected message kind " + kind);
			}
			return new ControlMessage(kind, path, handler);
		}

		static ControlMessage remove(CanonicalPath path) {
			return new ControlMessage(ControlMessageKind.REMOVE, path, null);
		}

		static ControlMessage poison() {
			return new ControlMessage(ControlMessageKind.POISON, null, null);
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
				throw new WatchingException("Failed to determine the canonical path of: " + fullFilePath, ex);
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

}
