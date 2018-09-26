package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;

import java.io.File;
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
public final class CommentedFileConfigBuilder extends GenericBuilder<CommentedConfig, CommentedFileConfig> {
	CommentedFileConfigBuilder(File file, ConfigFormat<? extends CommentedConfig> format) {
		super(file, format);
	}

	@Override
	protected CommentedFileConfig buildAutosave(FileConfig chain) {
		return new AutosaveCommentedFileConfig(getConfig(), chain);
	}

	@Override
	protected CommentedFileConfig buildNormal(FileConfig chain) {
		return new SimpleCommentedFileConfig(getConfig(), chain);
	}
}