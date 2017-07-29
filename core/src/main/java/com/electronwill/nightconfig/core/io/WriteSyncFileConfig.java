package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author TheElectronWill
 */
final class WriteSyncFileConfig<C extends Config> extends ConfigWrapper<C> implements FileConfig {
	private final File file;
	private final Charset charset;

	private final ConfigWriter<? super C> writer;
	private final WritingMode writingMode;

	private final ConfigParser<?, ? super C> parser;
	private final FileNotFoundAction nefAction;
	private final ParsingMode parsingMode;

	WriteSyncFileConfig(C config, File file, Charset charset, ConfigWriter<? super C> writer,
						 WritingMode writingMode, ConfigParser<?, ? super C> parser,
						 ParsingMode parsingMode, FileNotFoundAction nefAction) {
		super(config);
		this.file = file;
		this.charset = charset;
		this.writer = writer;
		this.parser = parser;
		this.parsingMode = parsingMode;
		this.nefAction = nefAction;
		this.writingMode = writingMode;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public void save() {
		writer.write(config, file, writingMode, charset);
	}

	@Override
	public void load() {
		parser.parse(file, config, parsingMode, nefAction);
	}
}