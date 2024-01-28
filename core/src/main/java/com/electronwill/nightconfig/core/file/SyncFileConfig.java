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
final class SyncFileConfig extends CommentedConfigWrapper<SynchronizedConfig>
		implements CommentedFileConfig {
	private final Path nioPath;
	private final Charset charset;
	private volatile boolean closed;

	// Serializing
	private final ConfigWriter writer;
	private final WritingMode writingMode;

	// Parsing
	private final ConfigParser<?> parser;
	private final FileNotFoundAction nefAction;
	private final ParsingMode parsingMode;

	// Listeners
	private final ConfigLoadFilter reloadFilter;
	private final Runnable saveListener, loadListener;

	SyncFileConfig(SynchronizedConfig config, Path nioPath, Charset charset, ConfigWriter writer,
			WritingMode writingMode, ConfigParser<?> parser,
			ParsingMode parsingMode, FileNotFoundAction nefAction,
			ConfigLoadFilter reloadFilter,
			Runnable saveListener, Runnable loadListener) {

		// Synchronize the reads and writes on the underlying configuration, to make it thread-safe.
		// Since this is `Write*Sync*FileConfig`, we only allow one read or write at a time.
		super(config);

		this.nioPath = nioPath;
		this.charset = charset;
		this.writer = writer;
		this.parser = parser;
		this.parsingMode = parsingMode;
		this.nefAction = nefAction;
		this.writingMode = writingMode;
		this.reloadFilter = reloadFilter;
		this.saveListener = saveListener;
		this.loadListener = loadListener;
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
		saveListener.run();
	}

	@Override
	public void load() {
		if (closed) {
			throw new IllegalStateException("This FileConfig is closed, cannot load().");
		}
		if (reloadFilter == null) {
			config.bulkCommentedUpdate(view -> {
				parser.parse(nioPath, view, parsingMode, nefAction, charset);
			});
		} else {
			Config newConfig = parser.parse(nioPath, nefAction, charset);
			CommentedConfig newCC = CommentedConfig.fake(newConfig);
			if (!reloadFilter.acceptNewVersion(newCC)) {
				return; // reload cancelled
			}
			switch (parsingMode) {
				case REPLACE:
					config.replaceContentBy(newCC);
					break;
				default:
					AsyncFileConfig.putWithParsingMode(parsingMode, newCC, config);
					break;
			}
		}
		loadListener.run();
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
