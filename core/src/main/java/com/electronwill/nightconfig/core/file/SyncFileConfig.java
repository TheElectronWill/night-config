package com.electronwill.nightconfig.core.file;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;

/**
 * @author TheElectronWill
 */
final class SyncFileConfig<C extends Config> extends CommentedConfigWrapper<SynchronizedConfig>
		implements CommentedFileConfig {
	private final Path nioPath;
	private final Charset charset;
	private volatile boolean closed;

	private final ConfigWriter writer;
	private final WritingMode writingMode;

	private final ConfigParser<?> parser;
	private final FileNotFoundAction nefAction;
	private final ParsingMode parsingMode;

	SyncFileConfig(C config, Path nioPath, Charset charset, ConfigWriter writer,
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
		if (closed) {
			throw new IllegalStateException("This FileConfig is closed, cannot save().");
		}
		config.bulkCommentedRead(config -> {
			writer.write(config, nioPath, writingMode, charset);
		});
	}

	@Override
	public void load() {
		if (closed) {
			throw new IllegalStateException("This FileConfig is closed, cannot load().");
		}
		config.bulkCommentedUpdate(config -> {
			parser.parse(nioPath, config, parsingMode, nefAction);
		});
	}

	@Override
	public void close() {
		closed = true;
	}

	@Override
	public <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action) {
		return config.bulkCommentedUpdate(action);
	}

	@Override
	public <R> R bulkUpdate(Function<? super Config, R> action) {
		return config.bulkUpdate(action);
	}
}
