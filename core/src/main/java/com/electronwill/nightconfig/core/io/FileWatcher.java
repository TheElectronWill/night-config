package com.electronwill.nightconfig.core.io;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author TheElectronWill
 */
public final class FileWatcher {
	private static final long SLEEP_TIME_NANOS = 1000;

	private final Map<Path, WatchedDir> watchedDirs = new HashMap<>();//dir -> watchService & infos
	private final Map<Path, WatchedFile> watchedFiles = new HashMap<>();//file -> watchKey & handler
	private final Thread thread = new WatcherThread();
	private volatile boolean run = true;

	/**
	 *
	 * @param file
	 * @param changeHandler
	 * @throws IOException
	 */
	public void addWatch(Path file, Runnable changeHandler) throws IOException {
		Path dir = file.getParent();
		WatchedDir watchedDir = watchedDirs.computeIfAbsent(dir, k -> new WatchedDir(dir));
		WatchKey watchKey = file.register(watchedDir.watchService,
										  StandardWatchEventKinds.ENTRY_MODIFY);
		watchedFiles.computeIfAbsent(file,
									 k -> new WatchedFile(watchedDir, watchKey, changeHandler));
	}

	/**
	 *
	 * @param file
	 * @param changeHandler
	 */
	public void setWatch(Path file, Runnable changeHandler) {
		WatchedFile watchedFile = watchedFiles.get(file);
		if (watchedFile != null) {
			watchedFile.changeHandler = changeHandler;
		}
	}

	/**
	 *
	 * @param file
	 */
	public void removeWatch(Path file) {
		Path dir = file.getParent();
		WatchedDir watchedDir = watchedDirs.get(dir);
		int remainingChildCount = watchedDir.watchedFileCount.decrementAndGet();
		if (remainingChildCount == 0) {
			watchedDirs.remove(dir);
		}
		WatchedFile watchedFile = watchedFiles.remove(file);
		if (watchedFile != null) {
			watchedFile.watchKey.cancel();
		}
	}

	/**
	 *
	 */
	public void stop(boolean force) {
		run = false;
		if(force) {
			thread.stop();
		}
	}

	/**
	 *
	 */
	private final class WatcherThread extends Thread {
		@Override
		public void run() {
			while (run) {
				boolean allNull = true;
				for (Iterator<WatchedDir> it = watchedDirs.values().iterator(); it.hasNext()
																				&& run; ) {
					WatchedDir watchedDir = it.next();
					WatchKey key = watchedDir.watchService.poll();
					if (key == null) {
						continue;
					}
					allNull = false;
					for (WatchEvent<?> event : key.pollEvents()) {
						if (event.kind() != StandardWatchEventKinds.ENTRY_MODIFY) {
							continue;
						}
						Path childPath = ((WatchEvent<Path>)event).context();
						Path filePath = watchedDir.dir.resolve(childPath);
						WatchedFile watchedFile = watchedFiles.get(filePath);
						if (watchedFile != null) {
							watchedFile.changeHandler.run();
						}
					}
					key.reset();
				}
				if (allNull) {
					LockSupport.parkNanos(SLEEP_TIME_NANOS);
				}
			}
		}
	}

	/**
	 *
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
	 *
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