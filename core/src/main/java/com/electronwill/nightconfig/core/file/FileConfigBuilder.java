package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.MemoryConfig;
import com.electronwill.nightconfig.core.NightConfig;
import com.electronwill.nightconfig.core.io.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
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
 * <li>Not thread-safe - change it with {@link #concurrent()}</li>
 * <li>Values' insertion order preserved if {@link NightConfig#isInsertionOrderPreserved()}
 * returns true when the builder is constructed.</li>
 * </ul>
 *
 * @author TheElectronWill
 */
public class FileConfigBuilder {
	protected final Path file;
	protected Config config;
	protected final ConfigFormat format;
	protected final ConfigWriter writer;
	protected final ConfigParser parser;
	protected Charset charset = StandardCharsets.UTF_8;
	protected WritingMode writingMode = WritingMode.REPLACE;
	protected ParsingMode parsingMode = ParsingMode.REPLACE;
	protected FileNotFoundAction nefAction = FileNotFoundAction.CREATE_EMPTY;
	protected boolean sync = false, autosave = false, autoreload = false, concurrent = false;
	protected Supplier<Config> baseSupplier = MemoryConfig::new;

	public FileConfigBuilder(Path file, ConfigFormat format) {
		this.file = file;
		this.format = format;
		this.writer = format.writer();
		this.parser = format.parser();
	}

	/**
	 * Sets the charset used for {@link FileConfig#save()} and {@link FileConfig#load()}.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder charset(Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * Sets the WritingMode used for {@link FileConfig#save()}
	 *
	 * @return this builder
	 */
	public FileConfigBuilder writingMode(WritingMode writingMode) {
		this.writingMode = writingMode;
		return this;
	}

	/**
	 * Sets the ParsingMode used for {@link FileConfig#load()}
	 *
	 * @return this builder
	 */
	public FileConfigBuilder parsingMode(ParsingMode parsingMode) {
		this.parsingMode = parsingMode;
		return this;
	}

	/**
	 * Sets the action to execute when the config's file is not found.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder onFileNotFound(FileNotFoundAction nefAction) {
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
	public FileConfigBuilder defaultResource(String resourcePath) {
		return onFileNotFound(FileNotFoundAction.copyResource(resourcePath));
	}

	/**
	 * Sets the file to copy when the config's file is not found. This is a shortcut for {@code
	 * onFileNotFound(FileNotFoundAction.copyData(file))}
	 *
	 * @param file the data file
	 * @return this builder
	 */
	public FileConfigBuilder defaultData(File file) {
		return onFileNotFound(FileNotFoundAction.copyData(file));
	}

	/**
	 * Sets the file to copy when the config's file is not found. This is a shortcut for {@code
	 * onFileNotFound(FileNotFoundAction.copyData(file))}
	 *
	 * @param file the data file
	 * @return this builder
	 */
	public FileConfigBuilder defaultData(Path file) {
		return onFileNotFound(FileNotFoundAction.copyData(file));
	}

	/**
	 * Sets the URL of the data to copy when the config's file is not found. This is a shortcut for
	 * {@code onFileNotFound(FileNotFoundAction.copyData(url))}
	 *
	 * @param url the data url
	 * @return this builder
	 */
	public FileConfigBuilder defaultData(URL url) {
		return onFileNotFound(FileNotFoundAction.copyData(url));
	}

	/**
	 * Makes the configuration "write-synchronized", that is, its {@link FileConfig#save()}
	 * method blocks until the write operation completes.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder sync() {
		sync = true;
		return this;
	}

	/**
	 * Makes the configuration "autosaved", that is, its {@link FileConfig#save()} method is
	 * automatically called when it is modified.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder autosave() {
		autosave = true;
		return this;
	}

	/**
	 * Makes the configuration "autoreloaded", that is, its {@link FileConfig#load()} method is
	 * automatically called when the file is modified.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder autoreload() {
		autoreload = true;
		return this;
	}

	/**
	 * Makes the configuration concurrent, that is, thread-safe.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder concurrent() {
		// TODO implement
		return this;
	}

	/**
	 * Modifies the `baseSupplier` so that the configuration preserves the insertion order
	 * of its values.
	 *
	 * @see #baseSupplier(Supplier)
	 * @return this builder
	 */
	public FileConfigBuilder preserveInsertionOrder() {
		baseSupplier = () -> new MemoryConfig(LinkedHashMap::new);
		return this;
	}

	/**
	 * Defines how the base configuration is obtained.
	 * <p>
  	 * <b>Warning :</b> if {@link #autoreload()} is called, the config supplier
  	 * must return thread-safe configurations, because the autoreloading system will modify
  	 * the FileConfig from another thread.
	 * </p>
	 * @param supplier supplies the config that will stores the FileConfig's data
	 * @return this builder
	 */
	public FileConfigBuilder baseSupplier(Supplier<Config> supplier) {
		baseSupplier = supplier;
		return this;
	}

	/**
	 * Creates a new FileConfig with the chosen settings.
	 *
	 * @return the config
	 */
	public FileConfig build() {
		FileConfig fileConfig;
		if (sync) {
			fileConfig = new WriteSyncFileConfig<>(getConfig(), file, charset, writer, writingMode,
				parser, parsingMode, nefAction);
		} else {
			if (autoreload) {
				concurrent();
				// Autoreloading is done from a background thread, therefore we need thread-safety
				// This isn't needed with WriteSyncFileConfig because it synchronizes loads and writes.
			}
			fileConfig = new WriteAsyncFileConfig<>(getConfig(), file, charset, writer, writingMode,
				parser, parsingMode, nefAction);
		}
		if (autoreload) {
			if (Files.notExists(file)) {
				try {
					nefAction.run(file, format);
				} catch (IOException e) {
					throw new WritingException(
						"An exception occured while executing the FileNotFoundAction for file "
						+ file, e
					);
				}
			}
			fileConfig = new AutoreloadFileConfig<>(fileConfig);
		}
		if (autosave) {
			return buildAutosave(fileConfig);
		}
		return buildNormal(fileConfig);
	}

	protected FileConfig buildAutosave(FileConfig chain) {
		return new AutosaveFileConfig<>(chain);
	}

	protected FileConfig buildNormal(FileConfig chain) {
		return chain;
	}

	protected final Config getConfig() {
		if (config == null) {
			config = baseSupplier.get();
		}
		return config;
	}
}