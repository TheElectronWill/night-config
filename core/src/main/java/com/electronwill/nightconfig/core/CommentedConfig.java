package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.FakeCommentedConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.electronwill.nightconfig.core.utils.StringUtils.split;

/**
 * A modifiable config that supports comments.
 *
 * @author TheElectronWill
 */
public interface CommentedConfig extends UnmodifiableCommentedConfig, Config {
	/**
	 * Sets a config comment.
	 *
	 * @param path    the comment's path, each part separated by a dot. Example "a.b.c"
	 * @param comment the comment to set
	 * @return the old comment if any, or {@code null}
	 */
	default String setComment(String path, String comment) {
		return setComment(split(path, '.'), comment);
	}

	/**
	 * Sets a config comment.
	 *
	 * @param path    the comment's path, each element of the list is a different part of the path.
	 * @param comment the comment to set
	 * @return the old comment if any, or {@code null}
	 */
	String setComment(List<String> path, String comment);

	/**
	 * Removes a comment from the config.
	 *
	 * @param path the comment's path, each part separated by a dot. Example "a.b.c"
	 * @return the old comment if any, or {@code null}
	 */
	default String removeComment(String path) {
		return removeComment(split(path, '.'));
	}

	/**
	 * Removes a comment from the config.
	 *
	 * @param path the comment's path, each element of the list is a different part of the path.
	 * @return the old comment if any, or {@code null}
	 */
	String removeComment(List<String> path);

	/**
	 * Removes all the comments from the config.
	 */
	void clearComments();

	/**
	 * Puts the comments in the given map to this config. Existing comments are replaced, missing
	 * comments are created.
	 *
	 * @param comments the comments to set
	 */
	default void putAllComments(Map<String, CommentNode> comments) {
		for (Map.Entry<String, CommentNode> entry : comments.entrySet()) {
			String key = entry.getKey();
			CommentNode node = entry.getValue();
			String comment = node.getComment();
			if (comment != null) {
				setComment(Collections.singletonList(key), comment);
			}
			Map<String, CommentNode> children = node.getChildren();
			if (children != null) {
				CommentedConfig config = get(Collections.singletonList(key));
				config.putAllComments(children);
			}
		}
	}

	/**
	 * Puts the comments in the given config to this config. Existing comments are replaced, missing
	 * comments are created.
	 *
	 * @param commentedConfig the config to copy its comments
	 */
	default void putAllComments(UnmodifiableCommentedConfig commentedConfig) {
		for (UnmodifiableCommentedConfig.Entry entry : commentedConfig.entrySet()) {
			String key = entry.getKey();
			String comment = entry.getComment();
			if (comment != null) {
				setComment(Collections.singletonList(key), comment);
			}
			Object value = entry.getValue();
			if (value instanceof UnmodifiableCommentedConfig) {
				CommentedConfig config = get(Collections.singletonList(key));
				config.putAllComments((UnmodifiableCommentedConfig)value);
			}

		}
	}

	@Override
	default UnmodifiableCommentedConfig unmodifiable() {
		return new UnmodifiableCommentedConfig() {
			@Override
			public <T> T get(List<String> path) {
				return CommentedConfig.this.get(path);
			}

			@Override
			public String getComment(List<String> path) {
				return CommentedConfig.this.getComment(path);
			}

			@Override
			public boolean contains(List<String> path) {
				return CommentedConfig.this.contains(path);
			}

			@Override
			public boolean containsComment(List<String> path) {
				return CommentedConfig.this.containsComment(path);
			}

			@Override
			public int size() {
				return CommentedConfig.this.size();
			}

			@Override
			public Map<String, Object> valueMap() {
				return Collections.unmodifiableMap(CommentedConfig.this.valueMap());
			}

			@Override
			public Map<String, String> commentMap() {
				return Collections.unmodifiableMap(CommentedConfig.this.commentMap());
			}

			@Override
			public Map<String, CommentNode> getComments() {
				return CommentedConfig.this.getComments();
			}

			@Override
			public Set<? extends Entry> entrySet() {
				return CommentedConfig.this.entrySet();
			}

			@Override
			public ConfigFormat<?> configFormat() {
				return CommentedConfig.this.configFormat();
			}
		};
	}

	default CommentedConfig checked() {
		return new CheckedCommentedConfig(this);
	}

	/**
	 * Returns a Map view of the config's comments. Any change to the map is reflected in the
	 * config and vice-versa.
	 * <p>
	 * The comment map contains only the comments of the direct elements of the configuration, not
	 * the comments of their sub-elements.
	 */
	@Override
	Map<String, String> commentMap();

	@Override
	Set<? extends Entry> entrySet();

	/**
	 * A modifiable commented config entry.
	 */
	interface Entry extends Config.Entry, UnmodifiableCommentedConfig.Entry {
		/**
		 * Sets the entry's comment.
		 *
		 * @param comment the comment to set, may contain several lines.
		 * @return the previous comment, or {@code null} if none.
		 */
		String setComment(String comment);

		/**
		 * Removes the entry's comment.
		 *
		 * @return the previous comment, or {@code null} if none.
		 */
		String removeComment();
	}

