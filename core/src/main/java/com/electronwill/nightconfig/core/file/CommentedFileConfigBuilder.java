package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Builder for CommentedFileConfig. The default settings are:
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
public final class CommentedFileConfigBuilder extends FileConfigBuilder<CommentedConfig> {
	CommentedFileConfigBuilder(File file,
							   ConfigFormat<? extends CommentedConfig, ? super CommentedConfig, ? super CommentedConfig> format) {
		super(file, format);
	}

	@Override
	public CommentedFileConfigBuilder charset(Charset charset) {
		super.charset(charset);
		return this;
	}

	@Override
	public CommentedFileConfigBuilder writingMode(WritingMode writingMode) {
		super.writingMode(writingMode);
		return this;
	}

	@Override
	public CommentedFileConfigBuilder parsingMode(ParsingMode parsingMode) {
		super.parsingMode(parsingMode);
		return this;
	}

	@Override
	public CommentedFileConfigBuilder onFileNotFound(FileNotFoundAction nefAction) {
		super.onFileNotFound(nefAction);
		return this;
	}

	@Override
	public CommentedFileConfigBuilder defaultResource(String resourcePath) {
		super.defaultResource(resourcePath);
		return this;
	}

	@Override
	public CommentedFileConfigBuilder defaultData(File file) {
		super.defaultData(file);
		return this;
	}

	@Override
	public CommentedFileConfigBuilder defaultData(URL url) {
		super.defaultData(url);
		return this;
	}

	@Override
	public CommentedFileConfigBuilder sync() {
		super.sync();
		return this;
	}

	@Override
	public CommentedFileConfigBuilder autosave() {
		super.autosave();
		return this;
	}

	@Override
	public CommentedFileConfigBuilder autoreload() {
		super.autoreload();
		return this;
	}

	@Override
	public CommentedFileConfigBuilder concurrent() {
		super.concurrent();
		return this;
	}

	public CommentedFileConfig build() {
		return new SimpleCommentedFileConfig(getConfig(), super.build());
	}
}