package com.electronwill.nightconfig.core.path;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Builder for PathConfig. The default settings are:
 * <ul>
 * <li>Charset: UTF-8 - change it with {@link #charset(Charset)}</li>
 * <li>WritingMode: REPLACE - change it with {@link #writingMode(WritingMode)}</li>
 * <li>ParsingMode: REPLACE - change it with {@link #parsingMode(ParsingMode)}</li>
 * <li>PathNotFoundAction: CREATE_EMPTY - change it with {@link #onFileNotFound(PathNotFoundAction)}</li>
 * <li>Asynchronous writing, ie config.save() returns quickly and operates in the background -
 * change it with {@link #sync()}</li>
 * <li>Not autosaved - change it with {@link #autosave()}</li>
 * <li>Not autoreloaded - change it with {@link #autoreload()}</li>
 * <li>Not thread-safe - change it with {@link #concurrent()}</li>
 * </ul>
 *
 * @author TheElectronWill
 */
public class PathConfigBuilder<C extends Config> {
	protected final Path path;
	private C config;
	protected final ConfigFormat<?> format;
	protected final ConfigWriter writer;
	protected final ConfigParser<?> parser;
	protected Charset charset = StandardCharsets.UTF_8;
	protected WritingMode writingMode = WritingMode.REPLACE;
	protected ParsingMode parsingMode = ParsingMode.REPLACE;
	protected PathNotFoundAction nefAction = PathNotFoundAction.CREATE_EMPTY;
	protected boolean sync = false, autosave = false, autoreload = false;

	PathConfigBuilder(Path path, ConfigFormat<? extends C> format) {
		this.path = path;
		this.format = format;
		this.writer = format.createWriter();
		this.parser = format.createParser();
	}

	/**
	 * Sets the charset used for {@link PathConfig#save()} and {@link PathConfig#load()}.
	 *
	 * @return this builder
	 */
	public PathConfigBuilder<C> charset(Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * Sets the WritingMode used for {@link PathConfig#save()}
	 *
	 * @return this builder
	 */
	public PathConfigBuilder<C> writingMode(WritingMode writingMode) {
		this.writingMode = writingMode;
		return this;
	}

	/**
	 * Sets the ParsingMode used for {@link PathConfig#load()}
	 *
	 * @return this builder
	 */
	public PathConfigBuilder<C> parsingMode(ParsingMode parsingMode) {
		this.parsingMode = parsingMode;
		return this;
	}

	/**
	 * Sets the action to execute when the config's file is not found.
	 *
	 * @return this builder
	 */
	public PathConfigBuilder<C> onFileNotFound(PathNotFoundAction nefAction) {
		this.nefAction = nefAction;
		return this;
	}

	/**
	 * Sets the resource (in the jar) to copy when the config's file is not found. This is a
	 * shortcut for {@code onFileNotFound(PathNotFoundAction.copyResource(resourcePath))}
	 *
	 * @param resourcePath the resource's path
	 * @return this builder
	 */
	public PathConfigBuilder<C> defaultResource(String resourcePath) {
		return onFileNotFound(PathNotFoundAction.copyResource(resourcePath));
	}

	/**
	 * Sets the path to copy when the config's path is not found. This is a shortcut for {@code
	 * onFileNotFound(PathNotFoundAction.copyData(path))}
	 *
	 * @param path the data path
	 * @return this builder
	 */
	public PathConfigBuilder<C> defaultData(Path path) {
		return onFileNotFound(PathNotFoundAction.copyData(path));
	}

	/**
	 * Sets the URL of the data to copy when the config's file is not found. This is a shortcut for
	 * {@code onFileNotFound(PathNotFoundAction.copyData(url))}
	 *
	 * @param url the data url
	 * @return this builder
	 */
	public PathConfigBuilder<C> defaultData(URL url) {
		return onFileNotFound(PathNotFoundAction.copyData(url));
	}

	/**
	 * Makes the configuration "write-synchronized", that is, its {@link PathConfig#save()}
	 * method blocks until the write operation completes.
	 *
	 * @return this builder
	 */
	public PathConfigBuilder<C> sync() {
		sync = true;
		return this;
	}

	/**
	 * Makes the configuration "autosaved", that is, its {@link PathConfig#save()} method is
	 * automatically called when it is modified.
	 *
	 * @return this builder
	 */
	public PathConfigBuilder<C> autosave() {
		autosave = true;
		return this;
	}

	/**
	 * Makes the configuration "autoreloaded", that is, its {@link PathConfig#load()} method is
	 * automatically called when the file is modified.
	 *
	 * @return this builder
	 */
	public PathConfigBuilder<C> autoreload() {
		autoreload = true;
		return this;
	}

	/**
	 * Makes the configuration concurrent, that is, thread-safe.
	 *
	 * @return this builder
	 */
	public PathConfigBuilder<C> concurrent() {
		config = (C)format.createConcurrentConfig();
		return this;
	}

	/**
	 * Creates a new PathConfig with the chosen settings.
	 *
	 * @return the config
	 */
	public PathConfig build() {
		PathConfig pathConfig;
		if (sync) {
			pathConfig = new WriteSyncPathConfig<>(getConfig(), path, charset, writer, writingMode,
												   parser, parsingMode, nefAction);
		} else {
			if (autoreload) {
				concurrent();
				// Autoreloading is done from a background thread, therefore we need thread-safety
				// This isn't needed with WriteSyncPathConfig because it synchronizes loads and writes.
			}
			pathConfig = new WriteAsyncPathConfig<>(getConfig(), path, charset, writer, writingMode,
													parser, parsingMode, nefAction);
		}
		if (autoreload) {
			if (!Files.exists(path)) {
				try {
					nefAction.run(path);
				} catch (IOException e) {
					throw new WritingException("An exception occured while executing the "
											   + "PathNotFoundAction for file "
											   + path, e);
				}
			}
			pathConfig = new AutoreloadPathConfig<>(pathConfig);
		}
		if (autosave) {
			pathConfig = new AutosavePathConfig<>(pathConfig);
		}
		return pathConfig;
	}

	protected final C getConfig() {
		if (config == null) {// concurrent() has not been called
			config = (C)format.createConfig();
		}
		return config;
	}
}