	@Override
	CommentedConfig createSubConfig();

	/**
	 * Creates a CommentedConfig of the given format.
	 *
	 * @param format the config's format
	 * @return a new empty config
	 */
	static CommentedConfig of(ConfigFormat<? extends CommentedConfig> format) {
		return new SimpleCommentedConfig(format, false);
	}

	/**
	 * Creates a thread-safe CommentedConfig of the given format.
	 *
	 * @param format the config's format
	 * @return a new empty, thread-safe config
	 */
	static CommentedConfig ofConcurrent(ConfigFormat<? extends CommentedConfig> format) {
		return new SimpleCommentedConfig(format, false);
	}

	/**
	 * Creates a CommentedConfig with format {@link InMemoryCommentedFormat#defaultInstance()}.
	 *
	 * @return a new empty config
	 */
	static CommentedConfig inMemory() {
		return InMemoryCommentedFormat.defaultInstance().createConfig();
	}

	/**
	 * Creates a CommentedConfig with format {@link InMemoryFormat#defaultInstance()}.
	 *
	 * @return a new empty config
	 */
	static CommentedConfig inMemoryConcurrent() {
		return InMemoryCommentedFormat.defaultInstance().createConcurrentConfig();
	}

	/**
	 * Creates a CommentedConfig backed by a Map. Any change to the map is reflected in the config
	 * and vice-versa.
	 *
	 * @param map    the Map to use
	 * @param format the config's format
	 * @return a new config backed by the map
	 */
	static CommentedConfig wrap(Map<String, Object> map, ConfigFormat<?> format) {
		return new SimpleCommentedConfig(map, format);
	}

	/**
	 * Creates a new CommentedConfig with the content of the given config. The returned config will
	 * have the same format as the copied config.
	 *
	 * @param config the config to copy
	 * @return a copy of the config
	 */
	static CommentedConfig copy(UnmodifiableConfig config) {
		return new SimpleCommentedConfig(config, config.configFormat(), false);
	}

	/**
	 * Creates a new CommentedConfig with the content of the given config.
	 *
	 * @param config the config to copy
	 * @param format the config's format
	 * @return a copy of the config
	 */
	static CommentedConfig copy(UnmodifiableConfig config, ConfigFormat<?> format) {
		return new SimpleCommentedConfig(config, format, false);
	}

	/**
	 * Creates a new CommentedConfig with the content of the given config. The returned config will
	 * have the same format as the copied config.
	 *
	 * @param config the config to copy
	 * @return a copy of the config
	 */
	static CommentedConfig copy(UnmodifiableCommentedConfig config) {
		return new SimpleCommentedConfig(config, config.configFormat(), false);
	}

	/**
	 * Creates a new CommentedConfig with the content of the given config.
	 *
	 * @param config the config to copy
	 * @param format the config's format
	 * @return a copy of the config
	 */
	static CommentedConfig copy(UnmodifiableCommentedConfig config, ConfigFormat<?> format) {
		return new SimpleCommentedConfig(config, format, false);
	}

	/**
	 * Creates a new CommentedConfig with the content of the given config. The returned config will
	 * have the same format as the copied config.
	 *
	 * @param config the config to copy
	 * @return a thread-safe copy of the config
	 */
	static CommentedConfig concurrentCopy(UnmodifiableConfig config) {
		return new SimpleCommentedConfig(config, config.configFormat(), true);
	}

	/**
	 * Creates a new CommentedConfig with the content of the given config.
	 *
	 * @param config the config to copy
	 * @param format the config's format
	 * @return a thread-safe copy of the config
	 */
	static CommentedConfig concurrentCopy(UnmodifiableConfig config, ConfigFormat<?> format) {
		return new SimpleCommentedConfig(config, format, true);
	}

	/**
	 * Creates a new CommentedConfig with the content of the given config. The returned config will
	 * have the same format as the copied config.
	 *
	 * @param config the config to copy
	 * @return a thread-safe copy of the config
	 */
	static CommentedConfig concurrentCopy(UnmodifiableCommentedConfig config) {
		return new SimpleCommentedConfig(config, config.configFormat(), true);
	}

	/**
	 * Creates a new CommentedConfig with the content of the given config.
	 *
	 * @param config the config to copy
	 * @param format the config's format
	 * @return a thread-safe copy of the config
	 */
	static CommentedConfig concurrentCopy(UnmodifiableCommentedConfig config,
										  ConfigFormat<?> format) {
		return new SimpleCommentedConfig(config, format, true);
	}

	/**
	 * If the specified config is an instance of CommentedConfig, returns it. Else, returns a
	 * "fake" CommentedConfig instance with the same values (ie the valueMaps are equal) as the
	 * config. This fake CommentedConfig doesn't actually store nor process comments, it just
	 * provides the methods of CommentedConfig.
	 *
	 * @param config the config
	 * @return a CommentedConfig instance backed by the specified config
	 */
	static CommentedConfig fake(Config config) {
		if (config instanceof CommentedConfig) {
			return (CommentedConfig)config;
		}
		return new FakeCommentedConfig(config);
	}
}
