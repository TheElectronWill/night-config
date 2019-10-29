package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * @author TheElectronWill
 */
final class WriteSyncFileConfig<C extends Config> extends ConfigWrapper<C> implements FileConfig {
	private final Path nioPath;
	private final Charset charset;
	private boolean closed;

	private final ConfigWriter writer;
	private final WritingMode writingMode;

	private final ConfigParser parser;
	private final FileNotFoundAction nefAction;
	private final ParsingMode parsingMode;

	private volatile boolean currentlyWriting = false;

	WriteSyncFileConfig(C config, Path nioPath, Charset charset, ConfigWriter writer,
						 WritingMode writingMode, ConfigParser parser,
						 ParsingMode parsingMode, FileNotFoundAction nefAction) {
		super(config);
		this.nioPath = nioPath;
		this.charset = charset;
		this.writer = writer;
		this.parser = parser;
		this.parsingMode = parsingMode;
		this.nefAction = nefAction;
		this.writingMode = writingMode;
	}

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
		synchronized (this) {
			if (closed) {
				throw new IllegalStateException("Cannot save a closed FileConfig");
			}
			currentlyWriting = true;
			writer.write(config, nioPath, writingMode, charset);
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
				parser.parse(nioPath, config, parsingMode, nefAction);
			}
		}
	}

	@Override
	public void close() {
		closed = true;
	}
}