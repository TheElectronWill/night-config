package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.utils.Consumer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A FileWatcher can watch several files asynchronously.
 * <p>
 * New watches are added with the {@link #addWatch(File, Runnable)} method, which specifies the
 * task to execute when the file is modified.
 * <p>
 * This class is thread-safe.
 *
 * @author TheElectronWill
 */
public final class FileWatcher {
	private static final long SLEEP_TIME_MILLIS = 5000;
	private static volatile FileWatcher DEFAULT_INSTANCE;

	/**
	 * Gets the default, global instance of FileWatcher.
	 *
	 * @return the default FileWatcher
	 */
	public static synchronized FileWatcher defaultInstance() {
		if (DEFAULT_INSTANCE == null || !DEFAULT_INSTANCE.run) {// null or stopped FileWatcher
			DEFAULT_INSTANCE = new FileWatcher();
		}
		return DEFAULT_INSTANCE;
	}

	private final Map<File, WatchedFile> watchedFiles = new HashMap<>();
	private final Thread thread = new WatcherThread();
	private final Consumer<Exception> exceptionHandler;
	private volatile boolean run = true;

	/**
	 * Creates a new FileWatcher. The watcher is immediately functional, there is no need (and no
	 * way, actually) to start it manually.
	 */
	public FileWatcher() {
		this(Throwable::printStackTrace);
	}

	/**
	 * Creates a new FileWatcher. The watcher is immediately functional, there is no need (and no
	 * way, actually) to start it manually.
	 */
	public FileWatcher(Consumer<Exception> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		thread.start();
	}

	/**
	 * Watches a file, if not already watched by this FileWatcher.
	 *
	 * @param file          the file to watch
	 * @param changeHandler the handler to call when the file is modified
	 */
	public void addWatch(File file, Runnable changeHandler) throws IOException {
		file = file.getAbsoluteFile();// Ensures that the File is absolute
		if (!file.exists()) {
			throw new FileNotFoundException("The file cannot be watched because it doesn't exist: " + file);
		}
		synchronized (watchedFiles) {
			if (!watchedFiles.containsKey(file)) {
				long currentSize = file.length();
				long lastModified = file.lastModified();
				watchedFiles.put(file, new WatchedFile(currentSize, lastModified, changeHandler));
			}
		}
	}

	/**
	 * Watches a file. If the file is already watched by this FileWatcher, its changeHandler is
	 * replaced.
	 *
	 * @param file          the file to watch
	 * @param changeHandler the handler to call when the file is modified
	 */
	public void setWatch(File file, Runnable changeHandler) throws IOException {
		file = file.getAbsoluteFile();// Ensures that the File is absolute
		if (!file.exists()) {
			throw new FileNotFoundException("The file cannot be watched because it doesn't exist: " + file);
		}
		synchronized (watchedFiles) {
			long currentSize = file.length();
			long lastModified = file.lastModified();
			watchedFiles.put(file, new WatchedFile(currentSize, lastModified, changeHandler));
		}
	}

	/**
	 * Stops watching a file.
	 *
	 * @param file the file to stop watching
	 */
	public void removeWatch(File file) {
		if (file == null) {
			return; // null cannot be in the map -> don't try to check and return immediately
		}
		file = file.getAbsoluteFile();// Ensures that the File is absolute
		synchronized (watchedFiles) {
			watchedFiles.remove(file);
		}
	}

	/**
	 * Stops this FileWatcher. The underlying ressources (ie the WatchServices) are closed, and
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
				try {
					Thread.sleep(SLEEP_TIME_MILLIS);
				} catch (InterruptedException e) {
					exceptionHandler.accept(e);
				}
				for (Map.Entry<File, WatchedFile> watchedFile : watchedFiles.entrySet()) {
					File file = watchedFile.getKey();
					WatchedFile infos = watchedFile.getValue();
					if (!file.exists()) {
						// File removed -> let the handler handle it :^)
						// Don't try to call length() or lastModified() on a deleted file
						infos.changeHandler.run();
					} else {
						long newSize = file.length();
						long newLastModified = file.lastModified();
						if (newSize != infos.lastSize || newLastModified != infos.lastModified) {
							// Change detected -> call the handler
							infos.changeHandler.run();

							// Update the infos
							infos.lastSize = newSize;
							infos.lastModified = newLastModified;
						}
					}
				}
			}
		}
	}

	/**
	 * Informations about a watched file, with an associated handler.
	 */
	private static final class WatchedFile {
		long lastSize;
		long lastModified;
		Runnable changeHandler;

		private WatchedFile(long lastSize, long lastModified, Runnable changeHandler) {
			this.lastSize = lastSize;
			this.lastModified = lastModified;
			this.changeHandler = changeHandler;
		}
	}
}