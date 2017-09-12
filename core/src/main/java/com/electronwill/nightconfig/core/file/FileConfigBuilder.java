package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Builder for FileConfig. The default settings are:
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
 * </ul>
 *
 * @author TheElectronWill
 */
public class FileConfigBuilder<C extends Config> {
	protected final File file;
	private C config;
	protected final ConfigFormat format;// <? extends C, ? super C, ? super C> doesn't compile
	protected final ConfigWriter<? super C> writer;
	protected final ConfigParser<?, ? super C> parser;
	protected Charset charset = StandardCharsets.UTF_8;
	protected WritingMode writingMode = WritingMode.REPLACE;
	protected ParsingMode parsingMode = ParsingMode.REPLACE;
	protected FileNotFoundAction nefAction = FileNotFoundAction.CREATE_EMPTY;
	protected boolean sync = false, autosave = false, autoreload = false;

	<T extends C> FileConfigBuilder(File file, ConfigFormat<T, ? super C, ? super C> format) {
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
	public FileConfigBuilder<C> charset(Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * Sets the WritingMode used for {@link FileConfig#save()}
	 *
	 * @return this builder
	 */
	public FileConfigBuilder<C> writingMode(WritingMode writingMode) {
		this.writingMode = writingMode;
		return this;
	}

	/**
	 * Sets the ParsingMode used for {@link FileConfig#load()}
	 *
	 * @return this builder
	 */
	public FileConfigBuilder<C> parsingMode(ParsingMode parsingMode) {
		this.parsingMode = parsingMode;
		return this;
	}

	/**
	 * Sets the action to execute when the config's file is not found.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder<C> onFileNotFound(FileNotFoundAction nefAction) {
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
	public FileConfigBuilder<C> defaultResource(String resourcePath) {
		return onFileNotFound(FileNotFoundAction.copyResource(resourcePath));
	}

	/**
	 * Sets the file to copy when the config's file is not found. This is a shortcut for {@code
	 * onFileNotFound(FileNotFoundAction.copyData(file))}
	 *
	 * @param file the data file
	 * @return this builder
	 */
	public FileConfigBuilder<C> defaultData(File file) {
		return onFileNotFound(FileNotFoundAction.copyData(file));
	}

	/**
	 * Sets the URL of the data to copy when the config's file is not found. This is a shortcut for
	 * {@code onFileNotFound(FileNotFoundAction.copyData(url))}
	 *
	 * @param url the data url
	 * @return this builder
	 */
	public FileConfigBuilder<C> defaultData(URL url) {
		return onFileNotFound(FileNotFoundAction.copyData(url));
	}

	/**
	 * Makes the configuration "write-synchronized", that is, its {@link FileConfig#save()}
	 * method blocks until the write operation completes.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder<C> sync() {
		sync = true;
		return this;
	}

	/**
	 * Makes the configuration "autosaved", that is, its {@link FileConfig#save()} method is
	 * automatically called when it is modified.
	 * <p>
	 * <b>Warning: Using autoreload with autosave will lead to bad performance</b>, because the
	 * file will be written and read on each modification.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder<C> autosave() {
		autosave = true;
		return this;
	}

	/**
	 * Makes the configuration "autoreloaded", that is, its {@link FileConfig#load()} method is
	 * automatically called when the file is modified.
	 * <p>
	 * <b>Warning: Using autoreload with autosave will lead to bad performance</b>, because the
	 * file will be written and read on each modification.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder<C> autoreload() {
		autoreload = true;
		return this;
	}

	/**
	 * Makes the configuration concurrent, that is, thread-safe.
	 *
	 * @return this builder
	 */
	public FileConfigBuilder<C> concurrent() {
		config = (C)format.createConcurrentConfig();
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
			fileConfig = new WriteAsyncFileConfig<>(getConfig(), file, charset, writer, writingMode,
													parser, parsingMode, nefAction);
		}
		if (autoreload) {
			if (!file.exists()) {
				try {
					nefAction.run(file);
				} catch (IOException e) {
					throw new WritingException("An exception occured while executing the "
											   + "FileNotFoundAction for file "
											   + file, e);
				}
			}
			fileConfig = new AutoreloadFileConfig<>(fileConfig);
		}
		if (autosave) {
			fileConfig = new AutosaveFileConfig<>(fileConfig);
		}
		return fileConfig;
	}

	protected final C getConfig() {
		if (config == null) {// concurrent() has not been called
			config = (C)format.createConfig();
		}
		return config;
	}
}