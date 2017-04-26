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
 * A Config's wrapper that converts the values that are read from and put into the config.
 *
 * @author TheElectronWill
 */
public final class CommentedConvertedConfig extends AbstractConvertedConfig<CommentedConfig>
		implements CommentedConfig {
	/**
	 * Creates a new ConvertedConfig that uses two conversion tables.
	 *
	 * @param config           the config to wrap
	 * @param readTable        the ConversionTable used for parse operations (like getValue)
	 * @param writeTable       the ConversionTable used for write operations (like setValue)
	 * @param supportPredicate a Predicate that checks if a given class is supported by the
	 *                         ConvertedConfig
	 */
	public CommentedConvertedConfig(CommentedConfig config, ConversionTable readTable,
									ConversionTable writeTable,
									Predicate<Class<?>> supportPredicate) {
		this(config, readTable::convert, writeTable::convert, supportPredicate);
	}

	/**
	 * Creates a new ConvertedConfig that uses two custom conversion functions.
	 *
	 * @param config           the config to wrap
	 * @param readConversion   the Function used for parse operations (like getValue)
	 * @param writeConversion  the Function used for write operations (like setValue)
	 * @param supportPredicate a Predicate that checks if a given class is supported by the
	 *                         ConvertedConfig
	 */
	public CommentedConvertedConfig(CommentedConfig config, Function<Object, Object> readConversion,
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
	public void setComments(Map<String, CommentNode> comments) {
		config.setComments(comments);
	}

	@Override
	public void setComments(UnmodifiableCommentedConfig commentedConfig) {
		config.setComments(commentedConfig);
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
			public <T> T getValue() {
				return (T)readConversion.apply(entry.getValue());
			}

			@Override
			public <T> T setValue(Object value) {
				return (T)readConversion.apply(entry.setValue(writeConversion.apply(value)));
			}
		};
		return new TransformingSet<>(config.entrySet(), readTransfo, o -> null, e -> e);
	}
}
