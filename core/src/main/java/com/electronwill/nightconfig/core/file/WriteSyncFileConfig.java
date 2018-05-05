package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;

import java.io.File;
import java.nio.charset.Charset;

/**
 * @author TheElectronWill
 */
final class WriteSyncFileConfig<C extends Config> extends ConfigWrapper<C> implements FileConfig {
	private final File file;
	private final Charset charset;
	private boolean closed;

	private final ConfigWriter<? super C> writer;
	private final WritingMode writingMode;

	private final ConfigParser<?, ? super C> parser;
	private final FileNotFoundAction nefAction;
	private final ParsingMode parsingMode;

	private volatile boolean currentlyWriting = false;

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
		synchronized (this) {
			if (closed) {
				throw new IllegalStateException("Cannot save a closed FileConfig");
			}
			currentlyWriting = true;
			writer.write(config, file, writingMode, charset);
			currentlyWriting = false;
		}
	}

	@Override
	public void load() {
		if (!currentlyWriting) {
			synchronized (this) {
				if (closed) {
					throw new IllegalStateException("Cannot (re)load a closed FileConfig");
				}
				parser.parse(file, config, parsingMode, nefAction);
			}
		}
	}

	@Override
	public void close() {
		closed = true;
	}
}