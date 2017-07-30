package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author TheElectronWill
 */
public class FileConfigBuilder<C extends Config> {
	protected final File file;
	protected final C config;
	protected final ConfigWriter<? super C> writer;
	protected final ConfigParser<?, ? super C> parser;
	protected Charset charset = StandardCharsets.UTF_8;
	protected WritingMode writingMode = WritingMode.REPLACE;
	protected ParsingMode parsingMode = ParsingMode.REPLACE;
	protected FileNotFoundAction nefAction = FileNotFoundAction.CREATE_EMPTY;
	protected boolean sync = false, autosave = false;

	FileConfigBuilder(File file, C config, ConfigWriter<? super C> writer,
					  ConfigParser<?, ? super C> parser) {
		this.file = file;
		this.config = config;
		this.writer = writer;
		this.parser = parser;
	}

	<T extends C> FileConfigBuilder(File file, ConfigFormat<T, ? super C, ? super C> format) {
		this.file = file;
		this.config = format.createConfig();
		this.writer = format.createWriter();
		this.parser = format.createParser();
	}

	public FileConfigBuilder<C> charset(Charset charset) {
		this.charset = charset;
		return this;
	}

	public FileConfigBuilder<C> writingMode(WritingMode writingMode) {
		this.writingMode = writingMode;
		return this;
	}

	public FileConfigBuilder<C> parsingMode(ParsingMode parsingMode) {
		this.parsingMode = parsingMode;
		return this;
	}

	public FileConfigBuilder<C> onFileNotFound(FileNotFoundAction nefAction) {
		this.nefAction = nefAction;
		return this;
	}

	public FileConfigBuilder<C> defaultResource(String resourcePath) {
		return onFileNotFound(FileNotFoundAction.copyResource(resourcePath));
	}

	public FileConfigBuilder<C> sync() {
		sync = true;
		return this;
	}

	public FileConfigBuilder<C> autosave() {
		autosave = true;
		return this;
	}

	public FileConfig build() {
		FileConfig fileConfig;
		if (sync) {
			fileConfig = new WriteSyncFileConfig<>(config, file, charset, writer, writingMode,
												   parser, parsingMode, nefAction);
		} else {
			fileConfig = new WriteAsyncFileConfig<>(config, file, charset, writer, writingMode,
													parser, parsingMode, nefAction);
		}
		return autosave ? new AutosaveFileConfig<>(fileConfig) : fileConfig;
	}
}