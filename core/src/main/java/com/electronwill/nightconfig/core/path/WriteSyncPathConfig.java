package com.electronwill.nightconfig.core.path;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;

import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * @author TheElectronWill
 */
final class WriteSyncPathConfig<C extends Config> extends ConfigWrapper<C> implements PathConfig {
	private final Path path;
	private final Charset charset;
	private boolean closed;

	private final ConfigWriter writer;
	private final WritingMode writingMode;

	private final ConfigParser<?> parser;
	private final PathNotFoundAction nefAction;
	private final ParsingMode parsingMode;

	private volatile boolean currentlyWriting = false;

	WriteSyncPathConfig(C config, Path path, Charset charset, ConfigWriter writer,
						WritingMode writingMode, ConfigParser<?> parser,
						ParsingMode parsingMode, PathNotFoundAction nefAction) {
		super(config);
		this.path = path;
		this.charset = charset;
		this.writer = writer;
		this.parser = parser;
		this.parsingMode = parsingMode;
		this.nefAction = nefAction;
		this.writingMode = writingMode;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public void save() {
		synchronized (this) {
			if (closed) {
				throw new IllegalStateException("Cannot save a closed PathConfig");
			}
			currentlyWriting = true;
			writer.write(config, path, writingMode, charset);
			currentlyWriting = false;
		}
	}

	@Override
	public void load() {
		if (!currentlyWriting) {
			synchronized (this) {
				if (closed) {
					throw new IllegalStateException("Cannot (re)load a closed PathConfig");
				}
				parser.parse(path, config, parsingMode, nefAction);
			}
		}
	}

	@Override
	public void close() {
		closed = true;
	}
}