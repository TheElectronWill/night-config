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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
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

	private final ConcurrentMap<FileSystem, FSWatcher> watchers = new ConcurrentHashMap<>();
	private final Consumer<Exception> exceptionHandler;
	private volatile boolean running = true;

	/**
	 * Creates a new FileWatcher with a default exception handler.
	 * The default exception handler is simply:
	 * <code>
	 * (e) -> e.printStackTrace();
	 * </code>
	 */
	public FileWatcher() {
		this(Throwable::printStackTrace);
	}

	/**
	 * Creates a new FileWatcher.
	 */
	public FileWatcher(Consumer<Exception> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
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
		CanonicalPath canon = CanonicalPath.from(file);
		FileSystem fs = canon.parentDirectory.getFileSystem();
		try {
			FSWatcher watcher = watchers.computeIfAbsent(fs, k -> {
				// start a new watcher for this filesystem
				try {
					WatchService service = fs.newWatchService();
					FSWatcher w = new FSWatcher(service);
					Thread t = new Thread(w);
					t.start();
					t.setDaemon(true);
					return w;
				} catch (IOException ex) {
					throw new WatchingException("Failed to start a new watcher thread for directory " + canon.parentDirectory, ex);
				}
			});
			// tell the watcher thread to watch the file
			watcher.send(new ControlMessage(kind, canon, changeHandler));
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
		CanonicalPath canon = CanonicalPath.from(file);
		FileSystem fs = canon.parentDirectory.getFileSystem();
		FSWatcher watcher = watchers.get(fs);
		if (watcher != null) {
			watcher.send(new ControlMessage(ControlMessageKind.REMOVE, canon, null));
		}
	}

	/**
	 * Stops this FileWatcher. The underlying ressources (ie the WatchServices) are closed, and
	 * the file modification handlers won't be called anymore.
	 */
	public void stop() {
		running = false; // volatile write
	}

	/** A directory watcher for one filesystem. */
	private final class FSWatcher implements Runnable {
		private final WatchService watchService;
		private final Map<Path, WatchedDirectory> watchedDirectories = new HashMap<>();
		private final ConcurrentLinkedQueue<ControlMessage> controlMessages = new ConcurrentLinkedQueue<>();

		FSWatcher(WatchService watchService) {
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

		private void close() throws IOException {
			watchService.close();
			watchedDirectories.clear();
		}

		@Override
		public void run() {
			while (running) {
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
								Runnable existingHandler = w.fileChangeHandlers.get(fileName);
								Runnable msgHandler = msg.handler;
								Runnable newHandler;
								if (existingHandler != null) {
									// combine the handlers
									newHandler = () -> {
										existingHandler.run();
										msgHandler.run();
									};
								} else {
									newHandler = msgHandler;
								}
								w.fileChangeHandlers.put(fileName, newHandler);
							}
							break;
						}
						case PUT: {
							// Set the handler, replacing any existing handler
							WatchedDirectory w = watchDirectory(dir);
							if (w != null) {
								w.fileChangeHandlers.put(fileName, msg.handler);
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
						default: {
							break;
						}
					}
				}

				// poll the events from the filesystem (monitoring of the files)
				WatchKey key = null;
				try {
					key = watchService.poll(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// we can proceed
				}

				if (key == null) {
					// no key available after the given delay, or interrupted
					continue;
				} else {
					Path dir = (Path) key.watchable();
					WatchedDirectory w = watchedDirectories.get(dir);
					for (WatchEvent<?> evt : key.pollEvents()) {
						WatchEvent.Kind<?> kind = evt.kind();
						if (kind == StandardWatchEventKinds.OVERFLOW) {
							// The probability of this happening is very low, and what to do is not obvious, especially from the pov of our library.
							// We could add a specific handler so that we can optionnaly notify the user and reload all files if they want to.
							exceptionHandler.accept(new WatchingException("Got event OVERFLOW"));
						} else {
							Path file = (Path)evt.context();
							Runnable changeHandler = w.fileChangeHandlers.get(file);
							if (changeHandler != null) {
								// The handler is null if the file that has changed is not in the list of monitored files
								// (there exist a sibling file in the same directory that we want to monitor).
								// A WatchService monitors directories, not files, that's why we need to do a check here.
								try {
									changeHandler.run();
								} catch (Exception ex) {
									exceptionHandler.accept(ex);
									// TODO: change the API to pass more information to the exception handler and the change handler
								}
							}
						}
						if (!running) {
							break; // early stop
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
				close();
			} catch (IOException e) {
				exceptionHandler.accept(e);
			}
		}
	}

	private static final class WatchedDirectory {
		final WatchKey key;
		final Map<Path, Runnable> fileChangeHandlers;

		WatchedDirectory(WatchKey key, Map<Path, Runnable> fileChangeHandlers) {
			this.key = Objects.requireNonNull(key);
			this.fileChangeHandlers = Objects.requireNonNull(fileChangeHandlers);
		}
	}

	private static final class ControlMessage {
		final ControlMessageKind kind;
		final Path parentDirectory, fileName;
		final Runnable handler; // null for some kinds of messages

		ControlMessage(ControlMessageKind kind, CanonicalPath path, Runnable handler) {
			this.parentDirectory = path.parentDirectory;
			this.fileName = path.fileName;
			this.kind = kind;
			this.handler = handler;
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
		PUT, ADD, REMOVE
	}

}
