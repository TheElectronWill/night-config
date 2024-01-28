package com.electronwill.nightconfig.core.file;

import static java.nio.file.StandardOpenOption.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.concurrent.ConcurrentCommentedConfig;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;

/**
 * @author TheElectronWill
 */
final class AsyncFileConfig extends CommentedConfigWrapper<StampedConfig>
		implements CommentedFileConfig {
	/**
	 * Holder for the executor service: allows to create the executor on demand in a thread-safe way
	 * (thanks to class initialization rules).
	 */
	private static final class LazyExecutorHolder {
		// Lazily evaluated executor service, shared between all instances of WriteAsyncFileConfig.
		private static final ScheduledExecutorService sharedExecutor;
		static {
			int poolSize = Runtime.getRuntime().availableProcessors();
			ThreadFactory defaultFactory = Executors.defaultThreadFactory();
			ThreadFactory factory = r -> {
				Thread t = defaultFactory.newThread(r);
				t.setDaemon(true);
				return t;
			};
			sharedExecutor = Executors.newScheduledThreadPool(poolSize, factory);
		}
	}

	/**
	 * When a config is modified, wait for the debounce time to expire before saving it to the file.
	 * Any modification resets the countdown, there needs to be a period of "calm" before the save is triggered.
	 */
	static final Duration DEFAULT_WRITE_DEBOUNCE_TIME = Duration.ofSeconds(1);

	/** True to make load() asynchronous (false by default). */
	private final boolean asyncLoad;

	/** True if the FileConfig is closed and cannot be used anymore. */
	private volatile boolean closed;

	/** Path of the file associated to the config. */
	private final Path nioPath;

	/** Debounced saving task. It runs on the shared executor. */
	private final DebouncedRunnable saveTask;

	/** Writes to the file (must be called from a task submitted to the executor). */
	private BufferedWriter fileWriter;

	// Serializing
	private final ConfigWriter configWriter;
	private final WritingMode writingMode;

	// Parsing
	private final ConfigParser<?> configParser;
	private final ParsingMode parsingMode;
	private final FileNotFoundAction notFoundAction;
	private final Charset charset;

	// Listeners
	private final ConfigLoadFilter reloadFilter;
	private final Runnable saveListener, loadListener;

	AsyncFileConfig(StampedConfig config, Path nioPath, Charset charset, ConfigWriter writer,
			WritingMode writingMode, ConfigParser<?> parser,
			ParsingMode parsingMode, FileNotFoundAction notFoundAction,
			boolean asyncLoad, ConfigLoadFilter reloadFilter,
			Runnable saveListener, Runnable loadListener,
			Duration debounceTime) {

		super(config);
		this.asyncLoad = asyncLoad;
		this.nioPath = nioPath;

		// writing
		this.writingMode = writingMode;
		this.configWriter = writer;
		this.saveTask = new DebouncedRunnable(this::saveNow, debounceTime);

		// parsing
		this.configParser = parser;
		this.parsingMode = parsingMode;
		this.notFoundAction = notFoundAction;
		this.charset = charset;

		// listeners
		this.reloadFilter = reloadFilter;
		this.saveListener = saveListener;
		this.loadListener = loadListener;
	}

	// ----- internal -----
	/**
	 * Saves the configuration now (blocking IO).
	 * It avoids locking the StampedConfig during the IO operation:
	 * - copy the config into a new config (lock!)
	 * - write the copy to the file (no lock)
	 */
	private void saveNow() {
		UnmodifiableConfig copy = config.newAccumulatorCopy();
		synchronized (this) {
			// If REPLACE_ATOMIC, write to a temporary file and then move it atomically to the config file.
			// The FileWriter is not kept open in that case, because the temporary file will no longer exist after the
			// move.
			if (writingMode == WritingMode.REPLACE_ATOMIC) {
				Path tmp = nioPath.resolveSibling(nioPath.getFileName() + ".new.tmp");
				try (BufferedWriter writer = Files.newBufferedWriter(tmp, charset, WRITE, CREATE, TRUNCATE_EXISTING)) {
					configWriter.write(copy, writer);
					Files.move(tmp, nioPath, StandardCopyOption.ATOMIC_MOVE);
				} catch (AtomicMoveNotSupportedException e) {
					// can fail in some conditions (OS and filesystem-dependent)
					String msg = String.format(
							"Failed to atomically move the config from '%s' to '%s': WritingMode.REPLACE_ATOMIC is not supported for this path, use WritingMode.REPLACE instead.\n%s",
							tmp.toString(), nioPath.toString(),
							"Note: you may see *.new.tmp files after this error, they contain the \"new version\" of your configurations and can be safely removed."
									+ "If you want, you can manually copy their content into your regular configuration files (replacing the old config).");
					throw new WritingException(msg, e);
				} catch (IOException e) {
					// regular IO exception
					String msg = String.format("Failed to write (%s) the config to: %s",
							writingMode.toString(), tmp.toString());
					throw new WritingException(msg, e);
				}
			} else {
				if (fileWriter == null) {
					OpenOption lastOption = (writingMode == WritingMode.APPEND) ? APPEND
							: TRUNCATE_EXISTING;
					try {
						fileWriter = Files.newBufferedWriter(nioPath, charset, WRITE, CREATE,
								lastOption);
					} catch (IOException e) {
						throw new WritingException("Failed to open a BufferedWriter on: " + nioPath,
								e);
					}
				}
				configWriter.write(copy, fileWriter);
				try {
					if (closed) {
						fileWriter.close();
					} else {
						fileWriter.flush();
					}
				} catch (IOException e) {
					String op = closed ? "close" : "flush";
					String msg = String
							.format("Buffer %s failed while saving asynchronous FileConfig.", op);
					throw new WritingException(msg, e);
				}
			}
		}
		saveListener.run();
	}

	/**
	 * Loads the configuration now (blocking IO).
	 * It locks the StampedConfig for a minimal amount of time:
	 * - parse the file into a new config (no lock)
	 * - convert the config entries to be compatible with StampedConfig (make the subconfigs all StampedConfigs -
	 * no lock)
	 * - atomically replace the old config by the new config (lock!)
	 */
	private void loadNow() {
		Config newConfig = configParser.parse(nioPath, notFoundAction, charset);
		CommentedConfig newCC = CommentedConfig.fake(newConfig);

		if (reloadFilter != null && !reloadFilter.acceptNewVersion(newCC)) {
			return; // reload cancelled
		}

		// We cannot choose the type of the config returned by the parser.
		// Even configParser.parse(nioPath, conf) is flawed because it creates subconfigs whose type
		// depends on the parser, not with conf.createSubConfig()!

		switch (parsingMode) {
			case REPLACE:
				StampedConfig newSafeContent = config.createSubConfig(); // this is actually an independant config
				convertConfig(newCC, newSafeContent);
				config.replaceContentBy(newSafeContent);
				// It could work with SynchronizedConfig too:
				// if (config instanceof SynchronizedConfig) {
				// SynchronizedConfig real = (SynchronizedConfig) config;
				// SynchronizedConfig newSafeContent = real.createSubConfig(); // not totally independent but ok for replace
				// convertConfig(newCC, newSafeContent);
				// real.replaceContentBy(newSafeContent);
				// }
				break;
			default:
				putWithParsingMode(parsingMode, newCC, config);
				break;
		}
		loadListener.run();
	}

	static void putWithParsingMode(ParsingMode parsingMode, CommentedConfig newCC, ConcurrentCommentedConfig config) {
		config.bulkCommentedUpdate(view -> {
			for (CommentedConfig.Entry entry : newCC.entrySet()) {
				List<String> key = Collections.singletonList(entry.getKey());
				Object value = entry.getRawValue();
				if (value instanceof UnmodifiableConfig
						&& !(value instanceof ConcurrentCommentedConfig)) {
					// convert the subconfig to a proper type
					value = convertSubConfig((UnmodifiableConfig) value, config);
				}
				parsingMode.put(view, key, value);
			}
		});
	}

	// ---- FileConfig ----
	@Override
	public File getFile() {
		return nioPath.toFile();
	}

	@Override
	public Path getNioPath() {
		return nioPath;
	}

	@Override
	public void save() {
		if (closed) {
			throw new IllegalStateException("This FileConfig is closed, cannot save().");
		}
		saveTask.run(LazyExecutorHolder.sharedExecutor);
	}

	/** Loads the config from a background thread. Returns quickly, without waiting the loading to complete. */
	public void asyncLoad() {
		LazyExecutorHolder.sharedExecutor.execute(() -> {
			loadNow();
		});
	}

	@Override
	public void load() {
		if (closed) {
			throw new IllegalStateException("This FileConfig is closed, cannot load().");
		}
		if (asyncLoad) {
			asyncLoad();
		} else {
			loadNow(); // blocking operation
		}
	}

	private static void convertConfig(UnmodifiableConfig input, ConcurrentCommentedConfig into) {
		if (input instanceof UnmodifiableCommentedConfig) {
			into.bulkCommentedUpdate(view -> {
				UnmodifiableCommentedConfig commentedSub = (UnmodifiableCommentedConfig) input;
				for (UnmodifiableCommentedConfig.Entry entry : commentedSub.entrySet()) {
					List<String> key = Collections.singletonList(entry.getKey());
					Object value = entry.getRawValue();
					if (value instanceof UnmodifiableConfig) {
						// recursively convert
						value = convertSubConfig((UnmodifiableConfig) value, into);
					}
					view.set(key, entry.getRawValue());
					view.setComment(key, entry.getComment());
				}
			});
		} else {
			into.bulkUpdate(view -> {
				for (UnmodifiableConfig.Entry entry : input.entrySet()) {
					List<String> key = Collections.singletonList(entry.getKey());
					Object value = entry.getRawValue();
					if (value instanceof UnmodifiableConfig) {
						// recursively convert
						value = convertSubConfig((UnmodifiableConfig) value, into);
					}
					view.set(key, value);
				}
			});
		}
	}

	private static ConcurrentCommentedConfig convertSubConfig(UnmodifiableConfig subconfig,
			ConcurrentCommentedConfig parent) {
		ConcurrentCommentedConfig safe = parent.createSubConfig();
		convertConfig(subconfig, safe);
		return safe;
	}

	@Override
	public void close() {
		this.closed = true;
	}

	@Override
	public <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action) {
		return config.bulkCommentedUpdate(action);
	}

	@Override
	public <R> R bulkUpdate(Function<? super Config, R> action) {
		return config.bulkUpdate(action);
	}
}
