package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.concurrent.ConcurrentCommentedConfig;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.io.*;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

import static java.nio.file.StandardOpenOption.*;

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
	private static final Duration DEFAULT_WRITE_DEBOUNCE_TIME = Duration.ofSeconds(1);

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

	// Parsing machinery
	private final ConfigParser<?> configParser;
	private final ParsingMode parsingMode;
	private final FileNotFoundAction notFoundAction;
	private final Charset charset;

	AsyncFileConfig(StampedConfig config, Path nioPath, Charset charset, ConfigWriter writer,
			WritingMode writingMode, ConfigParser<?> parser,
			ParsingMode parsingMode, FileNotFoundAction notFoundAction,
			boolean asyncLoad) {

		super(config);
		this.asyncLoad = asyncLoad;
		this.nioPath = nioPath;

		// parsing
		this.configParser = parser;
		this.parsingMode = parsingMode;
		this.notFoundAction = notFoundAction;
		this.charset = charset;

		// writing
		this.writingMode = writingMode;
		this.configWriter = writer;
		this.saveTask = new DebouncedRunnable(this::saveNow, DEFAULT_WRITE_DEBOUNCE_TIME);
	}

	// ----- internal -----
	private void saveNow() {
		UnmodifiableConfig copy = config.newAccumulatorCopy();
		synchronized (this) {
			if (fileWriter == null) {
				OpenOption[] openOptions;
				if (writingMode == WritingMode.APPEND) {
					openOptions = new OpenOption[] { WRITE, CREATE, APPEND };
				} else {
					openOptions = new OpenOption[] { WRITE, CREATE, TRUNCATE_EXISTING };
				}
				try {
					fileWriter = Files.newBufferedWriter(nioPath, charset, openOptions);
				} catch (IOException e) {
					throw new WritingException("Failed to open a BufferedWriter on: " + nioPath, e);
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
				throw new WritingException(
						"Buffer flush failed during asynchronous FileConfig saving.", e);
			}
		}
	}

	private void loadNow() {
		Config newConfig = configParser.parse(nioPath, notFoundAction, charset);
		CommentedConfig newCC = CommentedConfig.fake(newConfig);

		// We cannot choose the type of the config returned by the parser.
		// Even configParser.parse(nioPath, conf) is flawed because it creates subconfigs whose type
		// depends on the parser, not with conf.createSubConfig()!

		switch (parsingMode) {
			case ADD:
			case MERGE:
				config.bulkCommentedUpdate(config -> {
					for (CommentedConfig.Entry entry : newCC.entrySet()) {
						List<String> key = Collections.singletonList(entry.getKey());
						Object value = entry.getRawValue();
						if (value instanceof UnmodifiableConfig
								&& !(value instanceof ConcurrentCommentedConfig)) {
							// convert the subconfig to a proper type
							value = convertSubConfig((UnmodifiableConfig) value, this.config);
						}
						parsingMode.put(config, key, value);
					}
				});
				break;
			case REPLACE:
				StampedConfig newSafeContent = config.createSubConfig(); // this is actually an independant config
				convertConfig(newCC, newSafeContent);
				config.replaceContentBy(newSafeContent);
				// It could work with SynchronizedConfig too:
				// if (config instanceof SynchronizedConfig) {
				//     SynchronizedConfig real = (SynchronizedConfig) config;
				//     SynchronizedConfig newSafeContent = real.createSubConfig(); // not totally independent but ok for replace
				//     convertConfig(newCC, newSafeContent);
				//     real.replaceContentBy(newSafeContent);
				// }
				break;
			default:
				break;
		}
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

	private void convertConfig(UnmodifiableConfig input, ConcurrentCommentedConfig into) {
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

	private ConcurrentCommentedConfig convertSubConfig(UnmodifiableConfig subconfig,
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
