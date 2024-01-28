package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.concurrent.ConcurrentConfig;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.io.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A generic FileConfig/CommentedFileConfig/someOtherFileConfig builder. The default settings are:
 * <ul>
 * <li>Charset: UTF-8 - change it with {@link #charset(Charset)}</li>
 * <li>WritingMode: REPLACE - change it with {@link #writingMode(WritingMode)}</li>
 * <li>ParsingMode: REPLACE - change it with {@link #parsingMode(ParsingMode)}</li>
 * <li>FileNotFoundAction: CREATE_EMPTY - change it with {@link #onFileNotFound(FileNotFoundAction)}</li>
 * <li>Asynchronous writing, ie config.save() returns quickly and operates in the background -
 * change it with {@link #sync()}</li>
 * <li>Not autosaved - change it with {@link #autosave()}</li>
 * <li>Not autoreloaded - change it with {@link #autoreload()}</li>
 * <li>Values' insertion order preserved if {@link Config#isInsertionOrderPreserved()}
 * returns true when the builder is constructed.</li>
 * </ul>
 *
 * @author TheElectronWill
 */
public abstract class GenericBuilder<Base extends Config, Result extends FileConfig> {
	protected final Path file;
	protected final ConfigFormat<? extends Base> format;
	protected final ConfigWriter writer;
	protected final ConfigParser<? extends Base> parser;
	protected Charset charset = StandardCharsets.UTF_8;
	protected WritingMode writingMode = WritingMode.REPLACE;
	protected ParsingMode parsingMode = ParsingMode.REPLACE;
	protected FileNotFoundAction nefAction = FileNotFoundAction.CREATE_EMPTY;
	protected boolean sync = false, autosave = false, atomicMove = false;
	protected FileWatcher autoreloadFileWatcher = null;
	protected boolean preserveInsertionOrder = Config.isInsertionOrderPreserved();
	protected Supplier<Map<String, Object>> mapCreator = null;

	private ConfigLoadFilter loadFilter;
	protected Runnable loadListener, saveListener;
	protected Runnable autoLoadListener, autoSaveListener;
	private Duration debounceTime = AsyncFileConfig.DEFAULT_WRITE_DEBOUNCE_TIME;

	GenericBuilder(Path file, ConfigFormat<? extends Base> format) {
		this.file = file;
		this.format = format;
		this.writer = format.createWriter();
		this.parser = format.createParser();
	}

	/**
	 * Sets the charset used for {@link FileConfig#save()} and {@link FileConfig#load()}.
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> charset(Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * Sets the WritingMode used for {@link FileConfig#save()}.
	 * <p>
	 * Example:
	 * <pre>
	 * {@code
	 * FileConfig config = FileConfig.builder(file).writingMode(WritingMode.REPLACE_ATOMIC).build();
	 * }
	 * </pre>
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> writingMode(WritingMode writingMode) {
		this.writingMode = writingMode;
		return this;
	}

	/**
	 * Sets the ParsingMode used for {@link FileConfig#load()}.
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> parsingMode(ParsingMode parsingMode) {
		this.parsingMode = parsingMode;
		return this;
	}

	/**
	 * Sets the action to execute when the config's file is not found.
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> onFileNotFound(FileNotFoundAction nefAction) {
		this.nefAction = nefAction;
		return this;
	}

	/**
	 * Sets the resource (in the jar) to copy when the config's file is not found. This is a
	 * shortcut for {@code onFileNotFound(FileNotFoundAction.copyResource(resourcePath))}
	 *
	 * @param resourcePath the resource's path
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> defaultResource(String resourcePath) {
		return onFileNotFound(FileNotFoundAction.copyResource(resourcePath));
	}

	/**
	 * Sets the file to copy when the config's file is not found. This is a shortcut for {@code
	 * onFileNotFound(FileNotFoundAction.copyData(file))}
	 *
	 * @param file the data file
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> defaultData(File file) {
		return onFileNotFound(FileNotFoundAction.copyData(file));
	}

	/**
	 * Sets the file to copy when the config's file is not found. This is a shortcut for {@code
	 * onFileNotFound(FileNotFoundAction.copyData(file))}
	 *
	 * @param file the data file
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> defaultData(Path file) {
		return onFileNotFound(FileNotFoundAction.copyData(file));
	}

	/**
	 * Sets the URL of the data to copy when the config's file is not found. This is a shortcut for
	 * {@code onFileNotFound(FileNotFoundAction.copyData(url))}
	 *
	 * @param url the data url
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> defaultData(URL url) {
		return onFileNotFound(FileNotFoundAction.copyData(url));
	}

	/**
	 * Makes the configuration "write-synchronous", that is, its {@link FileConfig#save()}
	 * method blocks until the write operation completes.
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> sync() {
		sync = true;
		return this;
	}

	/**
	 * Makes the configuration "write-asynchronous", that is, its {@link FileConfig#save()}
	 * method does not wait for the write operation to complete.
	 * <p>
	 * An automatic debouncing of unspecified duration is applied.
	 * 
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> async() {
		sync = false;
		return this;
	}

	/**
	 * Makes the configuration "write-asynchronous" and specifies its debouncing time.
	 * <p>
	 * The config's {@link FileConfig#save()} method will not wait for the write operation to complete,
	 * and perform a debouncing of the given duration. Calling {@link FileConfig#save()} twice with
	 * less than {@code debounceTime} in between will cancel the first save and only perform the second.
	 * 
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> asyncWithDebouncing(Duration debounceTime) {
		sync = false;
		this.debounceTime = debounceTime;
		return this;
	}

	/**
	 * Makes the configuration "autosaved", that is, its {@link FileConfig#save()} method is
	 * automatically called when it is modified.
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> autosave() {
		autosave = true;
		return this;
	}

	/**
	 * Makes the configuration "autoreloaded", that is, its {@link FileConfig#load()} method is
	 * automatically called when the file is modified.
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> autoreload() {
		return autoreload(FileWatcher.defaultInstance());
	}

	/**
	 * Makes the configuration "autoreloaded", using the given FileWatcher to monitor the config file.
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> autoreload(FileWatcher fileWatcher) {
		autoreloadFileWatcher = fileWatcher;
		return this;
	}

	/**
	 * When the configuration is <b>automatically (re)loaded</b>, calls the given listener.
	 * Only one listener can be set, calling {@code onAutoReload} multiple times will replace the listener.
	 * <p>
	 * The listener is called once the loading operation is complete.
	 * If a {@link ConfigLoadFilter} is set and rejects the operation, the listener is not called.
	 * <p>
	 * If {@link FileConfig#load()} is called manually rather than automatically, the listener is not called.
	 * Use {@link #onLoad(Runnable)} to be notified of every call to {@link FileConfig#load()}, including manual
	 * ones.
	 * <p>
	 * If {@link #autoreload()} is not called, setting a listener has no effect.
	 * 
	 * @param listener the listener to call when the FileConfig is automatically reloaded
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> onAutoReload(Runnable listener) {
		autoLoadListener = listener;
		return this;
	}

	/**
	 * When the configuration triggers an <b>automatic save</b>, calls the given listener.
	 * Only one listener can be set, calling {@code onAutoSave} multiple times will replace the listener.
	 * <p>
	 * If the FileConfig is asynchronous and a debouncing occurs, the listener can be called without an actual saving operation being executed
	 * (due to the debouncing, the actual save will occur later).
	 * Otherwise, the listener is called once the saving operation is complete.
	 * <p>
	 * If {@link FileConfig#save()} is called manually rather than automatically, the listener is not called.
	 * Use {@link #onSave(Runnable)} to be notified of every call to {@link FileConfig#save()}, including manual
	 * ones.
	 * <p>
	 * If {@link #autosave()} is not called on the builder, setting a listener has no effect.
	 * 
	 * @param listener the listener to call when the FileConfig is automatically saved
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> onAutoSave(Runnable listener) {
		autoSaveListener = listener;
		return this;
	}

	/**
	 * When the configuration is (re)loaded, calls the given listener.
	 * Only one listener can be set, calling {@code onLoad} multiple times will replace the listener.
	 * It is possible to set both an "auto-load" listener and a "load" listener at the same time.
	 * <p>
	 * The listener is called once the loading operation is complete.
	 * If a {@link ConfigLoadFilter} is set and rejects the operation, the listener is not called.
	 * <p>
	 * If {@link #autoreload()} is not called on the builder, setting a listener has no effect.
	 * 
	 * @param listener the listener to call when the FileConfig is automatically reloaded
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> onLoad(Runnable listener) {
		loadListener = listener;
		return this;
	}

	/**
	 * When the configuration is saved, calls the given listener.
	 * Only one listener can be set, calling {@code onSave} multiple times will replace the listener.
	 * It is possible to set both an "auto-save" listener and a "save" listener at the same time.
	 * <p>
	 * The listener is called once the saving operation is complete.
	 * If a {@link ConfigLoadFilter} is set and rejects the operation, the listener is not called.
	 * <p>
	 * If {@link #autosave()} is not called, setting a listener has no effect.
	 * 
	 * @param listener the listener to call when the FileConfig is automatically saved
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> onSave(Runnable listener) {
		saveListener = listener;
		return this;
	}

	/**
	 * When the configuration is (re)loaded, applies the given filter first.
	 * The filter can either accept or reject the (re)load. If the operation is rejected, the configuration is not
	 * modified.
	 * <p>
	 * If a listener is set and the filter accepts the operation, the listener is called after the filter.
	 * If a listener is set and the filter rejects the operation, the listener is not called.
	 * 
	 * @param filter the filter to call on {@link FileConfig#load()}
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> onLoadFilter(ConfigLoadFilter filter) {
		loadFilter = filter;
		return this;
	}

	/**
	 * Makes the configuration concurrent, that is, thread-safe.
	 *
	 * @deprecated Since NightConfig v3.7, this method has no effect because all FileConfig are thread-safe, backed by {@link ConcurrentConfig}.
	 * @return this builder
	 */
	@Deprecated
	public GenericBuilder<Base, Result> concurrent() {
		// no-op
		return this;
	}

	/**
	 * Makes the configuration preserve the insertion order of its values.
	 * <p>
	 * If this method is not called, the default value of {@link Config#isInsertionOrderPreserved()} is applied.
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> preserveInsertionOrder() {
		preserveInsertionOrder = true;
		return this;
	}

	/**
	 * Uses a specific Supplier to create the backing maps (one for the top level
	 * and one for each sub-configuration) of the configuration.
	 *
	 * @param s the map supplier to use
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> backingMapCreator(Supplier<Map<String, Object>> s) {
		mapCreator = s;
		return this;
	}

	private Runnable runnableOrNothing(Runnable r) {
		return (r == null) ? () -> {} : r;
	}

	/**
	 * Creates a new FileConfig with the chosen settings.
	 *
	 * @return the config
	 */
	public Result build() {
		CommentedFileConfig fileConfig;

		// complete missing fields
		if (mapCreator == null) {
			mapCreator = preserveInsertionOrder ? LinkedHashMap::new : HashMap::new;
		}
		saveListener = runnableOrNothing(saveListener);
		loadListener = runnableOrNothing(loadListener);
		autoSaveListener = runnableOrNothing(autoSaveListener);
		autoLoadListener = runnableOrNothing(autoLoadListener);

		// initialize file if needed
		if (autoreloadFileWatcher != null && Files.notExists(file)) {
			try {
				nefAction.run(file, format);
			} catch (IOException e) {
				String msg = "An exception occured while executing the FileNotFoundAction for file: "
						+ file;
				throw new WritingException(msg, e);
			}
		}

		// build writing facilities
		if (sync) {
			SynchronizedConfig config = new SynchronizedConfig(format, mapCreator);
			fileConfig = new SyncFileConfig(config, file, charset, writer, writingMode,
					parser, parsingMode, nefAction, loadFilter, saveListener, loadListener);
		} else {
			StampedConfig config = new StampedConfig(format, mapCreator);
			fileConfig = new AsyncFileConfig(config, file, charset, writer, writingMode,
					parser, parsingMode, nefAction, false, loadFilter, saveListener, loadListener, debounceTime);
		}
		// add automatic reloading
		if (autoreloadFileWatcher != null) {
			fileConfig = new AutoreloadFileConfig<>(fileConfig, autoreloadFileWatcher,
					autoLoadListener);
		}
		// add automatic saving
		if (autosave) {
			return buildAutosave(fileConfig);
		} else {
			return buildNormal(fileConfig);
		}
	}

	@SuppressWarnings("unchecked")
	protected Result buildAutosave(CommentedFileConfig chain) {
		return (Result) new AutosaveCommentedFileConfig(chain, autoSaveListener);
	}

	@SuppressWarnings("unchecked")
	protected Result buildNormal(CommentedFileConfig chain) {
		return (Result) chain;
	}
}
