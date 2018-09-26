package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
 * </ul>
 *
 * @author TheElectronWill
 */
public abstract class GenericBuilder<Base extends Config, Result extends FileConfig> {
	protected final File file;
	private Base config;
	protected final ConfigFormat<? extends Base> format;
	protected final ConfigWriter writer;
	protected final ConfigParser<? extends Base> parser;
	protected Charset charset = StandardCharsets.UTF_8;
	protected WritingMode writingMode = WritingMode.REPLACE;
	protected ParsingMode parsingMode = ParsingMode.REPLACE;
	protected FileNotFoundAction nefAction = FileNotFoundAction.CREATE_EMPTY;
	protected boolean sync = false, autosave = false, autoreload = false;

	GenericBuilder(File file, ConfigFormat<? extends Base> format) {
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
	 * Sets the WritingMode used for {@link FileConfig#save()}
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> writingMode(WritingMode writingMode) {
		this.writingMode = writingMode;
		return this;
	}

	/**
	 * Sets the ParsingMode used for {@link FileConfig#load()}
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
	 * Makes the configuration "write-synchronized", that is, its {@link FileConfig#save()}
	 * method blocks until the write operation completes.
	 * <b>In core_android, this method has no effect because the async FileConfig isn't available
	 * in Android, due to the lack of java.nio.channels.AsynchronousFileChannel on Android &lt; 8</b>
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> sync() {
		sync = true;
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
		autoreload = true;
		return this;
	}

	/**
	 * Makes the configuration concurrent, that is, thread-safe.
	 *
	 * @return this builder
	 */
	public GenericBuilder<Base, Result> concurrent() {
		if (config == null) {
			config = format.createConcurrentConfig();
		}
		return this;
	}

	/**
	 * Creates a new FileConfig with the chosen settings.
	 *
	 * @return the config
	 */
	public Result build() {
		FileConfig fileConfig;
		/*
		 * java.nio.channels.AsynchronousFileChannel isn't available in Android < 8
		 * Therefore, only WriteSyncFileConfig will be used here.
		 */
		fileConfig = new WriteSyncFileConfig<>(getConfig(), file, charset, writer, writingMode,
			parser, parsingMode, nefAction);
		/*
		if (sync) {

		} else {
			if (autoreload) {
				concurrent();
				// Autoreloading is done from a background thread, therefore we need thread-safety
				// This isn't needed with WriteSyncFileConfig because it synchronizes loads and writes.
			}
			fileConfig = new WriteAsyncFileConfig<>(getConfig(), file, charset, writer, writingMode,
				parser, parsingMode, nefAction);
		}
		*/
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

	protected abstract Result buildAutosave(FileConfig chain);

	protected abstract Result buildNormal(FileConfig chain);

	protected final Base getConfig() {
		if (config == null) {// concurrent() has not been called
			config = format.createConfig();
		}
		return config;
	}
}