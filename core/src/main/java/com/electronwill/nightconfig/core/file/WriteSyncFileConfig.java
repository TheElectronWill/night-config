package com.electronwill.nightconfig.core.file;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;

/**
 * @author TheElectronWill
 */
final class WriteSyncFileConfig<C extends Config> extends ConfigWrapper<SynchronizedConfig> implements FileConfig {
	private final Path nioPath;
	private final Charset charset;
	private boolean closed;

	private final ConfigWriter writer;
	private final WritingMode writingMode;

	private final ConfigParser<?> parser;
	private final FileNotFoundAction nefAction;
	private final ParsingMode parsingMode;

	WriteSyncFileConfig(C config, Path nioPath, Charset charset, ConfigWriter writer,
			WritingMode writingMode, ConfigParser<?> parser,
			ParsingMode parsingMode, FileNotFoundAction nefAction) {

		// Synchronize the reads and writes on the underlying configuration, to make it thread-safe.
		// Since this is `Write*Sync*FileConfig`, we only allow one read or write at a time.
		super(SynchronizedConfig.wrap(config));

		this.nioPath = nioPath;
		this.charset = charset;
		this.writer = writer;
		this.parser = parser;
		this.parsingMode = parsingMode;
		this.nefAction = nefAction;
		this.writingMode = writingMode;
	}

	// ---- FileConfig ----

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
		synchronized (config.rootMonitor) {
			if (closed) {
				throw new IllegalStateException("Cannot save a closed FileConfig");
			}
			writer.write(config, nioPath, writingMode, charset);
		}
	}

	@Override
	public void load() {
		synchronized (config.rootMonitor) {
			if (closed) {
				throw new IllegalStateException("Cannot (re)load a closed FileConfig");
			}
			parser.parse(nioPath, config, parsingMode, nefAction);
		}
	}

	@Override
	public void close() {
		synchronized (config.rootMonitor) {
			closed = true;
		}
	}

	public boolean isClosed() {
		synchronized (config.rootMonitor) {
			return closed;
		}
	}
}
