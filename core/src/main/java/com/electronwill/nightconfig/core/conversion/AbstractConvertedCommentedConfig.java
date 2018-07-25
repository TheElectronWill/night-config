package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.utils.TransformingSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
abstract class AbstractConvertedCommentedConfig<C extends CommentedConfig>
		extends AbstractConvertedConfig<C> implements CommentedConfig {
	public AbstractConvertedCommentedConfig(C config, Function<Object, Object> readConversion,
											Function<Object, Object> writeConversion,
											Predicate<Class<?>> supportPredicate) {
		super(config, readConversion, writeConversion, supportPredicate);
	}

	@Override
	public String getComment(List<String> path) {
		return config.getComment(path);
	}

	@Override
	public boolean containsComment(List<String> path) {
		return config.containsComment(path);
	}

	@Override
	public String setComment(List<String> path, String comment) {
		return config.setComment(path, comment);
	}

	@Override
	public String removeComment(List<String> path) {
		return config.removeComment(path);
	}

	@Override
	public void clearComments() {
		config.clearComments();
	}

	@Override
	public Map<String, CommentNode> getComments() {
		return config.getComments();
	}

	@Override
	public void putAllComments(Map<String, CommentNode> comments) {
		config.putAllComments(comments);
	}

	@Override
	public void putAllComments(UnmodifiableCommentedConfig commentedConfig) {
		config.putAllComments(commentedConfig);
	}

	@Override
	public Map<String, String> commentMap() {
		return config.commentMap();
	}

	@Override
	public Set<? extends CommentedConfig.Entry> entrySet() {
		Function<CommentedConfig.Entry, CommentedConfig.Entry> readTransfo = entry -> new CommentedConfig.Entry() {
			@Override
			public String getComment() {
				return entry.getComment();
			}

			@Override
			public String setComment(String comment) {
				return entry.setComment(comment);
			}

			@Override
			public String removeComment() {
				return entry.removeComment();
			}

			@Override
			public String getKey() {
				return entry.getKey();
			}

			@Override
			public <T> T getRawValue() {
				return (T)readConversion.apply(entry.getRawValue());
			}

			@Override
			public <T> T setValue(Object value) {
				return (T)readConversion.apply(entry.setValue(writeConversion.apply(value)));
			}
		};
		return new TransformingSet<>(config.entrySet(), readTransfo, o -> null, e -> e);
	}

	@Override
	public CommentedConfig createSubConfig() {
		return config.createSubConfig();
	}
}