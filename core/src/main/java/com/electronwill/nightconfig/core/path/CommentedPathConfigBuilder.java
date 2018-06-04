package com.electronwill.nightconfig.core.path;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Builder for CommentedPathConfig. The default settings are:
 * <ul>
 * <li>Charset: UTF-8 - change it with {@link #charset(Charset)}</li>
 * <li>WritingMode: REPLACE - change it with {@link #writingMode(WritingMode)}</li>
 * <li>ParsingMode: REPLACE - change it with {@link #parsingMode(ParsingMode)}</li>
 * <li>PathNotFoundAction: CREATE_EMPTY - change it with {@link #onFileNotFound(PathNotFoundAction)}</li>
 * <li>Asynchronous writing, ie config.save() returns quickly and operates in the background -
 * change it with {@link #sync()}</li>
 * <li>Not autosaved - change it with {@link #autosave()}</li>
 * <li>Not autoreloaded - change it with {@link #autoreload()}</li>
 * <li>Not thread-safe - change it with {@link #concurrent()}</li>
 * </ul>
 *
 * @author TheElectronWill
 */
public final class CommentedPathConfigBuilder extends PathConfigBuilder<CommentedConfig> {
	CommentedPathConfigBuilder(Path path, ConfigFormat<? extends CommentedConfig> format) {
		super(path, format);
	}

	@Override
	public CommentedPathConfigBuilder charset(Charset charset) {
		super.charset(charset);
		return this;
	}

	@Override
	public CommentedPathConfigBuilder writingMode(WritingMode writingMode) {
		super.writingMode(writingMode);
		return this;
	}

	@Override
	public CommentedPathConfigBuilder parsingMode(ParsingMode parsingMode) {
		super.parsingMode(parsingMode);
		return this;
	}

	@Override
	public CommentedPathConfigBuilder onFileNotFound(PathNotFoundAction nefAction) {
		super.onFileNotFound(nefAction);
		return this;
	}

	@Override
	public CommentedPathConfigBuilder defaultResource(String resourcePath) {
		super.defaultResource(resourcePath);
		return this;
	}

	@Override
	public CommentedPathConfigBuilder defaultData(Path path) {
		super.defaultData(path);
		return this;
	}

	@Override
	public CommentedPathConfigBuilder defaultData(URL url) {
		super.defaultData(url);
		return this;
	}

	@Override
	public CommentedPathConfigBuilder sync() {
		super.sync();
		return this;
	}

	@Override
	public CommentedPathConfigBuilder autosave() {
		super.autosave();
		return this;
	}

	@Override
	public CommentedPathConfigBuilder autoreload() {
		super.autoreload();
		return this;
	}

	@Override
	public CommentedPathConfigBuilder concurrent() {
		super.concurrent();
		return this;
	}

	public CommentedPathConfig build() {
		return new SimpleCommentedPathConfig(getConfig(), super.build());
	}
}