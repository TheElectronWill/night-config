package com.electronwill.nightconfig.core.path;

import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

/**
 * A PathWatcher can watch several files asynchronously.
 * <p>
 * New watches are added with the {@link #addWatch(Path, Runnable)} method, which specifies the
 * task to execute when the file is modified.
 * <p>
 * This class is thread-safe.
 *
 * @author TheElectronWill
 */
public final class PathWatcher {
	private static final long SLEEP_TIME_NANOS = 1000;
	private static volatile PathWatcher DEFAULT_INSTANCE;

	/**
	 * Gets the default, global instance of PathWatcher.
	 *
	 * @return the default PathWatcher
	 */
	public static synchronized PathWatcher defaultInstance() {
		if (DEFAULT_INSTANCE == null || !DEFAULT_INSTANCE.run) {// null or stopped PathWatcher
			DEFAULT_INSTANCE = new PathWatcher();
		}
		return DEFAULT_INSTANCE;
	}

	private final Map<Path, WatchedDir> watchedDirs = new ConcurrentHashMap<>();//dir -> watchService & infos
	private final Map<Path, WatchedFile> watchedFiles = new ConcurrentHashMap<>();//file -> watchKey & handler
	private final Thread thread = new WatcherThread();
	private final Consumer<Exception> exceptionHandler;
	private volatile boolean run = true;

	/**
	 * Creates a new PathWatcher. The watcher is immediately functional, there is no need (and no
	 * way, actually) to start it manually.
	 */
	public PathWatcher() {
		this(Throwable::printStackTrace);
	}

	/**
	 * Creates a new PathWatcher. The watcher is immediately functional, there is no need (and no
	 * way, actually) to start it manually.
	 */
	public PathWatcher(Consumer<Exception> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		thread.start();
	}

	/**
	 * Watches a path, if not already watched by this PathWatcher.
	 *
	 * @param path          the path to watch
	 * @param changeHandler the handler to call when the path is modified
	 */
	public void addWatch(Path path, Runnable changeHandler) throws IOException {
		path = path.toAbsolutePath();// Ensures that the Path is absolute
		Path dir = path.getParent();
		WatchedDir watchedDir = watchedDirs.computeIfAbsent(dir, k -> new WatchedDir(dir));
		WatchKey watchKey = dir.register(watchedDir.watchService,
										 StandardWatchEventKinds.ENTRY_MODIFY);
		watchedFiles.computeIfAbsent(path,
									 k -> new WatchedFile(watchedDir, watchKey, changeHandler));
	}

	/**
	 * Watches a path. If the path is already watched by this PathWatcher, its changeHandler is
	 * replaced.
	 *
	 * @param path          the path to watch
	 * @param changeHandler the handler to call when the path is modified
	 */
	public void setWatch(Path path, Runnable changeHandler) throws IOException {
		path = path.toAbsolutePath();// Ensures that the Path is absolute
		WatchedFile watchedFile = watchedFiles.get(path);
		if (watchedFile == null) {
			addWatch(path, changeHandler);
		} else {
			watchedFile.changeHandler = changeHandler;
		}
	}

	/**
	 * Stops watching a path.
	 *
	 * @param path the path to stop watching
	 */
	public void removeWatch(Path path) {
		path = path.toAbsolutePath();// Ensures that the Path is absolute
		Path dir = path.getParent();
		WatchedDir watchedDir = watchedDirs.get(dir);
		int remainingChildCount = watchedDir.watchedFileCount.decrementAndGet();
		if (remainingChildCount == 0) {
			watchedDirs.remove(dir);
		}
		WatchedFile watchedFile = watchedFiles.remove(path);
		if (watchedFile != null) {
			watchedFile.watchKey.cancel();
		}
	}

	/**
	 * Stops this PathWatcher. The underlying ressources (ie the WatchServices) are closed, and
	 * the file modification handlers won't be called anymore.
	 */
	public void stop() throws IOException {
		run = false;
	}

	private final class WatcherThread extends Thread {
		{
			setDaemon(true);
		}

		@Override
		public void run() {
			while (run) {
				boolean allNull = true;
				dirsIter:
				for (Iterator<WatchedDir> it = watchedDirs.values().iterator(); it.hasNext() && run; ) {
					WatchedDir watchedDir = it.next();
					WatchKey key = watchedDir.watchService.poll();
					if (key == null) {
						continue;
					}
					allNull = false;
					for (WatchEvent<?> event : key.pollEvents()) {
						if (!run) {
							break dirsIter;
						}
						if (event.kind() != StandardWatchEventKinds.ENTRY_MODIFY || event.count() > 1) {
							continue;
						}
						Path childPath = ((WatchEvent<Path>)event).context();
						Path filePath = watchedDir.dir.resolve(childPath);
						WatchedFile watchedFile = watchedFiles.get(filePath);
						if (watchedFile != null) {
							try {
								watchedFile.changeHandler.run();
							} catch (Exception e) {
								exceptionHandler.accept(e);
							}
						}
					}
					key.reset();
				}
				if (allNull) {
					LockSupport.parkNanos(SLEEP_TIME_NANOS);
				}
			}
			// Closes the WatchServices
			for (WatchedDir watchedDir : watchedDirs.values()) {
				try {
					watchedDir.watchService.close();
				} catch (IOException e) {
					exceptionHandler.accept(e);
				}
			}
			// Clears the maps
			watchedDirs.clear();
			watchedFiles.clear();
		}
	}

	/**
	 * Informations about a watched directory, ie a directory that contains watched files.
	 */
	private static final class WatchedDir {
		final Path dir;
		final WatchService watchService;
		final AtomicInteger watchedFileCount = new AtomicInteger();

		private WatchedDir(Path dir) {
			this.dir = dir;
			try {
				this.watchService = dir.getFileSystem().newWatchService();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Informations about a watched file, with an associated handler.
	 */
	private static final class WatchedFile {
		final WatchKey watchKey;
		volatile Runnable changeHandler;

		private WatchedFile(WatchedDir watchedDir, WatchKey watchKey, Runnable changeHandler) {
			this.watchKey = watchKey;
			this.changeHandler = changeHandler;
			watchedDir.watchedFileCount.getAndIncrement();
		}
	}
}