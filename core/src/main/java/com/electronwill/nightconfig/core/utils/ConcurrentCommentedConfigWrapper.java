package com.electronwill.nightconfig.core.utils;

import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.concurrent.ConcurrentCommentedConfig;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author TheElectronWill
 */
public abstract class ConcurrentCommentedConfigWrapper<C extends ConcurrentCommentedConfig> extends CommentedConfigWrapper<C>
		implements ConcurrentCommentedConfig {

	protected ConcurrentCommentedConfigWrapper(C config) {
		super(config);
	}

	@Override
	public ConcurrentCommentedConfig createSubConfig() {
		return config.createSubConfig();
	}

	@Override
	public void bulkRead(Consumer<? super UnmodifiableConfig> action) {
		config.bulkRead(action);
	}

	@Override
	public <R> R bulkRead(Function<? super UnmodifiableConfig, R> action) {
		return config.bulkRead(action);
	}

	@Override
	public void bulkCommentedRead(Consumer<? super UnmodifiableCommentedConfig> action) {
		config.bulkCommentedRead(action);
	}

	@Override
	public <R> R bulkCommentedRead(Function<? super UnmodifiableCommentedConfig, R> action) {
		return config.bulkCommentedRead(action);
	}

	@Override
	public void bulkUpdate(Consumer<? super Config> action) {
		config.bulkUpdate(action);
	}

	@Override
	public <R> R bulkUpdate(Function<? super Config, R> action) {
		return config.bulkUpdate(action);
	}

	@Override
	public void bulkCommentedUpdate(Consumer<? super CommentedConfig> action) {
		config.bulkCommentedUpdate(action);
	}

	@Override
	public <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action) {
		return config.bulkCommentedUpdate(action);
	}
}
